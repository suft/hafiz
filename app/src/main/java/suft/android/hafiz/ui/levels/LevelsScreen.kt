package suft.android.hafiz.ui.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import suft.android.hafiz.data.Progress
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Level
import suft.android.hafiz.domain.Levels
import suft.android.hafiz.domain.Quran
import suft.android.hafiz.ui.components.AppTopBar
import suft.android.hafiz.ui.components.RoundedProgressBar
import suft.android.hafiz.ui.components.hafizCardColors
import suft.android.hafiz.ui.components.pressScale
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.icons.HafizIcons
import suft.android.hafiz.ui.theme.LocalQuizColors

@Composable
fun LevelsScreen(
    progress: Progress,
    quran: Quran,
    mode: GameMode,
    style: GameStyle,
    onBack: () -> Unit,
    onPlay: (Level) -> Unit,
) {
    val strings = LocalStrings.current
    val levels = remember(style) { Levels.forStyle(style, quran) }
    val total = progress.points(levels)
    val mastered = progress.mastered(levels)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = if (mode.endless) {
                    "${strings.styleTitle(style)} · ${strings.gameModeTitle(mode)}"
                } else {
                    strings.styleTitle(style)
                },
                onBack = onBack,
            )
        },
    ) { padding ->
        val direction = LocalLayoutDirection.current
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                // The top bar is transparent, so the list must start below it (not scroll under it).
                modifier = Modifier.padding(top = padding.calculateTopPadding()).widthIn(max = 640.dp),
                contentPadding = PaddingValues(
                    // Side insets matter in landscape (cutouts, rounded corners).
                    start = padding.calculateStartPadding(direction) + 16.dp,
                    end = padding.calculateEndPadding(direction) + 16.dp,
                    top = 4.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    if (mode.endless) {
                        EndlessSummary(
                            subtitle = strings.gameModeSubtitle(mode),
                            best = levels.maxOf { progress.runBest(mode, it.id) },
                        )
                    } else {
                        Summary(
                            subtitle = strings.styleSubtitle(style),
                            points = total,
                            maxPoints = levels.size * style.questionsPerRound,
                            mastered = mastered,
                            levelCount = levels.size,
                        )
                    }
                }
                items(levels, key = { it.id }) { level ->
                    LevelRow(
                        level = level,
                        best = if (mode.endless) progress.runBest(mode, level.id) else progress.best(level.id),
                        endless = mode.endless,
                        onClick = { onPlay(level) },
                    )
                }
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
            RoundedProgressBar(
                progress = { if (maxPoints == 0) 0f else points.toFloat() / maxPoints },
                height = 8.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
            )
        }
    }
}

@Composable
private fun EndlessSummary(subtitle: String, best: Int) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Text(strings.bestScore(best), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun LevelRow(level: Level, best: Int, endless: Boolean, onClick: () -> Unit) {
    val questions = level.questionsPerRound
    val mastered = !endless && best >= level.masteryScore
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current

    val source = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.fillMaxWidth().testTag("level.${level.index}").pressScale(source),
        colors = hafizCardColors(),
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
                        RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (mastered) {
                    Icon(HafizIcons.CheckCircle, contentDescription = strings.mastered, tint = quizColors.correct)
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
                if (!endless) {
                    Spacer(Modifier.height(6.dp))
                    RoundedProgressBar(
                        progress = { best.toFloat() / questions },
                        color = if (mastered) quizColors.correct else MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Text(
                if (!endless) {
                    "${strings.number(best)}/${strings.number(questions)}"
                } else if (best > 0) {
                    strings.number(best)
                } else {
                    "—"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(HafizIcons.Play, contentDescription = strings.start, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
