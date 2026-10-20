package suft.android.ayahiq.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.Score

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

    suspend fun recordAnswer(surahIndex: Int, correct: Boolean) {
        store.updateData { p ->
            if (correct) {
                p.copy(
                    totalCorrect = p.totalCorrect + 1,
                    surahCorrect = p.surahCorrect + (surahIndex to (p.surahCorrect[surahIndex] ?: 0) + 1),
                )
            } else {
                p.copy(
                    totalWrong = p.totalWrong + 1,
                    surahWrong = p.surahWrong + (surahIndex to (p.surahWrong[surahIndex] ?: 0) + 1),
                )
            }
        }
    }

    /** Keeps the higher of the stored and the new score. */
    suspend fun recordLevelScore(levelId: String, score: Int) {
        store.updateData { p ->
            if (score <= p.best(levelId)) p else p.copy(bestScores = p.bestScores + (levelId to score))
        }
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
            it.copy(totalCorrect = 0, totalWrong = 0, surahCorrect = emptyMap(), surahWrong = emptyMap())
        }
    }

    suspend fun resetLevelScores() {
        store.updateData { it.copy(bestScores = emptyMap()) }
    }
}
