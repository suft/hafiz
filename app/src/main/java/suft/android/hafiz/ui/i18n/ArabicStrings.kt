package suft.android.hafiz.ui.i18n

import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.MushafEdition

object ArabicStrings : Strings {
    override val language = AppLanguage.Arabic

    override fun percent(value: Int) = "${number(value)}٪"

    override val appName = "حافظ"
    override val back = "رجوع"
    override val loadingQuran = "جارٍ تحميل المصحف…"
    override val tagline = "مسابقة لاختبار حفظك للقرآن الكريم"
    override fun styleTitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "آية وسورة"
        GameStyle.NextAyah -> "آيات"
        GameStyle.AyahPosition -> "أين الآية؟"
    }

    override fun styleSubtitle(style: GameStyle) = when (style) {
        GameStyle.GuessSurah -> "اقرأ الآية واختر السورة التي وردت فيها"
        GameStyle.NextAyah -> "أكمل التلاوة واختر الآية التالية"
        GameStyle.AyahPosition -> "اقرأ الآية واختر موضع بدايتها في مصحف المدينة"
    }

    override fun gameModeTitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "عادي"
        GameMode.Survival -> "البقاء"
        GameMode.Mutashabihat -> "المتشابهات"
    }

    override fun gameModeSubtitle(mode: GameMode) = when (mode) {
        GameMode.Regular -> "جولات محددة الأسئلة، أتقن كل مرحلة"
        GameMode.Survival -> "أسئلة بلا نهاية، وأول خطأ ينهي اللعبة"
        GameMode.Mutashabihat -> "أسئلة من القرآن كله بخيارات من الآيات المتشابهة، وثلاثة أخطاء تنهي اللعبة"
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
    override val play = "العب"
    override val playHint = "اختر وضع اللعب، ثم نوع الأسئلة والمرحلة"
    override val gameModes = "أوضاع اللعب"
    override val howToPlay = "طريقة اللعب"
    override val howToPlayHint = "تعرّف على أنواع الأسئلة وأوضاع اللعب والمراحل والإعدادات"
    override val guideStylesHeading = "أنواع الأسئلة"
    override val guideLevelsHeading = "المراحل والإتقان"
    override val guideLevels =
        "لكل نوع من الأسئلة مراحل تغطي أجزاء مختلفة من القرآن، من قصار السور حتى القرآن كله. " +
            "في الوضع العادي تُحفظ أفضل نتيجة لكل مرحلة، وفي البقاء أفضل لعبة لكل مرحلة. أما المتشابهات فبلا مراحل: أسئلتها من القرآن كله."
    override fun guideMastery(styleTitle: String, questions: Int, mastery: Int) =
        "«$styleTitle»: تتكون الجولة العادية من ${questionsCount(questions)}، وتُتقَن المرحلة إذا بلغت نتيجتك ${number(mastery)} أو أكثر."
    override val guideStatsHeading = "الإحصائيات والمراجعة"
    override val guideStats = "تعرض «إحصائيات» دقة إجاباتك والسور التي تحتاج إلى مراجعة، ومنها تبدأ جولة مراجعة لسورة واحدة."
    override val guideSettings
        get() = "زر الإعدادات أعلى القائمة الرئيسية يغيّر لغة التطبيق والمصحف الذي تعتمده " +
            "«${styleTitle(GameStyle.AyahPosition)}»: ${mushafEditionName(MushafEdition.V1)} وهي الافتراضية، أو " +
            "${mushafEditionName(MushafEdition.V2)}. تبدأ بعض الآيات في موضع مختلف في كل طبعة، والنتائج مشتركة بين الطبعتين."
    override fun statisticsSummary(percent: Int, total: Int) =
        "دقة إجاباتك ${percent(percent)} في ${questionsCount(total)}"

    override val languageLabel = "اللغة"
    override val settings = "الإعدادات"
    override val mushafEditionLabel get() = "المصحف في «${styleTitle(GameStyle.AyahPosition)}»"

    override fun mushafEditionName(edition: MushafEdition) = when (edition) {
        MushafEdition.V1 -> "طبعة ${number(edition.hijriYear)}هـ (الإصدار الأول)"
        MushafEdition.V2 -> "طبعة ${number(edition.hijriYear)}هـ (الإصدار الثاني)"
    }
    override val credits = "شكر وتقدير"
    override val creditsQuranText = "نص القرآن الكريم"
    override val creditsRecitations = "التلاوات"
    override val creditsMutashabihat = "المتشابهات"
    override val creditsMushafLayout = "مواضع الآيات في المصحف"
    override val creditsTanzil = "مشروع تنزيل"
    override val creditsAswaatulQurraa = "أصوات القرّاء"
    override val creditsDeveloper = "المطوّر"
    override val developerName = "سفيان طوط"
    override val creditsInspiredBy = "مستوحى من"
    override val privacyPolicy = "سياسة الخصوصية"
    override val creditsSourcesHeading = "المصادر"
    override val creditsPeopleHeading = "الأشخاص"
    override val creditsLegalHeading = "قانوني"
    override val creditsTanzilLicence = "ترخيص CC BY 3.0، والنص مستخدم دون تعديل"
    override val creditsMushafEditions get() = "طبعتا ${number(MushafEdition.V1.hijriYear)}هـ و${number(MushafEdition.V2.hijriYear)}هـ"
    override val creditsIcons = "الأيقونات"
    override val creditsIconsLicence = "ترخيص MIT"
    override val opensInBrowser = "يفتح في المتصفح"
    override fun versionLabel(version: String) =
        "الإصدار ${version.map { if (it in '0'..'9') '٠' + (it - '0') else it }.joinToString("")}"
    override fun totalPoints(points: Int) = "مجموع النقاط: ${number(points)}"
    override fun masteredCount(mastered: Int, total: Int) = "${number(mastered)} من ${number(total)} مُتقَنة"
    override val start = "ابدأ"
    override fun questionProgress(current: Int, total: Int) = "السؤال ${number(current)} من ${number(total)}"
    override val next = "التالي"
    override fun points(score: Int) = "النقاط ${number(score)}"
    override val whichSurah = "من أي سورة هذه الآية؟"
    override val whatIsNextAyah = "ما الآية التالية؟"
    override val whereIsAyah = "أين تبدأ هذه الآية في المصحف؟"
    override fun optionMarker(index: Int) = listOf("أ", "ب", "ج", "د")[index]
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
            "وعدد ألعاب البقاء والمتشابهات ومتوسطاتها وأطول سلاسلها. أفضل نتيجة في كل مرحلة لن تتأثر."
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
    override val revelationTitle = "مكية أم مدنية"
    override val revelationSubtitle = "خمس سور: اختر هل نزلت في مكة أم في المدينة"
    override val revelationQuestion = "أين نزلت هذه السورة؟"
    override fun revelationSurah(name: String) = "سورة $name"
    override val makki = "مكية"
    override val madani = "مدنية"
    override val revelationGood = "أحسنت! واصل حتى تبلغ العلامة الكاملة"
    override val revelationNeedsReview = "راجع أماكن نزول السور وحاول مرة أخرى"
}
