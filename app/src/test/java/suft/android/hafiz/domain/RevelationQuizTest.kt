package suft.android.hafiz.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RevelationQuizTest {

    @Test
    fun placesMatchQuranCom() {
        assertEquals(28, (0 until Quran.SURAH_COUNT).count { RevelationPlaces.of(it) == RevelationPlace.Madinah })
        assertEquals(RevelationPlace.Makkah, RevelationPlaces.of(0)) // Al-Fatihah
        assertEquals(RevelationPlace.Madinah, RevelationPlaces.of(1)) // Al-Baqarah
        assertEquals(RevelationPlace.Madinah, RevelationPlaces.of(109)) // An-Nasr
        assertEquals(RevelationPlace.Makkah, RevelationPlaces.of(113)) // An-Nas
    }

    @Test
    fun dealsAreDistinctCorrectAndBalanced() {
        var madinah = 0
        for (seed in 0 until 1000) {
            val questions = RevelationQuiz.deal(Random(seed))
            assertEquals(RevelationQuiz.QUESTIONS, questions.size)
            assertEquals(RevelationQuiz.QUESTIONS, questions.map { it.surahIndex }.distinct().size)
            for (q in questions) {
                assertEquals(RevelationPlaces.of(q.surahIndex), q.place)
                if (q.place == RevelationPlace.Madinah) madinah++
            }
        }
        val share = madinah / (1000.0 * RevelationQuiz.QUESTIONS)
        assertTrue("Madinah share $share", share in 0.45..0.55)
    }
}
