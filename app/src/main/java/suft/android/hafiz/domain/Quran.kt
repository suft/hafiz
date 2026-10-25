package suft.android.hafiz.domain

/**
 * A surah of the Quran.
 *
 * @property index 0-based position (Al-Fatiha = 0).
 * @property firstAyah global (0-based) index of its first ayah within [Quran].
 */
class Surah(
    val index: Int,
    val name: String,
    val firstAyah: Int,
    val ayahCount: Int,
) {
    /** 1-based surah number as used by mushafs and audio file names. */
    val number: Int get() = index + 1
    val lastAyah: Int get() = firstAyah + ayahCount - 1
    val ayahRange: IntRange get() = firstAyah..lastAyah

    fun displayName(language: AppLanguage): String =
        if (language == AppLanguage.English) SurahNames.english[index] else name

    override fun toString() = "Surah($number $name, $ayahCount ayahs)"
}

/**
 * Immutable, fully indexed Quran text. Ayahs are addressed by a global 0-based index
 * (0..6235); helpers convert to (surah, ayah-in-surah).
 */
class Quran private constructor(
    val surahs: List<Surah>,
    private val texts: List<String>,
    private val surahOfAyah: IntArray,
) {
    val ayahCount: Int get() = texts.size

    fun text(ayah: Int): String = texts[ayah]

    fun surahOf(ayah: Int): Surah = surahs[surahOfAyah[ayah]]

    /** 1-based ayah number inside its surah. */
    fun ayahNumberInSurah(ayah: Int): Int = ayah - surahOf(ayah).firstAyah + 1

    /** "سورة البقرة · الآية ٢٥٥" / "Surah Al-Baqarah · Ayah 255"; multi-ayah ranges use an en dash. */
    fun reference(ayahs: IntRange, language: AppLanguage): String {
        val name = surahOf(ayahs.first).displayName(language)
        val first = language.number(ayahNumberInSurah(ayahs.first))
        val last = language.number(ayahNumberInSurah(ayahs.last))
        val single = ayahs.first == ayahs.last
        return when (language) {
            AppLanguage.Arabic -> "سورة $name · " + if (single) "الآية $first" else "الآيات $first–$last"
            AppLanguage.English -> "Surah $name · " + if (single) "Ayah $first" else "Ayahs $first–$last"
        }
    }

    fun isLastOfSurah(ayah: Int): Boolean = surahOf(ayah).lastAyah == ayah

    /** [ArabicText.equalityKey] of an ayah: its text apart from pause marks. */
    fun key(ayah: Int): String = keys[ayah]

    /** Every global index whose text equals that of [ayah] apart from pause marks (including [ayah] itself). */
    fun occurrencesOf(ayah: Int): IntArray = keyIndex.getValue(keys[ayah])

    /**
     * Indices of the surahs that contain the consecutive ayahs of [shown] (same order, inside one surah,
     * equal apart from pause marks). Used to avoid offering a second, equally correct answer.
     */
    fun surahsContaining(shown: IntRange): Set<Int> {
        val wanted = shown.map(::key)
        return occurrencesOf(shown.first)
            .filter { start ->
                val surah = surahOf(start)
                wanted.indices.all { k ->
                    val g = start + k
                    g <= surah.lastAyah && keys[g] == wanted[k]
                }
            }
            .mapTo(mutableSetOf()) { surahOfAyah[it] }
    }

    private val keys: List<String> = texts.map(ArabicText::equalityKey)

    private val keyIndex: Map<String, IntArray> by lazy {
        keys.indices.groupBy { keys[it] }.mapValues { (_, v) -> v.toIntArray() }
    }

    companion object {
        const val SURAH_COUNT = 114

        /** Builds the index from (surahName, ayahTexts) pairs in mushaf order. */
        fun build(surahs: List<Pair<String, List<String>>>): Quran {
            val texts = ArrayList<String>(6236)
            val owner = ArrayList<Int>(6236)
            val built = surahs.mapIndexed { i, (name, ayahs) ->
                require(ayahs.isNotEmpty()) { "Surah ${i + 1} has no ayahs" }
                val first = texts.size
                texts += ayahs
                repeat(ayahs.size) { owner += i }
                Surah(index = i, name = name, firstAyah = first, ayahCount = ayahs.size)
            }
            return Quran(built, texts, owner.toIntArray())
        }
    }
}
