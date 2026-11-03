#!/usr/bin/env python3
"""Checks that every reciter in Reciters.kt has all 6236 ayah files on everyayah.com.

Reads the reciter folders from app/src/main/java/suft/android/hafiz/audio/Reciters.kt and the per-surah ayah
counts from the bundled Quran text, then lists each folder on everyayah.com and reports missing files and
suspiciously small ones (truncated uploads).

Exit status: 0 = complete, 1 = missing or tiny files or a missing reciter folder (HTTP 4xx), 2 = the site could not be reached.

Usage: python3 tools/check_reciters.py      (or: mise run reciters)
"""
import http.client
import re
import sys
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RECITERS_KT = ROOT / "app/src/main/java/suft/android/hafiz/audio/Reciters.kt"
QURAN_XML = ROOT / "app/src/main/assets/quran_simple.xml"
BASE_URL = "https://everyayah.com/data/{}/"
USER_AGENT = "Hafiz-reciter-check"
TIMEOUT_S = 60
BACKOFF_S = (5, 10)
MIN_BYTES = 4096


class FolderMissing(Exception):
    pass


# Known server-side defects we cannot fix: (folder, file stem) -> reported as a warning only.
KNOWN_TRUNCATED = {("mahmoud_ali_al_banna_32kbps", "012067")}

FOLDER = re.compile(r'Reciter\(\s*"[^"]*",\s*"[^"]*",\s*"([^"]+)"\s*,?\s*\)')
FILE = re.compile(r'<span class="name">(\d{6})\.mp3</span>.*?<td data-order="(\d+)">', re.S)


def reciter_folders():
    text = RECITERS_KT.read_text(encoding="utf-8")
    folders = FOLDER.findall(text)
    if not folders:
        sys.exit(f"no reciters found in {RECITERS_KT}")
    entries = len(re.findall(r'Reciter\(\s*"', text))
    if len(folders) != entries:
        sys.exit(f"parsed {len(folders)} of {entries} Reciter(...) entries in Reciters.kt")
    return folders


def expected_files():
    root = ET.parse(QURAN_XML).getroot()
    names = set()
    for sura in root.iter("sura"):
        surah = int(sura.get("index"))
        for aya in sura.iter("aya"):
            names.add(f"{surah:03d}{int(aya.get('index')):03d}")
    return names


def listing(folder):
    """{file stem: size in bytes} of a folder, retrying transient failures."""
    last = None
    for attempt in range(len(BACKOFF_S) + 1):
        try:
            request = urllib.request.Request(BASE_URL.format(folder), headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(request, timeout=TIMEOUT_S) as response:
                html = response.read().decode("utf-8", "replace")
            return {stem: int(size) for stem, size in FILE.findall(html)}
        except urllib.error.HTTPError as e:
            if e.code < 500:
                raise FolderMissing(f"{folder}: HTTP {e.code}") from e
            last = e
        except (OSError, http.client.HTTPException) as e:
            last = e
        if attempt < len(BACKOFF_S):
            time.sleep(BACKOFF_S[attempt])
    raise ConnectionError(f"{folder}: {last}")


def main():
    expected = expected_files()
    failed = False
    for folder in reciter_folders():
        try:
            found = listing(folder)
        except FolderMissing as e:
            failed = True
            print(f"FAIL {e}")
            continue
        except ConnectionError as e:
            print(f"network error: {e}", file=sys.stderr)
            return 2
        missing = sorted(expected - found.keys())
        tiny = sorted(s for s, size in found.items() if s in expected and size < MIN_BYTES)
        known = [s for s in tiny if (folder, s) in KNOWN_TRUNCATED]
        unknown = [s for s in tiny if (folder, s) not in KNOWN_TRUNCATED]
        for s in known:
            print(f"warning: {folder}/{s}.mp3 is truncated on the server ({found[s]} bytes); known, not fixable here")
        if missing or unknown:
            failed = True
            print(f"FAIL {folder}: {len(missing)} missing, {len(unknown)} under {MIN_BYTES} bytes")
            if missing:
                print("  missing:", ", ".join(missing[:10]) + (" ..." if len(missing) > 10 else ""))
            if unknown:
                print("  tiny:", ", ".join(f"{s} ({found[s]} B)" for s in unknown[:10]))
        else:
            print(f"ok   {folder}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
