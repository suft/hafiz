package suft.android.ayahiq.ui.share

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import java.awt.Font
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.ArabicText
import suft.android.ayahiq.testing.TestData
import suft.android.ayahiq.ui.theme.AyahIqTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class ShareCardTest {

    @get:Rule val rule = createComposeRule()

    private val quran = TestData.quran

    @Test
    fun cardFontsCoverTheQuranText() {
        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val needed = sortedSetOf<Int>()
        for (i in 0 until quran.ayahCount) quran.text(i).codePoints().forEach { needed += it }
        needed += listOf(0xFD3E, 0xFD3F) + (0x0660..0x0669)
        needed.removeAll { Character.isWhitespace(it) }
        for (font in CardFont.entries) {
            val awt = resources.openRawResource(font.res).use { Font.createFont(Font.TRUETYPE_FONT, it) }
            val missing = needed.filter { !awt.canDisplay(it) }
            assertTrue("$font cannot display ${missing.map { "U+%04X".format(it) }}", missing.isEmpty())
        }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun longestAyahFitsAtLargeFontScale() {
        val ayah = quran.surahs[1].firstAyah + 281 // 2:282, the longest ayah
        val body = quran.text(ayah) + " " + ArabicText.ayahMarker(quran.ayahNumberInSurah(ayah))
        val reference = quran.reference(ayah..ayah, AppLanguage.Arabic)
        var frame by mutableStateOf(Frame.entries.first())
        val layouts = HashMap<Frame, TextLayoutResult>()
        rule.setContent {
            AyahIqTheme(AppLanguage.Arabic, darkTheme = false) {
                DeviceConfigurationOverride(
                    DeviceConfigurationOverride.Companion.ForcedSize(DpSize(360.dp, 900.dp))
                        .then(DeviceConfigurationOverride.Companion.FontScale(2f)),
                ) {
                    val shown = frame
                    AyahCard(
                        body = body,
                        reference = reference,
                        frame = shown,
                        font = CardFont.Quran,
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(frame.aspect),
                        onBodyLayout = { layouts[shown] = it },
                    )
                }
            }
        }
        for (f in Frame.entries) {
            frame = f
            rule.waitForIdle()
            val result = layouts[f]
            assertTrue("$f: no text layout", result != null)
            assertFalse("$f: text overflows the card", result!!.hasVisualOverflow)
        }
    }

    @Test
    fun frameThumbnailsAreRadioButtons() {
        rule.setContent {
            AyahIqTheme(AppLanguage.Arabic, darkTheme = false) {
                ShareCardScreen(quran = quran, ayahs = 0..0, onBack = {})
            }
        }
        val radio = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        rule.onAllNodes(radio).assertCountEquals(Frame.entries.size)
    }
}
