package suft.android.hafiz.data

import androidx.datastore.core.DataStore
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import suft.android.hafiz.domain.AppLanguage

class ProgressStoreTest {

    @get:Rule val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @After fun closeStore() = scope.cancel()

    @Test
    fun unknownEnumKeepsOtherFields() = runBlocking {
        val p = ProgressSerializer.readFrom("""{"totalCorrect":5,"language":"French"}""".byteInputStream())
        assertEquals(5, p.totalCorrect)
        assertEquals(AppLanguage.Arabic, p.language)
    }

    @Test
    fun corruptFileIsKept() = runBlocking {
        val original = """{"surahCorrect":{"x":1}}"""
        val file = File(tmp.root, "progress.json").apply { writeText(original) }
        val loaded = createProgressStore(file, scope).data.first()
        assertEquals(Progress(), loaded)
        val copy = File(file.path + ".corrupt")
        assertTrue("no .corrupt copy was kept", copy.exists())
        assertEquals(original, copy.readText())
    }

    @Test
    fun readErrorsDoNotCrash() = runBlocking {
        val failing = object : DataStore<Progress> {
            override val data: Flow<Progress> = flow { throw IOException("disk") }
            override suspend fun updateData(transform: suspend (t: Progress) -> Progress): Progress = error("unused")
        }
        assertEquals(Progress(), ProgressRepository(failing).progress.first())
    }
}
