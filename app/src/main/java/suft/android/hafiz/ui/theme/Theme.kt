package suft.android.hafiz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.unit.dp
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

/** Bold brush-calligraphy face (Rakkas, SIL OFL) for the Arabic app title. */
val TitleFont = FontFamily(Font(R.font.rakkas_regular, FontWeight.Normal))

/**
 * Heavy face with angular, flat-nib cuts (Marhey, SIL OFL; the Bold instance of its variable font, so API 24 and 25
 * do not fall back to the Light default) for the English app title: it echoes the strokes of [TitleFont].
 */
val LatinTitleFont = FontFamily(Font(R.font.marhey_bold, FontWeight.Normal))

/** Latin UI face for the English language (Inter, SIL OFL). */
val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Arabic UI face for body, label and small-title text (IBM Plex Sans Arabic, SIL OFL); it also has Latin glyphs. */
val PlexArabicFont = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold),
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
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0F6B45),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EEDD),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4E6355),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2EAE3),
    onSecondaryContainer = Color(0xFF0B1F14),
    tertiary = Color(0xFF7C5E10),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8E7B5),
    onTertiaryContainer = Color(0xFF271900),
    background = Color(0xFFF3F4F1),
    onBackground = Color(0xFF1B1C1A),
    surface = Color(0xFFF3F4F1),
    onSurface = Color(0xFF1B1C1A),
    surfaceVariant = Color(0xFFE3E4DF),
    onSurfaceVariant = Color(0xFF5D5F5A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFEEEFEB),
    surfaceContainerHigh = Color(0xFFE8E9E5),
    surfaceContainerHighest = Color(0xFFE2E3DF),
    outline = Color(0xFF8C8E88),
    outlineVariant = Color(0xFFD9DAD4),
    error = Color(0xFFBA1A1A),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF7FD8A6),
    onPrimary = Color(0xFF003920),
    primaryContainer = Color(0xFF0F5235),
    onPrimaryContainer = Color(0xFF9EF4C1),
    secondary = Color(0xFFB5CCBA),
    onSecondary = Color(0xFF213529),
    secondaryContainer = Color(0xFF374B3E),
    onSecondaryContainer = Color(0xFFD1E8D6),
    tertiary = Color(0xFFE6C26B),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = Color(0xFFFFDF9C),
    background = Color(0xFF111312),
    onBackground = Color(0xFFE3E3DF),
    surface = Color(0xFF111312),
    onSurface = Color(0xFFE3E3DF),
    surfaceVariant = Color(0xFF2E312F),
    onSurfaceVariant = Color(0xFFC2C5BF),
    surfaceContainerLowest = Color(0xFF0C0E0D),
    surfaceContainerLow = Color(0xFF1A1D1B),
    surfaceContainer = Color(0xFF1E2120),
    surfaceContainerHigh = Color(0xFF282B2A),
    surfaceContainerHighest = Color(0xFF333634),
    outline = Color(0xFF8C8F8A),
    outlineVariant = Color(0xFF424542),
    error = Color(0xFFFFB4AB),
)

private val LightQuiz = QuizColors(
    correct = Color(0xFF0F7A47),
    onCorrect = Color.White,
    correctContainer = Color(0xFFD4F0DF),
    wrong = Color(0xFFC0362C),
    wrongContainer = Color(0xFFFBE1DE),
    gold = Color(0xFFB8892A),
)

private val DarkQuiz = QuizColors(
    correct = Color(0xFF7FD8A6),
    onCorrect = Color(0xFF003920),
    correctContainer = Color(0xFF1C4A33),
    wrong = Color(0xFFFFB4AB),
    wrongContainer = Color(0xFF5F1F19),
    gold = Color(0xFFE6C26B),
)

private val HafizShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
    fun TextStyle.plex() = copy(fontFamily = PlexArabicFont)
    return base.copy(
        displayLarge = base.displayLarge.plex(),
        displayMedium = base.displayMedium.plex(),
        displaySmall = base.displaySmall.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = NaskhFont, fontWeight = FontWeight.Bold, lineHeightStyle = tightLines),
        titleMedium = base.titleMedium.plex(),
        titleSmall = base.titleSmall.plex(),
        bodyLarge = base.bodyLarge.plex(),
        bodyMedium = base.bodyMedium.plex(),
        bodySmall = base.bodySmall.plex(),
        labelLarge = base.labelLarge.plex(),
        labelMedium = base.labelMedium.plex(),
        labelSmall = base.labelSmall.plex(),
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
        MaterialTheme(
            colorScheme = scheme,
            shapes = HafizShapes,
            typography = remember(language) { typography(language) },
        ) {
            // The app background is not a Surface, so without this text defaults to black, unreadable in dark mode.
            CompositionLocalProvider(LocalContentColor provides scheme.onBackground, content = content)
        }
    }
}
