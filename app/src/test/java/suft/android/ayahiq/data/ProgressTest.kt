package suft.android.ayahiq.data

import java.util.TimeZone
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Score

class ProgressTest {

    private val guess = GameMode.GuessSurah

    @Test
    fun dayStreaks() {
        var p = Progress().withAnswer(guess, 0, true, day = 10)
        assertEquals(1, p.dayStreak)
        assertEquals(1, p.answersToday(10))

        p = p.withAnswer(guess, 0, true, day = 10)
        assertEquals(1, p.dayStreak)
        assertEquals(2, p.answersToday(10))

        p = p.withAnswer(guess, 0, true, day = 11)
        assertEquals(2, p.dayStreak)
        assertEquals(2, p.bestDayStreak)
        assertEquals(1, p.answersToday(11))

        p = p.withAnswer(guess, 0, true, day = 14)
        assertEquals(1, p.dayStreak)
        assertEquals(2, p.bestDayStreak)

        p = p.withAnswer(guess, 0, true, day = 13)
        assertEquals(1, p.dayStreak)
        assertEquals(14L, p.lastActiveDay)
        assertEquals(2, p.answersToday(14))
    }

    @Test
    fun currentDayStreakExpires() {
        val p = Progress(lastActiveDay = 11, dayStreak = 2)
        assertEquals(2, p.currentDayStreak(12))
        assertEquals(0, p.currentDayStreak(13))
        assertEquals(0, Progress().currentDayStreak(5))
    }

    @Test
    fun correctRuns() {
        var p = Progress()
        for (correct in listOf(true, true, false, true)) p = p.withAnswer(guess, 0, correct, day = 1)
        assertEquals(1, p.correctRun)
        assertEquals(2, p.bestCorrectRun)
    }

    @Test
    fun modeCountersFeedOverall() {
        val p = Progress()
            .withAnswer(GameMode.GuessSurah, 0, true, day = 1)
            .withAnswer(GameMode.NextAyah, 1, false, day = 1)
        assertEquals(Score(1, 0), p.modeScore(GameMode.GuessSurah))
        assertEquals(Score(0, 1), p.modeScore(GameMode.NextAyah))
        assertEquals(Score(1, 1), p.overall)
    }

    @Test
    fun recentRoundsAreCapped() {
        var p = Progress()
        for (score in 0 until 25) p = p.withCompletedRound(RoundRecord("surah:1-114", score, 20, day = 1))
        assertEquals(Progress.MAX_RECENT_ROUNDS, p.recentRounds.size)
        assertEquals(24, p.recentRounds.first().score)
        assertEquals(25, p.roundsCompleted)
    }

    @Test
    fun oldProgressFilesStillLoad() {
        val p = Json { ignoreUnknownKeys = true }
            .decodeFromString(Progress.serializer(), """{"totalCorrect":3,"surahCorrect":{"0":3}}""")
        assertEquals(Progress.NO_DAY, p.lastActiveDay)
        assertEquals(emptyList<RoundRecord>(), p.recentRounds)
        assertEquals(Score(3, 0), p.overall)
    }

    @Test
    fun localEpochDayUsesTimeZone() {
        assertEquals(0L, localEpochDay(0L, TimeZone.getTimeZone("UTC")))
        assertEquals(1L, localEpochDay(86_399_999L, TimeZone.getTimeZone("GMT+03:00")))
    }
}
