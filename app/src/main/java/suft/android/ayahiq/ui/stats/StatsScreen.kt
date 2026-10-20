package suft.android.ayahiq.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.data.localEpochDay
import suft.android.ayahiq.data.millisUntilNextLocalDay
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.i18n.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(container: AppContainer, progress: Progress, quran: Quran, onBack: () -> Unit, onPractice: (Int) -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    val today = rememberToday()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = strings.statistics,
                onBack = onBack,
                actions = {
                    if (progress.afterStatisticsReset() != progress) {
                        IconButton(onClick = { confirmReset = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = strings.clearStatistics)
                        }
                    }
                },
            )
        },
    ) { padding ->
        val bottomPadding = padding.calculateBottomPadding()
        Column(Modifier.padding(top = padding.calculateTopPadding())) {
            PrimaryTabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                listOf(strings.tabOverview, strings.tabSurahs, strings.tabActivity).forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> OverviewTab(progress, quran, bottomPadding)
                1 -> SurahsTab(progress, quran, selectedSurah, { selectedSurah = it }, onPractice, bottomPadding)
                else -> ActivityTab(progress, quran, today, bottomPadding)
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

/** Local epoch day that updates at midnight and whenever the screen resumes. */
@Composable
private fun rememberToday(): Long {
    var today by remember { mutableLongStateOf(localEpochDay()) }
    LifecycleResumeEffect(Unit) {
        today = localEpochDay()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(millisUntilNextLocalDay() + 1_000)
            today = localEpochDay()
        }
    }
    return today
}

@Composable
internal fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}
