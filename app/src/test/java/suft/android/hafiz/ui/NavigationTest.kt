package suft.android.hafiz.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.activity.ComponentActivity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import suft.android.hafiz.HafizApp
import suft.android.hafiz.MainActivity
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.ui.i18n.ArabicStrings
import suft.android.hafiz.ui.i18n.EnglishStrings

@RunWith(RobolectricTestRunner::class)
class NavigationTest {

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) =
        rule.waitUntil(15_000) { rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    // The Statistics card exists only on Home among Home/Modes/Guide/Styles/Levels.
    private fun waitForHome() = waitForText(ArabicStrings.statistics)

    private fun openStyles(mode: GameMode) {
        waitForHome()
        rule.onNodeWithText(ArabicStrings.play).performClick()
        waitForText(ArabicStrings.gameModes)
        rule.onNodeWithText(ArabicStrings.gameModeTitle(mode)).performClick()
        waitForText(ArabicStrings.chooseStyle)
    }

    private fun doubleBack() {
        rule.mainClock.autoAdvance = false
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
    }

    @Test
    fun doubleBackOnStylesStopsAtModes() {
        openStyles(GameMode.Regular)
        doubleBack()
        rule.onNodeWithText(ArabicStrings.gameModes).assertIsDisplayed()
    }

    @Test
    fun doubleBackOnLevelsStopsAtStyles() {
        openStyles(GameMode.Regular)
        rule.onNodeWithText(ArabicStrings.styleTitle(GameStyle.GuessSurah)).performClick()
        waitForText("العاديات حتى الناس")
        doubleBack()
        rule.onNodeWithText(ArabicStrings.chooseStyle).assertIsDisplayed()
    }

    @Test
    fun mutashabihatOpensTheWholeQuranQuiz() {
        // No audio streaming under Robolectric.
        runBlocking { (rule.activity.application as HafizApp).container.progress.setAutoPlay(false) }
        openStyles(GameMode.Mutashabihat)
        rule.onAllNodesWithText(ArabicStrings.styleTitle(GameStyle.AyahPosition)).assertCountEquals(0)
        rule.onNodeWithText(ArabicStrings.styleTitle(GameStyle.GuessSurah)).performClick()
        waitForText(ArabicStrings.whichSurah)
        rule.onNodeWithText("المصحف كاملاً").assertIsDisplayed()
        rule.onAllNodesWithText("العاديات حتى الناس").assertCountEquals(0)
    }

    @Test
    fun doubleTapOnPlayOpensModesOnce() {
        waitForHome()
        rule.mainClock.autoAdvance = false
        val card = rule.onNodeWithText(ArabicStrings.play)
        card.performClick()
        card.performClick()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(ArabicStrings.statistics).assertExists()
    }

    @Test
    fun howToPlayOpensGuideAndBackReturnsHome() {
        waitForHome()
        rule.onNodeWithText(ArabicStrings.howToPlay).performClick()
        waitForText(ArabicStrings.guideStylesHeading)
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(ArabicStrings.statistics).assertExists()
    }

    @Test
    fun makkiMadaniGameAppearsInActivity() {
        waitForHome()
        rule.onNodeWithText(ArabicStrings.play).performClick()
        waitForText(ArabicStrings.gameModes)
        rule.onNodeWithText(ArabicStrings.revelationTitle).performClick()
        waitForText(ArabicStrings.revelationQuestion)
        repeat(5) {
            rule.onNodeWithText(ArabicStrings.makki).performClick()
            rule.onNodeWithText(ArabicStrings.next).performClick()
        }
        waitForText(ArabicStrings.resultTitle)
        rule.onNodeWithText(ArabicStrings.mainMenu).performClick()
        waitForHome()
        rule.onNodeWithText(ArabicStrings.statistics).performClick()
        waitForText(ArabicStrings.tabActivity)
        rule.onNodeWithText(ArabicStrings.tabActivity).performClick()
        // The tiny test viewport composes only the first rows of the lazy list; scroll the round to it.
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(ArabicStrings.revelationTitle))
        rule.onNodeWithText(ArabicStrings.revelationTitle).assertIsDisplayed()
    }

    @Test
    fun languagePickerIsRadioGroupAndClosesOnSelect() {
        waitForHome()
        rule.onNodeWithContentDescription("${ArabicStrings.languageLabel}: العربية").performClick()
        val radio = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        rule.onAllNodes(radio).assertCountEquals(2)
        rule.onNodeWithContentDescription("English").performClick()
        // Closes only after the language has been written and applied.
        rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription("English").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithContentDescription("${EnglishStrings.languageLabel}: English").assertExists()
    }

    @Test
    fun creditsOverlayShowsTanzilAndCloses() {
        waitForHome()
        rule.onNodeWithContentDescription(ArabicStrings.credits).performClick()
        rule.onNodeWithText(ArabicStrings.creditsTanzil).assertIsDisplayed()
        rule.onNodeWithText("AlrazyLabs").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(ArabicStrings.developerName).performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText(ArabicStrings.creditsTanzil).fetchSemanticsNodes().isEmpty() }
    }
}
