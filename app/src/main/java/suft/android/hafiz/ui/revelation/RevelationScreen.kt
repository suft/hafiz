package suft.android.hafiz.ui.revelation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import suft.android.hafiz.AppContainer
import suft.android.hafiz.domain.Quran
import suft.android.hafiz.domain.RevelationPlace
import suft.android.hafiz.domain.RevelationQuiz
import suft.android.hafiz.ui.components.AppTopBar
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.quiz.OptionButton
import suft.android.hafiz.ui.quiz.OptionState
import suft.android.hafiz.ui.quiz.rememberTouchExplorationEnabled
import suft.android.hafiz.ui.theme.LocalQuizColors
import suft.android.hafiz.ui.theme.NaskhFont

/** The Makki-or-Madani game: five surahs, answer where each was revealed. */
@Composable
fun RevelationScreen(container: AppContainer, quran: Quran, onBack: () -> Unit, onMainMenu: () -> Unit) {
    val vm: RevelationViewModel = viewModel { RevelationViewModel(container) }
    val state by vm.state.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val touchExploration = rememberTouchExplorationEnabled()
    LaunchedEffect(touchExploration) { vm.setAutoAdvance(!touchExploration) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.revelationTitle, onBack = onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // The "next" TextButton's 48dp touch target must not grow the row when it appears.
                Row(modifier = Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    val label = strings.questionProgress(state.current + 1, state.total)
                    val correct = state.selected == state.question.place
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                if (state.answered) {
                                    contentDescription = "$label, ${if (correct) strings.correctAnswer else strings.wrongAnswer}"
                                }
                            },
                    )
                    if (state.answered && !state.finished) {
                        TextButton(onClick = vm::advanceNow) {
                            Text(strings.next)
                            Spacer(Modifier.width(4.dp))
                            // Auto-mirrored: points left (forward) in RTL.
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            strings.points(state.score),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (state.current + if (state.answered) 1 else 0).toFloat() / state.total },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, LocalQuizColors.current.gold.copy(alpha = 0.7f)),
                    shadowElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            strings.revelationQuestion,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            strings.revelationSurah(quran.surahs[state.question.surahIndex].displayName(strings.language)),
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = NaskhFont),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                listOf(RevelationPlace.Makkah to strings.makki, RevelationPlace.Madinah to strings.madani)
                    .forEachIndexed { i, (place, text) ->
                        if (i > 0) Spacer(Modifier.height(8.dp))
                        OptionButton(
                            text = text,
                            quranText = false,
                            state = when {
                                state.selected == null -> OptionState.Open
                                place == state.question.place -> OptionState.Correct
                                place == state.selected -> OptionState.Wrong
                                else -> OptionState.Dimmed
                            },
                            onClick = { vm.select(place) },
                        )
                    }
            }
        }
    }

    if (state.finished) {
        val message = when {
            state.score == state.total -> strings.perfectScore
            state.score >= RevelationQuiz.GOOD_SCORE -> strings.revelationGood
            else -> strings.revelationNeedsReview
        }
        AlertDialog(
            // Back and the system gesture close the dialog like the title-bar back arrow; a tap outside must not.
            onDismissRequest = onBack,
            properties = DialogProperties(dismissOnClickOutside = false),
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = LocalQuizColors.current.gold) },
            title = { Text(strings.resultTitle, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        strings.revelationTitle,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${strings.number(state.score)} / ${strings.number(state.total)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    if (state.isNewRecord && state.previousBest > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            strings.newRecord(state.previousBest),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = vm::restart, modifier = Modifier.fillMaxWidth()) { Text(strings.retry) }
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(strings.gameModes) }
                    TextButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) { Text(strings.mainMenu) }
                }
            },
        )
    }
}
