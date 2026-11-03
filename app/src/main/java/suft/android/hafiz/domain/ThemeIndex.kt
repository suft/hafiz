package suft.android.hafiz.domain

import kotlin.math.ln

/**
 * How much each surah shares the distinctive words of a prompt: BM25 over the bundled Tanzil text, with words folded by
 * [normalise]. Derived from the text itself, so no topic data set (or licence) is involved.
 */
class ThemeIndex(private val quran: Quran) {

    /** Word ids of every ayah, repeats kept. */
    private val ayahWords: Array<IntArray>
    private val idf: DoubleArray

    /** Per word: the surahs it occurs in and how often in each (parallel arrays). */
    private val postingSurahs: Array<IntArray>
    private val postingCounts: Array<IntArray>
    private val surahLength = IntArray(Quran.SURAH_COUNT)
    private val avgLength: Double

    init {
        val vocabulary = vocabulary(quran)
        val ids = HashMap<String, Int>()
        ayahWords = Array(quran.ayahCount) { ayah ->
            val ayahTokens = foldedWords(quran.text(ayah)).map { stem(it, vocabulary) }.filter { it.isNotEmpty() }
            IntArray(ayahTokens.size) { ids.getOrPut(ayahTokens[it]) { ids.size } }
        }
        val words = ids.size
        val ayahsWith = IntArray(words)
        val perSurah = Array(words) { HashMap<Int, Int>(2) }
        for (ayah in 0 until quran.ayahCount) {
            val surah = quran.surahOf(ayah).index
            surahLength[surah] += ayahWords[ayah].size
            for (w in ayahWords[ayah]) perSurah[w].merge(surah, 1, Int::plus)
            for (w in ayahWords[ayah].distinct()) ayahsWith[w]++
        }
        val n = quran.ayahCount.toDouble()
        idf = DoubleArray(words) { ln((n - ayahsWith[it] + 0.5) / (ayahsWith[it] + 0.5) + 1.0) }
        postingSurahs = Array(words) { perSurah[it].keys.toIntArray() }
        postingCounts = Array(words) { w -> IntArray(postingSurahs[w].size) { perSurah[w].getValue(postingSurahs[w][it]) } }
        avgLength = surahLength.sum().toDouble() / Quran.SURAH_COUNT
    }

    /**
     * Relatedness of every surah (index 0..113) to the ayahs [shown]; 0 when they share no word. The shown ayahs do not
     * count for their own surah. Divided by the prompt's total idf, so prompts of any length score on the same scale (~0..1.5).
     */
    fun relatedness(shown: IntRange): DoubleArray {
        val inPrompt = HashMap<Int, Int>()
        var shownTokens = 0
        for (ayah in shown) {
            for (w in ayahWords[ayah]) inPrompt.merge(w, 1, Int::plus)
            shownTokens += ayahWords[ayah].size
        }
        val own = quran.surahOf(shown.first).index
        val ownLength = surahLength[own] - shownTokens
        val scores = DoubleArray(Quran.SURAH_COUNT)
        var idfSum = 0.0
        for ((w, inShown) in inPrompt) {
            idfSum += idf[w]
            val surahs = postingSurahs[w]
            val counts = postingCounts[w]
            for (i in surahs.indices) {
                val s = surahs[i]
                val tf = counts[i] - if (s == own) inShown else 0
                if (tf <= 0) continue
                val length = if (s == own) ownLength else surahLength[s]
                scores[s] += idf[w] * tf * (K1 + 1) / (tf + K1 * (1 - B + B * length / avgLength))
            }
        }
        if (idfSum > 0) for (s in scores.indices) scores[s] /= idfSum
        return scores
    }

    companion object {
        private const val K1 = 1.2
        private const val B = 0.75

        private val prefixes = listOf("بال", "كال", "فال", "وال", "لل", "ال")

        private fun isMark(c: Char) =
            c in '\u0610'..'\u061A' || c in '\u064B'..'\u065F' || c == '\u0670' || c in '\u06D6'..'\u06ED' || c == '\u0640'

        private val WHITESPACE = Regex("\\s+")

        /** Folded form of one word: marks dropped, letter variants unified. */
        private fun fold(word: String): String = buildString(word.length) {
            for (c in word) {
                if (isMark(c)) continue
                append(
                    when (c) {
                        'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                        'ى' -> 'ي'
                        'ة' -> 'ه'
                        'ؤ' -> 'و'
                        'ئ' -> 'ي'
                        else -> c
                    },
                )
            }
        }

        private fun foldedWords(text: String): List<String> =
            text.filterNot(::isMark).split(WHITESPACE).map(::fold).filter { it.isNotEmpty() }

        /** Every word of the Quran with marks removed and letter variants folded. */
        internal fun vocabulary(quran: Quran): Set<String> =
            (0 until quran.ayahCount).flatMapTo(HashSet()) { foldedWords(quran.text(it)) }

        /** Strips a leading و/ف only when the rest is itself a word of the Quran, then a leading article or preposition. */
        private fun stem(folded: String, vocabulary: Set<String>): String {
            val unjoined = if (
                folded.length > 3 &&
                (folded[0] == 'و' || folded[0] == 'ف') &&
                folded.substring(1) in vocabulary
            ) {
                folded.substring(1)
            } else {
                folded
            }
            val prefix = prefixes.firstOrNull { unjoined.startsWith(it) && unjoined.length - it.length >= 2 }
            return if (prefix != null) unjoined.substring(prefix.length) else unjoined
        }

        /** Folded and stemmed form of one word; "" for a token that is only marks. [vocabulary] is [vocabulary] of the Quran. */
        internal fun normalise(word: String, vocabulary: Set<String>): String = stem(fold(word), vocabulary)
    }
}
