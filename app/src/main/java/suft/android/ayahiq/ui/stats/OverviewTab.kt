package suft.android.ayahiq.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.domain.RunStats
import suft.android.ayahiq.domain.Score
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.LocalQuizColors

internal fun tabContentPadding(bottom: Dp) = PaddingValues(
    start = 16.dp,
    end = 16.dp,
    top = 12.dp,
    bottom = bottom + 16.dp,
)

@Composable
internal fun OverviewTab(progress: Progress, quran: Quran, bottomPadding: Dp) {
    val strings = LocalStrings.current
    LazyColumn(
        contentPadding = tabContentPadding(bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { OverallCard(progress.overall) }

        if (progress.overall.total == 0) {
            item {
                Text(
                    strings.statisticsEmpty,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                )
            }
            return@LazyColumn
        }

        items(GameStyle.entries) { style ->
            val levels = remember(style) { Levels.forStyle(style, quran) }
            StyleCard(style, progress, levels)
        }

        items(GameMode.entries.filter { it.endless }) { RunModeCard(it, progress.runStats(it)) }

        val styleAnswers = GameStyle.entries.sumOf { progress.styleScore(it).total }
        if (progress.overall.total > styleAnswers) {
            item {
                Text(
                    strings.styleAccuracySinceUpdate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StyleCard(style: GameStyle, progress: Progress, levels: List<Level>) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    val score = progress.styleScore(style)
    val mastered = progress.mastered(levels)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(strings.styleTitle(style), style = MaterialTheme.typography.titleLarge)
            Text(
                if (score.total > 0) strings.statisticsSummary(score.percent, score.total) else strings.noAnswersYet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(strings.masteredCount(mastered, levels.size), style = MaterialTheme.typography.labelLarge)
            LinearProgressIndicator(
                progress = { mastered.toFloat() / levels.size },
                modifier = Modifier.fillMaxWidth(),
                color = quizColors.correct,
            )
            Text(
                strings.totalPoints(progress.points(levels)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RunModeCard(mode: GameMode, stats: RunStats) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(strings.gameModeTitle(mode), style = MaterialTheme.typography.titleLarge)
            if (stats.played == 0) {
                Text(
                    strings.noGamesYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(strings.bestScore(stats.best), style = MaterialTheme.typography.labelLarge)
                Text(strings.gamesPlayed(stats.played), style = MaterialTheme.typography.labelLarge)
                Text(strings.averageScore(stats.average), style = MaterialTheme.typography.labelLarge)
                if (mode.showsLongestStreak) {
                    Text(strings.longestStreak(stats.bestStreak), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun OverallCard(overall: Score) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (overall.total == 0) 0f else overall.accuracy.toFloat() },
                    modifier = Modifier.size(96.dp),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                )
                Text(
                    if (overall.total == 0) "—" else strings.percent(overall.percent),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(strings.accuracy, style = MaterialTheme.typography.titleLarge)
                Text(
                    strings.correctAnswers(overall.correct),
                    style = MaterialTheme.typography.bodyLarge,
                    color = quizColors.correct,
                )
                Text(
                    strings.wrongAnswers(overall.wrong),
                    style = MaterialTheme.typography.bodyLarge,
                    color = quizColors.wrong,
                )
            }
        }
    }
}
