package suft.android.hafiz.ui.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import suft.android.hafiz.data.Progress
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Levels
import suft.android.hafiz.domain.Quran
import suft.android.hafiz.domain.RevelationQuiz
import suft.android.hafiz.ui.components.AppTopBar
import suft.android.hafiz.ui.components.MenuCard
import suft.android.hafiz.ui.i18n.LocalStrings

/** First step of Play: pick the game mode. */
@Composable
fun ModesScreen(
    progress: Progress,
    quran: Quran,
    onBack: () -> Unit,
    onPick: (GameMode) -> Unit,
    onPickRevelation: () -> Unit,
) {
    val strings = LocalStrings.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.gameModes, onBack = onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val styleLevels = remember { GameStyle.entries.map { Levels.forStyle(it, quran) } }
                GameMode.entries.forEach { mode ->
                    val badge: String?
                    val badgeLabel: String
                    if (mode.endless) {
                        val best = progress.runStats(mode).best
                        badge = if (best > 0) strings.number(best) else null
                        badgeLabel = strings.best
                    } else {
                        badge = "${strings.number(styleLevels.sumOf { progress.mastered(it) })}/${strings.number(styleLevels.sumOf { it.size })}"
                        badgeLabel = strings.mastered
                    }
                    MenuCard(
                        icon = mode.icon,
                        title = strings.gameModeTitle(mode),
                        subtitle = strings.gameModeSubtitle(mode),
                        badge = badge,
                        badgeLabel = badgeLabel,
                        onClick = { onPick(mode) },
                    )
                }
                val revelationBest = progress.best(RevelationQuiz.LEVEL_ID)
                MenuCard(
                    icon = RevelationIcon,
                    title = strings.revelationTitle,
                    subtitle = strings.revelationSubtitle,
                    badge = revelationBest.takeIf { it > 0 }
                        ?.let { "${strings.number(it)}/${strings.number(RevelationQuiz.QUESTIONS)}" },
                    badgeLabel = strings.best,
                    onClick = onPickRevelation,
                )
            }
        }
    }
}

/** The icon that stands for a game mode on the Play and How to play screens. */
val GameMode.icon: ImageVector
    get() = when (this) {
        GameMode.Regular -> Icons.Filled.Flag
        GameMode.Survival -> Icons.Filled.LocalFireDepartment
        GameMode.Mutashabihat -> Icons.Filled.Psychology
    }

/** The icon of the Makki-or-Madani game on the Play and How to play screens. */
val RevelationIcon: ImageVector get() = Icons.Filled.Mosque
