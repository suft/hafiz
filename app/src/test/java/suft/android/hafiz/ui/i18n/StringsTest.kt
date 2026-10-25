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
}
