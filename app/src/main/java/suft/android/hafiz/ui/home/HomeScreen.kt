package suft.android.hafiz.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import suft.android.hafiz.AppContainer
import suft.android.hafiz.data.Progress
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.MushafEdition
import suft.android.hafiz.ui.components.AppTitle
import suft.android.hafiz.ui.components.MenuCard
import suft.android.hafiz.ui.components.OrnamentDivider
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.i18n.SystemAppLanguage

@Composable
fun HomeScreen(
    container: AppContainer,
    progress: Progress,
    onOpenPlay: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenStats: () -> Unit,
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showCredits by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(32.dp))
                AppTitle()
                Spacer(Modifier.height(12.dp))
                OrnamentDivider()
                Spacer(Modifier.height(16.dp))

                MenuCard(
                    icon = Icons.Filled.PlayArrow,
                    title = strings.play,
                    subtitle = strings.playHint,
                    badge = null,
                    badgeLabel = null,
                    onClick = onOpenPlay,
                )
                Spacer(Modifier.height(12.dp))
                MenuCard(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = strings.howToPlay,
                    subtitle = strings.howToPlayHint,
                    badge = null,
                    badgeLabel = null,
                    onClick = onOpenGuide,
                )
                Spacer(Modifier.height(12.dp))

                val overall = progress.overall
                MenuCard(
                    icon = Icons.Filled.Insights,
                    title = strings.statistics,
                    subtitle = if (overall.total == 0) {
                        strings.statisticsHint
                    } else {
                        strings.statisticsSummary(overall.percent, overall.total)
                    },
                    badge = null,
                    badgeLabel = null,
                    onClick = onOpenStats,
                )
            }
        }

        // Pinned to the same corners in both languages, so the buttons do not jump sides when the language changes.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
                CornerButton(
                    strings.credits,
                    onClick = { showCredits = true },
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                CornerButton(
                    strings.settings,
                    onClick = { showSettings = true },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        SettingsOverlay(
            visible = showSettings,
            language = strings.language,
            onLanguage = { scope.launch { container.progress.setLanguage(it); SystemAppLanguage.set(context, it) } },
            edition = progress.mushafEdition,
            onEdition = { scope.launch { container.progress.setMushafEdition(it) } },
            onClose = { showSettings = false },
        )
        CreditsOverlay(visible = showCredits, onClose = { showCredits = false })
    }
}

/** Endonyms: they do not change with the UI language. */
private val AppLanguage.endonym: String
    get() = when (this) {
        AppLanguage.Arabic -> "العربية"
        AppLanguage.English -> "English"
    }

/** What the language button and picker show; TalkBack reads the [endonym] instead. */
private val AppLanguage.flag: String
    get() = when (this) {
        AppLanguage.Arabic -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        AppLanguage.English -> "\uD83C\uDDEC\uD83C\uDDE7" // 🇬🇧
    }

/** 48 dp round button pinned to a top corner of the main menu. */
@Composable
private fun CornerButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Box(Modifier.semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/** Opaque full-screen overlay over the main menu: fades in/out, Back or × closes it; [content] is centred and scrolls. */
@Composable
private fun FullScreenOverlay(
    visible: Boolean,
    onClose: () -> Unit,
    closeAlignment: Alignment,
    content: @Composable ColumnScope.() -> Unit,
) {
    val strings = LocalStrings.current
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        BackHandler(onBack = onClose)
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 360.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content,
                )
                // Declared last so it stays on top: a content column taller than the screen would otherwise swallow its taps.
                IconButton(onClick = onClose, modifier = Modifier.align(closeAlignment).padding(8.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = strings.back)
                }
            }
        }
    }
}

/**
 * Full-screen, opaque settings page: the app language (flags only, forced LTR so they keep their places when the
 * language switches; TalkBack reads the endonyms) and the mushaf edition that Where's the Ayah? uses. Choices apply
 * immediately and the page stays open until × or Back.
 */
@Composable
private fun SettingsOverlay(
    visible: Boolean,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    edition: MushafEdition,
    onEdition: (MushafEdition) -> Unit,
    onClose: () -> Unit,
) {
    val strings = LocalStrings.current
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    FullScreenOverlay(visible, onClose, closeAlignment = AbsoluteAlignment.TopRight) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(strings.settings, style = MaterialTheme.typography.headlineSmall)
        Text(strings.languageLabel, style = MaterialTheme.typography.labelLarge, color = labelColor)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                AppLanguage.entries.forEach { lang ->
                    LanguageOption(lang, lang == language) { if (lang != language) onLanguage(lang) }
                }
            }
        }
        Text(
            strings.mushafEditionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
        Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MushafEdition.entries.forEach { e ->
                EditionOption(e, e == edition) { if (e != edition) onEdition(e) }
            }
        }
    }
}

@Composable
private fun LanguageOption(language: AppLanguage, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier
            .size(112.dp)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = language.endonym },
        shape = shape,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLowest,
        border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Sized in dp so a large system font cannot push the flag out of its tile.
            Text(
                language.flag,
                fontSize = with(LocalDensity.current) { 56.dp.toSp() },
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

@Composable
private fun EditionOption(edition: MushafEdition, selected: Boolean, onClick: () -> Unit) {
    val strings = LocalStrings.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = shape,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLowest,
        border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                strings.mushafEditionName(edition),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

private const val PRIVACY_POLICY_URL = "https://suft.github.io/hafiz/privacy-policy.html"

/**
 * Credits the data sources: Tanzil's licence asks for a link; the mutashabihat come from Aswaatul Qurraa;
 * the ayah positions in the mushaf come from quran.com; the last entry links the privacy policy.
 */
@Composable
private fun CreditsOverlay(visible: Boolean, onClose: () -> Unit) {
    val strings = LocalStrings.current
    FullScreenOverlay(visible, onClose, closeAlignment = AbsoluteAlignment.TopLeft) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(strings.credits, style = MaterialTheme.typography.headlineSmall)
        CreditEntry(strings.creditsQuranText, strings.creditsTanzil, "https://tanzil.net")
        CreditEntry(strings.creditsRecitations, "everyayah.com", "https://everyayah.com")
        CreditEntry(
            strings.creditsMutashabihat,
            strings.creditsAswaatulQurraa,
            "https://www.aswaatulqurraa.com/mutashabihat/",
        )
        CreditEntry(strings.creditsMushafLayout, "quran.com", "https://quran.com")
        CreditEntry(strings.creditsInspiredBy, "AlrazyLabs")
        CreditEntry(strings.creditsDeveloper, strings.developerName)
        CreditEntry(strings.privacyPolicy, "suft.github.io/hafiz", PRIVACY_POLICY_URL)
    }
}

@Composable
private fun CreditEntry(label: String, source: String, url: String? = null) {
    val linkStyle = TextLinkStyles(
        SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
    )
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = buildAnnotatedString {
                if (url != null) withLink(LinkAnnotation.Url(url, linkStyle)) { append(source) } else append(source)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}
