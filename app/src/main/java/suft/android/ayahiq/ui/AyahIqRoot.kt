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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.PaperBackground
import suft.android.ayahiq.ui.home.HomeScreen
import suft.android.ayahiq.ui.levels.LevelsScreen
import suft.android.ayahiq.ui.quiz.QuizScreen
import suft.android.ayahiq.ui.share.ShareCardScreen
import suft.android.ayahiq.ui.stats.StatsScreen
import suft.android.ayahiq.ui.styles.StylesScreen
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.AyahIqTheme

// ---------------------------------------------------------------- routes

@Serializable
object HomeRoute

/** Question-style picker for one [GameMode.key]. */
@Serializable
data class StylesRoute(val mode: String)

@Serializable
data class LevelsRoute(val mode: String, val style: String)

@Serializable
data class QuizRoute(val mode: String, val style: String, val level: Int)

/** Next-ayah review round for one 0-based surah, opened from Statistics. */
@Serializable
data class PracticeRoute(val surah: Int)

@Serializable
object StatsRoute

/** Global ayah indices (inclusive) to render on the shareable card. */
@Serializable
data class ShareRoute(val firstAyah: Int, val lastAyah: Int)

// ---------------------------------------------------------------- root

@Composable
fun AyahIqRoot(container: AppContainer, onOpenLeaderboards: (suspend () -> Boolean)?) {
    val progress by container.progressState.collectAsStateWithLifecycle()
    val loaded = progress
    AyahIqTheme(language = loaded?.language ?: AppLanguage.Arabic) {
        PaperBackground {
            val quran by container.quran.collectAsStateWithLifecycle()
            val loadedQuran = quran
            when {
                // The settings read takes milliseconds; render nothing to avoid flashing the wrong language.
                loaded == null -> Unit
                loadedQuran == null -> LoadingScreen()
                else -> AppNavHost(container, loaded, loadedQuran, onOpenLeaderboards)
            }
        }
    }
}

// A tap that lands while a screen is animating out (or a second tap on the same control) would navigate again
// from a screen that is already leaving: two pops empty the stack, two pushes stack a duplicate. Only the entry
// that is fully resumed may navigate.
private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavController.navigateFrom(entry: NavBackStackEntry, route: Any) {
    if (entry.isResumed()) navigate(route) { launchSingleTop = true }
}

private fun NavController.popFrom(entry: NavBackStackEntry) {
    if (entry.isResumed()) popBackStack()
}

@Composable
private fun AppNavHost(
    container: AppContainer,
    progress: Progress,
    quran: Quran,
    onOpenLeaderboards: (suspend () -> Boolean)?,
) {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable<HomeRoute> { entry ->
            HomeScreen(
                container = container,
                progress = progress,
                quran = quran,
                onOpenMode = { mode -> nav.navigateFrom(entry, StylesRoute(mode.key)) },
                onOpenStats = { nav.navigateFrom(entry, StatsRoute) },
                onOpenLeaderboards = onOpenLeaderboards,
            )
        }
        composable<StylesRoute> { entry ->
            val mode = GameMode.fromKey(entry.toRoute<StylesRoute>().mode)
            StylesScreen(
                progress = progress,
                quran = quran,
                mode = mode,
                onBack = { nav.popFrom(entry) },
                onPick = { style -> nav.navigateFrom(entry, LevelsRoute(mode.key, style.key)) },
            )
        }
        composable<LevelsRoute> { entry ->
            val route = entry.toRoute<LevelsRoute>()
            val mode = GameMode.fromKey(route.mode)
            val style = GameStyle.fromKey(route.style)
            LevelsScreen(
                progress = progress,
                quran = quran,
                mode = mode,
                style = style,
                onBack = { nav.popFrom(entry) },
                onPlay = { level -> nav.navigateFrom(entry, QuizRoute(mode.key, style.key, level.index)) },
            )
        }
        composable<QuizRoute> { entry ->
            val route = entry.toRoute<QuizRoute>()
            val level = remember(route) { Levels.forStyle(GameStyle.fromKey(route.style), quran)[route.level] }
            QuizScreen(
                container = container,
                quran = quran,
                level = level,
                mode = GameMode.fromKey(route.mode),
                onBack = { nav.popFrom(entry) },
                onMainMenu = { if (entry.isResumed()) nav.popBackStack<HomeRoute>(inclusive = false) },
                onShareAyah = { range -> nav.navigateFrom(entry, ShareRoute(range.first, range.last)) },
            )
        }
        composable<PracticeRoute> { entry ->
            val route = entry.toRoute<PracticeRoute>()
            val level = remember(route.surah) { Levels.surahPractice(route.surah, quran) }
            QuizScreen(
                container = container,
                quran = quran,
                level = level,
                mode = GameMode.Regular,
                onBack = { nav.popFrom(entry) },
                onMainMenu = { if (entry.isResumed()) nav.popBackStack<HomeRoute>(inclusive = false) },
                onShareAyah = { range -> nav.navigateFrom(entry, ShareRoute(range.first, range.last)) },
            )
        }
        composable<StatsRoute> { entry ->
            StatsScreen(
                container = container,
                progress = progress,
                quran = quran,
                onBack = { nav.popFrom(entry) },
                onPractice = { surah -> nav.navigateFrom(entry, PracticeRoute(surah)) },
            )
        }
        composable<ShareRoute> { entry ->
            val route = entry.toRoute<ShareRoute>()
            ShareCardScreen(
                quran = quran,
                ayahs = route.firstAyah..route.lastAyah,
                onBack = { nav.popFrom(entry) },
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
