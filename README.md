# AyahIQ 3.0

A Quran memorisation quiz for Android, rewritten from the original *AyahSurah 2.0* APK
(Java + Android Views, 2013) in **Kotlin + Jetpack Compose**, with a **mise** toolchain.

| Mode | What you do | Levels | Questions / round |
|---|---|---|---|
| **آية وسورة** (guess the surah) | Read/hear an ayah and pick the surah it's from | 20 | 20 |
| **آيات** (next ayah) | Read/hear an ayah and pick the one that follows it | 25 | 10 |
| **إحصائيات** (stats) | Overall accuracy and the surahs you most often get wrong | — | — |

Recitations stream from [everyayah.com](https://everyayah.com) (27 reciters). Quran text: [Tanzil](https://tanzil.net) (Simple, v1.0.2).

---

## Build it

You only need [mise](https://mise.jdx.dev/getting-started.html) and `curl`/`unzip`. mise installs the JDK and the tasks download the Android SDK, so Android Studio isn't required.

```bash
cd AyahSurah
mise trust          # allow this project's mise.toml
mise install        # Temurin JDK 21
mise run setup      # Android cmdline-tools + platform 37 + build-tools (first run only)
mise run build      # -> dist/AyahIQ-debug.apk
```

Install on a phone with USB debugging enabled: `mise run run` (installs and launches it), or copy `dist/AyahIQ-debug.apk` to the phone.

### All tasks (`mise tasks`)

| Task | What it does |
|---|---|
| `setup` / `sdk` | Download the Android SDK into `.android-sdk/` (or use `$ANDROID_HOME` if it's already set) and write `local.properties` |
| `build` (`b`) | Debug APK → `dist/AyahIQ-debug.apk` |
| `release` | Minified release APK → `dist/AyahIQ-release.apk` |
| `bundle` | Release `.aab` for Google Play |
| `keystore` | Create `release.jks` + `keystore.properties` for release signing |
| `test` (`t`) | JVM unit tests for the quiz logic |
| `lint` / `check` | Android lint / tests + lint |
| `install`, `run`, `logcat` | Install on a device via adb, launch it, follow its logs |
| `clean` | Delete build outputs |

**Release signing.** If `keystore.properties` doesn't exist, the release APK is signed with the debug key, so it still installs. For a real release run `mise run keystore` once, then back up `release.jks` and its password. Both files are git-ignored.

**Upgrading from the old app.** The package name changed from `zozo.android.ayahsurra` to `suft.android.ayahiq`, so this installs as a separate app and does not update the old one. Scores and settings are not carried over. The debug build uses `suft.android.ayahiq.debug`.

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
- Recitations are **cached on disk** (200 MB LRU), and the next question's audio is prefetched, so replays work offline.
- Your reciter and auto-play choices are remembered. Six new reciters: القطامي، الرفاعي، الحذيفي، فارس عباد، محمد أيوب، البدير.
- Ayah text is set in the **Amiri Quran** font. The next-ayah mode shows ornate ayah numbers ﴿٥﴾. Long answer options are shortened to a distinct opening phrase.
- New level list with progress bars and a "mastered" mark, a result dialog that shows new records, a "next" button to skip the feedback delay, and resettable statistics.
- **Statistics screen with three tabs.** Overview shows overall accuracy plus per-mode accuracy, mastered levels and points. Surahs shows a 114-cell accuracy map, the surahs to review and the strongest surahs, with a **Practise** button that starts a 10-question next-ayah round limited to one surah (records answers and history, never a best score). Activity shows day streaks, questions today, completed rounds, correct-answer runs, a score trend chart and the last 20 rounds. Per-mode counts and activity are only tracked from the version that added them.
- The share card renders the ayah on one of the five original Islamic frames, auto-fits the text, and offers a font choice. It is shared as a PNG through a `FileProvider`.
- Edge-to-edge layout, dark theme, adaptive and themed launcher icon, and backup rules for scores.
- **Arabic / English UI.** A selector on the main menu switches labels, digits, surah and level names, reciter names, ayah references and the layout direction (RTL / LTR). The choice is saved. Quran text always stays Arabic. Strings live in `ui/i18n/Strings.kt`; English surah names are quran.com transliterations in `domain/SurahNames.kt`.

---

## Project layout

```
mise.toml                        tools (JDK), env (ANDROID_HOME), tasks
gradle/libs.versions.toml        all dependency versions
app/src/main/
  assets/quran_simple.xml        Tanzil Quran text (verbatim, keep the licence header)
  java/suft/android/ayahiq/
    domain/                      pure Kotlin: Quran index, XML parser, levels, question generator, stats
    data/ProgressRepository.kt   DataStore: best scores, per-surah stats, settings
    audio/                       reciters, ExoPlayer wrapper, disk cache
    ui/                          Compose: home, levels, quiz (+ViewModel), stats, share card, theme
  res/font/                      Amiri (OFL), Cordoba (from the original app)
  res/drawable-nodpi/            paper texture, frames, logo (from the original app)
app/src/test/                    QuizLogicTest — runs against the real Quran text
```

### Versions
AGP 9.4.1 · Gradle 9.8.2 · Kotlin 2.4.21 · Compose BOM 2026.09.00 · Media3 1.11.1 · compileSdk 37 · targetSdk 35 · minSdk 24.

All versions are in `gradle/libs.versions.toml`. Android Studio's *Upgrade Assistant* can bump them.

---

## Licences & credits
- **Quran text:** Tanzil Project, CC BY 3.0. The text must not be modified and the source must be credited with a link; the home screen does this.
- **Recitations:** streamed from everyayah.com.
- **Amiri / Amiri Quran fonts:** SIL Open Font License 1.1 (`assets/licenses/Amiri-OFL.txt`).
- **Cordoba font, paper texture, frames and logo** are reused from the original APK. Check that you have the rights to them before publishing.
