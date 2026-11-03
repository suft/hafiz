package suft.android.hafiz.domain

/** Question styles: the two of the original app, plus Where's the Ayah?. */
enum class GameStyle(
    val key: String,
    val questionsPerRound: Int,
    /** Score needed for a level to count as "mastered" (original: > 15/20 and > 8/10). */
    val masteryScore: Int,
    /** False when the options are fixed, not drawn from look-alikes: Mutashabihat mode cannot offer the style. */
    val hasLookAlikes: Boolean = true,
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
    ),

    /** Show an ayah, pick where it starts on the open Madinah mushaf (see [MushafCell]). */
    AyahPosition(
        key = "position",
        questionsPerRound = 10,
        masteryScore = 8,
        hasLookAlikes = false,
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
    val names: Map<AppLanguage, String>,
    val firstSurah: Int,
    val lastSurah: Int,
    val openings: Boolean = false,
    val practice: Boolean = false,
    val questionsPerRound: Int = style.questionsPerRound,
) {
    /** Score needed for this level to count as mastered. */
    val masteryScore: Int get() = style.masteryFor(questionsPerRound)

    fun name(language: AppLanguage): String = names.getValue(language)

    /** Stable key used to persist the best score (independent of list order). */
    val id: String
        get() = "${style.key}:${firstSurah + 1}-${lastSurah + 1}" +
            (if (openings) ":openings" else "") +
            (if (practice) ":practice" else "")

    val surahRange: IntRange get() = firstSurah..lastSurah
}

/** Level tables, taken from the original app (surah numbers are 1-based here). */
object Levels {

    /** Guess-the-Surah levels named after a part of the Quran, followed by their surah range in brackets. */
    private enum class Part { JuzQadSami, JuzAdhDhariyat, LastQuarterFromYaSin, SecondHalf }

    private data class Spec(
        val first: Int,
        val last: Int,
        val openings: Boolean = false,
        val arabicName: String? = null,
        val part: Part? = null,
    )

    private class LevelWords(
        val openings: String,
        val wholeQuran: String,
        val juzAmma: String,
        val juzTabarak: String,
        val juzTabarakAndAmma: String,
        val part: (Part) -> String,
        val surah: (String) -> String,
        val twoSurahs: (String, String) -> String,
        val range: (String, String) -> String,
        val review: (String) -> String,
    )

    private fun wordsFor(language: AppLanguage): LevelWords = when (language) {
        AppLanguage.Arabic -> LevelWords(
            openings = "فواتح السور",
            wholeQuran = "المصحف كاملاً",
            juzAmma = "جزء عمَّ",
            juzTabarak = "جزء تبارك",
            juzTabarakAndAmma = "جزءا تبارك وعمَّ",
            part = {
                when (it) {
                    Part.JuzQadSami -> "جزء قد سمع"
                    Part.JuzAdhDhariyat -> "جزء الذاريات"
                    Part.LastQuarterFromYaSin -> "ربع يس"
                    Part.SecondHalf -> "نصف القرآن"
                }
            },
            surah = { n -> "سورة $n" },
            twoSurahs = { a, b -> "سورتا $a و$b" },
            range = { a, b -> "$a حتى $b" },
            review = { n -> "مراجعة سورة $n" },
        )
        AppLanguage.English -> LevelWords(
            openings = "Surah Openings",
            wholeQuran = "The Whole Quran",
            juzAmma = "Juz' 'Amma",
            juzTabarak = "Juz' Tabarak",
            juzTabarakAndAmma = "Juz' Tabarak and Juz' 'Amma",
            part = {
                when (it) {
                    Part.JuzQadSami -> "Juz' Qad Sami'"
                    Part.JuzAdhDhariyat -> "Juz' Adh-Dhariyat"
                    Part.LastQuarterFromYaSin -> "Last quarter from Ya-Sin"
                    Part.SecondHalf -> "Second half of the Quran"
                }
            },
            surah = { n -> "Surah $n" },
            twoSurahs = { a, b -> "Surahs $a and $b" },
            range = { a, b -> "$a to $b" },
            review = { n -> "Review: Surah $n" },
        )
        AppLanguage.German -> LevelWords(
            openings = "Anfänge der Suren",
            wholeQuran = "Der ganze Koran",
            juzAmma = JUZ_AMMA,
            juzTabarak = JUZ_TABARAK,
            juzTabarakAndAmma = "$JUZ_TABARAK und $JUZ_AMMA",
            part = { latinPart(it, "Letztes Viertel ab Ya-Sin", "Zweite Hälfte des Korans") },
            surah = { n -> "Sure $n" },
            twoSurahs = { a, b -> "Suren $a und $b" },
            range = { a, b -> "$a bis $b" },
            review = { n -> "Wiederholung: Sure $n" },
        )
        AppLanguage.Spanish -> LevelWords(
            openings = "Inicios de las suras",
            wholeQuran = "El Corán completo",
            juzAmma = JUZ_AMMA,
            juzTabarak = JUZ_TABARAK,
            juzTabarakAndAmma = "$JUZ_TABARAK y $JUZ_AMMA",
            part = { latinPart(it, "Último cuarto desde Ya-Sin", "Segunda mitad del Corán") },
            surah = { n -> "Sura $n" },
            twoSurahs = { a, b -> "Suras $a y $b" },
            range = { a, b -> "$a hasta $b" },
            review = { n -> "Repaso: sura $n" },
        )
        AppLanguage.French -> LevelWords(
            openings = "Débuts des sourates",
            wholeQuran = "Le Coran entier",
            juzAmma = JUZ_AMMA,
            juzTabarak = JUZ_TABARAK,
            juzTabarakAndAmma = "$JUZ_TABARAK et $JUZ_AMMA",
            part = { latinPart(it, "Dernier quart depuis Ya-Sin", "Seconde moitié du Coran") },
            surah = { n -> "Sourate $n" },
            twoSurahs = { a, b -> "Sourates $a et $b" },
            range = { a, b -> "$a à $b" },
            review = { n -> "Révision\u00A0: sourate $n" },
        )
        AppLanguage.Italian -> LevelWords(
            openings = "Inizi delle sure",
            wholeQuran = "Il Corano intero",
            juzAmma = JUZ_AMMA,
            juzTabarak = JUZ_TABARAK,
            juzTabarakAndAmma = "$JUZ_TABARAK e $JUZ_AMMA",
            part = { latinPart(it, "Ultimo quarto da Ya-Sin", "Seconda metà del Corano") },
            surah = { n -> "Sura $n" },
            twoSurahs = { a, b -> "Sure $a e $b" },
            range = { a, b -> "$a fino a $b" },
            review = { n -> "Ripasso: sura $n" },
        )
        AppLanguage.Portuguese -> LevelWords(
            openings = "Inícios das suratas",
            wholeQuran = "O Alcorão completo",
            juzAmma = JUZ_AMMA,
            juzTabarak = JUZ_TABARAK,
            juzTabarakAndAmma = "$JUZ_TABARAK e $JUZ_AMMA",
            part = { latinPart(it, "Último quarto a partir de Ya-Sin", "Segunda metade do Alcorão") },
            surah = { n -> "Surata $n" },
            twoSurahs = { a, b -> "Suratas $a e $b" },
            range = { a, b -> "$a até $b" },
            review = { n -> "Revisão: surata $n" },
        )
    }

    // The Latin-script languages other than English share the English spelling of the juz' names.
    private const val JUZ_AMMA = "Juz' 'Amma"
    private const val JUZ_TABARAK = "Juz' Tabarak"

    private fun latinPart(part: Part, lastQuarter: String, secondHalf: String): String = when (part) {
        Part.JuzQadSami -> "Juz' Qad Sami'"
        Part.JuzAdhDhariyat -> "Juz' Adh-Dhariyat"
        Part.LastQuarterFromYaSin -> lastQuarter
        Part.SecondHalf -> secondHalf
    }

    private val guessSurah = listOf(
        Spec(100, 114, arabicName = "العاديات حتى الناس"),
        Spec(90, 114, arabicName = "البلد حتى الناس"),
        Spec(78, 89, arabicName = "النبأ حتى الفجر"),
        Spec(78, 114, arabicName = "جزء عمَّ"),
        Spec(67, 77, arabicName = "جزء تبارك"),
        Spec(67, 114, arabicName = "جزءا تبارك وعمَّ معاً"),
        Spec(58, 66, part = Part.JuzQadSami),
        Spec(51, 57, part = Part.JuzAdhDhariyat),
        Spec(43, 50, arabicName = "الزخرف حتى ق"),
        Spec(36, 42, arabicName = "يس حتى الشورى"),
        Spec(36, 114, part = Part.LastQuarterFromYaSin),
        Spec(30, 35, arabicName = "الروم حتى فاطر"),
        Spec(24, 29, arabicName = "النور حتى العنكبوت"),
        Spec(18, 23, arabicName = "الكهف حتى المؤمنون"),
        Spec(18, 114, part = Part.SecondHalf),
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
            // Where's the Ayah? reuses the next-ayah surah ranges: small steps plus single long surahs suit page-by-page review.
            GameStyle.NextAyah, GameStyle.AyahPosition -> nextAyah
        }
        return specs.mapIndexed { i, s ->
            Level(
                style = style,
                index = i,
                names = AppLanguage.entries.associateWith { name(s, quran, it) },
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
            names = AppLanguage.entries.associateWith { wordsFor(it).review(surah.displayName(it)) },
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

    /** Same naming rules as the original "complete the ayah" levels, built from [wordsFor]. */
    private fun name(s: Spec, quran: Quran, language: AppLanguage): String {
        if (language == AppLanguage.Arabic && s.arabicName != null) return s.arabicName
        val w = wordsFor(language)
        fun n(number: Int) = quran.surahs[number - 1].displayName(language)
        return when {
            s.part != null -> "${w.part(s.part)} (${w.range(n(s.first), n(s.last))})"
            s.openings -> w.openings
            s.first == 1 && s.last == 114 -> w.wholeQuran
            s.first == 78 && s.last == 114 -> w.juzAmma
            s.first == 67 && s.last == 77 -> w.juzTabarak
            s.first == 67 && s.last == 114 -> w.juzTabarakAndAmma
            s.first == s.last -> w.surah(n(s.first))
            s.first + 1 == s.last -> w.twoSurahs(n(s.first), n(s.last))
            else -> w.range(n(s.first), n(s.last))
        }
    }
}
