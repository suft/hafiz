# AGENTS.md

Hafiz: a Quran memorisation quiz for Android, written in Kotlin with Jetpack Compose. The toolchain is mise; Android Studio is not needed. `README.md` is the user-facing reference (modes, styles, tasks, licences); this file covers how to work in the repo.

## Commands

Run every command from the repo root.

| Command | Use |
|---|---|
| `mise install` then `mise run setup` | First run: Temurin JDK 21 and the Android SDK in `.android-sdk/` |
| `mise run check` | **Required before calling any change done**: JVM + Robolectric tests and Android lint (CI's test and lint steps) |
| `mise run test` | Tests only (`./gradlew :app:testDebugUnitTest`) |
| `./gradlew :app:testDebugUnitTest --tests 'suft.android.hafiz.domain.QuizLogicTest'` | One test class |
| `mise run build` | Debug APK → `dist/Hafiz-debug.apk` |
| `mise run mutashabihat` / `mise run mushaf-layout` | Regenerate the generated assets (python3, network) |
| `mise run screenshots` | Regenerate the 8 Play/README screenshots on a headless emulator (KVM) |

CI (`.github/workflows/build.yml`) runs test, lint, the debug build and the release bundle; `release.yml` publishes a signed GitHub Release for each `v*` tag.

## Layout

- `app/src/main/java/suft/android/hafiz/domain/`: pure Kotlin with no Android imports. Quran index, levels, game modes, `QuestionGenerator`, `ThemeIndex`, `Mutashabihat`, `MushafLayout`, stats. Keep it Android-free so it stays unit-testable.
- `data/ProgressRepository.kt`: DataStore (scores, stats, settings). `audio/`: reciters, ExoPlayer, disk cache. `ui/`: Compose screens and ViewModels.
- `app/src/test/`: JVM tests on the real bundled Quran text, plus Robolectric UI tests. Load assets through `testing/TestData.kt` (`TestData.quran`, `.mutashabihat`, `.mushafLayout`).
- `tools/`: python generators and checkers behind the mise tasks.

## Code style

- Follow `.editorconfig`: UTF-8 and LF; 4-space indent; Kotlin max line 120 with trailing commas allowed; 2-space indent for TOML, XML and YAML.
- Dependency versions live only in `gradle/libs.versions.toml`.
- UI text: every non-Quran string goes through the `Strings` interface in `ui/i18n/Strings.kt`. Add each new member to both `ArabicStrings` and `EnglishStrings`, and format numbers with `number()` (Arabic-Indic digits in Arabic). Quran text always stays Arabic.

## Data and licences

- `app/src/main/assets/quran_simple.xml` is the Tanzil text: never modify it, and keep its licence header.
- `assets/mutashabihat.txt` and `assets/mushaf_layout.txt` are generated. Never hand-edit them; change `tools/` (hand-curated pairs go in `tools/mutashabihat_supplement.txt`) and rerun the mise task.
- Never commit signing material or local config: `keystore.properties`, `*.jks`, `local.properties`, `.env*` (see `.gitignore`).

## Keep docs in sync

Update these in the same change as the code. A change is not done while one of them describes old behaviour.

- **`ALGORITHMS.md`**: when behaviour changes in how prompts are chosen or answer options are built. That covers:
  - `domain/QuestionGenerator.kt` (prompt pool, deck/recent window, traps, wrong-option picking, constants);
  - `domain/ThemeIndex.kt` (relatedness);
  - `domain/Mutashabihat.kt` (look-alike data);
  - `domain/MushafLayout.kt` (cells);
  - `domain/Levels.kt` (surah ranges, openings, practice levels);
  - `domain/GameMode.kt` (`trapOptions`, `wholeQuranOnly`, `offers`);
  - `GameStyle` (styles, `hasLookAlikes`).

  Edit the affected mermaid diagram(s) and the Summary table; add a section for a new style or mode. Check every number in the doc (weights, window sizes, trap counts) against the code constants.
- **`README.md`**: when modes, styles, level counts, questions per round, mise tasks, project layout, the test class list, assets, versions or licences change.
- **`docs/privacy-policy.md`** and the Data safety section of `fastlane/play-console.md`: when data leaving the device changes (new network endpoint, new SDK, backup rules).
- **`fastlane/metadata/android/{ar,en-US}/`**: when modes, styles, level counts or the reciter count change; add `changelogs/<versionCode>.txt` (both locales) for every release.
- **How-to-play text** in `ui/i18n/Strings.kt` (both `ArabicStrings` and `EnglishStrings`: the `guide*` members and the style/mode titles and subtitles): when the rules a player sees change (modes, levels, mastery, lives).
- **Screenshots** (`fastlane/metadata/android/*/images/phoneScreenshots/`, shown in `README.md`): rerun `mise run screenshots` when the main menu, the quiz screen or Statistics → Surahs visibly change, or when a label in `tools/screenshots.py` changes in `Strings.kt`.

## Commits

Use Conventional Commits with an imperative description that starts lowercase, `type(scope): description`, and imperative `- ` bullets in the body. Mark breaking changes with `!` and a `BREAKING CHANGE:` footer. Examples from history:
`feat(modes): restrict Mutashabihat to the whole Quran without levels`, `docs: add answer-option algorithm documentation`. Types in use: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`, `ci`, `test`.
