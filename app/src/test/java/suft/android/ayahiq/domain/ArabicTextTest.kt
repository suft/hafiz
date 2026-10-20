package suft.android.ayahiq.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicTextTest {

    // U+06DA and U+06D6 are small waqf (pause) signs; they are written between words but are not words.
    private val jim = "\u06DA"
    private val sad = "\u06D6"

    @Test
    fun firstWordsSkipsPauseMarks() {
        assertEquals("أ $jim ب …", ArabicText.firstWords("أ $jim ب $sad ج د", 2))
        assertEquals("أ $jim ب", ArabicText.firstWords("أ $jim ب", 2))
    }
}
