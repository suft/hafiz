package suft.android.ayahiq.data

import java.util.TimeZone
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Score

class ProgressTest {

    private val guess = GameStyle.GuessSurah
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
        assertEquals(13L, p.lastStreakDay)
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
        assertEquals(9L, p.lastActiveDay)
        assertEquals(1, p.answersToday(9))
        assertEquals(0, p.answersToday(11))
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
    fun styleCountersFeedOverall() {
        val p = Progress()
            .withAnswer(GameStyle.GuessSurah, 0, true, day = 1)
            .withAnswer(GameStyle.NextAyah, 1, false, day = 1)
        assertEquals(Score(1, 0), p.styleScore(GameStyle.GuessSurah))
        assertEquals(Score(0, 1), p.styleScore(GameStyle.NextAyah))
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
    fun legacyModeCountersStillLoad() {
        val p = json.decodeFromString(
            Progress.serializer(),
            """{"modeCorrect":{"surah":2},"modeWrong":{"ayah":1}}""",
        )
        assertEquals(Score(2, 0), p.styleScore(GameStyle.GuessSurah))
        assertEquals(Score(0, 1), p.styleScore(GameStyle.NextAyah))
    }

    @Test
    fun oldRoundRecordsAreRegular() {
        val p = json.decodeFromString(
            Progress.serializer(),
            """{"recentRounds":[{"levelId":"ayah:1-114","score":5,"total":10,"day":3}]}""",
        )
        assertEquals("regular", p.recentRounds[0].mode)
    }

    @Test
    fun runBestKeepsHigherPerModeAndLevel() {
        val id = "surah:1-114"
        val p = Progress()
            .withRunBest(GameMode.Survival, id, 5, 5)
            .withRunBest(GameMode.Survival, id, 3, 3)
        assertEquals(5, p.runBest(GameMode.Survival, id))
        assertEquals(5, p.runStats(GameMode.Survival).bestStreak)
        assertEquals(0, p.runBest(GameMode.Mutashabihat, id))
    }

    @Test
    fun leaderboardScoreSumsLevelBestsPerMode() {
        val l0 = Level(guess, 0, "a", "a", 99, 113)
        val l1 = Level(guess, 1, "b", "b", 89, 113)
        val levels = listOf(l0, l1)
        val p = Progress(bestScores = mapOf(l0.id to 18, l1.id to 12, "ayah:1-114" to 9))
            .withRunBest(GameMode.Survival, l0.id, 7, 7)
            .withRunBest(GameMode.Mutashabihat, l1.id, 4, 3)
        assertEquals(30, p.leaderboardScore(GameMode.Regular, levels))
        assertEquals(7, p.leaderboardScore(GameMode.Survival, levels))
        assertEquals(4, p.leaderboardScore(GameMode.Mutashabihat, levels))
    }

    @Test
    fun finishedRunsFeedStatsAndActivity() {
        val id = "surah:1-114"
        val p = Progress()
            .withFinishedRun(RoundRecord(id, 6, 9, day = 10, mode = GameMode.Mutashabihat.key))
            .withFinishedRun(RoundRecord(id, 3, 5, day = 11, mode = GameMode.Mutashabihat.key))
        val stats = p.runStats(GameMode.Mutashabihat)
        assertEquals(2, stats.played)
        assertEquals(5, stats.average)
        assertEquals(2, p.roundsCompleted)
        assertEquals(2, p.dayStreak)
        assertEquals(3, p.recentRounds.first().score)
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

    @Test
    fun forwardSkewedClockRecovers() {
        // A clock that was wrong (far in the future) once must not freeze today's counters and streak.
        var p = Progress()
            .withCompletedRound(RoundRecord("ayah:1-114", 5, 10, day = 1000))
            .withAnswer(guess, 0, true, day = 1000)
        p = p.withAnswer(guess, 0, true, day = 10)
        assertEquals(1, p.answersToday(10))

        p = p.withCompletedRound(RoundRecord("ayah:1-114", 5, 10, day = 10))
        p = p.withCompletedRound(RoundRecord("ayah:1-114", 5, 10, day = 11))
        assertEquals(2, p.dayStreak)
        assertEquals(2, p.currentDayStreak(11))
    }

    @Test
    fun legacyReciterIndexIsMigrated() = runBlocking {
        suspend fun folder(legacy: String) = ProgressSerializer.readFrom(legacy.byteInputStream()).reciterFolder
        assertEquals("Alafasy_64kbps", folder("""{"reciter":17}"""))
        assertEquals("Ahmed_ibn_Ali_al-Ajamy_64kbps_QuranExplorer.Com", folder("""{"reciter":18}"""))
        assertEquals(null, folder("""{"reciter":99}"""))
    }
}
