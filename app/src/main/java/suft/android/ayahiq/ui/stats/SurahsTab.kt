package suft.android.ayahiq.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.domain.Score
import suft.android.ayahiq.domain.Stats
import suft.android.ayahiq.domain.SurahTier
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.LocalQuizColors

@Composable
internal fun SurahsTab(progress: Progress, quran: Quran, onPractice: (Int) -> Unit, bottomPadding: Dp) {
    val strings = LocalStrings.current
    val scores = remember(progress) { progress.surahScores() }
    val practisedCount = scores.count { it.value.total > 0 }
    val ranked = remember(progress) { Stats.weakestFirst(scores) }
    val strong = remember(progress) { Stats.strongestFirst(scores) }

    LazyColumn(
        contentPadding = tabContentPadding(bottomPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SectionHeader(strings.surahMap) }
        item {
            Text(
                strings.surahsPractised(practisedCount, Quran.SURAH_COUNT),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { SurahMap(scores, quran, onPractice) }

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

        if (ranked.isEmpty()) {
            item {
                Text(
                    strings.noMistakes,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                )
            }
        } else {
            item { SectionHeader(strings.surahsToReview) }
            itemsIndexed(ranked, key = { _, entry -> "weak-${entry.surahIndex}" }) { i, item ->
                SurahRow(
                    rank = i + 1,
                    name = quran.surahs[item.surahIndex].displayName(strings.language),
                    score = item.score,
                    highlight = i < 3,
                    onPractice = { onPractice(item.surahIndex) },
                )
            }
        }

        if (strong.isNotEmpty()) {
            item { SectionHeader(strings.strongestSurahs) }
            itemsIndexed(strong, key = { _, entry -> "strong-${entry.surahIndex}" }) { i, item ->
                SurahRow(
                    rank = i + 1,
                    name = quran.surahs[item.surahIndex].displayName(strings.language),
                    score = item.score,
                    highlight = false,
                    onPractice = null,
                )
            }
        }
    }
}

private const val MAP_COLUMNS = 10
private val CellShape = RoundedCornerShape(6.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurahMap(scores: Map<Int, Score>, quran: Quran, onPractice: (Int) -> Unit) {
    val strings = LocalStrings.current
    val quizColors = LocalQuizColors.current
    val scheme = MaterialTheme.colorScheme
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }

    fun tierColor(tier: SurahTier): Color = when (tier) {
        SurahTier.NotPractised -> scheme.surfaceVariant.copy(alpha = 0.6f)
        SurahTier.Weak -> quizColors.wrongContainer
        SurahTier.Fair -> scheme.secondaryContainer
        SurahTier.Strong -> quizColors.correctContainer
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in (0 until Quran.SURAH_COUNT).chunked(MAP_COLUMNS)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in row) {
                        val name = quran.surahs[i].displayName(strings.language)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CellShape)
                                .background(tierColor(Stats.tier(scores[i])))
                                .then(if (selected == i) Modifier.border(2.dp, scheme.primary, CellShape) else Modifier)
                                .clickable { selected = i }
                                .semantics { contentDescription = name },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(strings.number(i + 1), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    // Keep the last row's cells the same size as the full rows.
                    repeat(MAP_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LegendEntry(tierColor(SurahTier.NotPractised), strings.tierNotPractised)
            LegendEntry(tierColor(SurahTier.Weak), strings.tierWeak)
            LegendEntry(tierColor(SurahTier.Fair), strings.tierFair)
            LegendEntry(tierColor(SurahTier.Strong), strings.tierStrong)
        }

        selected?.let { index ->
            val score = scores[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerLowest.copy(alpha = 0.92f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            strings.rankedSurah(index + 1, quran.surahs[index].displayName(strings.language)),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            if (score != null && score.total > 0) {
                                strings.surahCounts(score.correct, score.wrong)
                            } else {
                                strings.notPractisedYet
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { onPractice(index) }) { Text(strings.practice) }
                }
            }
        }
    }
}

@Composable
private fun LegendEntry(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(12.dp).clip(CellShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SurahRow(rank: Int, name: String, score: Score, highlight: Boolean, onPractice: (() -> Unit)?) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) {
                quizColors.wrongContainer.copy(alpha = 0.9f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f)
            },
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    strings.rankedSurah(rank, name),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    strings.percent(score.percent),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (score.percent >= 50) quizColors.correct else quizColors.wrong,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { score.accuracy.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = quizColors.correct,
                trackColor = quizColors.wrong.copy(alpha = 0.35f),
            )
            Spacer(Modifier.height(4.dp))
            val counts = strings.surahCounts(score.correct, score.wrong)
            if (onPractice == null) {
                Text(
                    counts,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        counts,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onPractice) { Text(strings.practice) }
                }
            }
        }
    }
}
