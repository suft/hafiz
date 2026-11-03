#!/usr/bin/env python3
"""Regenerates the Phosphor and Hugeicons vector drawables in app/src/main/res/drawable/ (ph_*.xml and hi_*.xml).

Source: the Iconify JSON packages @iconify-json/ph (Phosphor Icons) and @iconify-json/hugeicons, pinned in SOURCES
and fetched from jsDelivr. Both are MIT; the licence texts are in app/src/main/assets/licenses/.
Each icon's SVG body is converted to an Android <vector>: only <g> and <path> elements with the attributes
d, fill, fill-rule, stroke, stroke-width, stroke-linecap and stroke-linejoin are supported, and anything else is an
error. Colours are written black; Compose's Icon tints the whole drawable, so they never show.
Every icon is resolved before anything is written, so any failure aborts without touching the drawables.
Files named ph_*.xml or hi_*.xml that are not in ICONS are deleted.

Usage: python3 tools/icons.py      (or: mise run icons)
"""
import json
import sys
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path
from xml.sax.saxutils import quoteattr

ROOT = Path(__file__).resolve().parent.parent
SOURCES = {
    # prefix: (npm package, version, credit, licence file)
    "ph": ("@iconify-json/ph", "1.2.2", "Phosphor Icons 2.1.1", "Phosphor-MIT.txt"),
    "hi": ("@iconify-json/hugeicons", "1.2.35", "Hugeicons", "Hugeicons-MIT.txt"),
}
URL = "https://cdn.jsdelivr.net/npm/{package}@{version}/icons.json"
OUT_DIR = ROOT / "app/src/main/res/drawable"
ATTEMPTS = 3
# (prefix, source icon name, autoMirrored)
ICONS = [
    ("ph", "arrow-left", True), ("ph", "arrow-right", True), ("ph", "caret-right", True),
    ("ph", "caret-down", False), ("ph", "caret-double-right", True), ("ph", "question", True),
    ("ph", "arrow-square-out", True), ("ph", "chart-line-up", False), ("ph", "info", False),
    ("ph", "gear-six", False), ("ph", "play-fill", False), ("ph", "pause-fill", False),
    ("ph", "x", False), ("ph", "check", False), ("ph", "flag", False), ("ph", "fire", False),
    ("ph", "copy", False), ("ph", "mosque", False), ("ph", "heart", False), ("ph", "heart-fill", False),
    ("ph", "star-fill", False), ("ph", "trophy-fill", False), ("ph", "check-circle-fill", False),
    ("ph", "x-circle-fill", False), ("ph", "broom", False), ("ph", "list-checks", False),
    ("ph", "map-pin", False),
    ("hi", "kaaba-01", False), ("hi", "the-prophets-mosque", False),
]
ALLOWED = {"d", "fill", "fill-rule", "stroke", "stroke-width", "stroke-linecap", "stroke-linejoin"}
SVG_NS = "{http://www.w3.org/2000/svg}"
BLACK = "#FF000000"
CLEAR = "#00000000"


class IconError(Exception):
    pass


def fetch(url):
    """The URL's JSON; ATTEMPTS tries, 2 s * attempt apart."""
    last = None
    for attempt in range(1, ATTEMPTS + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "hafiz-tools"})
            with urllib.request.urlopen(req, timeout=60) as r:
                return json.load(r)
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as e:
            last = e
            if attempt < ATTEMPTS:
                time.sleep(2 * attempt)
    raise IconError(f"download failed: {url}: {last}")


def resolve(pack, name):
    """(body, width, height) of the icon `name`, following one level of alias."""
    icons = pack.get("icons", {})
    if name in icons:
        icon = icons[name]
    elif name in pack.get("aliases", {}):
        alias = pack["aliases"][name]
        bad = [k for k in ("hFlip", "vFlip", "rotate") if k in alias]
        if bad:
            raise IconError(f"alias of {alias.get('parent')!r} uses unsupported transform {bad}")
        parent = icons.get(alias.get("parent"))
        if parent is None:
            raise IconError(f"alias parent {alias.get('parent')!r} not found")
        icon = {**parent, **alias}
    else:
        raise IconError("not in package")
    width = icon.get("width", pack.get("width"))
    height = icon.get("height", pack.get("height"))
    if "body" not in icon or not width or not height:
        raise IconError("missing body or size")
    return icon["body"], width, height


def paths(element, inherited):
    """Flat list of attribute dicts, one per <path> under `element`; <g> attributes pass down to children."""
    out = []
    for child in element:
        tag = child.tag.removeprefix(SVG_NS)
        extra = set(child.attrib) - ALLOWED
        if extra:
            raise IconError(f"unsupported attribute {sorted(extra)} on <{tag}>")
        attrs = {**inherited, **child.attrib}
        if tag == "g":
            attrs.pop("d", None)
            out += paths(child, attrs)
        elif tag == "path":
            if "d" not in child.attrib:
                raise IconError("<path> without d")
            out.append(attrs)
        else:
            raise IconError(f"unsupported element <{tag}>")
    return out


def path_xml(attrs):
    fill = CLEAR if attrs.get("fill") == "none" else BLACK
    parts = [f'android:fillColor="{fill}"']
    if attrs.get("fill-rule") == "evenodd":
        parts.append('android:fillType="evenOdd"')
    stroke = attrs.get("stroke")
    if stroke is not None and stroke != "none":
        parts.append(f'android:strokeColor="{BLACK}"')
        parts.append(f'android:strokeWidth={quoteattr(attrs.get("stroke-width", "1"))}')
        if "stroke-linecap" in attrs:
            parts.append(f'android:strokeLineCap={quoteattr(attrs["stroke-linecap"])}')
        if "stroke-linejoin" in attrs:
            parts.append(f'android:strokeLineJoin={quoteattr(attrs["stroke-linejoin"])}')
    parts.append(f"android:pathData={quoteattr(attrs['d'])}")
    return "  <path " + " ".join(parts) + "/>"


def vector_xml(prefix, name, mirrored, body, width, height):
    try:
        root = ET.fromstring('<svg xmlns="http://www.w3.org/2000/svg">' + body + "</svg>")
    except ET.ParseError as e:
        raise IconError(f"unparsable body: {e}")
    elements = paths(root, {})
    if not elements:
        raise IconError("no paths")
    _, _, credit, licence = SOURCES[prefix]
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        f'<!-- Generated by tools/icons.py from {credit} "{name}" (MIT, assets/licenses/{licence}). Do not edit. -->',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '  android:width="24dp"',
        '  android:height="24dp"',
        f'  android:viewportWidth="{width}"',
        f'  android:viewportHeight="{height}"' + ("" if mirrored else ">"),
    ]
    if mirrored:
        lines.append('  android:autoMirrored="true">')
    lines += [path_xml(a) for a in elements]
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def main():
    packs = {}
    files = {}
    try:
        for prefix, name, mirrored in ICONS:
            if prefix not in packs:
                package, version = SOURCES[prefix][:2]
                packs[prefix] = fetch(URL.format(package=package, version=version))
            try:
                body, width, height = resolve(packs[prefix], name)
                files[f"{prefix}_{name.replace('-', '_')}.xml"] = vector_xml(prefix, name, mirrored, body, width, height)
            except IconError as e:
                raise IconError(f"{prefix}:{name}: {e}")
    except IconError as e:
        print(f"error: {e}", file=sys.stderr)
        sys.exit(1)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for stale in [*OUT_DIR.glob("ph_*.xml"), *OUT_DIR.glob("hi_*.xml")]:
        if stale.name not in files:
            stale.unlink()
    for file, text in files.items():
        (OUT_DIR / file).write_text(text, encoding="utf-8", newline="\n")
    print(f"✓ {len(files)} icons → {OUT_DIR.relative_to(ROOT)}/")


if __name__ == "__main__":
    main()
