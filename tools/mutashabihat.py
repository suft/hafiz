#!/usr/bin/env python3
"""Regenerates app/src/main/assets/mutashabihat.txt from Aswaatul Qurraa's mutashabihat data.

Source: https://www.aswaatulqurraa.com/mutashabihat/ (one JSON file per juz). Only ayah references and a
strength score are kept, never the site's text. Every reference is checked against the bundled Tanzil text
and dropped when its words do not match (the site has a few wrong ayah numbers). The hand-curated pairs in
tools/mutashabihat_supplement.txt (surahs the source does not cover) are merged in.

Usage: python3 tools/mutashabihat.py [--out PATH] [--offline] [--force]      (or: mise run mutashabihat)
  --offline  do not fetch: re-read the pairs from --out and merge the supplement into them
  --force    write even when the fetched data has under 90% of the pairs already in --out
"""
import argparse
import json
import re
import sys
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
QURAN_XML = ROOT / "app/src/main/assets/quran_simple.xml"
DEFAULT_OUT = ROOT / "app/src/main/assets/mutashabihat.txt"
SUPPLEMENT = ROOT / "tools/mutashabihat_supplement.txt"
PAIR_LINE = re.compile(r"^(\d+):(\d+) (\d+):(\d+) (\d+)$")
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


def read_pairs(path, quran):
    """{((surah, ayah), (surah, ayah)) in mushaf order: strength} from a `<s>:<a> <s>:<a> <n>` file; exits on a bad line."""
    pairs = {}
    for n, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        m = PAIR_LINE.match(line)
        if not m:
            sys.exit(f"{path.name} line {n}: expected '<surah>:<ayah> <surah>:<ayah> <strength>', got {line!r}")
        s1, a1, s2, a2, strength = map(int, m.groups())
        for s, a in ((s1, a1), (s2, a2)):
            if s not in quran or not 1 <= a <= len(quran[s]):
                sys.exit(f"{path.name} line {n}: no ayah {s}:{a}")
        if (s1, a1) == (s2, a2):
            sys.exit(f"{path.name} line {n}: an ayah cannot resemble itself")
        if not 0 <= strength <= 100:
            sys.exit(f"{path.name} line {n}: strength {strength} is not in 0-100")
        key = tuple(sorted(((s1, a1), (s2, a2))))
        pairs[key] = max(pairs.get(key, 0), strength)
    return pairs


def fetch(juz):
    """The juz' JSON; three attempts (2 s, then 4 s apart), exits if it stays unreachable, unparseable or empty."""
    last = None
    for delay in (2, 4, None):
        try:
            req = urllib.request.Request(URL.format(juz), headers={"User-Agent": "AyahIQ-mutashabihat-import"})
            with urllib.request.urlopen(req, timeout=60) as r:
                data = json.load(r)
            if not data:
                last = "empty list"
                break
            return data
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as e:
            last = e
            if delay:
                time.sleep(delay)
    sys.exit(f"juz {juz}: {last}")


def fetch_pairs(quran, dropped):
    """Pairs derived from the live source; `dropped` collects the references that were skipped."""
    unknown = set()

    def ayahs(entry):
        """(surah, [ayah, …]) for a reference such as {"ch": "Baqarah", "v": "67"} or "v": "5-6"; None if it does not match Tanzil."""
        ref = entry["reference"]
        if ref["ch"] not in SURAH_NUMBERS:
            unknown.add(ref["ch"])
            return None
        surah = SURAH_NUMBERS[ref["ch"]]
        m = re.fullmatch(r"\s*(\d+)\s*(?:-\s*(\d+))?\s*", str(ref["v"]).replace("–", "-"))
        if not m:
            dropped.append(f"{ref['ch']} {ref['v']}: unparseable reference")
            return None
        first, last = int(m.group(1)), int(m.group(2) or m.group(1))
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
                if len(a1) == len(a2):
                    links = zip(a1, a2)
                else:
                    # Ranges of different length cannot be paired ayah by ayah; link only where they start.
                    links = [(a1[0], a2[0])]
                    dropped.append(f"{s1}:{a1[0]}-{a1[-1]} ~ {s2}:{a2[0]}-{a2[-1]}: unequal ranges, only the first ayahs are linked")
                for x, y in links:
                    if (s1, x) != (s2, y):
                        key = ((s1, x), (s2, y))
                        listed[key] = max(listed.get(key, 0), strength)
    if unknown:
        sys.exit(f"Unknown surah names {sorted(unknown)}: add them to SURAH_NUMBERS")

    pairs = {}  # ((surah, ayah), (surah, ayah)) in mushaf order -> strength
    for (p, q), strength in listed.items():
        back = listed.get((q, p))
        pairs[tuple(sorted((p, q)))] = strength if back is None else min(strength, back)
    return pairs


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    parser.add_argument("--offline", action="store_true", help="re-read the pairs from --out instead of fetching, then merge the supplement")
    parser.add_argument("--force", action="store_true", help="write even if the result has under 90%% of the existing pairs")
    args = parser.parse_args()
    out = args.out

    quran = load_quran()
    dropped = []
    previous = read_pairs(out, quran) if out.exists() else {}
    if args.offline and not previous:
        sys.exit(f"--offline needs an existing {out}")
    pairs = dict(previous) if args.offline else fetch_pairs(quran, dropped)

    for key, strength in read_pairs(SUPPLEMENT, quran).items():
        pairs[key] = max(pairs.get(key, 0), strength)

    if not args.offline and previous and len(pairs) < 0.9 * len(previous) and not args.force:
        sys.exit(f"Only {len(pairs)} pairs against {len(previous)} in {out}: the source looks incomplete (use --force to write anyway)")

    lines = [
        "# Mutashabihat: pairs of ayahs that closely resemble each other.",
        "# Source: Aswaatul Qurraa, https://www.aswaatulqurraa.com/mutashabihat/ (c) Team Aswaatul Qurraa.",
        "# Generated by tools/mutashabihat.py (mise run mutashabihat); do not edit by hand.",
        "# Plus hand-curated pairs from tools/mutashabihat_supplement.txt for surahs the source does not cover.",
        "# Format: <surah>:<ayah> <surah>:<ayah> <strength 0-100: % of words the source marks as shared>",
    ]
    lines += [f"{p[0]}:{p[1]} {q[0]}:{q[1]} {s}" for (p, q), s in sorted(pairs.items())]
    tmp = out.with_suffix(".tmp")
    tmp.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    tmp.replace(out)
    print(f"{len(pairs)} pairs -> {out}; {len(dropped)} references dropped", file=sys.stderr)
    for d in dropped:
        print(f"  dropped {d}", file=sys.stderr)


if __name__ == "__main__":
    main()
