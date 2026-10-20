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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.data.localEpochDay
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.i18n.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(container: AppContainer, quran: Quran, onBack: () -> Unit, onPractice: (Int) -> Unit) {
    val progress by container.progress.progress.collectAsStateWithLifecycle(initialValue = Progress())
    var confirmReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    val today = remember { localEpochDay() }
    var tab by rememberSaveable { mutableIntStateOf(0) }

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
        val bottomPadding = padding.calculateBottomPadding()
        Column(Modifier.padding(top = padding.calculateTopPadding())) {
            PrimaryTabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                listOf(strings.tabOverview, strings.tabSurahs, strings.tabActivity).forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            when (tab) {
                0 -> OverviewTab(progress, quran, bottomPadding)
                1 -> SurahsTab(progress, quran, onPractice, bottomPadding)
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

@Composable
internal fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}
