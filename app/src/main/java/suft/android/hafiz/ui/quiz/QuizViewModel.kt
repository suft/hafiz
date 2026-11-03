package suft.android.hafiz.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import suft.android.hafiz.AppContainer
import suft.android.hafiz.audio.PlaybackStatus
import suft.android.hafiz.audio.RecitationPlayer
import suft.android.hafiz.audio.Reciter
import suft.android.hafiz.audio.Reciters
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Level
import suft.android.hafiz.domain.Question
import suft.android.hafiz.domain.QuestionGenerator
import suft.android.hafiz.domain.Quran

/** Why a recitation could not be played; the screen turns it into a message. */
enum class PlaybackError { LoadFailed, LoadFailedAutoPlayOff }

data class QuizUiState(
    val level: Level,
    /** 1-based number of the question on screen. */
    val number: Int = 0,
    val score: Int = 0,
    val question: Question? = null,
    /** Option the user tapped, or null while the question is open. */
    val selected: Int? = null,
    val finished: Boolean = false,
    /** Best score for this level in this mode before the current game. */
    val previousBest: Int = 0,
    val reciter: Reciter = Reciters.default,
    val autoPlay: Boolean = true,
    val mode: GameMode = GameMode.Regular,
    /** Wrong answers, correct streak and its best in the current game. */
    val wrong: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    /** Set until the screen has shown it and called [QuizViewModel.playbackErrorShown]. */
    val playbackError: PlaybackError? = null,
) {
    val total: Int get() = level.questionsPerRound
    val livesLeft: Int get() = (mode.lives ?: 0) - wrong
    val answered: Boolean get() = selected != null
    val isNewRecord: Boolean get() = finished && score > previousBest
}

class QuizViewModel(
    private val container: AppContainer,
    quran: Quran,
    private val level: Level,
    private val mode: GameMode,
    language: AppLanguage,
) : ViewModel() {

    private val repo = container.progress
    private lateinit var generator: QuestionGenerator

    private val player = RecitationPlayer(
        context = container.appContext,
        audioCache = container.audioCache,
        scope = viewModelScope,
        trimEndMs = if (level.style == GameStyle.NextAyah) 300 else 0,
    )

    private val _state = MutableStateFlow(QuizUiState(level = level, mode = mode))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    val playback: StateFlow<PlaybackStatus> = player.status

    private var upcoming: Question? = null
    private var advanceJob: Job? = null
    private var roundRecorded = false

    // Nothing plays and no feedback timer runs while the screen is not in the foreground (Home, another
    // screen on top…); what was skipped is picked up again by [setForeground].
    private var foreground = false
    private var pendingAutoPlay = false
    private var resumeOnForeground = false
    private var advancePending = false
    private var pendingAdvanceMs = 0L

    /** Off with a screen reader: the answer stays on screen until the user taps "Next". */
    private var autoAdvance = true

    init {
        viewModelScope.launch {
            val saved = repo.progress.first()
            _state.update {
                it.copy(
                    reciter = Reciters.byFolder(saved.reciterFolder),
                    autoPlay = saved.autoPlay,
                    previousBest = if (mode.endless) saved.runBest(mode, level.id) else saved.best(level.id),
                )
            }
            val similar = container.mutashabihat.filterNotNull().first()
            val layout = container.mushafLayouts.filterNotNull().first().getValue(saved.mushafEdition)
            // Mutashabihat levels weigh thousands of prompts; build the pool and the first question off the main thread.
            generator = withContext(Dispatchers.Default) {
                QuestionGenerator(quran, level, language, mutashabihat = similar, trapMode = mode.trapOptions, layout = layout)
                    .also { upcoming = it.next() }
            }
            showNextQuestion()
        }
        viewModelScope.launch {
            player.errors.collect { onPlaybackError() }
        }
    }

    // ---------------------------------------------------------------- answering

    fun select(option: Int) {
        val s = _state.value
        val q = s.question ?: return
        if (s.answered || s.finished) return

        val correct = option == q.correctOption
        _state.update {
            val streak = if (correct) it.streak + 1 else 0
            it.copy(
                selected = option,
                score = it.score + if (correct) 1 else 0,
                wrong = it.wrong + if (correct) 0 else 1,
                streak = streak,
                bestStreak = maxOf(it.bestStreak, streak),
            )
        }
        // appScope: the answer counts even if the user leaves before the write happens.
        container.appScope.launch { repo.recordAnswer(q.style, q.surahIndex, correct) }
        // The best score is kept as it grows, so a game that ends by the process dying still counts.
        if (correct && !level.practice) persistBest()

        scheduleAdvance(if (correct) CORRECT_DELAY_MS else WRONG_DELAY_MS)
    }

    /** Skip the remaining feedback animation. */
    fun advanceNow() {
        if (!_state.value.answered) return
        advanceJob?.cancel()
        advance()
    }

    private fun scheduleAdvance(ms: Long) {
        if (!autoAdvance) return
        pendingAdvanceMs = ms
        if (foreground) startAdvanceTimer(ms) else advancePending = true
    }

    private fun startAdvanceTimer(ms: Long) {
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(ms)
            advance()
        }
    }

    fun setAutoAdvance(enabled: Boolean) {
        autoAdvance = enabled
        if (!enabled) {
            advanceJob?.cancel()
            advancePending = false
        }
    }

    private fun advance() {
        advancePending = false
        val s = _state.value
        if (s.finished) return
        if (mode.isOver(s.number, s.wrong, s.total)) finishRound() else showNextQuestion()
    }

    private fun showNextQuestion() {
        player.stop()
        val q = upcoming ?: generator.next()
        upcoming = generator.next()
        _state.update { it.copy(number = it.number + 1, question = q, selected = null) }

        if (_state.value.autoPlay) autoPlay(q)
    }

    private fun finishRound() {
        player.stop()
        _state.update { it.copy(finished = true) }
        recordRound()
        val s = _state.value
        container.appScope.launch {
            if (mode.endless) {
                repo.recordFinishedRun(mode, level.id, s.score, s.number)
            } else {
                repo.recordCompletedRound(level.id, s.score, s.total)
            }
        }
    }

    fun restart() {
        // A second tap on "Retry" (or one after the game is already running) must not start another game.
        if (!_state.value.finished) return
        advanceJob?.cancel()
        advancePending = false
        player.stop()
        val best = maxOf(_state.value.previousBest, _state.value.score)
        roundRecorded = false
        _state.update {
            it.copy(
                number = 0,
                score = 0,
                wrong = 0,
                streak = 0,
                bestStreak = 0,
                selected = null,
                finished = false,
                previousBest = best,
                question = null,
            )
        }
        showNextQuestion()
    }

    private fun recordRound() {
        if (roundRecorded) return
        roundRecorded = true
        persistBest()
    }

    /** Stores the best score (and the best streak of an endless game) reached so far. */
    private fun persistBest() {
        if (level.practice) return
        val s = _state.value
        // appScope: must complete even if the screen is being closed.
        container.appScope.launch {
            if (mode.endless) repo.recordRunBest(mode, level.id, s.score, s.bestStreak)
            else repo.recordLevelScore(level.id, s.score)
        }
    }

    // ---------------------------------------------------------------- audio

    /** Called as the screen enters or leaves the resumed state. */
    fun setForeground(value: Boolean) {
        if (foreground == value) return
        foreground = value
        if (!value) {
            resumeOnForeground = playback.value == PlaybackStatus.Playing || playback.value == PlaybackStatus.Buffering
            player.pause()
            if (advanceJob?.isActive == true) {
                advanceJob?.cancel()
                advancePending = true
            }
        } else {
            if (advancePending) {
                advancePending = false
                // The user gets the full feedback time again.
                startAdvanceTimer(pendingAdvanceMs)
            }
            val s = _state.value
            val q = s.question
            if ((pendingAutoPlay || resumeOnForeground) && q != null && !s.finished) autoPlay(q)
            pendingAutoPlay = false
            resumeOnForeground = false
        }
    }

    /** Plays [q] and warms the cache for the next question — or remembers to, until the screen is in the foreground. */
    private fun autoPlay(q: Question) {
        if (foreground) {
            player.play(urlsFor(q))
            upcoming?.let { player.prefetch(urlsFor(it)) }
        } else {
            pendingAutoPlay = true
        }
    }

    fun togglePlayback() {
        val q = _state.value.question ?: return
        player.toggle(urlsFor(q))
    }

    fun setReciter(reciter: Reciter) {
        if (reciter == _state.value.reciter) return
        _state.update { it.copy(reciter = reciter) }
        viewModelScope.launch { repo.setReciter(reciter.folder) }
        player.stop()
        val s = _state.value
        val q = s.question ?: return
        if (s.autoPlay && !s.finished) autoPlay(q)
    }

    fun setAutoPlay(enabled: Boolean) {
        _state.update { it.copy(autoPlay = enabled) }
        viewModelScope.launch { repo.setAutoPlay(enabled) }
        val q = _state.value.question ?: return
        if (enabled) {
            autoPlay(q)
        } else {
            pendingAutoPlay = false
            player.pause()
        }
    }

    private fun onPlaybackError() {
        // Like the original: stop auto-playing for this round instead of failing on every question.
        val wasAutoPlay = _state.value.autoPlay
        _state.update {
            it.copy(
                autoPlay = false,
                playbackError = if (wasAutoPlay) PlaybackError.LoadFailedAutoPlayOff else PlaybackError.LoadFailed,
            )
        }
    }

    fun playbackErrorShown() {
        _state.update { it.copy(playbackError = null) }
    }

    private fun urlsFor(q: Question): List<String> {
        val reciter = _state.value.reciter
        return q.recitation.map { (surah, ayah) -> reciter.url(surah, ayah) }
    }

    override fun onCleared() {
        // Leaving mid-round keeps the points earned so far (as the original did). A round whose last answer
        // was given during the feedback delay still counts as finished — an endless game whose last life went,
        // or a regular round whose last question was answered.
        val s = _state.value
        if (!s.finished && s.answered && mode.isOver(s.number, s.wrong, s.total)) finishRound()
        else if (s.score > 0) recordRound()
        player.release()
    }

    private companion object {
        const val CORRECT_DELAY_MS = 1_200L
        const val WRONG_DELAY_MS = 2_200L
    }
}
