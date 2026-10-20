#!/usr/bin/env python3
"""Regenerates app/src/main/assets/mutashabihat.txt from Aswaatul Qurraa's mutashabihat data.

Source: https://www.aswaatulqurraa.com/mutashabihat/ (one JSON file per juz). Only ayah references and a
strength score are kept, never the site's text. Every reference is checked against the bundled Tanzil text
and dropped when its words do not match (the site has a few wrong ayah numbers).

Usage: python3 tools/mutashabihat.py [--out PATH]      (or: mise run mutashabihat)
"""
import argparse
import json
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
QURAN_XML = ROOT / "app/src/main/assets/quran_simple.xml"
DEFAULT_OUT = ROOT / "app/src/main/assets/mutashabihat.txt"
URL = "https://aswaatulqurraa.b-cdn.net/Files/Json/Mutashabihat/Juz%20{}.json"
MIN_WORD_OVERLAP = 0.3  # Jaccard overlap between the site's verse and the Tanzil ayah(s)

# Surah names as spelled by the site -> surah number. Unknown names abort the run: add them here.
SURAH_NUMBERS = {
    "Fatihah": 1, "Baqarah": 2, "Al 'Imran": 3, "An Nisa'": 4, "Al Ma'idah": 5, "Al An'am": 6,
    "Al A'raf": 7, "Al Anfal": 8, "At Taubah": 9, "Yunus": 10, "Hud": 11, "Yusuf": 12, "Ar Ra'd": 13,
    "Ibrahim": 14, "Al Hijr": 15, "An Nahl": 16, "Al Isra": 17, "Al Kahf": 18, "Maryam": 19, "Taha": 20,
    "Al Anbiya'": 21, "Al Hajj": 22, "Al Mu'minun": 23, "An Nur": 24, "Al Furqan": 25,
    "Ash Shu'ara": 26, "Ash Shu'ara'": 26, "An Naml": 27, "Al Qasas": 28, "Al 'Ankabut": 29,
    "Ar Rum": 30, "Luqman": 31, "As Sajdah": 32, "Al Ahzab": 33, "Saba'": 34, "Fatir": 35,
    "Ya Sin": 36, "As Saffat": 37, "Sad": 38, "Az Zumar": 39, "Al Mu'min": 40, "Ha Mim": 41,
    "Ash Shuraa": 42, "Az Zukhruf": 43, "Ad Dukhan": 44, "Al Jathiyah": 45, "Al Ahqaf": 46,
    "Muhammad": 47, "Al Fath": 48, "Al Hujurat": 49, "Qaf": 50, "Ad Dhariyat": 51, "At Tur": 52,
    "An Najm": 53, "Al Qamar": 54, "Ar Rahman": 55, "Al Waqi'ah": 56, "Al Hadid": 57,
    "Al Mujadilah": 58, "Al Hashr": 59, "Al Mumtahina": 60, "As Saff": 61, "Al Jumu'ah": 62,
    "Al Munafiqun": 63, "At Taghabun": 64, "At Talaq": 65, "At Tahrim": 66, "Al Mulk": 67,
    "Al Qalam": 68, "Al Haqqah": 69, "Al Ma'arij": 70, "Nuh": 71, "Al Jinn": 72,
    "Al Muzammil": 73, "Al Muzzammil": 73, "Al Muddaththir": 74, "Al Qiyamah": 75, "Al Insan": 76,
    "Al Mursalat": 77, "An Naba'": 78, "An Nazi'at": 79, "'Abasa": 80, "At Takwir": 81,
    "Al Infitar": 82, "Al Mutaffifin": 83, "Al Inshiqaq": 84, "Al Buruj": 85, "At Tariq": 86,
    "Al A'la": 87, "Al Ghashiyah": 88, "Al Fajr": 89, "Al Balad": 90, "Ash Shams": 91, "Al Lail": 92,
    "Ad Duha": 93, "Al Inshirah": 94, "At Tin": 95, "Al 'Alaq": 96, "Al Qadr": 97, "Al Bayyinah": 98,
    "Al 'Adiyat": 100, "Al Qari'ah": 101, "Al 'Asr": 103, "Al Humazah": 104, "Al Fil": 105,
    "Al Ma'un": 107, "An Nasr": 110, "Al Ikhlas": 112,
}

TAG = re.compile(r"<[^>]+>")
SEGMENT = re.compile(r"<(c\d*)>(.*?)</c\d*>", re.S)
MARKS = re.compile("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640\u200B-\u200F]")
NOT_LETTER = re.compile("[^\u0621-\u064A ]")


def words(text):
    """Arabic words without diacritics or tags, with letter variants folded (Tanzil vs. Indo-Pak script)."""
    t = MARKS.sub("", TAG.sub(" ", text))
    t = re.sub("[أإآٱ]", "ا", t).replace("ى", "ي").replace("ی", "ي").replace("ک", "ك").replace("ە", "ه")
    return NOT_LETTER.sub(" ", t).split()


def shared_percent(verses):
    """Share of the verse's words that the site marks as common (<c0>…<c9>; plain <c> is the differing part)."""
    shared = total = 0
    for verse in verses:
        for tag, text in SEGMENT.findall(verse):
            n = len(words(text))
            total += n
            if tag != "c":
                shared += n
    return int(100 * shared / total + 0.5) if total else 0


def load_quran():
    surahs = {int(s.get("index")): [a.get("text") for a in s.iter("aya")] for s in ET.parse(QURAN_XML).getroot().iter("sura")}
    assert len(surahs) == 114, "expected 114 surahs"
    return surahs


def fetch(juz):
    req = urllib.request.Request(URL.format(juz), headers={"User-Agent": "AyahIQ-mutashabihat-import"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    out = parser.parse_args().out

    quran = load_quran()
    dropped = []

    def ayahs(entry):
        """(surah, [ayah, …]) for a reference such as {"ch": "Baqarah", "v": "67"} or "v": "5-6"; None if it does not match Tanzil."""
        ref = entry["reference"]
        if ref["ch"] not in SURAH_NUMBERS:
            sys.exit(f"Unknown surah name {ref['ch']!r}: add it to SURAH_NUMBERS")
        surah = SURAH_NUMBERS[ref["ch"]]
        first, _, last = str(ref["v"]).replace("–", "-").partition("-")
        first, last = int(first), int(last or first)
        if not 1 <= first <= last <= len(quran[surah]):
            dropped.append(f"{ref['ch']} {ref['v']}: no such ayah")
            return None
        tanzil = set(w for a in range(first, last + 1) for w in words(quran[surah][a - 1]))
        site = set(w for v in entry["verses"] for w in words(v))
        overlap = len(tanzil & site) / max(1, len(tanzil | site))
        if overlap < MIN_WORD_OVERLAP:
            dropped.append(f"{ref['ch']} {ref['v']}: text does not match Tanzil ({overlap:.2f})")
            return None
        return surah, list(range(first, last + 1))

    # (ayah, similar ayah) -> % of the similar ayah's words marked shared; an ayah's partner is usually
    # listed in both directions, and the weaker direction is what both ayahs really have in common.
    listed = {}
    for juz in range(1, 31):
        for item in fetch(juz):
            source = ayahs(item)
            if source is None:
                continue
            for similar in item["similarities"]:
                target = ayahs(similar)
                if target is None:
                    continue
                strength = shared_percent(similar["verses"])
                (s1, a1), (s2, a2) = source, target
                links = zip(a1, a2) if len(a1) == len(a2) else ((x, y) for x in a1 for y in a2)
                for x, y in links:
                    if (s1, x) != (s2, y):
                        key = ((s1, x), (s2, y))
                        listed[key] = max(listed.get(key, 0), strength)

    pairs = {}  # ((surah, ayah), (surah, ayah)) in mushaf order -> strength
    for (p, q), strength in listed.items():
        back = listed.get((q, p))
        pairs[tuple(sorted((p, q)))] = strength if back is None else min(strength, back)

    lines = [
        "# Mutashabihat: pairs of ayahs that closely resemble each other.",
        "# Source: Aswaatul Qurraa, https://www.aswaatulqurraa.com/mutashabihat/ (c) Team Aswaatul Qurraa.",
        "# Generated by tools/mutashabihat.py (mise run mutashabihat); do not edit by hand.",
        "# Format: <surah>:<ayah> <surah>:<ayah> <strength 0-100: % of words the source marks as shared>",
    ]
    lines += [f"{p[0]}:{p[1]} {q[0]}:{q[1]} {s}" for (p, q), s in sorted(pairs.items())]
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{len(pairs)} pairs -> {out}; {len(dropped)} references dropped", file=sys.stderr)
    for d in dropped:
        print(f"  dropped {d}", file=sys.stderr)


if __name__ == "__main__":
    main()
