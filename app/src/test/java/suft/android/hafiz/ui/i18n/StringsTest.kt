package suft.android.hafiz.ui.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class StringsTest {

    @Test
    fun arabicQuestionCountsAgree() {
        assertEquals("٠ صحيحة من ٣ أسئلة", ArabicStrings.livesResult(0, 3))
        assertEquals("٢ صحيحة من ١١ سؤالاً", ArabicStrings.livesResult(2, 11))
        assertEquals("دقة إجاباتك ٥٠٪ في ٥ أسئلة", ArabicStrings.statisticsSummary(50, 5))
        assertEquals("دقة إجاباتك ٩٠٪ في سؤالين", ArabicStrings.statisticsSummary(90, 2))
    }

    @Test
    fun latinQuestionCountsAgree() {
        assertEquals("0 question", FrenchStrings.questionsCount(0))
        assertEquals("1 question", FrenchStrings.questionsCount(1))
        assertEquals("2 questions", FrenchStrings.questionsCount(2))
        assertEquals("1 Frage", GermanStrings.questionsCount(1))
        assertEquals("0 Fragen", GermanStrings.questionsCount(0))
        assertEquals("1 pregunta", SpanishStrings.questionsCount(1))
        assertEquals("2 domande", ItalianStrings.questionsCount(2))
        assertEquals("1 pergunta", PortugueseStrings.questionsCount(1))
        assertEquals("50\u202F%", FrenchStrings.percent(50))
    }
}
