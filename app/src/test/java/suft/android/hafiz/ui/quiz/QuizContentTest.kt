package suft.android.hafiz.ui.quiz

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.then
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import suft.android.hafiz.audio.PlaybackStatus
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.domain.Levels
import suft.android.hafiz.domain.MushafCell
import suft.android.hafiz.domain.QuestionGenerator
import suft.android.hafiz.ui.i18n.ArabicStrings
import suft.android.hafiz.ui.theme.HafizTheme
import suft.android.hafiz.testing.TestData

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class)
class QuizContentTest {

    @get:Rule val rule = createComposeRule()

    private val quran = TestData.quran

    /** Ayat al-Kursi (2:255) on the whole-Quran level of [style]. */
    private fun state(style: GameStyle, selected: Int? = null, finished: Boolean = false): QuizUiState {
        val level = Levels.forStyle(style, quran).first { it.surahRange == 0..113 && !it.openings }
        val ayah = quran.surahs[1].firstAyah + 254
        val question = QuestionGenerator(quran, level, AppLanguage.Arabic, Random(1), layout = TestData.mushafLayout)
            .questionFor(ayah..ayah)
        return QuizUiState(
            level = level,
            number = 1,
            question = question,
            selected = selected,
            finished = finished,
            mode = GameMode.Regular,
        )
    }

    private var backs = 0

    private fun show(state: QuizUiState, size: DpSize? = null, fontScale: Float? = null) {
        rule.setContent {
            HafizTheme(AppLanguage.Arabic, darkTheme = false) {
                var override = DeviceConfigurationOverride.Companion.ForcedSize(size ?: DpSize(360.dp, 800.dp))
                if (fontScale != null) override = override.then(DeviceConfigurationOverride.Companion.FontScale(fontScale))
                DeviceConfigurationOverride(override) { Content(state) }
            }
        }
    }

    @Composable
    private fun Content(state: QuizUiState) {
        QuizContent(
            state = state,
            playback = PlaybackStatus.Idle,
            snackbar = remember { SnackbarHostState() },
            onBack = { backs++ },
            onMainMenu = {},
            onSelect = {},
            onNext = {},
            onTogglePlayback = {},
            onReciter = {},
            onAutoPlay = {},
            onRestart = {},
            onShareResult = {},
        )
    }

    private fun assertEverythingReachable(state: QuizUiState) {
        rule.onNodeWithTag(QuizTestTags.PROMPT).assertIsDisplayed().assertHeightIsAtLeast(120.dp)
        state.question!!.reference?.let { rule.onNodeWithText(it).performScrollTo().assertIsDisplayed() }
        state.question.options.indices.forEach { rule.onNodeWithTag(QuizTestTags.option(it)).performScrollTo().assertIsDisplayed() }
    }

    @Test
    fun nextAyahFits360x640() {
        val s = state(GameStyle.NextAyah)
        show(s, DpSize(360.dp, 640.dp))
        assertEverythingReachable(s)
    }

    @Test
    fun nextAyahFitsAtFontScale2() {
        val s = state(GameStyle.NextAyah)
        show(s, DpSize(411.dp, 914.dp), fontScale = 2f)
        assertEverythingReachable(s)
    }

    @Test
    fun guessSurahFits320x568() {
        val s = state(GameStyle.GuessSurah)
        show(s, DpSize(320.dp, 568.dp))
        assertEverythingReachable(s)
    }

    @Test
    fun positionFits320x568() {
        val s = state(GameStyle.AyahPosition)
        show(s, DpSize(320.dp, 568.dp))
        assertEverythingReachable(s)
    }

    @Test
    fun positionGridPutsTheRightPageOnTheRight() {
        show(state(GameStyle.AyahPosition), DpSize(360.dp, 800.dp))
        fun bounds(cell: MushafCell) = rule.onNodeWithTag(QuizTestTags.option(cell.ordinal)).getUnclippedBoundsInRoot()
        assertTrue("right page right of left page", bounds(MushafCell.RightTop).left > bounds(MushafCell.LeftTop).left)
        assertTrue(bounds(MushafCell.RightTop).top < bounds(MushafCell.RightMiddle).top)
        assertTrue(bounds(MushafCell.RightMiddle).top < bounds(MushafCell.RightBottom).top)
    }

    @Test
    fun landscapeUsesTwoPanes() {
        val s = state(GameStyle.NextAyah)
        show(s, DpSize(640.dp, 360.dp))
        assertEverythingReachable(s)
        val promptBottom = rule.onNodeWithTag(QuizTestTags.PROMPT).getUnclippedBoundsInRoot().bottom
        val optionTop = rule.onNodeWithTag(QuizTestTags.option(0)).getUnclippedBoundsInRoot().top
        assertTrue("options start below the prompt ($optionTop >= $promptBottom)", optionTop < promptBottom)
    }

    @Test
    fun nextAyahOptionsStayPut() = assertOptionsStayPut(GameStyle.NextAyah)

    @Test
    fun guessSurahOptionsStayPut() = assertOptionsStayPut(GameStyle.GuessSurah)

    /** The options pane sits at the same place for a long prompt (2:255) and a short one (108:1). */
    private fun assertOptionsStayPut(style: GameStyle) {
        run {
            val level = Levels.forStyle(style, quran).first { it.surahRange == 0..113 && !it.openings }
            fun stateFor(ayah: Int) = QuizUiState(
                level = level,
                number = 1,
                question = QuestionGenerator(quran, level, AppLanguage.Arabic, Random(1), layout = TestData.mushafLayout)
                    .questionFor(ayah..ayah),
                mode = GameMode.Regular,
            )
            var current by mutableStateOf(stateFor(quran.surahs[1].firstAyah + 254)) // 2:255, long
            rule.setContent {
                HafizTheme(AppLanguage.Arabic, darkTheme = false) {
                    DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(360.dp, 800.dp))) {
                        Content(current)
                    }
                }
            }
            fun tops() = List(4) { rule.onNodeWithTag(QuizTestTags.option(it)).getUnclippedBoundsInRoot().top.value }
            val long = tops()
            current = stateFor(quran.surahs[107].firstAyah) // 108:1, short
            rule.waitForIdle()
            assertEquals("$style: options moved", long[0], tops()[0])
        }
    }

    @Test
    fun autoPlaySwitchHasAName() {
        show(state(GameStyle.GuessSurah))
        rule.onNode(isToggleable() and hasText(ArabicStrings.autoPlay)).assertExists()
    }

    @Test
    fun backClosesResultDialog() {
        show(state(GameStyle.GuessSurah, selected = 0, finished = true))
        rule.waitForIdle()
        Espresso.pressBackUnconditionally()
        rule.waitForIdle()
        assertEquals(1, backs)
    }

    @Test
    fun answerIsAnnounced() {
        val s = state(GameStyle.GuessSurah)
        show(s.copy(selected = s.question!!.correctOption))
        rule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite) and
                hasContentDescription(ArabicStrings.correctAnswer, substring = true),
        ).onFirst().assertExists()
    }
}
