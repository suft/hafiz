package suft.android.hafiz.ui.revelation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import suft.android.hafiz.AppContainer
import suft.android.hafiz.domain.RevelationPlace
import suft.android.hafiz.domain.RevelationQuestion
import suft.android.hafiz.domain.RevelationQuiz

data class RevelationUiState(
    val questions: List<RevelationQuestion>,
    /** 0-based index of the question on screen. */
    val current: Int = 0,
    /** Place the user tapped, or null while the question is open. */
    val selected: RevelationPlace? = null,
    val score: Int = 0,
    val finished: Boolean = false,
    /** Best score before this game. */
    val previousBest: Int = 0,
) {
    val question: RevelationQuestion get() = questions[current]
    val total: Int get() = questions.size
    val answered: Boolean get() = selected != null
    val isNewRecord: Boolean get() = finished && score > previousBest
}

class RevelationViewModel(
    private val container: AppContainer,
    private val random: Random = Random.Default,
) : ViewModel() {
    private val _state = MutableStateFlow(RevelationUiState(questions = RevelationQuiz.deal(random)))
    val state: StateFlow<RevelationUiState> = _state.asStateFlow()

    private var advanceJob: Job? = null

    /** Off with a screen reader: the answer stays on screen until the user taps "Next". */
    private var autoAdvance = true

    // The feedback timer runs only while the screen is in the foreground (not in the background, not behind another
    // screen); a skipped one is started again by [setForeground].
    private var foreground = false
    private var advancePending = false
    private var pendingAdvanceMs = 0L

    init {
        viewModelScope.launch {
            val best = container.progress.progress.first().best(RevelationQuiz.LEVEL_ID)
            _state.update { it.copy(previousBest = best) }
        }
    }

    fun select(place: RevelationPlace) {
        val s = _state.value
        if (s.answered || s.finished) return
        val correct = place == s.question.place
        _state.update { it.copy(selected = place, score = if (correct) it.score + 1 else it.score) }
        scheduleAdvance(if (correct) CORRECT_DELAY_MS else WRONG_DELAY_MS)
    }

    /** Skip the remaining feedback delay. */
    fun advanceNow() {
        if (!_state.value.answered) return
        advanceJob?.cancel()
        advance()
    }

    fun setAutoAdvance(enabled: Boolean) {
        val was = autoAdvance
        autoAdvance = enabled
        if (!enabled) {
            advanceJob?.cancel()
            advancePending = false
        } else if (!was) {
            // An answer given while auto-advance was off (TalkBack) still moves on once it is back on.
            val s = _state.value
            if (s.answered && !s.finished) scheduleAdvance(pendingAdvanceMs)
        }
    }

    fun restart() {
        if (!_state.value.finished) return
        advanceJob?.cancel()
        advancePending = false
        _state.update {
            it.copy(
                questions = RevelationQuiz.deal(random),
                current = 0,
                selected = null,
                score = 0,
                finished = false,
                previousBest = maxOf(it.previousBest, it.score),
            )
        }
    }

    private fun scheduleAdvance(ms: Long) {
        pendingAdvanceMs = ms
        if (!autoAdvance) return
        if (foreground) startAdvanceTimer(ms) else advancePending = true
    }

    private fun startAdvanceTimer(ms: Long) {
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(ms)
            advance()
        }
    }

    /** Called as the screen enters or leaves the resumed state; the feedback timer runs only while it is resumed. */
    fun setForeground(value: Boolean) {
        if (foreground == value) return
        foreground = value
        if (!value) {
            if (advanceJob?.isActive == true) {
                advanceJob?.cancel()
                advancePending = true
            }
        } else if (advancePending) {
            advancePending = false
            // The user gets the full feedback time again.
            startAdvanceTimer(pendingAdvanceMs)
        }
    }

    private fun advance() {
        advancePending = false
        val s = _state.value
        if (s.finished) return
        if (s.current + 1 >= s.total) {
            finish()
        } else {
            _state.update { it.copy(current = it.current + 1, selected = null) }
        }
    }

    private fun finish() {
        if (_state.value.finished) return
        _state.update { it.copy(finished = true) }
        val s = _state.value
        // appScope: the write must complete even if the user leaves the screen.
        container.appScope.launch { container.progress.recordRevelationGame(s.score, s.total) }
    }

    override fun onCleared() {
        // A last answer given during the feedback delay still counts; a game left earlier records nothing.
        val s = _state.value
        if (!s.finished && s.answered && s.current == s.total - 1) finish()
    }

    private companion object {
        const val CORRECT_DELAY_MS = 1_200L
        const val WRONG_DELAY_MS = 2_200L
    }
}
