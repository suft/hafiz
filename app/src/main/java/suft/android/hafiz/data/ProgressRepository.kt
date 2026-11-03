package suft.android.hafiz.data

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import suft.android.hafiz.audio.Reciters
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Level
import suft.android.hafiz.domain.MushafEdition
import suft.android.hafiz.domain.RevelationQuiz
import suft.android.hafiz.domain.RunStats
import suft.android.hafiz.domain.Score

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

/** Milliseconds until the next local midnight (23 or 25 h across a daylight-saving change). */
fun millisUntilNextLocalDay(nowMillis: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Long {
    val next = Calendar.getInstance(zone).apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }
    return next.timeInMillis - nowMillis
}

/** Everything the app remembers between launches. Stored as JSON via DataStore. */
@Serializable
data class Progress(
    /** Best round score per [suft.android.hafiz.domain.Level.id]. */
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
    /** [suft.android.hafiz.audio.Reciter.folder] of the chosen reciter; null = the default reciter. */
    val reciterFolder: String? = null,
    val autoPlay: Boolean = true,
    val language: AppLanguage = AppLanguage.Arabic,
    val mushafEdition: MushafEdition = MushafEdition.V1,
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
        val sameDay = lastActiveDay != NO_DAY && day == lastActiveDay
        val run = if (correct) correctRun + 1 else 0
        val base = copy(
            // A clock that went backwards moves the "today" counters back with it, so they never freeze.
            lastActiveDay = day,
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
        val streak = when {
            lastStreakDay == NO_DAY -> 1
            record.day == lastStreakDay -> dayStreak
            record.day == lastStreakDay + 1 -> dayStreak + 1
            // The clock moved back, or a day in the future was stored: keep the streak and continue from today.
            record.day < lastStreakDay -> dayStreak
            else -> 1
        }
        return copy(
            lastStreakDay = record.day,
            dayStreak = streak,
            bestDayStreak = maxOf(bestDayStreak, streak),
            roundsCompleted = roundsCompleted + 1,
            recentRounds = (listOf(record) + recentRounds).take(MAX_RECENT_ROUNDS),
        )
    }

    /**
     * What "Clear statistics" leaves: the best score of every level (as the dialog says) and the settings,
     * nothing else.
     */
    fun afterStatisticsReset(): Progress = Progress(
        bestScores = bestScores,
        runBestScores = runBestScores,
        reciterFolder = reciterFolder,
        autoPlay = autoPlay,
        language = language,
        mushafEdition = mushafEdition,
    )

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

    /** A finished Makki/Madani game: a completed round in the history (day streak, rounds completed) and the best score. */
    fun withRevelationGame(score: Int, total: Int, day: Long): Progress {
        val p = withCompletedRound(RoundRecord(RevelationQuiz.LEVEL_ID, score, total, day, RevelationQuiz.MODE_KEY))
        return if (score > p.best(RevelationQuiz.LEVEL_ID)) p.copy(bestScores = p.bestScores + (RevelationQuiz.LEVEL_ID to score)) else p
    }

    companion object {
        const val NO_DAY = -1L
        const val MAX_RECENT_ROUNDS = 20
    }
}

internal object ProgressSerializer : Serializer<Progress> {
    // coerceInputValues: an enum value this version does not know (a downgrade) falls back to its default
    // instead of discarding the whole file.
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false; coerceInputValues = true }

    override val defaultValue = Progress()

    override suspend fun readFrom(input: InputStream): Progress =
        try {
            val root = json.parseToJsonElement(input.readBytes().decodeToString()).jsonObject
            json.decodeFromJsonElement(Progress.serializer(), migrateLegacyReciter(root))
        } catch (e: IllegalArgumentException) { // SerializationException and "not an object" alike
            throw CorruptionException("Unreadable progress file", e)
        }

    /** Older files stored the reciter as an index into a list that has since changed; they store the folder now. */
    private fun migrateLegacyReciter(root: JsonObject): JsonObject {
        val index = (root["reciter"] as? JsonPrimitive)?.intOrNull ?: return root
        val folder = Reciters.legacyIndexFolders.getOrNull(index)
        return JsonObject(
            root.toMutableMap().apply {
                remove("reciter")
                if (folder != null && "reciterFolder" !in this) put("reciterFolder", JsonPrimitive(folder))
            },
        )
    }

    override suspend fun writeTo(t: Progress, output: OutputStream) {
        output.write(json.encodeToString(Progress.serializer(), t).encodeToByteArray())
    }
}

/** Opens the progress file as a DataStore. Tests pass a temp file and their own [scope]. */
internal fun createProgressStore(
    file: File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): DataStore<Progress> = DataStoreFactory.create(
    serializer = ProgressSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { e ->
        keepCorruptCopy(file, e)
        Progress()
    },
    scope = scope,
    produceFile = { file },
)

/** The handler is about to overwrite the unreadable file with an empty one; keep the bytes for support/recovery. */
private fun keepCorruptCopy(file: File, cause: CorruptionException) {
    Log.e(TAG, "The progress file is unreadable; starting empty", cause)
    try {
        file.copyTo(File(file.path + ".corrupt"), overwrite = true)
    } catch (e: IOException) {
        Log.e(TAG, "Could not keep a copy of the unreadable progress file", e)
    }
}

class ProgressRepository(private val store: DataStore<Progress>) {

    /** An unreadable store (disk error) reads as empty progress rather than crashing the collector. */
    val progress: Flow<Progress> = store.data.catch { e ->
        if (e !is IOException) throw e
        Log.e(TAG, "Could not read the progress file", e)
        emit(Progress())
    }

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

    suspend fun recordRevelationGame(score: Int, total: Int) {
        store.updateData { it.withRevelationGame(score, total, localEpochDay()) }
    }

    suspend fun setReciter(folder: String) {
        store.updateData { it.copy(reciterFolder = folder) }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        store.updateData { it.copy(autoPlay = enabled) }
    }

    suspend fun setLanguage(language: AppLanguage) {
        store.updateData { it.copy(language = language) }
    }

    suspend fun setMushafEdition(edition: MushafEdition) {
        store.updateData { it.copy(mushafEdition = edition) }
    }

    suspend fun resetStatistics() {
        store.updateData { it.afterStatisticsReset() }
    }
}

private const val TAG = "ProgressRepository"
