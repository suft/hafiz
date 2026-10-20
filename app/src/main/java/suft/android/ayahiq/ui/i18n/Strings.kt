package suft.android.ayahiq.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode

/** Every piece of UI text that is not Quran text. Numbers always go through [number]. */
interface Strings {
    val language: AppLanguage

    fun number(value: Int): String = language.number(value)

    fun percent(value: Int): String

    val appName: String
    val back: String
    val loadingQuran: String
    val homeTagline: String
    fun modeTitle(mode: GameMode): String
    fun modeSubtitle(mode: GameMode): String
    val mastered: String
    val statistics: String
    val statisticsHint: String
    fun statisticsSummary(percent: Int, total: Int): String
    val languageLabel: String
    val attributionQuranFrom: String
    val attributionTanzil: String
    val attributionRecitationsFrom: String
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
    val goBack: String
    val mainMenu: String
    fun shareScoreText(score: Int, total: Int, levelName: String, modeTitle: String): String
    val playbackFailed: String
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
    val modeAccuracySinceUpdate: String
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
    val fontCordoba: String
    val shareImage: String
}

object ArabicStrings : Strings {
    override val language = AppLanguage.Arabic

    override fun percent(value: Int) = "${number(value)}٪"

    override val appName = "AyahIQ"
    override val back = "رجوع"
    override val loadingQuran = "جارٍ تحميل المصحف…"
    override val homeTagline = "مسابقة لاختبار حفظك للقرآن الكريم"
    override fun modeTitle(mode: GameMode) = when (mode) {
        GameMode.GuessSurah -> "آية وسورة"
        GameMode.NextAyah -> "آيات"
    }

    override fun modeSubtitle(mode: GameMode) = when (mode) {
        GameMode.GuessSurah -> "اقرأ الآية واختر السورة التي وردت فيها"
        GameMode.NextAyah -> "أكمل التلاوة واختر الآية التالية"
    }

    override val mastered = "مُتقَنة"
    override val statistics = "إحصائيات"
    override val statisticsHint = "تعرّف على السور التي تحتاج إلى مراجعة"
    override fun statisticsSummary(percent: Int, total: Int) =
        "دقة إجاباتك ${number(percent)}٪ في ${number(total)} سؤالاً"

    override val languageLabel = "اللغة"
    override val attributionQuranFrom = "نص القرآن الكريم من "
    override val attributionTanzil = "مشروع تنزيل"
    override val attributionRecitationsFrom = " · التلاوات من "
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
    override val goBack = "عودة"
    override val mainMenu = "القائمة الرئيسية"
    override fun shareScoreText(score: Int, total: Int, levelName: String, modeTitle: String) =
        "حصلت على ${number(score)} من ${number(total)} في مرحلة «$levelName» في مسابقة القرآن الكريم «AyahIQ – $modeTitle»"

    override val playbackFailed = "تعذّر تحميل التلاوة. تأكد من الاتصال بالإنترنت."
    override val clearStatistics = "مسح الإحصائيات"
    override val clearStatisticsQuestion = "مسح الإحصائيات؟"
    override val clearStatisticsBody =
        "سيتم حذف سجل الإجابات لكل السور وسجل النشاط والجولات. نقاط المراحل لن تتأثر."
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
    override val modeAccuracySinceUpdate = "دقة كل نوع تشمل الإجابات منذ هذا التحديث فقط، والإجابات الأقدم محسوبة في الدقة الكلية."
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
    override val fontCordoba = "قرطبة"
    override val shareImage = "انشر الصورة"
}

object EnglishStrings : Strings {
    override val language = AppLanguage.English

    override fun percent(value: Int) = "$value%"

    override val appName = "AyahIQ"
    override val back = "Back"
    override val loadingQuran = "Loading the Quran…"
    override val homeTagline = "Test your memorization of the Holy Quran"
    override fun modeTitle(mode: GameMode) = when (mode) {
        GameMode.GuessSurah -> "Guess the Surah"
        GameMode.NextAyah -> "Next Ayah"
    }

    override fun modeSubtitle(mode: GameMode) = when (mode) {
        GameMode.GuessSurah -> "Read the ayah and pick its surah"
        GameMode.NextAyah -> "Pick the ayah that comes next"
    }

    override val mastered = "mastered"
    override val statistics = "Statistics"
    override val statisticsHint = "See which surahs need more review"
    override fun statisticsSummary(percent: Int, total: Int) =
        "Accuracy $percent% across $total ${if (total == 1) "question" else "questions"}"

    override val languageLabel = "Language"
    override val attributionQuranFrom = "Quran text from "
    override val attributionTanzil = "the Tanzil Project"
    override val attributionRecitationsFrom = " · Recitations from "
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
    override val goBack = "Back"
    override val mainMenu = "Main menu"
    override fun shareScoreText(score: Int, total: Int, levelName: String, modeTitle: String) =
        "I scored $score out of $total on the level “$levelName” in the Quran quiz “AyahIQ – $modeTitle”"

    override val playbackFailed = "Couldn't load the recitation. Check your internet connection."
    override val clearStatistics = "Clear statistics"
    override val clearStatisticsQuestion = "Clear statistics?"
    override val clearStatisticsBody =
        "The answer history for every surah, your activity and round history will be deleted. Level scores are not affected."
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
    override val modeAccuracySinceUpdate = "Per-mode accuracy counts answers given since this update; older answers are only in the overall accuracy."
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
    override val fontCordoba = "Cordoba"
    override val shareImage = "Share image"
}

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.Arabic -> ArabicStrings
    AppLanguage.English -> EnglishStrings
}

val LocalStrings = staticCompositionLocalOf<Strings> { ArabicStrings }
