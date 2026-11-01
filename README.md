# Hafiz 3.0

A Quran memorisation quiz for Android, rewritten from the original *AyahSurah 2.0* APK
(Java + Android Views, 2013) in **Kotlin + Jetpack Compose**, with a **mise** toolchain.

| Question style | What you do | Levels | Questions / round (Regular) |
|---|---|---|---|
| **آية وسورة** (guess the surah) | Read/hear an ayah and pick the surah it's from | 20 | 20 |
| **آيات** (next ayah) | Read/hear an ayah and pick the one that follows it | 25 | 10 |
| **أين الآية؟** (where's the ayah) | Read/hear an ayah and pick where it starts in the Madinah mushaf | 25 | 10 |

From the main menu, **Play** opens the **game modes**; pick a mode, then a question style and a level (Mutashabihat has no levels: it always plays the whole Quran). **How to play** explains the styles, modes and levels in the app.

| Game mode | How it ends | Score |
|---|---|---|
| **عادي** (Regular) | After a fixed-length round | Correct answers out of the round |
| **البقاء** (Survival) | First wrong answer | Correct answers in a row |
| **المتشابهات** (Mutashabihat) | Third wrong answer | Correct answers; the wrong options are *mutashabihat* traps: surahs and next ayahs that resemble the right answer |

In guess-the-surah, wrong surahs are drawn from the whole level, weighted by surah length and (Regular, Survival) by how many of the ayah's words they share, how close their typical ayah length (geometric mean of words per ayah) is to the answer's and whether they were revealed in the same place (Makkah or Madinah), so no option stands out by number, length, topic or style. The word overlap is computed on the device from the bundled Tanzil text (`domain/ThemeIndex.kt`); no topic data set is used. Regular and Survival also swap in one *mutashabihat* look-alike (a surah or next ayah from the same level that resembles the answer) when the prompt has one.

Survival keeps a best score per level; Mutashabihat keeps one best score per style. Their games show up in **إحصائيات** (stats: overall and per-style accuracy, the surahs to review, per-mode games played, best score, average and longest streak) and its activity history.

**مكية أم مدنية** (Makki or Madani) sits next to the modes on the Play screen. It has no styles or levels: five surahs, each answered Makki or Madani (equal chance of each; revelation places are quran.com's `revelation_place`, bundled in `domain/Revelation.kt`). The card shows the best score and every finished game appears in the activity history.

**أين الآية؟** (Where's the Ayah?) shows an ayah and asks where it starts in the open Madinah mushaf (1405H print): the right or left page × the top, middle or bottom third (lines 1–5, 6–10, 11–15), as a 3×2 grid with the right page on the right. It reuses the next-ayah levels and is not offered in Mutashabihat mode (its options are fixed, with no look-alikes). Ayahs on the framed pages 1–2, and ayahs whose identical copy starts in another part of the mushaf, are never asked.

Recitations stream from [everyayah.com](https://everyayah.com) (26 reciters). Quran text: [Tanzil](https://tanzil.net) (Simple, v1.0.2).

---

## Build it

You only need [mise](https://mise.jdx.dev/getting-started.html) and `curl`/`unzip`. mise installs the JDK and the tasks download the Android SDK, so Android Studio isn't required.

```bash
cd hafiz
mise trust          # allow this project's mise.toml
mise install        # Temurin JDK 21
mise run setup      # Android cmdline-tools + platform 37 + build-tools (first run only)
mise run build      # -> dist/Hafiz-debug.apk
```

Install on a phone with USB debugging enabled: `mise run run` (installs and launches it), or copy `dist/Hafiz-debug.apk` to the phone.

### All tasks (`mise tasks`)

| Task | What it does |
|---|---|
| `setup` / `sdk` | Download the Android SDK into `.android-sdk/` (or use `$ANDROID_HOME` if it's already set) and write `local.properties` |
| `build` (`b`) | Debug APK → `dist/Hafiz-debug.apk` |
| `release` | Minified release APK → `dist/Hafiz-release.apk` |
| `bundle` | Release `.aab` for Google Play |
| `keystore` | Create `release.jks` + `keystore.properties` for release signing |
| `mutashabihat` | Regenerate `assets/mutashabihat.txt` from Aswaatul Qurraa (python3) and merge the supplement |
| `mushaf-layout` | Regenerate `assets/mushaf_layout.txt` (the page and line of every ayah in the Madinah mushaf) from quran.com (python3) |
| `reciters` | Check that every reciter has all 6236 ayah files on everyayah.com (python3) |
| `test` (`t`) | JVM unit tests (quiz logic, progress, stats) and Robolectric UI tests |
| `lint` / `check` | Android lint / tests + lint |
| `install`, `run`, `logcat` | Install on a device via adb, launch it, follow its logs |
| `clean` | Delete build outputs |

**Release signing.** A release build needs a real key: run `mise run keystore` once (then back up `release.jks` and its password; both files are git-ignored), or set `HAFIZ_KEYSTORE_FILE`, `HAFIZ_KEYSTORE_PASSWORD`, `HAFIZ_KEY_ALIAS` and `HAFIZ_KEY_PASSWORD`. Without one, `assembleRelease` stops with an explanation instead of silently signing with the public debug key. For a local test build only, set `HAFIZ_ALLOW_DEBUG_SIGNING=1` (CI does); never distribute such an APK.

**Upgrading from the old app.** The package name changed from `zozo.android.ayahsurra` to `suft.android.hafiz`, so this installs as a separate app and does not update the old one. Scores and settings are not carried over. The debug build uses `suft.android.hafiz.debug`.

**Android Studio** works too: open the folder and it will use the Gradle wrapper. Run `mise run sdk` first, or let Studio create `local.properties` itself.

---

## What changed vs. 2.0

**Rewrite**
- 100 % Kotlin, a single activity, Jetpack Compose with Material 3, and type-safe Navigation Compose.
- Media3/ExoPlayer replaces the hand-rolled pool of three `MediaPlayer`s. Each question's ayahs play as a playlist.
- DataStore (JSON) replaces Java `ObjectOutputStream` files.
- The quiz logic is plain Kotlin with unit tests. They generate thousands of questions per run across all 45 levels.
- Removed: the AppBrain ad SDK, the old bit.ly/Play-store promo links, and cleartext HTTP. Audio is HTTPS-only.

**Fixes**
- **Ambiguous questions removed.** Some ayahs appear in several surahs: «الم» opens six of them. The refrains of الرحمن, القمر and المرسلات are followed by different ayahs each time. The original could offer a second correct answer as a "wrong" option. The generator now excludes these.
- The two **Minshawi** reciter folders were swapped.
- Levels with few surahs or ayahs could loop forever while picking wrong answers. The generator now falls back to a wider pool.
- Audio URLs are formatted with `Locale.ROOT`. An Arabic device locale would otherwise produce Arabic-Indic digits and break the URL.
- No repeated ayahs within a round. Locked or half-migrated level data is gone: every level is open, as it effectively was in 2.0.

**Improvements**
- Recitations are **cached on disk** (200 MB LRU). With auto-play on, the next question's audio is prefetched, so replays work offline; with it off nothing is downloaded until you press play.
- Your reciter and auto-play choices are remembered. Six new reciters: القطامي، الرفاعي، الحذيفي، فارس عباد، محمد أيوب، البدير.
- Ayah text is set in the **Amiri Quran** font. The next-ayah mode shows ornate ayah numbers ﴿٥﴾. Long answer options are shortened to a distinct opening phrase.
- New level list with progress bars and a "mastered" mark, a result dialog that shows new records, a "next" button to skip the feedback delay, and resettable statistics.
- **Statistics screen with three tabs.** Overview shows overall accuracy plus per-mode accuracy, mastered levels and points. Surahs shows a 114-cell accuracy map, the surahs to review and the strongest surahs, with a **Practise** button that starts a 10-question next-ayah round limited to one surah (records answers and history, never a best score). Activity shows day streaks, questions today, completed rounds, correct-answer runs, a score trend chart and the last 20 rounds. Per-mode counts and activity are only tracked from the version that added them.
- Edge-to-edge layout, dark theme, adaptive and themed launcher icon, and backup rules for scores.
- **Arabic / English UI.** A selector on the main menu switches labels, digits, surah and level names, reciter names, ayah references and the layout direction (RTL / LTR). The choice is saved. Quran text always stays Arabic. Strings live in `ui/i18n/Strings.kt`; English surah names are quran.com transliterations in `domain/SurahNames.kt`.

---

## Project layout

```
mise.toml                        tools (JDK), env (ANDROID_HOME), tasks
gradle/libs.versions.toml        all dependency versions
app/src/main/
  assets/quran_simple.xml        Tanzil Quran text (verbatim, keep the licence header)
  assets/mutashabihat.txt        similar-ayah pairs for Mutashabihat mode (generated: mise run mutashabihat)
  assets/mushaf_layout.txt       page and line where each ayah starts in the Madinah mushaf (generated: mise run mushaf-layout)
  java/suft/android/hafiz/
    domain/                      pure Kotlin: Quran index, XML parser, levels, question generator, stats
    data/ProgressRepository.kt   DataStore: best scores, per-surah stats, settings
    audio/                       reciters, ExoPlayer wrapper, disk cache
    games/                       Google Play Games leaderboards (dormant until configured)
    ui/                          Compose: home, modes, guide, levels, quiz (+ViewModel), stats, theme
  res/font/                      Amiri, Rakkas, Inter (all OFL)
  res/drawable-nodpi/            paper texture (from the original app)
tools/mutashabihat.py            regenerates assets/mutashabihat.txt from Aswaatul Qurraa (python3; --offline only merges the supplement)
tools/mutashabihat_supplement.txt  hand-curated pairs for the surahs the source does not cover
tools/check_reciters.py          checks every reciter's files on everyayah.com (also .github/workflows/reciters.yml)
tools/mushaf_layout.py           regenerates assets/mushaf_layout.txt from the quran.com API (python3)
app/src/test/                    JVM tests on the real Quran text (QuizLogicTest, QuestionCorrectnessTest, ProgressTest, ProgressStoreTest, StatsTest, ArabicTextTest, StringsTest)
                                 and Robolectric UI tests (quiz layout/lifecycle, navigation, stats); all run in `mise run test`
```

### Versions
AGP 9.4.1 · Gradle 9.8.2 · Kotlin 2.4.21 · Compose BOM 2026.09.00 · Media3 1.11.1 · compileSdk 37 · targetSdk 36 · minSdk 24.

All versions are in `gradle/libs.versions.toml`. Android Studio's *Upgrade Assistant* can bump them.

---

## Licences & credits
- **Quran text:** Tanzil Project, CC BY 3.0. The text must not be modified and the source must be credited with a link; the credits screen (the ⓘ button on the main menu) does this.
- **Recitations:** streamed from everyayah.com. One clip is truncated on the server and cannot be fixed here: `mahmoud_ali_al_banna_32kbps/012067.mp3` (surah 12, ayah 67). `mise run reciters` (also a weekly GitHub workflow) checks that every reciter has all 6236 files and lists known defects as warnings.
- **Mutashabihat mode:** ayah pairs derived from the data of [Aswaatul Qurraa](https://www.aswaatulqurraa.com/mutashabihat/), © Team Aswaatul Qurraa; only ayah references and a similarity score are bundled, and the credits screen credits the source. No licence is stated on the site, so get their permission before publishing. The source covers 106 of the 114 surahs; `tools/mutashabihat_supplement.txt` adds hand-curated pairs for the other eight (99, 102, 106, 108, 109, 111, 113, 114) so Mutashabihat mode has traps for them too. Review those pairs before a release.
- **Where's the Ayah?:** the page and line where each ayah starts in the Madinah mushaf (1405H print, KFGQPC v1: 604 pages of 15 lines) come from the [quran.com API](https://api.quran.com/api/v4) (`v1_page` / `line_v1` of each ayah's first word); only those two numbers are bundled (`assets/mushaf_layout.txt`) and the credits screen credits the source.
- **Fonts:** Amiri / Amiri Quran, Rakkas and Inter are SIL Open Font License 1.1 (`assets/licenses/*-OFL.txt`).
- **Paper texture** is reused from the original APK. Check that you have the rights to it before publishing.
- **Launcher icon & loading-screen artwork:** `app/src/main/res/drawable-nodpi/hafiz.png`.

### Google Play Games and privacy
The leaderboards (Google Play Games Services) are dormant until `res/values/games-ids.xml` is filled in; debug builds never use them. Once enabled, the player's scores go to Google through Play Games, and the Play SDK is part of every release build. Publish a privacy policy and fill in Play Console's data-safety form before releasing with leaderboards enabled.
