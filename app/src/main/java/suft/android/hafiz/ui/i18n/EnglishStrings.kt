package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object EnglishStrings : Strings {
    override val language = AppLanguage.English

    override fun percent(value: Int) = "$value%"

    override val appName = "Hafiz"
    override val back = "Back"
    override val loadingQuran = "Loading the Quran…"
    override val tagline = "Test your memorization of the Holy Quran"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Guess the Surah"
        GameStyle.NextAyah -> "Next Ayah"
        GameStyle.AyahPosition -> "Where's the Ayah?"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Read the ayah and pick its surah"
        GameStyle.NextAyah -> "Pick the ayah that comes next"
        GameStyle.AyahPosition -> "Read the ayah and pick where it starts in the Madinah mushaf"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Regular"
        GameMode.Survival -> "Survival"
        GameMode.Mutashabihat -> "Mutashabihat"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Fixed-length rounds; master every level"
        GameMode.Survival -> "Endless questions; one mistake ends the game"
        GameMode.Mutashabihat -> "Questions from the whole Quran with similar-ayah traps (mutashabihat); three mistakes end the game"
    }

    override val chooseStyle = "Choose a question style"
    override val best = "best"
    override fun questionNumber(n: Int) = "Question $n"
    override fun livesLeft(n: Int) = "Lives left: $n"
    override val gameOver = "Game over"
    override fun survivalResult(score: Int) = "Correct in a row: $score"
    override fun livesResult(score: Int, answered: Int) = "$score correct out of ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "Longest streak: $n"
    override fun bestScore(n: Int) = "Best score: $n"
    override fun gamesPlayed(n: Int) = "Games played: $n"
    override fun averageScore(n: Int) = "Average score: $n"
    override val noGamesYet = "No games yet"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "I scored $score in $modeTitle mode on the level “$levelName” in the Quran quiz “$appName – $styleTitle”"

    override val mastered = "mastered"
    override val statistics = "Statistics"
    override val statisticsHint = "See which surahs need more review"
    override val play = "Play"
    override val playHint = "Pick a game mode, then a question style and a level"
    override val gameModes = "Game modes"
    override val howToPlay = "How to play"
    override val howToPlayHint = "Learn the question styles, game modes, levels and settings"
    override val guideStylesHeading = "Question styles"
    override val guideLevelsHeading = "Levels and mastery"
    override val guideLevels =
        "Each question style has levels covering different parts of the Quran, from the short surahs to the whole Quran. " +
            "Regular keeps your best round on each level and Survival your best game on each level. Mutashabihat has no levels: its questions come from the whole Quran."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "$styleTitle: a Regular round has ${questionsCount(questions)}; a score of ${number(mastery)} or more masters the level."
    override val guideStatsHeading = "Statistics and review"
    override val guideStats = "Statistics shows your accuracy and the surahs that need review; start a review round for a single surah from there."
    override val guideSettings
        get() = "The gear at the top of the main menu changes the app language and the mushaf used by " +
            "${styleTitle(GameStyle.AyahPosition)}: the ${mushafEditionName(MushafEdition.V1)}, the default, or the " +
            "${mushafEditionName(MushafEdition.V2)}. Some ayahs start in a different place in each print; " +
            "scores are shared by both."
    override fun statisticsSummary(percent: Int, total: Int) =
        "Accuracy ${percent(percent)} across ${questionsCount(total)}"

    override val languageLabel = "Language"
    override val settings = "Settings"
    override val mushafEditionLabel get() = "Mushaf for “${styleTitle(GameStyle.AyahPosition)}”"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "${number(edition.hijriYear)}H print (v1)"
        MushafEdition.V2 -> "${number(edition.hijriYear)}H print (v2)"
    }
    override val credits = "Credits"
    override val creditsQuranText = "Quran text"
    override val creditsRecitations = "Recitations"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsMushafLayout = "Ayah positions in the mushaf"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override val creditsDeveloper = "Developer"
    override val developerName = "Sufien Tout"
    override val creditsInspiredBy = "Inspired by"
    override val privacyPolicy = "Privacy policy"
    override val creditsSourcesHeading = "Sources"
    override val creditsPeopleHeading = "People"
    override val creditsLegalHeading = "Legal"
    override val creditsTanzilLicence = "CC BY 3.0, used unmodified"
    override val creditsMushafEditions get() = "${number(MushafEdition.V1.hijriYear)}H and ${number(MushafEdition.V2.hijriYear)}H prints"
    override val creditsIcons = "Icons"
    override val creditsIconsLicence = "MIT licence"
    override val opensInBrowser = "Opens in your browser"
    override fun versionLabel(version: String) = "Version $version"
    override fun totalPoints(points: Int) = "Total points: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered of $total mastered"
    override val start = "Start"
    override fun questionProgress(current: Int, total: Int) = "Question $current of $total"
    override val next = "Next"
    override fun points(score: Int) = "Points: $score"
    override val whichSurah = "Which surah is this ayah from?"
    override val whatIsNextAyah = "What is the next ayah?"
    override val whereIsAyah = "Where does this ayah start in the mushaf?"
    override fun optionMarker(index: Int) = listOf("A", "B", "C", "D")[index]
    override val pause = "Pause"
    override val listen = "Listen"
    override val chooseReciter = "Choose reciter"
    override val autoPlay = "Auto"
    override val correctAnswer = "Correct answer"
    override val wrongAnswer = "Wrong answer"
    override val resultTitle = "Result"
    override val perfectScore = "Masha'Allah! A perfect score"
    override val levelMastered = "Well done! You have mastered this level"
    override val keepReviewing = "Good, keep reviewing to master it"
    override val needsReview = "This level needs more review"
    override val surahReviewMastered = "Well done! You know this surah well"
    override val surahNeedsReview = "This surah needs more review"
    override fun newRecord(previous: Int) = "New record! (previous: $previous)"
    override val shareResult = "Share result"
    override val retry = "Retry"
    override val mainMenu = "Main menu"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "I scored $score out of $total on the level “$levelName” in the Quran quiz “$appName – $styleTitle”"

    override val playbackFailed = "Couldn't load the recitation. Check your internet connection."
    override val playbackFailedAutoPlayOff =
        "Couldn't load the recitation, so auto-play was turned off. Check your internet connection."
    override val clearStatistics = "Clear statistics"
    override val clearStatisticsQuestion = "Clear statistics?"
    override val clearStatisticsBody =
        "This deletes the answer history for every surah, the accuracy per question style, day streaks and correct-answer runs, " +
            "the round history, and the games played, averages and longest streaks of Survival and Mutashabihat. " +
            "Your best score on each level is kept."
    override val clear = "Clear"
    override val cancel = "Cancel"
    override val statisticsEmpty = "Answer some questions to see the surahs that need review here."
    override val noMistakes = "Masha'Allah! No mistakes so far."
    override val surahsToReview = "Surahs that need review"
    override val accuracy = "Accuracy"
    override fun correctAnswers(n: Int) = "Correct answers: $n"
    override fun wrongAnswers(n: Int) = "Wrong answers: $n"
    override fun rankedSurah(rank: Int, name: String) = "$rank. Surah $name"
    override fun surahCounts(correct: Int, wrong: Int) = "$correct correct · $wrong wrong"
    override val tabOverview = "Overview"
    override val tabSurahs = "Surahs"
    override val tabActivity = "Activity"
    override val noAnswersYet = "No answers yet"
    override val styleAccuracySinceUpdate = "Per-style accuracy counts answers given since this update; older answers are only in the overall accuracy."
    override val surahMap = "Surah map"
    override fun surahsPractised(n: Int, total: Int) = "$n of $total surahs practised"
    override val tierNotPractised = "Not practised"
    override val tierWeak = "Needs review"
    override val tierFair = "Good"
    override val tierStrong = "Strong"
    override val strongestSurahs = "Strongest surahs"
    override val practice = "Practise"
    override val notPractisedYet = "Not practised yet"
    override val currentStreak = "Current day streak"
    override val bestStreak = "Best day streak"
    override val questionsToday = "Questions today"
    override val roundsCompleted = "Rounds completed"
    override val currentRun = "Correct in a row"
    override val bestRun = "Best correct run"
    override val scoreTrend = "Score trend"
    override val recentRounds = "Finished rounds"
    override val noRoundsYet = "Finish a round to see your history here."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> "$days days ago"
    }
    override fun questionsCount(n: Int) = if (n == 1) "1 question" else "$n questions"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Surah $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Score trend: from ${percent(first)} in the oldest round to ${percent(last)} in the latest"
    override val notMastered = "not mastered"
    override val levelsList = "Levels"
    override val revelationTitle = "Makki or Madani"
    override val revelationSubtitle = "Five surahs: say whether each was revealed in Makkah or Madinah"
    override val revelationQuestion = "Where was this surah revealed?"
    override fun revelationSurah(name: String) = "Surah $name"
    override val makki = "Makki"
    override val madani = "Madani"
    override val revelationGood = "Well done! Keep going for a perfect score"
    override val revelationNeedsReview = "Review where the surahs were revealed and try again"
}
