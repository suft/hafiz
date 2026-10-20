package suft.android.ayahiq.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle

/** Every piece of UI text that is not Quran text. Numbers always go through [number]. */
interface Strings {
    val language: AppLanguage

    fun number(value: Int): String = language.number(value)

    fun percent(value: Int): String

    val appName: String
    val back: String
    val loadingQuran: String
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
    val statistics: String
    val statisticsHint: String
    fun statisticsSummary(percent: Int, total: Int): String
    val leaderboards: String
    val leaderboardsHint: String
    val leaderboardsUnavailable: String
    val languageLabel: String
    val credits: String
    val creditsQuranText: String
    val creditsRecitations: String
    val creditsMutashabihat: String
    val creditsTanzil: String
    val creditsAswaatulQurraa: String
    fun totalPoints(points: Int): String
    fun masteredCount(mastered: Int, total: Int): String
    val start: String
    val shareAyah: String
    fun questionProgress(current: Int, total: Int): String
    val next: String
    fun points(score: Int): String
    val whichSurah: String
    val whatIsNextAyah: String
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
    val frame: String
    fun frameNumber(n: Int): String
    val font: String
    val fontMushaf: String
    val fontScheherazade: String
    val shareImage: String
    val shareFailed: String
    fun questionsCount(n: Int): String
    fun surahCell(name: String, tier: String, percent: Int?): String
    fun scoreTrendDescription(first: Int, last: Int): String
    val notMastered: String
    val levelsList: String
}

object ArabicStrings : Strings {
    override val language = AppLanguage.Arabic

    override fun percent(value: Int) = "${number(value)}٪"

    override val appName = "آية آي كيو"
    override val back = "رجوع"
    override val loadingQuran = "جارٍ تحميل المصحف…"
    override val tagline = "مسابقة لاختبار حفظك للقرآن الكريم"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "آية وسورة"
        GameStyle.NextAyah -> "آيات"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "اقرأ الآية واختر السورة التي وردت فيها"
        GameStyle.NextAyah -> "أكمل التلاوة واختر الآية التالية"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "عادي"
        GameMode.Survival -> "البقاء"
        GameMode.ThreeLives -> "ثلاث فرص"
        GameMode.Hafiz -> "حافظ"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "جولات محددة الأسئلة، أتقن كل مرحلة"
        GameMode.Survival -> "أسئلة بلا نهاية، وأول خطأ ينهي اللعبة"
        GameMode.ThreeLives -> "أسئلة بلا نهاية، وثلاثة أخطاء تنهي اللعبة"
        GameMode.Hafiz -> "خيارات من الآيات المتشابهة، وثلاثة أخطاء تنهي اللعبة"
    }

    override val chooseStyle = "اختر نوع الأسئلة"
    override val best = "الأفضل"
    override fun questionNumber(n: Int) = "السؤال ${number(n)}"
    override fun livesLeft(n: Int) = "الفرص المتبقية: ${number(n)}"
    override val gameOver = "انتهت اللعبة"
    override fun survivalResult(score: Int) = "إجابات صحيحة متتالية: ${number(score)}"
    override fun livesResult(score: Int, answered: Int) = "${number(score)} صحيحة من ${questionsCount(answered)}"
    override fun longestStreak(n: Int) = "أطول سلسلة صحيحة: ${number(n)}"
    override fun bestScore(n: Int) = "أفضل نتيجة: ${number(n)}"
    override fun gamesPlayed(n: Int) = "ألعاب مكتملة: ${number(n)}"
    override fun averageScore(n: Int) = "متوسط النتيجة: ${number(n)}"
    override val noGamesYet = "لم تلعب بعد"
    override fun shareRunText(score: Int, levelName: String, modeTitle: String, styleTitle: String) =
        "حصلت على ${number(score)} في وضع «$modeTitle» بمرحلة «$levelName» في مسابقة القرآن الكريم «$appName – $styleTitle»"

    override val mastered = "مُتقَنة"
    override val statistics = "إحصائيات"
    override val statisticsHint = "تعرّف على السور التي تحتاج إلى مراجعة"
    override fun statisticsSummary(percent: Int, total: Int) =
        "دقة إجاباتك ${percent(percent)} في ${questionsCount(total)}"
    override val leaderboards = "لوحات الصدارة"
    override val leaderboardsHint = "قارن نتائجك مع اللاعبين على Google Play Games"
    override val leaderboardsUnavailable = "تعذّر فتح لوحات الصدارة. سجّل الدخول إلى Google Play Games وحاول مرة أخرى."

    override val languageLabel = "اللغة"
    override val credits = "شكر وتقدير"
    override val creditsQuranText = "نص القرآن الكريم"
    override val creditsRecitations = "التلاوات"
    override val creditsMutashabihat = "المتشابهات"
    override val creditsTanzil = "مشروع تنزيل"
    override val creditsAswaatulQurraa = "أصوات القرّاء"
    override fun totalPoints(points: Int) = "مجموع النقاط: ${number(points)}"
    override fun masteredCount(mastered: Int, total: Int) = "${number(mastered)} من ${number(total)} مُتقَنة"
    override val start = "ابدأ"
    override val shareAyah = "انشر الآية"
    override fun questionProgress(current: Int, total: Int) = "السؤال ${number(current)} من ${number(total)}"
    override val next = "التالي"
    override fun points(score: Int) = "النقاط ${number(score)}"
    override val whichSurah = "من أي سورة هذه الآية؟"
    override val whatIsNextAyah = "ما الآية التالية؟"
    override val pause = "إيقاف مؤقت"
    override val listen = "استمع"
    override val chooseReciter = "اختر القارئ"
    override val autoPlay = "تلقائي"
    override val correctAnswer = "إجابة صحيحة"
    override val wrongAnswer = "إجابة خاطئة"
    override val resultTitle = "النتيجة"
    override val perfectScore = "ما شاء الله! علامة كاملة"
    override val levelMastered = "أحسنت! لقد أتقنت هذه المرحلة"
    override val keepReviewing = "جيد، واصل المراجعة لتتقنها"
    override val needsReview = "هذه المرحلة تحتاج إلى مزيد من المراجعة"
    override val surahReviewMastered = "أحسنت! حفظك لهذه السورة متقن"
    override val surahNeedsReview = "هذه السورة تحتاج إلى مزيد من المراجعة"
    override fun newRecord(previous: Int) = "رقم قياسي جديد! (السابق ${number(previous)})"
    override val shareResult = "انشر النتيجة"
    override val retry = "إعادة"
    override val mainMenu = "القائمة الرئيسية"
    override fun shareScoreText(score: Int, total: Int, levelName: String, styleTitle: String) =
        "حصلت على ${number(score)} من ${number(total)} في مرحلة «$levelName» في مسابقة القرآن الكريم «$appName – $styleTitle»"

    override val playbackFailed = "تعذّر تحميل التلاوة. تأكد من الاتصال بالإنترنت."
    override val playbackFailedAutoPlayOff = "تعذّر تحميل التلاوة، فأُوقف التشغيل التلقائي. تأكد من الاتصال بالإنترنت."
    override val clearStatistics = "مسح الإحصائيات"
    override val clearStatisticsQuestion = "مسح الإحصائيات؟"
    override val clearStatisticsBody =
        "سيتم حذف سجل الإجابات لكل السور، ودقة كل نوع من الأسئلة، وسلاسل الأيام والإجابات الصحيحة، وسجل الجولات، " +
            "وعدد ألعاب البقاء والفرص الثلاث والحافظ ومتوسطاتها وأطول سلاسلها. أفضل نتيجة في كل مرحلة لن تتأثر."
    override val clear = "مسح"
    override val cancel = "إلغاء"
    override val statisticsEmpty = "أجب عن بعض الأسئلة لتظهر هنا السور التي تحتاج إلى مراجعة."
    override val noMistakes = "ما شاء الله! لا أخطاء حتى الآن."
    override val surahsToReview = "السور التي تحتاج إلى مراجعة"
    override val accuracy = "دقة الإجابات"
    override fun correctAnswers(n: Int) = "إجابات صحيحة: ${number(n)}"
    override fun wrongAnswers(n: Int) = "إجابات خاطئة: ${number(n)}"
    override fun rankedSurah(rank: Int, name: String) = "${number(rank)}. سورة $name"
    override fun surahCounts(correct: Int, wrong: Int) = "${number(correct)} صحيحة · ${number(wrong)} خاطئة"
    override val tabOverview = "نظرة عامة"
    override val tabSurahs = "السور"
    override val tabActivity = "النشاط"
    override val noAnswersYet = "لا إجابات بعد"
    override val styleAccuracySinceUpdate = "دقة كل نوع تشمل الإجابات منذ هذا التحديث فقط، والإجابات الأقدم محسوبة في الدقة الكلية."
    override val surahMap = "خريطة السور"
    override fun surahsPractised(n: Int, total: Int) = "تدرّبت على ${number(n)} من ${number(total)} سورة"
    override val tierNotPractised = "لم تُختبر"
    override val tierWeak = "تحتاج مراجعة"
    override val tierFair = "جيد"
    override val tierStrong = "متقنة"
    override val strongestSurahs = "السور الأقوى"
    override val practice = "تدرّب"
    override val notPractisedYet = "لم تتدرّب على هذه السورة بعد"
    override val currentStreak = "أيام متتالية حالياً"
    override val bestStreak = "أطول سلسلة أيام"
    override val questionsToday = "أسئلة اليوم"
    override val roundsCompleted = "جولات مكتملة"
    override val currentRun = "إجابات صحيحة متتالية"
    override val bestRun = "أطول سلسلة إجابات صحيحة"
    override val scoreTrend = "تطور النتائج"
    override val recentRounds = "آخر الجولات المكتملة"
    override val noRoundsYet = "أكمل جولة لتظهر نتائجك هنا."
    override fun daysAgo(days: Int) = when (days) {
        0 -> "اليوم"
        1 -> "أمس"
        2 -> "منذ يومين"
        in 3..10 -> "منذ ${number(days)} أيام"
        else -> "منذ ${number(days)} يوماً"
    }
    override val frame = "الإطار"
    override fun frameNumber(n: Int) = "إطار ${number(n)}"
    override val font = "الخط"
    override val fontMushaf = "مصحف"
    override val fontScheherazade = "شهرزاد"
    override val shareImage = "انشر الصورة"
    override val shareFailed = "تعذّر إنشاء صورة المشاركة."
    override fun questionsCount(n: Int) = when {
        n == 1 -> "سؤال واحد"
        n == 2 -> "سؤالين"
        n % 100 in 3..10 -> "${number(n)} أسئلة"
        n % 100 in 11..99 -> "${number(n)} سؤالاً"
        else -> "${number(n)} سؤال"
    }
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "سورة $name، $tier" + (percent?.let { "، ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "تطور النتائج: من ${percent(first)} في أقدم جولة إلى ${percent(last)} في أحدثها"
    override val notMastered = "غير متقنة"
    override val levelsList = "المراحل"
}

object EnglishStrings : Strings {
    override val language = AppLanguage.English

    override fun percent(value: Int) = "$value%"

    override val appName = "AyahIQ"
    override val back = "Back"
    override val loadingQuran = "Loading the Quran…"
    override val tagline = "Test your memorization of the Holy Quran"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Guess the Surah"
        GameStyle.NextAyah -> "Next Ayah"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "Read the ayah and pick its surah"
        GameStyle.NextAyah -> "Pick the ayah that comes next"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Regular"
        GameMode.Survival -> "Survival"
        GameMode.ThreeLives -> "3 Lives"
        GameMode.Hafiz -> "Hafiz"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "Fixed-length rounds; master every level"
        GameMode.Survival -> "Endless questions; one mistake ends the game"
        GameMode.ThreeLives -> "Endless questions; three mistakes end the game"
        GameMode.Hafiz -> "Similar-ayah traps (mutashabihat); three mistakes end the game"
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
        "I scored $score in $modeTitle mode on the level “$levelName” in the Quran quiz “AyahIQ – $styleTitle”"

    override val mastered = "mastered"
    override val statistics = "Statistics"
    override val statisticsHint = "See which surahs need more review"
    override fun statisticsSummary(percent: Int, total: Int) =
        "Accuracy ${percent(percent)} across ${questionsCount(total)}"
    override val leaderboards = "Leaderboards"
    override val leaderboardsHint = "Compare your scores with other players on Google Play Games"
    override val leaderboardsUnavailable = "Couldn't open the leaderboards. Sign in to Google Play Games and try again."

    override val languageLabel = "Language"
    override val credits = "Credits"
    override val creditsQuranText = "Quran text"
    override val creditsRecitations = "Recitations"
    override val creditsMutashabihat = "Mutashabihat"
    override val creditsTanzil = "Tanzil Project"
    override val creditsAswaatulQurraa = "Aswaatul Qurraa"
    override fun totalPoints(points: Int) = "Total points: $points"
    override fun masteredCount(mastered: Int, total: Int) = "$mastered of $total mastered"
    override val start = "Start"
    override val shareAyah = "Share ayah"
    override fun questionProgress(current: Int, total: Int) = "Question $current of $total"
    override val next = "Next"
    override fun points(score: Int) = "Points: $score"
    override val whichSurah = "Which surah is this ayah from?"
    override val whatIsNextAyah = "What is the next ayah?"
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
        "I scored $score out of $total on the level “$levelName” in the Quran quiz “AyahIQ – $styleTitle”"

    override val playbackFailed = "Couldn't load the recitation. Check your internet connection."
    override val playbackFailedAutoPlayOff =
        "Couldn't load the recitation, so auto-play was turned off. Check your internet connection."
    override val clearStatistics = "Clear statistics"
    override val clearStatisticsQuestion = "Clear statistics?"
    override val clearStatisticsBody =
        "This deletes the answer history for every surah, the accuracy per question style, day streaks and correct-answer runs, " +
            "the round history, and the games played, averages and longest streaks of Survival, 3 Lives and Hafiz. " +
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
    override val frame = "Frame"
    override fun frameNumber(n: Int) = "Frame $n"
    override val font = "Font"
    override val fontMushaf = "Mushaf"
    override val fontScheherazade = "Scheherazade"
    override val shareImage = "Share image"
    override val shareFailed = "Couldn't create the image to share."
    override fun questionsCount(n: Int) = if (n == 1) "1 question" else "$n questions"
    override fun surahCell(name: String, tier: String, percent: Int?) =
        "Surah $name, $tier" + (percent?.let { ", ${percent(it)}" } ?: "")
    override fun scoreTrendDescription(first: Int, last: Int) =
        "Score trend: from ${percent(first)} in the oldest round to ${percent(last)} in the latest"
    override val notMastered = "not mastered"
    override val levelsList = "Levels"
}

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.Arabic -> ArabicStrings
    AppLanguage.English -> EnglishStrings
}

val LocalStrings = staticCompositionLocalOf<Strings> { ArabicStrings }
