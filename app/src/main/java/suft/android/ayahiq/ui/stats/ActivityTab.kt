package suft.android.ayahiq.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.data.RoundRecord
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.LocalQuizColors

private class ResolvedRound(val record: RoundRecord, val level: Level, val mode: GameMode)

@Composable
internal fun ActivityTab(progress: Progress, quran: Quran, today: Long, bottomPadding: Dp) {
    val strings = LocalStrings.current
    val rounds = remember(progress.recentRounds) {
        progress.recentRounds.mapNotNull { r ->
            val mode = GameMode.entries.firstOrNull { it.key == r.mode } ?: return@mapNotNull null
            Levels.byId(r.levelId, quran)?.let { ResolvedRound(r, it, mode) }
        }
    }
    // The percentage chart is meaningless for endless games.
    val regular = remember(rounds) { rounds.filter { it.mode == GameMode.Regular } }

    LazyColumn(
        contentPadding = tabContentPadding(bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TileRow(
                    progress.currentDayStreak(today), strings.currentStreak,
                    progress.bestDayStreak, strings.bestStreak,
                )
                TileRow(
                    progress.answersToday(today), strings.questionsToday,
                    progress.roundsCompleted, strings.roundsCompleted,
                )
                TileRow(
                    progress.correctRun, strings.currentRun,
                    progress.bestCorrectRun, strings.bestRun,
                )
            }
        }

        if (rounds.isEmpty()) {
            item {
                Text(
                    strings.noRoundsYet,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                )
            }
            return@LazyColumn
        }

        if (regular.size >= 2) {
            item { SectionHeader(strings.scoreTrend) }
            item { ScoreTrendChart(regular.asReversed()) }
        }

        item { SectionHeader(strings.recentRounds) }
        itemsIndexed(rounds, key = { i, _ -> i }) { _, round ->
            RoundRow(round, today)
        }
    }
}

@Composable
private fun TileRow(value1: Int, label1: String, value2: Int, label2: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(value1, label1, Modifier.weight(1f))
        StatTile(value2, label2, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: Int, label: String, modifier: Modifier) {
    val strings = LocalStrings.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(strings.number(value), style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** [rounds] oldest first. */
@Composable
private fun ScoreTrendChart(rounds: List<ResolvedRound>) {
    val strings = LocalStrings.current
    val quizColors = LocalQuizColors.current
    val scheme = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .semantics { contentDescription = strings.scoreTrend },
        ) {
            val inset = 8.dp.toPx()
            val plotHeight = size.height - 2 * inset
            val guide = 1.dp.toPx()
            for (percent in intArrayOf(0, 50, 100)) {
                val y = inset + plotHeight * (1f - percent / 100f)
                drawLine(scheme.outlineVariant, Offset(0f, y), Offset(size.width, y), strokeWidth = guide)
            }

            val step = (size.width - 2 * inset) / (rounds.size - 1)
            val points = rounds.mapIndexed { i, round ->
                val x = inset + step * i
                val y = inset + plotHeight * (1f - round.record.score * 1f / round.record.total.coerceAtLeast(1))
                Offset(if (rtl) size.width - x else x, y)
            }
            for (i in 1 until points.size) {
                drawLine(
                    scheme.primary, points[i - 1], points[i],
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round,
                )
            }
            points.forEachIndexed { i, p ->
                val round = rounds[i]
                val mastered = round.record.score >= round.level.style.masteryFor(round.record.total)
                drawCircle(if (mastered) quizColors.correct else scheme.secondary, radius = 4.dp.toPx(), center = p)
            }
        }
    }
}

@Composable
private fun RoundRow(round: ResolvedRound, today: Long) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    val record = round.record
    val level = round.level
    val mastered = record.score >= level.style.masteryFor(record.total)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(level.name(strings.language), style = MaterialTheme.typography.titleSmall)
                Text(
                    if (round.mode.endless) {
                        "${strings.gameModeTitle(round.mode)} · ${strings.styleTitle(level.style)} · " +
                            strings.daysAgo((today - record.day).coerceAtLeast(0).toInt())
                    } else {
                        "${strings.styleTitle(level.style)} · ${strings.daysAgo((today - record.day).coerceAtLeast(0).toInt())}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (round.mode.endless) {
                Text(
                    strings.number(record.score),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "${strings.number(record.score)} / ${strings.number(record.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (mastered) quizColors.correct else quizColors.wrong,
                )
            }
        }
    }
}
