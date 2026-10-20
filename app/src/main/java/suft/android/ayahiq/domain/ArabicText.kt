package suft.android.ayahiq.domain

object ArabicText {

    private const val ARABIC_INDIC_ZERO = '٠'

    /** 123 -> "١٢٣" */
    fun digits(value: Int): String =
        value.toString().map { if (it in '0'..'9') ARABIC_INDIC_ZERO + (it - '0') else it }.joinToString("")

    /** Ornate ayah-end marker with the ayah number, e.g. "﴿٥﴾". */
    fun ayahMarker(number: Int): String = "﴿${digits(number)}﴾"

    fun words(text: String): List<String> = text.trim().split(Regex("\\s+"))

    /** Waqf (pause) signs, small marks and the sajdah sign: written between words but not words themselves. */
    private fun isMark(token: String) = token.all { it in MARKS }

    private val MARKS = '\u06D6'..'\u06ED'
    private val WHITESPACE = Regex("\\s+")

    /** Number of real words in [text]; pause marks do not count. */
    fun wordCount(text: String): Int = words(text).count { !isMark(it) }

    /**
     * What makes two ayahs "the same" for the quiz: the text apart from pause marks. The Tanzil text
     * writes the same Quranic ayah with different waqf signs where it recurs (e.g. 2:162 and 3:88).
     */
    fun equalityKey(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) sb.append(if (c in MARKS) ' ' else c)
        return sb.toString().trim().replace(WHITESPACE, " ")
    }

    /** [text] without the sajdah sign ۩, which would mark an ayah as a prostration verse and give the answer away. */
    fun withoutSajdahSign(text: String): String = words(text.replace('\u06E9', ' ')).joinToString(" ")

    /**
     * First [count] words of [text] (pause marks between them are kept), followed by "…" when it was shortened.
     * Pause marks never count as words and never end the excerpt.
     */
    fun firstWords(text: String, count: Int): String {
        val tokens = words(text)
        var taken = 0
        var end = 0
        for ((i, token) in tokens.withIndex()) {
            if (isMark(token)) continue
            if (taken == count) break
            taken++
            end = i + 1
        }
        return if (tokens.drop(end).all(::isMark)) text.trim() else tokens.take(end).joinToString(" ") + " …"
    }

    /**
     * Shortens every text to at least [minWords] words, adding words where needed so that no two excerpts
     * look the same (unless the full texts are the same) and no shortened excerpt could pass for the start of
     * one of the [valid] texts (given as [equalityKey]s) that it is not. Pause marks are ignored when
     * comparing: two excerpts that differ only by a waqf sign look the same to the reader.
     */
    fun distinctPrefixes(texts: List<String>, minWords: Int, valid: Collection<String> = emptyList()): List<String> {
        val counts = IntArray(texts.size) { minWords }
        val maxWords = texts.maxOf { wordCount(it) }
        val fullKeys = texts.map(::equalityKey)
        while (true) {
            val shown = texts.mapIndexed { i, t -> firstWords(t, counts[i]) }
            val stems = shown.map { equalityKey(it.removeSuffix("…")) }
            val clashes = shown.indices.filter { i ->
                val truncated = shown[i].endsWith("…")
                shown.indices.any { j -> j != i && stems[j] == stems[i] && fullKeys[j] != fullKeys[i] } ||
                    (truncated && valid.any { it != fullKeys[i] && it.startsWith("${stems[i]} ") })
            }
            if (clashes.isEmpty() || clashes.all { counts[it] >= maxWords }) return shown
            clashes.forEach { counts[it] += 2 }
        }
    }
}
