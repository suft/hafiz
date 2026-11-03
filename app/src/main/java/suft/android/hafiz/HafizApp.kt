package suft.android.hafiz

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.dataStoreFile
import java.io.File
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import suft.android.hafiz.audio.AudioCache
import suft.android.hafiz.data.Progress
import suft.android.hafiz.data.ProgressRepository
import suft.android.hafiz.data.createProgressStore
import suft.android.hafiz.domain.MushafEdition
import suft.android.hafiz.domain.MushafLayout
import suft.android.hafiz.domain.Mutashabihat
import suft.android.hafiz.domain.Quran
import suft.android.hafiz.domain.QuranXmlParser

class HafizApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency container — the app is small enough not to need a DI framework. */
class AppContainer(
    context: Context,
    progressFile: File = context.applicationContext.dataStoreFile(PROGRESS_FILE),
) {
    val appContext: Context = context.applicationContext

    /** Outlives screens; used for writes that must finish even if the user leaves. */
    val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
            CoroutineExceptionHandler { _, e -> Log.e("Hafiz", "Background task failed", e) },
    )

    /** Parsed once, off the main thread, at startup (`null` while loading). */
    val quran: StateFlow<Quran?> = flow<Quran?> {
        emit(appContext.assets.open(QURAN_ASSET).let(QuranXmlParser::parse))
    }
        .flowOn(Dispatchers.IO)
        .stateIn(appScope, SharingStarted.Eagerly, null)

    /** Similar-ayah pairs used for wrong options (Mutashabihat mode fully, the other modes for one option), parsed once the Quran is loaded (`null` until then). */
    val mutashabihat: StateFlow<Mutashabihat?> = quran.filterNotNull()
        .map<Quran, Mutashabihat?> { q -> appContext.assets.open(MUTASHABIHAT_ASSET).use { Mutashabihat.parse(it, q) } }
        .flowOn(Dispatchers.IO)
        .stateIn(appScope, SharingStarted.Eagerly, null)

    /** Both editions' layouts (Where's the Ayah? questions), parsed once the Quran is loaded (`null` until then). */
    val mushafLayouts: StateFlow<Map<MushafEdition, MushafLayout>?> = quran.filterNotNull()
        .map<Quran, Map<MushafEdition, MushafLayout>?> { q ->
            MushafEdition.entries.associateWith { e -> appContext.assets.open(e.asset).use { MushafLayout.parse(it, q) } }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(appScope, SharingStarted.Eagerly, null)

    val progress = ProgressRepository(createProgressStore(progressFile))

    /** Settings and statistics; `null` until the file has been read (milliseconds). Screens read it from here. */
    val progressState: StateFlow<Progress?> = progress.progress
        .stateIn(appScope, SharingStarted.Eagerly, null)

    val audioCache: AudioCache by lazy { AudioCache(appContext) }

    init {
        // The cache opens a database and scans its directory; do that off the main thread, before the quiz needs it.
        appScope.launch(Dispatchers.IO) { audioCache }
    }

    companion object {
        /** Under `filesDir/datastore/`, where the old `dataStore(fileName = ...)` delegate kept it. */
        const val PROGRESS_FILE = "progress.json"
        private const val QURAN_ASSET = "quran_simple.xml"
        private const val MUTASHABIHAT_ASSET = "mutashabihat.txt"
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as HafizApp).container
