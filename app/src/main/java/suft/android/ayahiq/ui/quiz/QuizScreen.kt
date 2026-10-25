package suft.android.ayahiq.ui.quiz

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.audio.PlaybackStatus
import suft.android.ayahiq.audio.Reciter
import suft.android.ayahiq.audio.Reciters
import suft.android.ayahiq.domain.ArabicText
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Question
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.components.indicatedVerticalScroll
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.i18n.Strings
import suft.android.ayahiq.ui.theme.LocalQuizColors
import suft.android.ayahiq.ui.theme.NaskhFont
import suft.android.ayahiq.ui.theme.quranTextStyle

private val NextAyahOptionLineHeight = 30.sp

/** Share of the body height the options pane takes on portrait screens. */
private const val OPTIONS_HEIGHT_FRACTION = 0.45f

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

    // Audio and the feedback timer run only while the screen is resumed: not in the background, not behind
    // another screen (share card), not while the activity is paused.
    LifecycleResumeEffect(vm) {
        vm.setForeground(true)
        onPauseOrDispose { vm.setForeground(false) }
    }
    val touchExploration = rememberTouchExplorationEnabled()
    LaunchedEffect(touchExploration) { vm.setAutoAdvance(!touchExploration) }
    LaunchedEffect(state.playbackError) {
        val error = state.playbackError ?: return@LaunchedEffect
        snackbar.showSnackbar(
            if (error == PlaybackError.LoadFailedAutoPlayOff) strings.playbackFailedAutoPlayOff else strings.playbackFailed,
        )
        vm.playbackErrorShown()
    }

    QuizContent(
        state = state,
        playback = playback,
        snackbar = snackbar,
        onBack = onBack,
        onMainMenu = onMainMenu,
        onShareAyah = onShareAyah,
        onSelect = vm::select,
        onNext = vm::advanceNow,
        onTogglePlayback = vm::togglePlayback,
        onReciter = vm::setReciter,
        onAutoPlay = vm::setAutoPlay,
        onRestart = vm::restart,
        onShareResult = { shareScore(context, state, strings) },
    )
}

/** True while a screen reader with touch exploration (TalkBack) is on. */
@Composable
private fun rememberTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager }
    var enabled by remember(manager) { mutableStateOf(manager.isTouchExplorationEnabled) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled = it }
        manager.addTouchExplorationStateChangeListener(listener)
        onDispose { manager.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}

/** Test tags for the quiz body. */
internal object QuizTestTags {
    const val PROMPT = "quiz.prompt"
    fun option(i: Int) = "quiz.option.$i"
}

@Composable
internal fun QuizContent(
    state: QuizUiState,
    playback: PlaybackStatus,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onMainMenu: () -> Unit,
    onShareAyah: (IntRange) -> Unit,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onTogglePlayback: () -> Unit,
    onReciter: (Reciter) -> Unit,
    onAutoPlay: (Boolean) -> Unit,
    onRestart: () -> Unit,
    onShareResult: () -> Unit,
) {
    val strings = LocalStrings.current

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AppTopBar(
                title = state.level.name(strings.language),
                onBack = onBack,
                actions = {
                    val q = state.question
                    // Sharing the ayah of a guess-the-surah question before answering would reveal the surah.
                    IconButton(
                        onClick = { q?.let { onShareAyah(it.shownAyahs) } },
                        enabled = q != null && (q.style == GameStyle.NextAyah || state.answered),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = strings.shareAyah)
                    }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val q = state.question
            if (q != null) {
                // Room for the snackbar, so the last option can be scrolled above it.
                val snackbarSpace = if (snackbar.currentSnackbarData != null) 80.dp else 0.dp
                val audio: @Composable () -> Unit = {
                    AudioBar(
                        status = playback,
                        reciter = state.reciter,
                        autoPlay = state.autoPlay,
                        onToggle = onTogglePlayback,
                        onReciter = onReciter,
                        onAutoPlay = onAutoPlay,
                    )
                }
                val options: @Composable () -> Unit = {
                    q.options.forEachIndexed { i, text ->
                        OptionButton(
                            text = text,
                            style = q.style,
                            state = optionState(i, q, state.selected),
                            onClick = { onSelect(i) },
                            modifier = Modifier.testTag(QuizTestTags.option(i)),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                if (maxWidth >= 560.dp && maxWidth > maxHeight) {
                    // Landscape: the prompt on one side, the controls on the other, so neither is squeezed.
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            ProgressHeader(state, onNext = onNext)
                            Spacer(Modifier.height(10.dp))
                            PromptCard(q, Modifier.weight(1f).padding(bottom = 8.dp).testTag(QuizTestTags.PROMPT))
                        }
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            audio()
                            Spacer(Modifier.height(8.dp))
                            Column(Modifier.weight(1f).fillMaxWidth().indicatedVerticalScroll(rememberScrollState()).padding(end = 8.dp)) {
                                options()
                                Spacer(Modifier.height(4.dp + snackbarSpace))
                            }
                        }
                    }
                } else {
                    // Nothing moves between questions: the header and audio bar are fixed, the options sit in a pane
                    // of fixed size at the bottom, and the prompt takes the rest. A prompt or an option that does not
                    // fit scrolls inside its own pane.
                    val optionsMaxHeight = this@BoxWithConstraints.maxHeight * OPTIONS_HEIGHT_FRACTION
                    Column(
                        Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    ) {
                        ProgressHeader(state, onNext = onNext)
                        Spacer(Modifier.height(10.dp))
                        PromptCard(
                            q,
                            Modifier
                                .weight(1f)
                                .heightIn(min = 120.dp)
                                .testTag(QuizTestTags.PROMPT),
                        )
                        Spacer(Modifier.height(8.dp))
                        audio()
                        Spacer(Modifier.height(8.dp))
                        // Full ayahs vary in length, so next-ayah options get a pane of constant height; guess-surah
                        // options are all one line and size the pane themselves.
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .then(
                                    if (q.style == GameStyle.NextAyah) {
                                        Modifier.height(optionsMaxHeight)
                                    } else {
                                        Modifier.heightIn(max = optionsMaxHeight)
                                    },
                                )
                                .indicatedVerticalScroll(rememberScrollState())
                                .padding(end = 8.dp),
                        ) {
                            options()
                            Spacer(Modifier.height(4.dp + snackbarSpace))
                        }
                    }
                }
            }
        }
    }

    if (state.finished) {
        ResultDialog(
            state = state,
            onRestart = onRestart,
            onShare = onShareResult,
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
        // Announced politely when the answer is given: the question, then whether it was right.
        val progressLabel =
            if (state.mode.endless) strings.questionNumber(state.number) else strings.questionProgress(state.number, state.total)
        val correct = state.selected != null && state.selected == state.question?.correctOption
        Text(
            progressLabel,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    if (state.answered) {
                        contentDescription = "$progressLabel, ${if (correct) strings.correctAnswer else strings.wrongAnswer}"
                    }
                },
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
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .indicatedVerticalScroll(rememberScrollState(), inset = 8.dp)
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
    reciter: Reciter,
    autoPlay: Boolean,
    onToggle: () -> Unit,
    onReciter: (Reciter) -> Unit,
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
                // A long name shortens; the arrow that shows this is a menu always stays.
                Text(
                    reciter.name(strings.language),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = strings.chooseReciter)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                Reciters.all.forEach { r ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                r.name(strings.language),
                                fontWeight = if (r == reciter) FontWeight.Bold else FontWeight.Normal,
                                color = if (r == reciter) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onReciter(r)
                        },
                    )
                }
            }
        }

        // The label is part of the switch, so a screen reader announces "Auto, switch, on".
        Row(
            Modifier
                .heightIn(min = 48.dp)
                .toggleable(value = autoPlay, role = Role.Switch, onValueChange = onAutoPlay),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.autoPlay, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(4.dp))
            Switch(checked = autoPlay, onCheckedChange = null)
        }
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
private fun OptionButton(
    text: String,
    style: GameStyle,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
    // The original blinked the correct answer; a gentle pulse does the same job, unless animations are off.
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val pulse: State<Float>? = if (state == OptionState.Correct && !reduceMotion) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
            label = "pulseAlpha",
        )
    } else {
        null
    }
    OutlinedButton(
        onClick = onClick,
        enabled = state == OptionState.Open,
        modifier = modifier
            .fillMaxWidth()
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
                .graphicsLayer { alpha = pulse?.value ?: 1f },
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
        // Back and the system gesture close the dialog like the title-bar back arrow; a tap outside must not.
        onDismissRequest = onBack,
        properties = DialogProperties(dismissOnClickOutside = false),
        icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = LocalQuizColors.current.gold) },
        title = {
            Text(
                if (endless) strings.gameOver else strings.resultTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    state.level.name(strings.language),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                if (endless) {
                    Text(
                        "${strings.gameModeTitle(state.mode)} · ${strings.styleTitle(state.level.style)}",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
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
                if (state.mode.showsLongestStreak) {
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
        // One column of full-width buttons: four buttons in two cramped rows wrapped badly at large font sizes.
        confirmButton = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text(strings.retry) }
                OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) { Text(strings.shareResult) }
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(if (practice) strings.statistics else strings.levelsList)
                }
                TextButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) { Text(strings.mainMenu) }
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
