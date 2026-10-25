package suft.android.hafiz.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {

    @Test
    fun strongestRequiresStrongAccuracy() {
        assertTrue(Stats.strongestFirst(mapOf(0 to Score(1, 9))).isEmpty())
    }

    @Test
    fun reviewListSkipsStrongSurahsAndIsCapped() {
        assertTrue(Stats.weakestFirst(mapOf(0 to Score(199, 1))).isEmpty())
        val many = (0 until 15).associateWith { Score(1, 1) }
        assertEquals(10, Stats.weakestFirst(many).size)
    }

    @Test
    fun percentIsExact() {
        assertEquals(29, Score(29, 71).percent)
        assertEquals(58, Score(29, 21).percent)
        assertEquals(57, Score(57, 43).percent)
        assertEquals(0, Score().percent)
    }
}
