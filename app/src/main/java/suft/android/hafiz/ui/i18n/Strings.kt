package suft.android.hafiz.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

/** Every piece of UI text that is not Quran text. Numbers always go through [number]. */
interface Strings {
    val language: AppLanguage

    fun number(value: Int): String = language.number(value)

    fun percent(value: Int): String

    val appName: String
    val back: String
    val loadingQuran: String
    val loadFailed: String
    val tagline: String
    fun styleTitle(style: GameStyle): String
    fun styleSubtitle(style: GameStyle): String
    fun gameModeTitle(mode: GameMode): String
    fun gameModeSubtitle(mode: GameMode): String
    val chooseStyle: String
    val best: String
    fun questionNumber(n: Int): String
    fun livesLeft(n: Int): String
    val gameOver: String
    fun survivalResult(score: Int): String
    fun livesResult(score: Int, answered: Int): String
    fun longestStreak(n: Int): String
    fun bestScore(n: Int): String
    fun gamesPlayed(n: Int): String
    fun averageScore(n: Int): String
    val noGamesYet: String
    fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String): String
    val mastered: String
    val play: String
    val playHint: String
    val gameModes: String
    val howToPlay: String
    val howToPlayHint: String
    val guideStylesHeading: String
    val guideLevelsHeading: String
    val guideLevels: String
    fun guideMastery(styleTitle: String, questions: Int, mastery: Int): String
    val guideStatsHeading: String
    val guideStats: String
    val guideSettings: String
    val guideRevelationSource: String
    val statistics: String
    val statisticsHint: String
    fun statisticsSummary(percent: Int, total: Int): String
    val languageLabel: String
    val settings: String
    val mushafEditionLabel: String
    fun mushafEditionName(edition: MushafEdition): String
    val credits: String
    val creditsQuranText: String
    val creditsRecitations: String
    val creditsMutashabihat: String
    val creditsMushafLayout: String
    val creditsTanzil: String
    val creditsAswaatulQurraa: String
    val creditsDeveloper: String
    val developerName: String
    val creditsInspiredBy: String
    val privacyPolicy: String
    val creditsSourcesHeading: String
    val creditsPeopleHeading: String
    val creditsLegalHeading: String
    val creditsTanzilLicence: String
    val creditsMushafEditions: String
    val creditsIcons: String
    val creditsIconsLicence: String
    val opensInBrowser: String
    fun versionLabel(version: String): String
    fun totalPoints(points: Int): String
    fun masteredCount(mastered: Int, total: Int): String
    val start: String
    fun questionProgress(current: Int, total: Int): String
    val next: String
    fun points(score: Int): String
    val whichSurah: String
    val whatIsNextAyah: String
    val whereIsAyah: String
    /** Letter shown on an answer tile: أ ب ج د in Arabic, A B C D in the Latin-script languages. */
    fun optionMarker(index: Int): String
    val pause: String
    val listen: String
    val chooseReciter: String
    val autoPlay: String
    val correctAnswer: String
    val wrongAnswer: String
    val resultTitle: String
    val perfectScore: String
    val levelMastered: String
    val keepReviewing: String
    val needsReview: String
    val surahReviewMastered: String
    val surahNeedsReview: String
    fun newRecord(previous: Int): String
    val shareResult: String
    val retry: String
    val mainMenu: String
    fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String): String
    val playbackFailed: String
    val playbackFailedAutoPlayOff: String
    val clearStatistics: String
    val clearStatisticsQuestion: String
    val clearStatisticsBody: String
    val clear: String
    val cancel: String
    val statisticsEmpty: String
    val noMistakes: String
    val surahsToReview: String
    val accuracy: String
    fun correctAnswers(n: Int): String
    fun wrongAnswers(n: Int): String
    fun rankedSurah(rank: Int, name: String): String
    fun surahCounts(correct: Int, wrong: Int): String
    val tabOverview: String
    val tabSurahs: String
    val tabActivity: String
    val noAnswersYet: String
    val styleAccuracySinceUpdate: String
    val surahMap: String
    fun surahsPractised(n: Int, total: Int): String
    val tierNotPractised: String
    val tierWeak: String
    val tierFair: String
    val tierStrong: String
    val strongestSurahs: String
    val practice: String
    val notPractisedYet: String
    val currentStreak: String
    val bestStreak: String
    val questionsToday: String
    val roundsCompleted: String
    val currentRun: String
    val bestRun: String
    val scoreTrend: String
    val recentRounds: String
    val noRoundsYet: String
    fun daysAgo(days: Int): String
    fun questionsCount(n: Int): String
    fun surahCell(name: String, tier: String, percent: Int?): String
    fun scoreTrendDescription(first: Int, last: Int): String
    val notMastered: String
    val levelsList: String
    val revelationTitle: String
    val revelationSubtitle: String
    val revelationQuestion: String
    fun revelationSurah(name: String): String
    val makki: String
    val madani: String
    val revelationGood: String
    val revelationNeedsReview: String
}

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.Arabic -> ArabicStrings
    AppLanguage.English -> EnglishStrings
    AppLanguage.German -> GermanStrings
    AppLanguage.Spanish -> SpanishStrings
    AppLanguage.French -> FrenchStrings
    AppLanguage.Italian -> ItalianStrings
    AppLanguage.Portuguese -> PortugueseStrings
}

val LocalStrings = staticCompositionLocalOf<Strings> { ArabicStrings }
