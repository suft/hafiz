package suft.android.hafiz.ui.styles

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import suft.android.hafiz.ui.components.AppTopBar
import suft.android.hafiz.ui.components.MenuCard
import suft.android.hafiz.ui.i18n.LocalStrings

/** Second step of Play: pick the question style for the chosen [mode]. */
@Composable
fun StylesScreen(
    progress: Progress,
    quran: Quran,
    mode: GameMode,
    onBack: () -> Unit,
    onPick: (GameStyle) -> Unit,
) {
    val strings = LocalStrings.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.gameModeTitle(mode), onBack = onBack) },
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
                Text(strings.gameModeSubtitle(mode), style = MaterialTheme.typography.bodyLarge)
                Text(strings.chooseStyle, style = MaterialTheme.typography.titleMedium)
                GameStyle.entries.forEach { style ->
                    val levels = remember(style) { Levels.forStyle(style, quran) }
                    val badge: String?
                    val badgeLabel: String
                    if (mode.endless) {
                        val best = levels.maxOf { progress.runBest(mode, it.id) }
                        badge = if (best > 0) strings.number(best) else null
                        badgeLabel = strings.best
                    } else {
                        badge = "${strings.number(progress.mastered(levels))}/${strings.number(levels.size)}"
                        badgeLabel = strings.mastered
                    }
                    MenuCard(
                        icon = style.icon,
                        title = strings.styleTitle(style),
                        subtitle = strings.styleSubtitle(style),
                        badge = badge,
                        badgeLabel = badgeLabel,
                        onClick = { onPick(style) },
                    )
                }
            }
        }
    }
}

/** The icon that stands for a question style on the Styles and How to play screens. */
val GameStyle.icon: ImageVector
    get() = if (this == GameStyle.GuessSurah) Icons.Filled.Quiz else Icons.AutoMirrored.Filled.MenuBook
