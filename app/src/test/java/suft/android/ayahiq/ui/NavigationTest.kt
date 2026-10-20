package suft.android.ayahiq.ui

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
import androidx.activity.ComponentActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import suft.android.ayahiq.MainActivity
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.GameStyle
import suft.android.ayahiq.ui.i18n.ArabicStrings
import suft.android.ayahiq.ui.i18n.EnglishStrings

@RunWith(RobolectricTestRunner::class)
class NavigationTest {

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) =
        rule.waitUntil(15_000) { rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    // The Statistics card exists only on Home among Home/Styles/Levels.
    private fun waitForHome() = waitForText(ArabicStrings.statistics)

    private fun openRegularStyles() {
        waitForHome()
        rule.onNodeWithText(ArabicStrings.gameModeTitle(GameMode.Regular)).performClick()
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
    fun doubleBackOnStylesStopsAtHome() {
        openRegularStyles()
        doubleBack()
        rule.onNodeWithText(ArabicStrings.statistics).assertExists()
    }

    @Test
    fun doubleBackOnLevelsStopsAtStyles() {
        openRegularStyles()
        rule.onNodeWithText(ArabicStrings.styleTitle(GameStyle.GuessSurah)).performClick()
        waitForText("العاديات حتى الناس")
        doubleBack()
        rule.onNodeWithText(ArabicStrings.chooseStyle).assertIsDisplayed()
    }

    @Test
    fun doubleTapOnAModeOpensItOnce() {
        waitForHome()
        rule.mainClock.autoAdvance = false
        val card = rule.onNodeWithText(ArabicStrings.gameModeTitle(GameMode.Regular))
        card.performClick()
        card.performClick()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(ArabicStrings.statistics).assertExists()
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
        rule.onNodeWithContentDescription(ArabicStrings.back).performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText(ArabicStrings.creditsTanzil).fetchSemanticsNodes().isEmpty() }
    }
}
