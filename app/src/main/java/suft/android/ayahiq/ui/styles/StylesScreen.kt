package suft.android.ayahiq.ui.styles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.components.MenuCard
import suft.android.ayahiq.ui.i18n.LocalStrings

/** Second step of the menu: pick the question style for the chosen [mode]. */
@Composable
fun StylesScreen(
    container: AppContainer,
    quran: Quran,
    mode: GameMode,
    onBack: () -> Unit,
    onPick: (GameStyle) -> Unit,
) {
    val progress by container.progress.progress.collectAsStateWithLifecycle(initialValue = Progress())
    val strings = LocalStrings.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.gameModeTitle(mode), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
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
                    icon = if (style == GameStyle.GuessSurah) Icons.Filled.Quiz else Icons.AutoMirrored.Filled.MenuBook,
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
