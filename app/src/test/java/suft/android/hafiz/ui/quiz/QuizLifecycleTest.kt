package suft.android.hafiz.ui.quiz

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Levels
import suft.android.hafiz.testing.TestData
import suft.android.hafiz.testing.testContainer
import suft.android.hafiz.ui.i18n.ArabicStrings
import suft.android.hafiz.ui.theme.HafizTheme

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class QuizLifecycleTest {

    @get:Rule val rule = createComposeRule()
    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun feedbackTimerWaitsWhileStopped() {
        val container = testContainer(tmp)
        runBlocking { container.progress.setAutoPlay(false) }
        val quran = TestData.quran
        val level = Levels.forStyle(GameStyle.GuessSurah, quran)[0]
        val owner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                HafizTheme(AppLanguage.Arabic, darkTheme = false) {
                    QuizScreen(
                        container = container,
                        quran = quran,
                        level = level,
                        mode = GameMode.Regular,
                        onBack = {},
                        onMainMenu = {},
                    )
                }
            }
        }
        val first = ArabicStrings.questionProgress(1, 20)
        rule.waitUntil(10_000) { rule.onAllNodesWithText(first).fetchSemanticsNodes().isNotEmpty() }

        rule.onNodeWithTag(QuizTestTags.option(0)).performClick()
        rule.runOnUiThread { owner.currentState = Lifecycle.State.CREATED }
        ShadowLooper.idleMainLooper(5, TimeUnit.SECONDS)
        rule.runOnUiThread { owner.currentState = Lifecycle.State.RESUMED }
        rule.waitForIdle()

        rule.onNodeWithText(first).assertIsDisplayed()
    }
}
