package suft.android.hafiz

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.launch
import suft.android.hafiz.ui.HafizRoot
import suft.android.hafiz.ui.i18n.SystemAppLanguage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        syncLanguageFromSystem()
        setContent { HafizRoot(appContainer) }
    }

    // The manifest declares configChanges="locale|layoutDirection": a per-app language change from system settings
    // arrives here instead of recreating the activity. The UI takes its strings and direction from AppLanguage.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        syncLanguageFromSystem()
    }

    /** A language set in system settings wins; "System default" (or Android below 13) leaves the stored one. */
    private fun syncLanguageFromSystem() {
        val language = SystemAppLanguage.get(this) ?: return
        appContainer.appScope.launch { appContainer.progress.setLanguage(language) }
    }
}
