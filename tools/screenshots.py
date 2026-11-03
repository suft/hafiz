#!/usr/bin/env python3
"""Captures the Play/README screenshots (6 Arabic, 6 English, 1080x1920) on a headless emulator.

Boots a dedicated AVD (created on first run), installs dist/Hafiz-debug.apk, seeds the statistics, drives the
real app through adb + uiautomator and captures with screencap. Output goes to
fastlane/metadata/android/{ar,en-US}/images/phoneScreenshots/{1..6}.png:

  1 main menu, 2 a Guess the Surah question, 3 a Where's the Ayah? question, 4 Statistics -> Surahs tab,
  5 the load screen, 6 the Credits overlay.

Needs KVM, adb/sdkmanager/avdmanager on PATH and ANDROID_HOME set (mise does both). The first run downloads the
emulator and a system image (about 2 GB) into the SDK. Python 3 standard library only.

Usage: python3 tools/screenshots.py      (or: mise run screenshots, which builds the debug APK first)
"""
import json
import os
import platform
import re
import struct
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
os.chdir(ROOT)

PKG = "suft.android.hafiz.debug"
ACTIVITY = f"{PKG}/suft.android.hafiz.MainActivity"
APK = "dist/Hafiz-debug.apk"
AVD = "hafiz_screenshots"
PORT = 5580
SERIAL = f"emulator-{PORT}"
WIDTH, HEIGHT, DENSITY = 1080, 1920, 420
ABI = "x86_64" if platform.machine() in ("x86_64", "AMD64") else "arm64-v8a"
# API 33+ is needed for `cmd locale set-app-locales`. Not android-37.0: its current image (rev 7) crash-loops
# surfaceflinger on the software renderers, which restarts system_server every ~10 s.
IMAGE = f"system-images;android-36;google_apis_playstore;{ABI}"
IMAGE_DIR = f"system-images/android-36/google_apis_playstore/{ABI}"
OUT = {
    "ar": "fastlane/metadata/android/ar/images/phoneScreenshots",
    "en": "fastlane/metadata/android/en-US/images/phoneScreenshots",
}
# Must follow ArabicStrings / EnglishStrings in app/src/main/java/suft/android/hafiz/ui/i18n/Strings.kt.
LABELS = {
    "ar": {"play": "العب", "regular": "عادي", "guess": "آية وسورة", "position": "أين الآية؟",
           "stats": "إحصائيات", "surahs": "السور", "credits": "شكر وتقدير", "sources": "المصادر"},
    "en": {"play": "Play", "regular": "Regular", "guess": "Guess the Surah", "position": "Where's the Ayah?",
           "stats": "Statistics", "surahs": "Surahs", "credits": "Credits", "sources": "Sources"},
}
LANGUAGE = {"ar": "Arabic", "en": "English"}
BUILD = ROOT / "build"
BOOT_TIMEOUT_S = 600
WAIT_TIMEOUT_S = 30
# The load screen stays up at least 1.5 s after a process start (MIN_LOAD_SCREEN_MILLIS in HafizRoot.kt) and
# `am start -W` returns once its first frame is drawn, so a capture that finishes this soon afterwards shows it.
LOAD_CAPTURE_MAX_S = 1.0
LOAD_ATTEMPTS = 3
LOAD_SETTLE_S = 0.4  # lets the spinner sweep out of its start-of-animation dot


def fail(message):
    print(f"error: {message}", file=sys.stderr)
    sys.exit(1)


def run(cmd, **kwargs):
    return subprocess.run(cmd, check=False, capture_output=True, **kwargs)


def adb(*args, check=True, binary=False, input=None):
    result = run(["adb", "-s", SERIAL, *args], input=input)
    if check and result.returncode != 0:
        detail = (result.stderr + result.stdout).decode(errors="replace").strip()
        fail(f"adb {' '.join(args)} failed ({result.returncode}): {detail}")
    return result.stdout if binary else result.stdout.decode(errors="replace")


def sh(command, check=True):
    return adb("shell", command, check=check)


# ---------------------------------------------------------------------------------------------------------------
# Setup


def android_home():
    home = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if not home:
        fail("ANDROID_HOME is not set (run through mise)")
    return Path(home)


def preflight():
    if sys.platform.startswith("linux"):
        if not os.access("/dev/kvm", os.R_OK | os.W_OK):
            fail("KVM is required: /dev/kvm is missing or not writable")
    if not Path(APK).exists():
        fail("run mise run build first")


def ensure_sdk_packages(home):
    if (home / "emulator/emulator").exists() and (home / IMAGE_DIR).is_dir():
        return
    print("Installing the emulator and system image (first run only, about 2 GB)...")
    # Licences are only accepted once; the exit status of `yes` (SIGPIPE) is irrelevant.
    subprocess.run(f'yes | sdkmanager --sdk_root="{home}" --licenses > /dev/null', shell=True, check=False)
    subprocess.run(["sdkmanager", f"--sdk_root={home}", "--install", "emulator", IMAGE], check=True)


def avd_dir():
    """The AVD's directory as avdmanager reports it (it follows ANDROID_AVD_HOME and XDG_CONFIG_HOME)."""
    listing = run(["avdmanager", "list", "avd"]).stdout.decode()
    for block in listing.split("---------"):
        if re.search(rf"^\s*Name: {AVD}$", block, re.M):
            m = re.search(r"^\s*Path: (.+)$", block, re.M)
            if m:
                return Path(m.group(1).strip())
    fail(f"avdmanager does not list the {AVD} AVD")


def _uses_image(directory):
    config = directory / "config.ini"
    return config.exists() and f"image.sysdir.1={IMAGE_DIR}/" in config.read_text().replace(" = ", "=")


def ensure_avd():
    listed = run(["avdmanager", "list", "avd", "-c"]).stdout.decode().split()
    if AVD not in listed or not _uses_image(avd_dir()):
        print(f"Creating the {AVD} AVD...")
        created = run(["avdmanager", "create", "avd", "-n", AVD, "-k", IMAGE, "-d", "pixel_5", "--force"],
                      input=b"no\n")
        if created.returncode != 0:
            fail(f"avdmanager create avd failed: {created.stderr.decode(errors='replace').strip()}")
    config = avd_dir() / "config.ini"
    if not config.exists():
        fail(f"{config} not found after creating the AVD")
    wanted = {
        "hw.lcd.width": WIDTH,
        "hw.lcd.height": HEIGHT,
        "hw.lcd.density": DENSITY,
        "hw.keyboard": "no",
        "showDeviceFrame": "no",
    }
    lines = config.read_text().splitlines()
    seen = set()
    for i, line in enumerate(lines):
        key = line.split("=", 1)[0].strip()
        if key in wanted:
            lines[i] = f"{key}={wanted[key]}"
            seen.add(key)
    lines += [f"{key}={value}" for key, value in wanted.items() if key not in seen]
    config.write_text("\n".join(lines) + "\n")


def device_is_up():
    for line in run(["adb", "devices"]).stdout.decode().splitlines()[1:]:
        parts = line.split()
        if len(parts) == 2 and parts[0] == SERIAL and parts[1] == "device":
            return True
    return False


def boot(home):
    """Starts the emulator; returns its process, or None if one is already running (it is then left alone)."""
    if device_is_up():
        print(f"Reusing the running {SERIAL}")
        return None
    BUILD.mkdir(exist_ok=True)
    log_path = BUILD / "screenshots-emulator.log"
    print(f"Booting {AVD} (log: {log_path.relative_to(ROOT)})...")
    log = open(log_path, "wb")
    return subprocess.Popen(
        [str(home / "emulator/emulator"), "-avd", AVD, "-port", str(PORT), "-skin", f"{WIDTH}x{HEIGHT}",
         "-no-window", "-no-audio", "-no-boot-anim", "-no-snapshot", "-gpu", "swiftshader_indirect"],
        stdout=log, stderr=log, stdin=subprocess.DEVNULL, start_new_session=True,
    )


def wait_for_boot(proc):
    log_path = (BUILD / "screenshots-emulator.log").relative_to(ROOT)
    deadline = time.time() + BOOT_TIMEOUT_S
    while time.time() < deadline:
        if proc is not None and proc.poll() is not None:
            fail(f"the emulator exited with status {proc.returncode}; see {log_path}")
        if (device_is_up() and adb("shell", "getprop", "sys.boot_completed", check=False).strip() == "1"
                and "package:" in adb("shell", "pm", "path", "android", check=False)):
            time.sleep(5)  # system services keep starting after sys.boot_completed
            return
        time.sleep(2)
    fail(f"the emulator did not finish booting within {BOOT_TIMEOUT_S} s; see {log_path}")


def setup_device():
    for key in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
        sh(f"settings put global {key} 0")
    sh("cmd uimode night no")
    sh("settings put global sysui_demo_allowed 1")
    for extra in (
        "-e command enter",
        "-e command clock -e hhmm 1200",
        "-e command battery -e level 100 -e plugged false",
        "-e command network -e wifi show -e level 4 -e fully true",
        "-e command network -e mobile hide -e sims 0 -e nosim hide",
        "-e command notifications -e visible false",
    ):
        sh(f"am broadcast -a com.android.systemui.demo {extra}")
    for attempt in range(5):
        installed = run(["adb", "-s", SERIAL, "install", "-r", APK])
        if installed.returncode == 0:
            break
        time.sleep(3)
    else:
        fail(f"adb install failed: {(installed.stderr + installed.stdout).decode(errors='replace').strip()}")


def fixture(lang):
    """Progress JSON matching data/ProgressRepository.kt (unlisted fields take their defaults)."""
    correct, wrong = {}, {}
    for i in range(77, 114):
        correct[i], wrong[i] = 6 + (i * 7) % 9, (i * 5) % 4
    for i in range(66, 77):
        correct[i], wrong[i] = 3 + i % 4, 1 + i % 3
    for i, (c, w) in {0: (12, 0), 1: (4, 5), 17: (5, 3), 35: (6, 2)}.items():
        correct[i], wrong[i] = c, w
    return json.dumps({
        "totalCorrect": sum(correct.values()),
        "totalWrong": sum(wrong.values()),
        "surahCorrect": {str(k): v for k, v in correct.items()},
        "surahWrong": {str(k): v for k, v in wrong.items()},
        "autoPlay": False,  # an idle play button, and no audio download
        "language": LANGUAGE[lang],
    })


def seed(lang):
    sh(f"am force-stop {PKG}")
    adb("shell", "pm", "clear", PKG)
    with tempfile.NamedTemporaryFile("w", suffix=".json", delete=False) as tmp:
        tmp.write(fixture(lang))
    try:
        adb("push", tmp.name, "/data/local/tmp/hafiz-progress.json")
    finally:
        os.unlink(tmp.name)
    sh("chmod 644 /data/local/tmp/hafiz-progress.json")
    adb("shell", "run-as", PKG, "sh", "-c",
        "'mkdir -p files/datastore && cp /data/local/tmp/hafiz-progress.json files/datastore/progress.json'")
    # The system value is what the app adopts on launch: this exercises the system -> app language sync.
    sh(f"cmd locale set-app-locales {PKG} --locales {lang}")


# ---------------------------------------------------------------------------------------------------------------
# uiautomator


def dump():
    for _ in range(3):
        result = run(["adb", "-s", SERIAL, "shell", "uiautomator", "dump", "/sdcard/hafiz-ui.xml"])
        if result.returncode == 0:
            xml = adb("exec-out", "cat", "/sdcard/hafiz-ui.xml", check=False, binary=True)
            if xml.strip():
                try:
                    return xml, ET.fromstring(xml)
                except ET.ParseError:
                    pass
        time.sleep(0.5)  # uiautomator sometimes reports "could not get idle state"
    return b"", None


BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def find_in(root, text=None, rid=None):
    for node in root.iter("node"):
        hit = (text is not None and text in (node.get("text"), node.get("content-desc"))) or (
            rid is not None and node.get("resource-id") == rid)
        if hit:
            m = BOUNDS.fullmatch(node.get("bounds", ""))
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                return (x1 + x2) // 2, (y1 + y2) // 2
    return None


def wait_for(text=None, rid=None):
    target = text if text is not None else rid
    deadline = time.time() + WAIT_TIMEOUT_S
    xml = b""
    while True:
        xml, root = dump()
        if root is not None:
            found = find_in(root, text, rid)
            if found:
                return found
        if time.time() >= deadline:
            break
        time.sleep(0.5)
    BUILD.mkdir(exist_ok=True)
    (BUILD / "screenshots-failure.xml").write_bytes(xml)
    (BUILD / "screenshots-failure.png").write_bytes(adb("exec-out", "screencap", "-p", check=False, binary=True))
    fail(f"timed out waiting for {target!r} (see build/screenshots-failure.png/.xml)")


def tap(text=None, rid=None):
    x, y = wait_for(text, rid)
    sh(f"input tap {x} {y}")
    time.sleep(0.7)


def back():
    sh("input keyevent KEYCODE_BACK")
    time.sleep(0.7)


def launch():
    sh(f"am force-stop {PKG}")
    adb("shell", "am", "start", "-W", "-n", ACTIVITY)


def screencap():
    png = adb("exec-out", "screencap", "-p", binary=True)
    width, height = struct.unpack(">II", png[16:24])
    if (width, height) != (WIDTH, HEIGHT):
        fail(f"screenshot is {width}x{height}, expected {WIDTH}x{HEIGHT}")
    return png


def save(lang, n, png):
    path = Path(OUT[lang]) / f"{n}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)
    print(f"✓ {path}")


def shot(lang, n):
    time.sleep(1)
    save(lang, n, screencap())


def shot_loading(lang, n):
    """Cold-starts the app and captures its load screen; retries when the capture came too late."""
    for _ in range(LOAD_ATTEMPTS):
        launch()
        started = time.monotonic()
        time.sleep(LOAD_SETTLE_S)
        png = screencap()
        if time.monotonic() - started <= LOAD_CAPTURE_MAX_S:
            save(lang, n, png)
            return
    fail(f"could not capture the load screen within {LOAD_CAPTURE_MAX_S} s of launch in {LOAD_ATTEMPTS} attempts")


def capture(lang):
    label = LABELS[lang]
    seed(lang)
    shot_loading(lang, 5)
    wait_for(label["play"])
    shot(lang, 1)

    tap(label["play"])
    tap(label["regular"])
    tap(label["guess"])
    tap(rid="level.0")
    wait_for(rid="quiz.option.3")
    shot(lang, 2)

    back()
    wait_for(rid="level.0")
    back()
    wait_for(label["position"])
    tap(label["position"])
    tap(rid="level.0")
    wait_for(rid="quiz.option.5")
    shot(lang, 3)

    launch()
    wait_for(label["play"])
    tap(label["stats"])
    wait_for(label["surahs"])
    tap(label["surahs"])
    time.sleep(1.5)  # the map lays out after the tab switch
    shot(lang, 4)

    launch()
    wait_for(label["play"])
    tap(label["credits"])
    wait_for(label["sources"])
    shot(lang, 6)


def main():
    preflight()
    home = android_home()
    # Keep the AVD inside the SDK: avdmanager and the emulator otherwise disagree about where AVDs live
    # (avdmanager follows XDG_CONFIG_HOME, the emulator only $HOME/.android/avd).
    avd_home = Path(os.environ.setdefault("ANDROID_AVD_HOME", str(home / "avd")))
    avd_home.mkdir(parents=True, exist_ok=True)
    ensure_sdk_packages(home)
    ensure_avd()
    proc = boot(home)
    try:
        wait_for_boot(proc)
        setup_device()
        for lang in ("ar", "en"):
            capture(lang)
    finally:
        if proc is not None:
            run(["adb", "-s", SERIAL, "emu", "kill"])
            try:
                proc.wait(timeout=30)
            except subprocess.TimeoutExpired:
                proc.kill()


if __name__ == "__main__":
    main()
