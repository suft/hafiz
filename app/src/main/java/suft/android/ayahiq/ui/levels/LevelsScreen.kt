package suft.android.ayahiq.ui.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.LocalQuizColors

@Composable
fun LevelsScreen(
    container: AppContainer,
    quran: Quran,
    mode: GameMode,
    onBack: () -> Unit,
    onPlay: (Level) -> Unit,
) {
    val progress by container.progress.progress.collectAsStateWithLifecycle(initialValue = Progress())
    val strings = LocalStrings.current
    val levels = remember(mode) { Levels.forMode(mode, quran) }
    val total = progress.points(levels)
    val mastered = progress.mastered(levels)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.modeTitle(mode), onBack = onBack) },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Summary(
                    subtitle = strings.modeSubtitle(mode),
                    points = total,
                    maxPoints = levels.size * mode.questionsPerRound,
                    mastered = mastered,
                    levelCount = levels.size,
                )
            }
            items(levels, key = { it.id }) { level ->
                LevelRow(
                    level = level,
                    best = progress.best(level.id),
                    onClick = { onPlay(level) },
                )
            }
        }
    }
}

@Composable
private fun Summary(subtitle: String, points: Int, maxPoints: Int, mastered: Int, levelCount: Int) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    strings.totalPoints(points),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    strings.masteredCount(mastered, levelCount),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (maxPoints == 0) 0f else points.toFloat() / maxPoints },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LevelRow(level: Level, best: Int, onClick: () -> Unit) {
    val questions = level.questionsPerRound
    val mastered = best >= level.masteryScore
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (mastered) quizColors.correctContainer else MaterialTheme.colorScheme.secondaryContainer,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (mastered) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = strings.mastered, tint = quizColors.correct)
                } else {
                    Text(
                        strings.number(level.index + 1),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(level.name(strings.language), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { best.toFloat() / questions },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (mastered) quizColors.correct else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Text(
                "${strings.number(best)}/${strings.number(questions)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(Icons.Filled.PlayArrow, contentDescription = strings.start, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
