package suft.android.ayahiq.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.RunStats
import suft.android.ayahiq.domain.Score

/** One completed round, for the statistics history. [day] is a local epoch day ([localEpochDay]). */
@Serializable
data class RoundRecord(
    val levelId: String,
    val score: Int,
    val total: Int,
    val day: Long,
    /** [GameMode.key]; records from before game modes existed are Regular. */
    val mode: String = GameMode.Regular.key,
)

/** Days since 1970-01-01 in the device's time zone (java.time needs API 26; minSdk is 24). */
fun localEpochDay(nowMillis: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Long =
    Math.floorDiv(nowMillis + zone.getOffset(nowMillis), 86_400_000L)

/** Milliseconds until the next local midnight, in 1..86_400_000. */
fun millisUntilNextLocalDay(nowMillis: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Long =
    86_400_000L - Math.floorMod(nowMillis + zone.getOffset(nowMillis), 86_400_000L)

/** Everything the app remembers between launches. Stored as JSON via DataStore. */
@Serializable
data class Progress(
    /** Best round score per [suft.android.ayahiq.domain.Level.id]. */
    val bestScores: Map<String, Int> = emptyMap(),
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    /** Per-surah answer counts, keyed by 0-based surah index. */
    val surahCorrect: Map<Int, Int> = emptyMap(),
    val surahWrong: Map<Int, Int> = emptyMap(),
    /** Answer counts per [GameStyle.key]; only counted since this field was added. */
    @SerialName("modeCorrect") val styleCorrect: Map<String, Int> = emptyMap(),
    @SerialName("modeWrong") val styleWrong: Map<String, Int> = emptyMap(),
    /** Local epoch day of the latest answer, or [NO_DAY]. */
    val lastActiveDay: Long = NO_DAY,
    /** Local epoch day of the latest finished round, or [NO_DAY]. */
    val lastStreakDay: Long = NO_DAY,
    /** Consecutive days with a finished round, ending at [lastStreakDay]. */
    val dayStreak: Int = 0,
    val bestDayStreak: Int = 0,
    /** Answers given on [lastActiveDay]. */
    val answersOnLastActiveDay: Int = 0,
    /** Consecutive correct answers (across rounds) and the best such run. */
    val correctRun: Int = 0,
    val bestCorrectRun: Int = 0,
    val roundsCompleted: Int = 0,
    /** Newest first, at most [MAX_RECENT_ROUNDS]. */
    val recentRounds: List<RoundRecord> = emptyList(),
    /** Best endless-game score: [GameMode.key] → [Level.id] → score. */
    val runBestScores: Map<String, Map<String, Int>> = emptyMap(),
    /** Finished endless games per [GameMode.key]. */
    val runsPlayed: Map<String, Int> = emptyMap(),
    /** Sum of finished endless-game scores per [GameMode.key]. */
    val runPoints: Map<String, Int> = emptyMap(),
    /** Longest correct streak inside one endless game, per [GameMode.key]. */
    val runBestStreak: Map<String, Int> = emptyMap(),
    val reciter: Int = 0,
    val autoPlay: Boolean = true,
    val language: AppLanguage = AppLanguage.Arabic,
) {
    val overall: Score get() = Score(totalCorrect, totalWrong)

    fun surahScores(): Map<Int, Score> =
        (surahCorrect.keys + surahWrong.keys).associateWith {
            Score(surahCorrect[it] ?: 0, surahWrong[it] ?: 0)
        }

    fun best(levelId: String): Int = bestScores[levelId] ?: 0

    fun runBest(mode: GameMode, levelId: String): Int = runBestScores[mode.key]?.get(levelId) ?: 0

    fun runStats(mode: GameMode): RunStats = RunStats(
        played = runsPlayed[mode.key] ?: 0,
        totalScore = runPoints[mode.key] ?: 0,
        best = runBestScores[mode.key]?.values?.maxOrNull() ?: 0,
        bestStreak = runBestStreak[mode.key] ?: 0,
    )

    fun styleScore(style: GameStyle): Score = Score(styleCorrect[style.key] ?: 0, styleWrong[style.key] ?: 0)

    fun mastered(levels: List<Level>): Int = levels.count { best(it.id) >= it.masteryScore }

    fun points(levels: List<Level>): Int = levels.sumOf { best(it.id) }

    /** The streak still counts if the user played today or yesterday. */
    fun currentDayStreak(today: Long): Int =
        if (lastStreakDay != NO_DAY && today - lastStreakDay <= 1) dayStreak else 0

    fun answersToday(today: Long): Int = if (lastActiveDay == today) answersOnLastActiveDay else 0

    fun withAnswer(style: GameStyle, surahIndex: Int, correct: Boolean, day: Long): Progress {
        // A clock moved backwards (day < lastActiveDay) counts as the same day.
        val sameDay = lastActiveDay != NO_DAY && day <= lastActiveDay
        val run = if (correct) correctRun + 1 else 0
        val base = copy(
            lastActiveDay = if (sameDay) lastActiveDay else day,
            answersOnLastActiveDay = if (sameDay) answersOnLastActiveDay + 1 else 1,
            correctRun = run,
            bestCorrectRun = maxOf(bestCorrectRun, run),
        )
        return if (correct) {
            base.copy(
                totalCorrect = totalCorrect + 1,
                surahCorrect = surahCorrect + (surahIndex to (surahCorrect[surahIndex] ?: 0) + 1),
                styleCorrect = styleCorrect + (style.key to (styleCorrect[style.key] ?: 0) + 1),
            )
        } else {
            base.copy(
                totalWrong = totalWrong + 1,
                surahWrong = surahWrong + (surahIndex to (surahWrong[surahIndex] ?: 0) + 1),
                styleWrong = styleWrong + (style.key to (styleWrong[style.key] ?: 0) + 1),
            )
        }
    }

    fun withCompletedRound(record: RoundRecord): Progress {
        // A clock moved backwards (day < lastStreakDay) counts as the same day.
        val sameDay = lastStreakDay != NO_DAY && record.day <= lastStreakDay
        val streak = when {
            sameDay -> dayStreak
            lastStreakDay != NO_DAY && record.day == lastStreakDay + 1 -> dayStreak + 1
            else -> 1
        }
        return copy(
            lastStreakDay = if (sameDay) lastStreakDay else record.day,
            dayStreak = streak,
            bestDayStreak = maxOf(bestDayStreak, streak),
            roundsCompleted = roundsCompleted + 1,
            recentRounds = (listOf(record) + recentRounds).take(MAX_RECENT_ROUNDS),
        )
    }

    /** Keeps the higher of the stored and the new value, for the level's best score and the mode's best streak. */
    fun withRunBest(mode: GameMode, levelId: String, score: Int, streak: Int): Progress {
        val levels = runBestScores[mode.key].orEmpty()
        val newScores = if (score > (levels[levelId] ?: 0)) {
            runBestScores + (mode.key to (levels + (levelId to score)))
        } else {
            runBestScores
        }
        val newStreaks = if (streak > (runBestStreak[mode.key] ?: 0)) {
            runBestStreak + (mode.key to streak)
        } else {
            runBestStreak
        }
        return copy(runBestScores = newScores, runBestStreak = newStreaks)
    }

    /** An endless game is over: counts toward the day streak, rounds completed and the activity history too. */
    fun withFinishedRun(record: RoundRecord): Progress = withCompletedRound(record).copy(
        runsPlayed = runsPlayed + (record.mode to (runsPlayed[record.mode] ?: 0) + 1),
        runPoints = runPoints + (record.mode to (runPoints[record.mode] ?: 0) + record.score),
    )

    companion object {
        const val NO_DAY = -1L
        const val MAX_RECENT_ROUNDS = 20
    }
}

private object ProgressSerializer : Serializer<Progress> {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    override val defaultValue = Progress()

    override suspend fun readFrom(input: InputStream): Progress =
        try {
            json.decodeFromString(Progress.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Unreadable progress file", e)
        }

    override suspend fun writeTo(t: Progress, output: OutputStream) {
        output.write(json.encodeToString(Progress.serializer(), t).encodeToByteArray())
    }
}

private val Context.progressStore: DataStore<Progress> by dataStore(
    fileName = "progress.json",
    serializer = ProgressSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { Progress() },
)

class ProgressRepository(context: Context) {
    private val store = context.applicationContext.progressStore

    val progress: Flow<Progress> = store.data

    suspend fun recordAnswer(style: GameStyle, surahIndex: Int, correct: Boolean) {
        store.updateData { it.withAnswer(style, surahIndex, correct, localEpochDay()) }
    }

    suspend fun recordCompletedRound(levelId: String, score: Int, total: Int) {
        store.updateData { it.withCompletedRound(RoundRecord(levelId, score, total, localEpochDay())) }
    }

    /** Keeps the higher of the stored and the new score. */
    suspend fun recordLevelScore(levelId: String, score: Int) {
        store.updateData { p ->
            if (score <= p.best(levelId)) p else p.copy(bestScores = p.bestScores + (levelId to score))
        }
    }

    suspend fun recordRunBest(mode: GameMode, levelId: String, score: Int, streak: Int) {
        store.updateData { it.withRunBest(mode, levelId, score, streak) }
    }

    /** [answered] is stored as the record's total. */
    suspend fun recordFinishedRun(mode: GameMode, levelId: String, score: Int, answered: Int) {
        store.updateData { it.withFinishedRun(RoundRecord(levelId, score, answered, localEpochDay(), mode.key)) }
    }

    suspend fun setReciter(index: Int) {
        store.updateData { it.copy(reciter = index) }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        store.updateData { it.copy(autoPlay = enabled) }
    }

    suspend fun setLanguage(language: AppLanguage) {
        store.updateData { it.copy(language = language) }
    }

    suspend fun resetStatistics() {
        store.updateData {
            Progress(
                bestScores = it.bestScores,
                runBestScores = it.runBestScores,
                reciter = it.reciter,
                autoPlay = it.autoPlay,
                language = it.language,
            )
        }
    }

    suspend fun resetLevelScores() {
        store.updateData { it.copy(bestScores = emptyMap(), runBestScores = emptyMap()) }
    }
}
