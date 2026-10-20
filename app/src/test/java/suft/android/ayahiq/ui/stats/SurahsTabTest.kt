package suft.android.ayahiq.ui.stats

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.testing.TestData
import suft.android.ayahiq.ui.i18n.ArabicStrings
import suft.android.ayahiq.ui.theme.AyahIqTheme

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SurahsTabTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun mapCellsExposeTheirTier() {
        rule.setContent {
            AyahIqTheme(AppLanguage.Arabic, darkTheme = false) {
                SurahsTab(
                    progress = Progress(surahCorrect = mapOf(0 to 1), surahWrong = mapOf(0 to 9)),
                    quran = TestData.quran,
                    selected = null,
                    onSelect = {},
                    onPractice = {},
                    bottomPadding = 0.dp,
                )
            }
        }
        rule.onAllNodesWithContentDescription(ArabicStrings.tierWeak, substring = true).onFirst().assertExists()
    }
}
