package suft.android.ayahiq.data

import java.util.TimeZone
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Score

class ProgressTest {

    private val guess = GameMode.GuessSurah
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun dayStreaks() {
        var p = Progress()
        val expected = listOf(10L to 1, 10L to 1, 11L to 2, 14L to 1, 13L to 1)
        for ((day, streak) in expected) {
            p = p.withCompletedRound(RoundRecord("ayah:1-114", 5, 10, day = day))
            assertEquals("after day $day", streak, p.dayStreak)
        }
        assertEquals(2, p.bestDayStreak)
        assertEquals(14L, p.lastStreakDay)
    }

    @Test
    fun answersDoNotMoveStreak() {
        var p = Progress().withAnswer(guess, 0, true, day = 10).withAnswer(guess, 0, true, day = 10)
        assertEquals(2, p.answersToday(10))
        assertEquals(0, p.dayStreak)

        p = p.withAnswer(guess, 0, true, day = 11)
        assertEquals(1, p.answersToday(11))
        assertEquals(0, p.answersToday(10))

        p = p.withAnswer(guess, 0, true, day = 9)
        assertEquals(11L, p.lastActiveDay)
        assertEquals(2, p.answersToday(11))
    }

    @Test
    fun currentDayStreakExpires() {
        val p = Progress(lastStreakDay = 11, dayStreak = 2)
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
        val p = json
            .decodeFromString(Progress.serializer(), """{"totalCorrect":3,"surahCorrect":{"0":3}}""")
        assertEquals(Progress.NO_DAY, p.lastActiveDay)
        assertEquals(emptyList<RoundRecord>(), p.recentRounds)
        assertEquals(Score(3, 0), p.overall)
    }

    @Test
    fun localEpochDayUsesTimeZone() {
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals(0L, localEpochDay(0L, utc))
        assertEquals(1L, localEpochDay(86_399_999L, TimeZone.getTimeZone("GMT+03:00")))
        assertEquals(86_400_000L, millisUntilNextLocalDay(0L, utc))
        assertEquals(1_000L, millisUntilNextLocalDay(86_399_000L, utc))
        assertEquals(75_600_000L, millisUntilNextLocalDay(0L, TimeZone.getTimeZone("GMT+03:00")))
    }
}
