package suft.android.ayahiq.ui

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTitle
import suft.android.ayahiq.ui.components.OrnamentDivider
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

/** The load screen is the cold-start intro: it stays up at least this long so the tagline can be read. */
private const val MIN_LOAD_SCREEN_MILLIS = 1_500L

/** Uptime at which this process first showed the load screen (0 = not yet), so a recreated activity does not replay it. */
private var loadScreenStartedAt = 0L

@Composable
fun AyahIqRoot(container: AppContainer, onOpenLeaderboards: (suspend () -> Boolean)?) {
    val progress by container.progressState.collectAsStateWithLifecycle()
    val loaded = progress
    AyahIqTheme(language = loaded?.language ?: AppLanguage.Arabic) {
        PaperBackground {
            val quran by container.quran.collectAsStateWithLifecycle()
            val loadedQuran = quran
            // The settings read takes milliseconds; render nothing until the language is known, to avoid flashing the wrong one.
            if (loaded != null) {
                val remaining = remember {
                    if (loadScreenStartedAt == 0L) loadScreenStartedAt = SystemClock.uptimeMillis()
                    MIN_LOAD_SCREEN_MILLIS - (SystemClock.uptimeMillis() - loadScreenStartedAt)
                }
                var minShown by remember { mutableStateOf(remaining <= 0) }
                LaunchedEffect(Unit) {
                    if (!minShown) {
                        delay(remaining)
                        minShown = true
                    }
                }
                AnimatedContent(
                    targetState = loadedQuran?.takeIf { minShown },
                    contentKey = { it != null },
                    // Fade the load screen out before the menu fades in: both are transparent over the same paper.
                    transitionSpec = { fadeIn(tween(300, delayMillis = 150)) togetherWith fadeOut(tween(150)) },
                    label = "load",
                ) { readyQuran ->
                    if (readyQuran == null) LoadingScreen() else AppNavHost(container, loaded, readyQuran, onOpenLeaderboards)
                }
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

// ---------------------------------------------------------------- transitions

// Material "shared axis X". The screens are transparent over one paper background, so the outgoing screen fades
// out in the first 90 ms before the incoming one fades in (they never overlap); both drift 30 dp along the reading
// direction. Predictive back scrubs the same transition.
private const val SHARED_AXIS_MILLIS = 300
private const val SHARED_AXIS_FADE_OUT_MILLIS = 90
private val SharedAxisOffset = 30.dp

private fun sharedAxisEnter(fromX: Int): EnterTransition =
    slideInHorizontally(tween(SHARED_AXIS_MILLIS, easing = FastOutSlowInEasing)) { fromX } +
        fadeIn(
            tween(
                SHARED_AXIS_MILLIS - SHARED_AXIS_FADE_OUT_MILLIS,
                delayMillis = SHARED_AXIS_FADE_OUT_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        )

private fun sharedAxisExit(toX: Int): ExitTransition =
    slideOutHorizontally(tween(SHARED_AXIS_MILLIS, easing = FastOutSlowInEasing)) { toX } +
        fadeOut(tween(SHARED_AXIS_FADE_OUT_MILLIS, easing = FastOutLinearInEasing))

@Composable
private fun AppNavHost(
    container: AppContainer,
    progress: Progress,
    quran: Quran,
    onOpenLeaderboards: (suspend () -> Boolean)?,
) {
    val nav = rememberNavController()
    val offset = with(LocalDensity.current) { SharedAxisOffset.roundToPx() }
    // Forward = towards the end of the reading direction: new screens come from the right in English, the left in Arabic.
    val forward = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -offset else offset
    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        enterTransition = { sharedAxisEnter(forward) },
        exitTransition = { sharedAxisExit(-forward) },
        popEnterTransition = { sharedAxisEnter(-forward) },
        popExitTransition = { sharedAxisExit(forward) },
        // System back (button or gesture) runs through the predictive-back path, which otherwise falls back to the
        // library's default scale+fade instead of the pop transition used by in-app back.
        predictivePopEnterTransition = { sharedAxisEnter(-forward) },
        predictivePopExitTransition = { sharedAxisExit(forward) },
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
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppTitle()
        Text(
            text = strings.tagline,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        OrnamentDivider()
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp).semantics { contentDescription = strings.loadingQuran },
            strokeWidth = 3.dp,
        )
    }
}
