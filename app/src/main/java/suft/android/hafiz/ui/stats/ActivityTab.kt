package suft.android.hafiz.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import suft.android.hafiz.data.Progress
import suft.android.hafiz.data.RoundRecord
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.Level
import suft.android.hafiz.domain.Levels
import suft.android.hafiz.domain.Quran
import suft.android.hafiz.domain.RevelationQuiz
import suft.android.hafiz.ui.components.hafizCardColors
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.theme.LocalQuizColors

private sealed interface ResolvedRound {
    val record: RoundRecord
}

private class LevelRound(override val record: RoundRecord, val level: Level, val mode: GameMode) : ResolvedRound

/** A finished Makki-or-Madani game. */
private class RevelationRound(override val record: RoundRecord) : ResolvedRound

@Composable
internal fun ActivityTab(progress: Progress, quran: Quran, today: Long, bottomPadding: Dp) {
    val strings = LocalStrings.current
    val rounds = remember(progress.recentRounds) {
        progress.recentRounds.mapNotNull { r ->
            if (r.mode == RevelationQuiz.MODE_KEY) return@mapNotNull RevelationRound(r)
            val mode = GameMode.entries.firstOrNull { it.key == r.mode } ?: return@mapNotNull null
            Levels.byId(r.levelId, quran)?.let { LevelRound(r, it, mode) }
        }
    }
    // The percentage chart is meaningless for endless games, and practice rounds have their own length.
    val regular = remember(rounds) {
        rounds.filterIsInstance<LevelRound>().filter { it.mode == GameMode.Regular && !it.level.practice }
    }

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
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScoreTrendChart(rounds: List<LevelRound>) {
    val strings = LocalStrings.current
    val quizColors = LocalQuizColors.current
    val scheme = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = scheme.onSurfaceVariant)
    val guides = intArrayOf(0, 50, 100)
    val labels = guides.map { measurer.measure(strings.percent(it), labelStyle) }
    fun percentOf(r: RoundRecord) = r.score * 100 / r.total.coerceAtLeast(1)
    val description = strings.scoreTrendDescription(percentOf(rounds.first().record), percentOf(rounds.last().record))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = hafizCardColors(),
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .semantics { contentDescription = description },
        ) {
            val inset = 8.dp.toPx()
            val plotHeight = size.height - 2 * inset
            val guide = 1.dp.toPx()
            // x positions are measured from the start edge (left in LTR, right in RTL) and mirrored by [at].
            fun at(fromStart: Float) = if (rtl) size.width - fromStart else fromStart
            val labelWidth = labels.maxOf { it.size.width }
            val plotStart = inset + labelWidth + 6.dp.toPx()
            val plotEnd = size.width - inset
            guides.forEachIndexed { g, percent ->
                val y = inset + plotHeight * (1f - percent / 100f)
                drawLine(scheme.outlineVariant, Offset(at(plotStart), y), Offset(at(plotEnd), y), strokeWidth = guide)
                val label = labels[g]
                val left = if (rtl) size.width - inset - label.size.width else inset
                drawText(label, topLeft = Offset(left, y - label.size.height / 2f))
            }

            val step = (plotEnd - plotStart) / (rounds.size - 1)
            val points = rounds.mapIndexed { i, round ->
                val y = inset + plotHeight * (1f - round.record.score * 1f / round.record.total.coerceAtLeast(1))
                Offset(at(plotStart + step * i), y)
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
        FlowRow(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LegendEntry(quizColors.correct, strings.mastered)
            LegendEntry(scheme.secondary, strings.notMastered)
        }
    }
}

@Composable
private fun RoundRow(round: ResolvedRound, today: Long) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    val record = round.record
    val daysAgo = strings.daysAgo((today - record.day).coerceAtLeast(0).toInt())
    val title: String
    val subtitle: String
    val scoreText: String
    val scoreColor: Color
    when (round) {
        is LevelRound -> {
            val level = round.level
            title = level.name(strings.language)
            if (round.mode.endless) {
                subtitle = "${strings.gameModeTitle(round.mode)} · ${strings.styleTitle(level.style)} · $daysAgo"
                scoreText = strings.number(record.score)
                scoreColor = MaterialTheme.colorScheme.primary
            } else {
                subtitle = "${strings.styleTitle(level.style)} · $daysAgo"
                scoreText = "${strings.number(record.score)} / ${strings.number(record.total)}"
                scoreColor = if (record.score >= level.style.masteryFor(record.total)) quizColors.correct else quizColors.wrong
            }
        }
        is RevelationRound -> {
            title = strings.revelationTitle
            subtitle = daysAgo
            scoreText = "${strings.number(record.score)} / ${strings.number(record.total)}"
            scoreColor = if (record.score >= RevelationQuiz.GOOD_SCORE) quizColors.correct else quizColors.wrong
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = hafizCardColors(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(scoreText, style = MaterialTheme.typography.titleMedium, color = scoreColor)
        }
    }
}
