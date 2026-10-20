package suft.android.ayahiq.ui.quiz

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.audio.PlaybackStatus
import suft.android.ayahiq.audio.Reciters
import suft.android.ayahiq.domain.ArabicText
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Question
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.i18n.Strings
import suft.android.ayahiq.ui.theme.LocalQuizColors
import suft.android.ayahiq.ui.theme.NaskhFont
import suft.android.ayahiq.ui.theme.quranTextStyle

private val NextAyahOptionLineHeight = 30.sp

@Composable
fun QuizScreen(
    container: AppContainer,
    quran: Quran,
    level: Level,
    mode: GameMode,
    onBack: () -> Unit,
    onMainMenu: () -> Unit,
    onShareAyah: (IntRange) -> Unit,
) {
    val strings = LocalStrings.current
    val vm: QuizViewModel = viewModel { QuizViewModel(container, quran, level, mode, strings.language) }
    val state by vm.state.collectAsStateWithLifecycle()
    val playback by vm.playback.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(vm) { vm.playbackErrors.collect { snackbar.showSnackbar(strings.playbackFailed) } }
    // Stop the recitation when the app goes to the background.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.pausePlayback() }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AppTopBar(
                title = state.level.name(strings.language),
                onBack = onBack,
                actions = {
                    val q = state.question
                    IconButton(onClick = { q?.let { onShareAyah(it.shownAyahs) } }, enabled = q != null) {
                        Icon(Icons.Filled.Share, contentDescription = strings.shareAyah)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val q = state.question
            if (q != null) {
                Column(
                    Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    ProgressHeader(state, onNext = vm::advanceNow)
                    Spacer(Modifier.height(10.dp))
                    PromptCard(q, Modifier.weight(1f))
                    Spacer(Modifier.height(8.dp))
                    AudioBar(
                        status = playback,
                        reciter = state.reciter,
                        autoPlay = state.autoPlay,
                        onToggle = vm::togglePlayback,
                        onReciter = vm::setReciter,
                        onAutoPlay = vm::setAutoPlay,
                    )
                    Spacer(Modifier.height(8.dp))
                    q.options.forEachIndexed { i, text ->
                        OptionButton(
                            text = text,
                            style = q.style,
                            state = optionState(i, q, state.selected),
                            onClick = { vm.select(i) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }

    if (state.finished) {
        ResultDialog(
            state = state,
            onRestart = vm::restart,
            onShare = { shareScore(context, state, strings) },
            onBack = onBack,
            onMainMenu = onMainMenu,
        )
    }
}

// ---------------------------------------------------------------- header

@Composable
private fun ProgressHeader(state: QuizUiState, onNext: () -> Unit) {
    val strings = LocalStrings.current
    // The "next" TextButton's 48dp touch target must not grow the row when it appears.
    Row(modifier = Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (state.mode.endless) strings.questionNumber(state.number) else strings.questionProgress(state.number, state.total),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (state.answered && !state.finished) {
            TextButton(onClick = onNext) {
                Text(strings.next)
                Spacer(Modifier.width(4.dp))
                // Auto-mirrored: points left (forward) in RTL.
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
        if (state.mode.endless) {
            val livesLeft = state.livesLeft.coerceAtLeast(0)
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .semantics { contentDescription = strings.livesLeft(livesLeft) },
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                repeat(state.mode.lives!!) { i ->
                    if (i < livesLeft) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = LocalQuizColors.current.wrong,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                strings.points(state.score),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    if (!state.mode.endless) {
        val done = state.number - 1 + if (state.answered) 1 else 0
        LinearProgressIndicator(
            progress = { done.toFloat() / state.total },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// ---------------------------------------------------------------- prompt

@Composable
private fun PromptCard(q: Question, modifier: Modifier = Modifier) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    val prompt = formatPrompt(q)
    val size = when {
        prompt.length < 80 -> 28
        prompt.length < 200 -> 24
        prompt.length < 450 -> 21
        else -> 19
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, quizColors.gold.copy(alpha = 0.7f)),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = when (q.style) {
                    GameStyle.GuessSurah -> strings.whichSurah
                    GameStyle.NextAyah -> strings.whatIsNextAyah
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = prompt,
                style = quranTextStyle(size),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
            q.reference?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = NaskhFont),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Guess-the-surah hides ayah numbers (they would give hints); next-ayah shows them. */
private fun formatPrompt(q: Question): String = when (q.style) {
    GameStyle.GuessSurah -> q.promptAyahs.joinToString("  ۝  ")
    GameStyle.NextAyah -> q.promptAyahs.indices.joinToString(" ") {
        q.promptAyahs[it] + " " + ArabicText.ayahMarker(q.promptAyahNumbers[it])
    }
}

// ---------------------------------------------------------------- audio

@Composable
private fun AudioBar(
    status: PlaybackStatus,
    reciter: Int,
    autoPlay: Boolean,
    onToggle: () -> Unit,
    onReciter: (Int) -> Unit,
    onAutoPlay: (Boolean) -> Unit,
) {
    val strings = LocalStrings.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            FilledTonalIconButton(onClick = onToggle) {
                Icon(
                    if (status == PlaybackStatus.Playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (status == PlaybackStatus.Playing) strings.pause else strings.listen,
                )
            }
            if (status == PlaybackStatus.Buffering) {
                CircularProgressIndicator(Modifier.size(44.dp), strokeWidth = 2.dp)
            }
        }
        Spacer(Modifier.width(4.dp))

        var menuOpen by remember { mutableStateOf(false) }
        Box(Modifier.weight(1f)) {
            TextButton(onClick = { menuOpen = true }) {
                Text(
                    Reciters.get(reciter).name(strings.language),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = strings.chooseReciter)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                Reciters.all.forEachIndexed { i, r ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                r.name(strings.language),
                                fontWeight = if (i == reciter) FontWeight.Bold else FontWeight.Normal,
                                color = if (i == reciter) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onReciter(i)
                        },
                    )
                }
            }
        }

        Text(strings.autoPlay, style = MaterialTheme.typography.labelLarge)
        Switch(
            checked = autoPlay,
            onCheckedChange = onAutoPlay,
            modifier = Modifier.scale(0.8f),
        )
    }
}

// ---------------------------------------------------------------- options

private enum class OptionState { Open, Correct, Wrong, Dimmed }

private fun optionState(index: Int, q: Question, selected: Int?): OptionState = when {
    selected == null -> OptionState.Open
    index == q.correctOption -> OptionState.Correct
    index == selected -> OptionState.Wrong
    else -> OptionState.Dimmed
}

@Composable
private fun OptionButton(text: String, style: GameStyle, state: OptionState, onClick: () -> Unit) {
    val quizColors = LocalQuizColors.current
    val strings = LocalStrings.current
    val container by animateColorAsState(
        when (state) {
            OptionState.Correct -> quizColors.correctContainer
            OptionState.Wrong -> quizColors.wrongContainer
            else -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f)
        },
        label = "optionContainer",
    )
    val borderColor = when (state) {
        OptionState.Correct -> quizColors.correct
        OptionState.Wrong -> quizColors.wrong
        else -> MaterialTheme.colorScheme.outline
    }
    // The original blinked the correct answer; a gentle pulse does the same job.
    val pulse = if (state == OptionState.Correct) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
            label = "pulseAlpha",
        ).value
    } else {
        1f
    }

    // Every next-ayah option reserves two lines so 1- or 2-line options don't resize the options column.
    val minHeight = if (style == GameStyle.NextAyah) {
        with(LocalDensity.current) { (NextAyahOptionLineHeight * 2).toDp() } + 20.dp // 2 lines + vertical contentPadding
    } else {
        0.dp
    }
    OutlinedButton(
        onClick = onClick,
        enabled = state == OptionState.Open,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .alpha(if (state == OptionState.Dimmed) 0.5f else 1f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (state == OptionState.Open || state == OptionState.Dimmed) 1.dp else 2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = container,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = container,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            style = if (style == GameStyle.NextAyah) {
                quranTextStyle(17).copy(lineHeight = NextAyahOptionLineHeight)
            } else {
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .alpha(pulse),
        )
        when (state) {
            OptionState.Correct -> Icon(Icons.Filled.CheckCircle, contentDescription = strings.correctAnswer, tint = quizColors.correct)
            OptionState.Wrong -> Icon(Icons.Filled.Cancel, contentDescription = strings.wrongAnswer, tint = quizColors.wrong)
            else -> Unit
        }
    }
}

// ---------------------------------------------------------------- result

@Composable
private fun ResultDialog(
    state: QuizUiState,
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
    onMainMenu: () -> Unit,
) {
    val strings = LocalStrings.current
    val mastery = state.level.masteryScore
    val practice = state.level.practice
    val endless = state.mode.endless
    val message = when {
        state.mode == GameMode.Survival -> strings.survivalResult(state.score)
        endless -> strings.livesResult(state.score, state.number)
        state.score == state.total -> strings.perfectScore
        state.score >= mastery -> if (practice) strings.surahReviewMastered else strings.levelMastered
        state.score * 2 >= state.total -> strings.keepReviewing
        else -> if (practice) strings.surahNeedsReview else strings.needsReview
    }
    AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = LocalQuizColors.current.gold) },
        title = { Text(if (endless) strings.gameOver else strings.resultTitle) },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.level.name(strings.language), style = MaterialTheme.typography.titleMedium)
                if (endless) {
                    Text(
                        "${strings.gameModeTitle(state.mode)} · ${strings.styleTitle(state.level.style)}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (endless) strings.number(state.score) else "${strings.number(state.score)} / ${strings.number(state.total)}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                if (state.mode == GameMode.ThreeLives) {
                    Text(strings.longestStreak(state.bestStreak), style = MaterialTheme.typography.bodyMedium)
                }
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
            Row {
                TextButton(onClick = onShare) { Text(strings.shareResult) }
                TextButton(onClick = onRestart) { Text(strings.retry) }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onMainMenu) { Text(strings.mainMenu) }
                TextButton(onClick = onBack) { Text(strings.goBack) }
            }
        },
    )
}

private fun shareScore(context: Context, state: QuizUiState, strings: Strings) {
    val text = if (state.mode.endless) {
        strings.shareRunText(
            state.score,
            state.level.name(strings.language),
            strings.gameModeTitle(state.mode),
            strings.styleTitle(state.level.style),
        )
    } else {
        strings.shareScoreText(
            state.score,
            state.total,
            state.level.name(strings.language),
            strings.styleTitle(state.level.style),
        )
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, strings.appName)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, strings.shareResult))
}
