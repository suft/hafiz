package suft.android.ayahiq

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
import com.google.android.gms.games.PlayGamesSdk
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import suft.android.ayahiq.audio.AudioCache
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.data.ProgressRepository
import suft.android.ayahiq.data.createProgressStore
import suft.android.ayahiq.domain.Mutashabihat
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.domain.QuranXmlParser
import suft.android.ayahiq.games.PlayGames

class AyahIqApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // The SDK's init provider is removed in the manifest; initialize manually once configured.
        if (PlayGames.isEnabled(this)) PlayGamesSdk.initialize(this)
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
            CoroutineExceptionHandler { _, e -> Log.e("AyahIQ", "Background task failed", e) },
    )

    /** Parsed once, off the main thread, at startup (`null` while loading). */
    val quran: StateFlow<Quran?> = flow<Quran?> {
        emit(appContext.assets.open(QURAN_ASSET).let(QuranXmlParser::parse))
    }
        .flowOn(Dispatchers.IO)
        .stateIn(appScope, SharingStarted.Eagerly, null)

    /** Similar-ayah pairs for Hafiz mode, parsed once the Quran is loaded (`null` until then). */
    val mutashabihat: StateFlow<Mutashabihat?> = quran.filterNotNull()
        .map<Quran, Mutashabihat?> { q -> appContext.assets.open(MUTASHABIHAT_ASSET).use { Mutashabihat.parse(it, q) } }
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
    get() = (applicationContext as AyahIqApp).container
