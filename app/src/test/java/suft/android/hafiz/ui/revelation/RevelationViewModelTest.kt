package suft.android.hafiz.ui.revelation

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import suft.android.hafiz.AppContainer
import suft.android.hafiz.testing.awaitProgress
import suft.android.hafiz.testing.testContainer

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class RevelationViewModelTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun start(container: AppContainer): Pair<ViewModelStore, RevelationViewModel> {
        val store = ViewModelStore()
        val vm = ViewModelProvider(
            store,
            viewModelFactory { initializer { RevelationViewModel(container) } },
        )[RevelationViewModel::class.java]
        return store to vm
    }

    private fun RevelationViewModel.answerCorrectly() {
        select(state.value.question.place)
        advanceNow()
    }

    @Test
    fun finishedGameIsRecordedAndRestartDealsAgain() {
        val container = testContainer(tmp)
        val (_, vm) = start(container)

        repeat(5) { vm.answerCorrectly() }
        assertTrue(vm.state.value.finished)
        assertEquals(5, vm.state.value.score)
        awaitProgress(container) { it.best("revelation") == 5 && it.recentRounds.single().mode == "revelation" }

        vm.restart()
        val s = vm.state.value
        assertFalse(s.finished)
        assertEquals(0, s.score)
        assertEquals(5, s.previousBest)
        assertEquals(5, s.questions.size)
    }

    @Test
    fun lastAnswerBeforeLeavingCompletesTheGame() {
        val container = testContainer(tmp)
        val (store, vm) = start(container)

        repeat(4) { vm.answerCorrectly() }
        vm.select(vm.state.value.question.place)
        store.clear()
        awaitProgress(container) { it.roundsCompleted == 1 }
    }
}
