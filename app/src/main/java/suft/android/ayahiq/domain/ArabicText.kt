package suft.android.ayahiq.domain

object ArabicText {

    private const val ARABIC_INDIC_ZERO = '٠'

    /** 123 -> "١٢٣" */
    fun digits(value: Int): String =
        value.toString().map { if (it in '0'..'9') ARABIC_INDIC_ZERO + (it - '0') else it }.joinToString("")

    /** Ornate ayah-end marker with the ayah number, e.g. "﴿٥﴾". */
    fun ayahMarker(number: Int): String = "﴿${digits(number)}﴾"

    fun words(text: String): List<String> = text.trim().split(Regex("\\s+"))

    /** First [count] words of [text], followed by "…" when it was shortened. */
    fun firstWords(text: String, count: Int): String {
        val w = words(text)
        return if (w.size <= count) text.trim() else w.take(count).joinToString(" ") + " …"
    }

    /**
     * Shortens every text to at least [minWords] words, adding words where needed so
     * that no two shortened strings are identical (unless the full texts are).
     */
    fun distinctPrefixes(texts: List<String>, minWords: Int): List<String> {
        val counts = IntArray(texts.size) { minWords }
        val maxWords = texts.maxOf { words(it).size }
        while (true) {
            val shown = texts.mapIndexed { i, t -> firstWords(t, counts[i]) }
            val clashes = shown.indices.filter { i ->
                shown.indices.any { j -> j != i && shown[j] == shown[i] && texts[j] != texts[i] }
            }
            if (clashes.isEmpty() || clashes.all { counts[it] >= maxWords }) return shown
            clashes.forEach { counts[it] += 2 }
        }
    }
}
