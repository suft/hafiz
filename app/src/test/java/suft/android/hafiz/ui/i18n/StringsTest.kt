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

    @Test
    fun latinSingularsAgree() {
        assertEquals("1 bonne réponse sur 3 questions", FrenchStrings.livesResult(1, 3))
        assertEquals("2 bonnes réponses sur 3 questions", FrenchStrings.livesResult(2, 3))
        assertEquals("1 bonne · 1 mauvaise", FrenchStrings.surahCounts(1, 1))
        assertEquals("1 sourate travaillée sur 114", FrenchStrings.surahsPractised(1, 114))
        assertEquals("1 acierto de 1 pregunta", SpanishStrings.livesResult(1, 1))
        assertEquals("1 correcta · 2 incorrectas", SpanishStrings.surahCounts(1, 2))
        assertEquals("1 esatta su 2 domande", ItalianStrings.livesResult(1, 2))
        assertEquals("1 esatta · 1 sbagliata", ItalianStrings.surahCounts(1, 1))
        assertEquals("1 acerto em 2 perguntas", PortugueseStrings.livesResult(1, 2))
        assertEquals("1 certa · 0 erradas", PortugueseStrings.surahCounts(1, 0))
    }

    @Test
    fun arabicDaysAgoAgrees() {
        assertEquals("منذ ١٠ أيام", ArabicStrings.daysAgo(10))
        assertEquals("منذ ٢٥ يوماً", ArabicStrings.daysAgo(25))
        assertEquals("منذ ١٠٠ يوم", ArabicStrings.daysAgo(100))
        assertEquals("منذ ١٠٣ أيام", ArabicStrings.daysAgo(103))
    }
}
