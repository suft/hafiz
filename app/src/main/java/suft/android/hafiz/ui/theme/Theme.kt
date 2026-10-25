package suft.android.hafiz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import suft.android.hafiz.R
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.i18n.stringsFor

// ---------------------------------------------------------------- fonts

/** Quranic script (Amiri Quran, SIL OFL) for ayah text. */
val QuranFont = FontFamily(Font(R.font.amiri_quran))

/** Naskh display face for titles. */
val NaskhFont = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold),
)

/** Bold brush-calligraphy face (Rakkas, SIL OFL) for the app title, in Arabic and Latin. */
val TitleFont = FontFamily(Font(R.font.rakkas_regular, FontWeight.Normal))

/** Latin UI face for the English language (Inter, SIL OFL). */
val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
)

// ---------------------------------------------------------------- colours

/** Colours outside the Material scheme. */
@Immutable
data class QuizColors(
    val correct: Color,
    val onCorrect: Color,
    val correctContainer: Color,
    val wrong: Color,
    val wrongContainer: Color,
    val gold: Color,
    val paperIsTextured: Boolean,
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0B5D3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE8D6),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF7A5C14),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E1B0),
    onSecondaryContainer = Color(0xFF2B1F00),
    tertiary = Color(0xFF8A2232),
    background = Color(0xFFF4E9CF),
    onBackground = Color(0xFF2A1F12),
    surface = Color(0xFFFBF5E6),
    onSurface = Color(0xFF2A1F12),
    surfaceVariant = Color(0xFFEDE1C4),
    onSurfaceVariant = Color(0xFF5B4E37),
    surfaceContainerLowest = Color(0xFFFFFCF4),
    surfaceContainerLow = Color(0xFFFBF5E6),
    surfaceContainer = Color(0xFFF6EDD8),
    surfaceContainerHigh = Color(0xFFF0E5CB),
    surfaceContainerHighest = Color(0xFFEADDBE),
    outline = Color(0xFF8A7440),
    outlineVariant = Color(0xFFDCCBA4),
    error = Color(0xFFB3261E),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8BD5AB),
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF0E5233),
    onPrimaryContainer = Color(0xFFCDE8D6),
    secondary = Color(0xFFE3C46F),
    onSecondary = Color(0xFF3D2E00),
    secondaryContainer = Color(0xFF574413),
    onSecondaryContainer = Color(0xFFF3E1B0),
    tertiary = Color(0xFFF0B5A6),
    background = Color(0xFF101914),
    onBackground = Color(0xFFE6E1D6),
    surface = Color(0xFF151F19),
    onSurface = Color(0xFFE6E1D6),
    surfaceVariant = Color(0xFF2B352E),
    onSurfaceVariant = Color(0xFFC4C9BF),
    surfaceContainerLowest = Color(0xFF1E2A23),
    surfaceContainerLow = Color(0xFF18221C),
    surfaceContainer = Color(0xFF1C2620),
    surfaceContainerHigh = Color(0xFF26312A),
    surfaceContainerHighest = Color(0xFF313C35),
    outline = Color(0xFF8E9389),
    outlineVariant = Color(0xFF3F4942),
    error = Color(0xFFFFB4AB),
)

private val LightQuiz = QuizColors(
    correct = Color(0xFF095D33),
    onCorrect = Color.White,
    correctContainer = Color(0xFFCDEBD5),
    wrong = Color(0xFFB3261E),
    wrongContainer = Color(0xFFF9DEDA),
    gold = Color(0xFFC4942E),
    paperIsTextured = true,
)

private val DarkQuiz = QuizColors(
    correct = Color(0xFF8BD5AB),
    onCorrect = Color(0xFF00391D),
    correctContainer = Color(0xFF1E4A31),
    wrong = Color(0xFFFFB4AB),
    wrongContainer = Color(0xFF5C1E19),
    gold = Color(0xFFE3C46F),
    paperIsTextured = false,
)

val LocalQuizColors = staticCompositionLocalOf { LightQuiz }

// ---------------------------------------------------------------- typography

private fun typography(language: AppLanguage): Typography {
    val base = Typography()
    val tightLines = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    if (language == AppLanguage.English) {
        fun TextStyle.inter(weight: FontWeight? = null) = copy(fontFamily = InterFont, fontWeight = weight ?: fontWeight)
        return base.copy(
            displayLarge = base.displayLarge.inter(),
            displayMedium = base.displayMedium.inter(),
            displaySmall = base.displaySmall.inter(FontWeight.Bold),
            headlineLarge = base.headlineLarge.inter(FontWeight.Bold),
            headlineMedium = base.headlineMedium.inter(FontWeight.Bold),
            headlineSmall = base.headlineSmall.inter(FontWeight.Bold),
            titleLarge = base.titleLarge.inter(FontWeight.Bold).copy(lineHeightStyle = tightLines),
            titleMedium = base.titleMedium.inter(),
            titleSmall = base.titleSmall.inter(),
            bodyLarge = base.bodyLarge.inter(),
            bodyMedium = base.bodyMedium.inter(),
            bodySmall = base.bodySmall.inter(),
            labelLarge = base.labelLarge.inter(),
            labelMedium = base.labelMedium.inter(),
            labelSmall = base.labelSmall.inter(),
        )
    }
    return base.copy(
        displaySmall = base.displaySmall.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold, lineHeightStyle = tightLines),
    )
}

/** Style for Quranic text. */
fun quranTextStyle(size: Int): TextStyle = TextStyle(
    fontFamily = QuranFont,
    fontSize = size.sp,
    lineHeight = (size * 2.45).sp,
    // Ayah text stays right-to-left inside an LTR layout.
    textDirection = TextDirection.Rtl,
    localeList = LocaleList("ar"),
)

// ---------------------------------------------------------------- theme

@Composable
fun HafizTheme(
    language: AppLanguage,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme: ColorScheme = if (darkTheme) DarkScheme else LightScheme
    // The layout direction follows the UI language, regardless of the device language.
    CompositionLocalProvider(
        LocalLayoutDirection provides if (language == AppLanguage.English) LayoutDirection.Ltr else LayoutDirection.Rtl,
        LocalStrings provides stringsFor(language),
        LocalQuizColors provides if (darkTheme) DarkQuiz else LightQuiz,
    ) {
        MaterialTheme(colorScheme = scheme, typography = remember(language) { typography(language) }) {
            // The paper background is not a Surface, so without this text defaults to black, unreadable in dark mode.
            CompositionLocalProvider(LocalContentColor provides scheme.onBackground, content = content)
        }
    }
}
