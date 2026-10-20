package suft.android.ayahiq.ui.stats

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.domain.Score
import suft.android.ayahiq.domain.Stats
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.LocalQuizColors

@Composable
fun StatsScreen(container: AppContainer, quran: Quran, onBack: () -> Unit) {
    val progress by container.progress.progress.collectAsStateWithLifecycle(initialValue = Progress())
    val ranked = remember(progress) { Stats.weakestFirst(progress.surahScores()) }
    var confirmReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = strings.statistics,
                onBack = onBack,
                actions = {
                    if (progress.overall.total > 0) {
                        IconButton(onClick = { confirmReset = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = strings.clearStatistics)
                        }
                    }
                },
            )
        },
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
            } else if (ranked.isEmpty()) {
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
                item {
                    Text(
                        strings.surahsToReview,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                itemsIndexed(ranked, key = { _, entry -> entry.surahIndex }) { i, item ->
                    SurahRow(
                        rank = i + 1,
                        name = quran.surahs[item.surahIndex].displayName(strings.language),
                        score = item.score,
                        highlight = i < 3,
                    )
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(strings.clearStatisticsQuestion) },
            text = { Text(strings.clearStatisticsBody) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    scope.launch { container.progress.resetStatistics() }
                }) { Text(strings.clear) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(strings.cancel) } },
        )
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

@Composable
private fun SurahRow(rank: Int, name: String, score: Score, highlight: Boolean) {
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
            Text(
                strings.surahCounts(score.correct, score.wrong),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
