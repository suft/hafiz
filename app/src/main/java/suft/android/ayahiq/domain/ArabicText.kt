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
    private val MARKS = '\u06D6'..'\u06ED'
    private val WHITESPACE = Regex("\\s+")

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
}
