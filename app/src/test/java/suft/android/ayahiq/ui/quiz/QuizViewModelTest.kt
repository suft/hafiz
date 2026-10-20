package suft.android.ayahiq.ui.quiz

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.domain.Level
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.testing.TestData
import suft.android.ayahiq.testing.awaitProgress
import suft.android.ayahiq.testing.idleMainUntil
import suft.android.ayahiq.testing.testContainer

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class QuizViewModelTest {

    @get:Rule val tmp = TemporaryFolder()

    private val quran = TestData.quran

    private fun start(container: AppContainer, level: Level, mode: GameMode): Pair<ViewModelStore, QuizViewModel> {
        runBlocking { container.progress.setAutoPlay(false) }
        val store = ViewModelStore()
        val vm = ViewModelProvider(
            store,
            viewModelFactory { initializer { QuizViewModel(container, quran, level, mode, AppLanguage.Arabic) } },
        )[QuizViewModel::class.java]
        idleMainUntil { vm.state.value.question != null }
        return store to vm
    }

    private fun QuizViewModel.answerCorrectly() {
        val q = state.value.question!!
        select(q.correctOption)
        advanceNow()
    }

    @Test
    fun endlessBestSurvivesProcessDeath() {
        val container = testContainer(tmp)
        val level = Levels.forStyle(GameStyle.GuessSurah, quran)[0]
        val (_, vm) = start(container, level, GameMode.Survival)

        repeat(6) { vm.answerCorrectly() }
        // The store is deliberately not cleared: the process dies without onCleared.
        awaitProgress(container) { it.runBest(GameMode.Survival, level.id) == 6 }
    }

    @Test
    fun lastAnswerBeforeLeavingCompletesTheRound() {
        val container = testContainer(tmp)
        val level = Levels.surahPractice(102, quran) // two questions
        val (store, vm) = start(container, level, GameMode.Regular)

        vm.answerCorrectly()
        val q = vm.state.value.question!!
        vm.select(q.correctOption)
        store.clear()
        awaitProgress(container) { it.roundsCompleted == 1 && it.recentRounds.size == 1 }
    }
}
