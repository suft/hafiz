package suft.android.hafiz.domain

/** The two quiz types of the original app. */
enum class GameStyle(
    val key: String,
    val questionsPerRound: Int,
    /** Score needed for a level to count as "mastered" (original: > 15/20 and > 8/10). */
    val masteryScore: Int,
) {
    /** Show an ayah, guess which surah it belongs to. */
    GuessSurah(
        key = "surah",
        questionsPerRound = 20,
        masteryScore = 16,
    ),

    /** Show an ayah (with its surah), pick the ayah that follows it. */
    NextAyah(
        key = "ayah",
        questionsPerRound = 10,
        masteryScore = 9,
    );

    /** Mastery threshold for a round of [total] questions (same ratio as [masteryScore], rounded up). */
    fun masteryFor(total: Int): Int = (masteryScore * total + questionsPerRound - 1) / questionsPerRound

    companion object {
        fun fromKey(key: String): GameStyle = entries.first { it.key == key }
    }
}

/**
 * A playable level: a contiguous range of surahs (0-based, inclusive).
 *
 * @property openings "فواتح السور" — questions use the first two ayahs of a random surah.
 * @property practice single-surah review round started from Statistics; never stores a best score.
 * @property questionsPerRound questions in one round; shorter than the style's only for practice on short surahs.
 */
data class Level(
    val style: GameStyle,
    val index: Int,
    val arabicName: String,
    val englishName: String,
    val firstSurah: Int,
    val lastSurah: Int,
    val openings: Boolean = false,
    val practice: Boolean = false,
    val questionsPerRound: Int = style.questionsPerRound,
) {
    /** Score needed for this level to count as mastered. */
    val masteryScore: Int get() = style.masteryFor(questionsPerRound)

    fun name(language: AppLanguage): String = if (language == AppLanguage.English) englishName else arabicName

    /** Stable key used to persist the best score (independent of list order). */
    val id: String
        get() = "${style.key}:${firstSurah + 1}-${lastSurah + 1}" +
            (if (openings) ":openings" else "") +
            (if (practice) ":practice" else "")

    val surahRange: IntRange get() = firstSurah..lastSurah
}

/** Level tables, taken from the original app (surah numbers are 1-based here). */
object Levels {

    private data class Spec(
        val first: Int,
        val last: Int,
        val openings: Boolean = false,
        val arabicName: String? = null,
        val englishName: String? = null,
    )

    private val guessSurah = listOf(
        Spec(100, 114, arabicName = "العاديات حتى الناس"),
        Spec(90, 114, arabicName = "البلد حتى الناس"),
        Spec(78, 89, arabicName = "النبأ حتى الفجر"),
        Spec(78, 114, arabicName = "جزء عمَّ"),
        Spec(67, 77, arabicName = "جزء تبارك"),
        Spec(67, 114, arabicName = "جزءا تبارك وعمَّ معاً"),
        Spec(58, 66, arabicName = "جزء قد سمع (المجادلة حتى التحريم)", englishName = "Juz' Qad Sami' (Al-Mujadila to At-Tahrim)"),
        Spec(51, 57, arabicName = "جزء الذاريات (الذاريات حتى الحديد)", englishName = "Juz' Adh-Dhariyat (Adh-Dhariyat to Al-Hadid)"),
        Spec(43, 50, arabicName = "الزخرف حتى ق"),
        Spec(36, 42, arabicName = "يس حتى الشورى"),
        Spec(36, 114, arabicName = "ربع يس (يس حتى الناس)", englishName = "Last quarter from Ya-Sin (Ya-Sin to An-Nas)"),
        Spec(30, 35, arabicName = "الروم حتى فاطر"),
        Spec(24, 29, arabicName = "النور حتى العنكبوت"),
        Spec(18, 23, arabicName = "الكهف حتى المؤمنون"),
        Spec(18, 114, arabicName = "نصف القرآن (الكهف حتى الناس)", englishName = "Second half of the Quran (Al-Kahf to An-Nas)"),
        Spec(12, 17, arabicName = "يوسف حتى الإسراء"),
        Spec(7, 11, arabicName = "الأعراف حتى هود"),
        Spec(2, 6, arabicName = "البقرة حتى الأنعام"),
        Spec(1, 114, arabicName = "المصحف كاملاً"),
        Spec(1, 114, openings = true, arabicName = "فواتح السور"),
    )

    private val nextAyah = listOf(
        Spec(100, 114), Spec(90, 99), Spec(83, 89), Spec(81, 82), Spec(78, 80),
        Spec(73, 77), Spec(67, 72), Spec(67, 77), Spec(67, 114), Spec(58, 66),
        Spec(51, 57), Spec(44, 50), Spec(37, 43), Spec(36, 36), Spec(29, 35),
        Spec(22, 28), Spec(17, 21), Spec(18, 18), Spec(13, 16), Spec(12, 12),
        Spec(6, 11), Spec(2, 5), Spec(2, 2), Spec(1, 114, openings = true), Spec(1, 114),
    )

    fun forStyle(style: GameStyle, quran: Quran): List<Level> {
        val specs = when (style) {
            GameStyle.GuessSurah -> guessSurah
            GameStyle.NextAyah -> nextAyah
        }
        return specs.mapIndexed { i, s ->
            Level(
                style = style,
                index = i,
                arabicName = s.arabicName ?: autoName(s, quran, AppLanguage.Arabic),
                englishName = s.englishName ?: autoName(s, quran, AppLanguage.English),
                firstSurah = s.first - 1,
                lastSurah = s.last - 1,
                openings = s.openings,
            )
        }
    }

    /** The whole-Quran level of [style] (not the openings one); the only level of a [GameMode.wholeQuranOnly] mode. */
    fun wholeQuran(style: GameStyle, quran: Quran): Level =
        forStyle(style, quran).single { it.firstSurah == 0 && it.lastSurah == Quran.SURAH_COUNT - 1 && !it.openings }

    /** The levels [mode] offers for [style]. */
    fun forMode(mode: GameMode, style: GameStyle, quran: Quran): List<Level> =
        if (mode.wholeQuranOnly) listOf(wholeQuran(style, quran)) else forStyle(style, quran)

    /** Next-ayah round limited to one surah, started from the statistics screen. */
    fun surahPractice(surahIndex: Int, quran: Quran): Level {
        val surah = quran.surahs[surahIndex]
        return Level(
            style = GameStyle.NextAyah,
            index = -1, // not part of a level table
            arabicName = "مراجعة سورة ${surah.displayName(AppLanguage.Arabic)}",
            englishName = "Review: Surah ${surah.displayName(AppLanguage.English)}",
            firstSurah = surahIndex,
            lastSurah = surahIndex,
            practice = true,
            questionsPerRound = minOf(GameStyle.NextAyah.questionsPerRound, surah.ayahCount - 1),
        )
    }

    /** Resolves a stored [Level.id]; null for ids that no longer exist. */
    fun byId(id: String, quran: Quran): Level? {
        if (id.endsWith(":practice")) {
            val surah = id.substringAfter(':').substringBefore('-').toIntOrNull()?.minus(1)
            return surah?.takeIf { it in 0 until Quran.SURAH_COUNT }?.let { surahPractice(it, quran) }
        }
        return GameStyle.entries.firstNotNullOfOrNull { style -> forStyle(style, quran).firstOrNull { it.id == id } }
    }

    /** Same naming rules as the original "complete the ayah" levels. */
    private fun autoName(s: Spec, quran: Quran, language: AppLanguage): String {
        fun n(number: Int) = quran.surahs[number - 1].displayName(language)
        if (language == AppLanguage.English) {
            return when {
                s.openings -> "Surah Openings"
                s.first == 1 && s.last == 114 -> "The Whole Quran"
                s.first == 78 && s.last == 114 -> "Juz' 'Amma"
                s.first == 67 && s.last == 77 -> "Juz' Tabarak"
                s.first == 67 && s.last == 114 -> "Juz' Tabarak and Juz' 'Amma"
                s.first == s.last -> "Surah ${n(s.first)}"
                s.first + 1 == s.last -> "Surahs ${n(s.first)} and ${n(s.last)}"
                else -> "${n(s.first)} to ${n(s.last)}"
            }
        }
        return when {
            s.openings -> "فواتح السور"
            s.first == 1 && s.last == 114 -> "المصحف كاملاً"
            s.first == 78 && s.last == 114 -> "جزء عمَّ"
            s.first == 67 && s.last == 77 -> "جزء تبارك"
            s.first == 67 && s.last == 114 -> "جزءا تبارك وعمَّ"
            s.first == s.last -> "سورة ${n(s.first)}"
            s.first + 1 == s.last -> "سورتا ${n(s.first)} و${n(s.last)}"
            else -> "${n(s.first)} حتى ${n(s.last)}"
        }
    }
}
