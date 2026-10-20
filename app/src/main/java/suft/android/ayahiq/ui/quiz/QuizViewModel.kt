package suft.android.ayahiq.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.audio.PlaybackStatus
import suft.android.ayahiq.audio.RecitationPlayer
import suft.android.ayahiq.audio.Reciters
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Question
import suft.android.ayahiq.domain.QuestionGenerator
import suft.android.ayahiq.domain.Quran

data class QuizUiState(
    val level: Level,
    /** 1-based number of the question on screen. */
    val number: Int = 0,
    val score: Int = 0,
    val question: Question? = null,
    /** Option the user tapped, or null while the question is open. */
    val selected: Int? = null,
    val finished: Boolean = false,
    /** Best score for this level before the current round. */
    val previousBest: Int = 0,
    val reciter: Int = 0,
    val autoPlay: Boolean = true,
) {
    val total: Int get() = level.mode.questionsPerRound
    val answered: Boolean get() = selected != null
    val isNewRecord: Boolean get() = finished && score > previousBest
}

class QuizViewModel(
    private val container: AppContainer,
    quran: Quran,
    private val level: Level,
    language: AppLanguage,
) : ViewModel() {

    private val repo = container.progress
    private val generator = QuestionGenerator(quran, level, language)

    private val player = RecitationPlayer(
        context = container.appContext,
        audioCache = container.audioCache,
        scope = viewModelScope,
        trimEndMs = if (level.mode == GameMode.NextAyah) 300 else 0,
    )

    private val _state = MutableStateFlow(QuizUiState(level = level))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    val playback: StateFlow<PlaybackStatus> = player.status

    private val _playbackErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val playbackErrors: SharedFlow<Unit> = _playbackErrors.asSharedFlow()

    private var upcoming: Question? = null
    private var advanceJob: Job? = null
    private var roundRecorded = false

    init {
        viewModelScope.launch {
            val saved = repo.progress.first()
            _state.update {
                it.copy(
                    reciter = saved.reciter.coerceIn(Reciters.all.indices),
                    autoPlay = saved.autoPlay,
                    previousBest = saved.best(level.id),
                )
            }
            upcoming = generator.next()
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
        _state.update { it.copy(selected = option, score = it.score + if (correct) 1 else 0) }
        viewModelScope.launch { repo.recordAnswer(q.mode, q.surahIndex, correct) }

        advanceJob = viewModelScope.launch {
            delay(if (correct) CORRECT_DELAY_MS else WRONG_DELAY_MS)
            advance()
        }
    }

    /** Skip the remaining feedback animation. */
    fun advanceNow() {
        if (!_state.value.answered) return
        advanceJob?.cancel()
        advance()
    }

    private fun advance() {
        val s = _state.value
        if (s.finished) return
        if (s.number >= s.total) finishRound() else showNextQuestion()
    }

    private fun showNextQuestion() {
        player.stop()
        val q = upcoming ?: generator.next()
        upcoming = generator.next()
        _state.update { it.copy(number = it.number + 1, question = q, selected = null) }

        val s = _state.value
        if (s.autoPlay) {
            player.play(urlsFor(q))
            upcoming?.let { player.prefetch(urlsFor(it)) }
        }
    }

    private fun finishRound() {
        player.stop()
        _state.update { it.copy(finished = true) }
        recordRound()
        val s = _state.value
        container.appScope.launch { repo.recordCompletedRound(level.id, s.score, s.total) }
    }

    fun restart() {
        advanceJob?.cancel()
        player.stop()
        val best = maxOf(_state.value.previousBest, _state.value.score)
        roundRecorded = false
        _state.update { it.copy(number = 0, score = 0, selected = null, finished = false, previousBest = best, question = null) }
        upcoming = generator.next()
        showNextQuestion()
    }

    private fun recordRound() {
        if (roundRecorded) return
        roundRecorded = true
        val score = _state.value.score
        // appScope: must complete even if the screen is being closed.
        if (!level.practice) {
            container.appScope.launch { repo.recordLevelScore(level.id, score) }
        }
    }

    // ---------------------------------------------------------------- audio

    fun togglePlayback() {
        val q = _state.value.question ?: return
        player.toggle(urlsFor(q))
    }

    fun pausePlayback() = player.pause()

    fun setReciter(index: Int) {
        if (index == _state.value.reciter) return
        _state.update { it.copy(reciter = index) }
        viewModelScope.launch { repo.setReciter(index) }
        player.stop()
        val s = _state.value
        val q = s.question ?: return
        if (s.autoPlay && !s.finished) {
            player.play(urlsFor(q))
            upcoming?.let { player.prefetch(urlsFor(it)) }
        }
    }

    fun setAutoPlay(enabled: Boolean) {
        _state.update { it.copy(autoPlay = enabled) }
        viewModelScope.launch { repo.setAutoPlay(enabled) }
        val q = _state.value.question ?: return
        if (enabled) {
            player.play(urlsFor(q))
            upcoming?.let { player.prefetch(urlsFor(it)) }
        } else {
            player.pause()
        }
    }

    private fun onPlaybackError() {
        // Like the original: stop auto-playing for this round instead of failing on every question.
        if (_state.value.autoPlay) _state.update { it.copy(autoPlay = false) }
        _playbackErrors.tryEmit(Unit)
    }

    private fun urlsFor(q: Question): List<String> {
        val reciter = Reciters.get(_state.value.reciter)
        return q.recitation.map { (surah, ayah) -> reciter.url(surah, ayah) }
    }

    override fun onCleared() {
        // Leaving mid-round still keeps the points earned so far (as the original did).
        if (_state.value.score > 0) recordRound()
        player.release()
    }

    private companion object {
        const val CORRECT_DELAY_MS = 1_200L
        const val WRONG_DELAY_MS = 2_200L
    }
}
