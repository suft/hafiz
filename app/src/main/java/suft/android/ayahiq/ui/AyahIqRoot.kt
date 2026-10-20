package suft.android.ayahiq.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.PaperBackground
import suft.android.ayahiq.ui.home.HomeScreen
import suft.android.ayahiq.ui.levels.LevelsScreen
import suft.android.ayahiq.ui.quiz.QuizScreen
import suft.android.ayahiq.ui.share.ShareCardScreen
import suft.android.ayahiq.ui.stats.StatsScreen
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.AyahIqTheme

// ---------------------------------------------------------------- routes

@Serializable
object HomeRoute

@Serializable
data class LevelsRoute(val mode: String)

@Serializable
data class QuizRoute(val mode: String, val level: Int)

@Serializable
object StatsRoute

/** Global ayah indices (inclusive) to render on the shareable card. */
@Serializable
data class ShareRoute(val firstAyah: Int, val lastAyah: Int)

// ---------------------------------------------------------------- root

@Composable
fun AyahIqRoot(container: AppContainer) {
    val current by container.language.collectAsStateWithLifecycle()
    AyahIqTheme(language = current ?: AppLanguage.Arabic) {
        PaperBackground {
            val quran by container.quran.collectAsStateWithLifecycle()
            val loaded = quran
            when {
                // The settings read takes milliseconds; render nothing to avoid flashing the wrong language.
                current == null -> Unit
                loaded == null -> LoadingScreen()
                else -> AppNavHost(container, loaded)
            }
        }
    }
}

@Composable
private fun AppNavHost(container: AppContainer, quran: Quran) {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                container = container,
                quran = quran,
                onOpenMode = { mode -> nav.navigate(LevelsRoute(mode.key)) },
                onOpenStats = { nav.navigate(StatsRoute) },
            )
        }
        composable<LevelsRoute> { entry ->
            val mode = GameMode.fromKey(entry.toRoute<LevelsRoute>().mode)
            LevelsScreen(
                container = container,
                quran = quran,
                mode = mode,
                onBack = { nav.popBackStack() },
                onPlay = { level -> nav.navigate(QuizRoute(mode.key, level.index)) },
            )
        }
        composable<QuizRoute> { entry ->
            val route = entry.toRoute<QuizRoute>()
            QuizScreen(
                container = container,
                quran = quran,
                mode = GameMode.fromKey(route.mode),
                levelIndex = route.level,
                onBack = { nav.popBackStack() },
                onMainMenu = { nav.popBackStack<HomeRoute>(inclusive = false) },
                onShareAyah = { range -> nav.navigate(ShareRoute(range.first, range.last)) },
            )
        }
        composable<StatsRoute> {
            StatsScreen(container = container, quran = quran, onBack = { nav.popBackStack() })
        }
        composable<ShareRoute> { entry ->
            val route = entry.toRoute<ShareRoute>()
            ShareCardScreen(
                quran = quran,
                ayahs = route.firstAyah..route.lastAyah,
                onBack = { nav.popBackStack() },
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(Modifier.size(40.dp))
        Spacer(Modifier.height(16.dp))
        Text(LocalStrings.current.loadingQuran, style = MaterialTheme.typography.titleMedium)
    }
}
