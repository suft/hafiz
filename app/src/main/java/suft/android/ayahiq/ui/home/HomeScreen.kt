package suft.android.ayahiq.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import suft.android.ayahiq.AppContainer
import suft.android.ayahiq.R
import suft.android.ayahiq.data.Progress
import suft.android.ayahiq.domain.AppLanguage
import suft.android.ayahiq.domain.GameMode
import suft.android.ayahiq.domain.Levels
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.components.OrnamentDivider

@Composable
fun HomeScreen(
    container: AppContainer,
    quran: Quran,
    onOpenMode: (GameMode) -> Unit,
    onOpenStats: () -> Unit,
) {
    val progress by container.progress.progress.collectAsStateWithLifecycle(initialValue = Progress())
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var showLanguage by rememberSaveable { mutableStateOf(false) }

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
                Image(
                    painter = painterResource(R.drawable.logo_quran),
                    contentDescription = null,
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 132.dp).aspectRatio(1f),
                )
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = strings.homeTagline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OrnamentDivider()
                Spacer(Modifier.height(16.dp))

                GameMode.entries.forEach { mode ->
                    val levels = remember(mode) { Levels.forMode(mode, quran) }
                    val mastered = progress.mastered(levels)
                    MenuCard(
                        icon = if (mode == GameMode.GuessSurah) Icons.Filled.Quiz else Icons.AutoMirrored.Filled.MenuBook,
                        title = strings.modeTitle(mode),
                        subtitle = strings.modeSubtitle(mode),
                        badge = "${strings.number(mastered)}/${strings.number(levels.size)}",
                        onClick = { onOpenMode(mode) },
                    )
                    Spacer(Modifier.height(12.dp))
                }

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
                    onClick = onOpenStats,
                )

                Spacer(Modifier.height(28.dp))
                Attribution()
            }
        }

        // Pinned to the top-right in both languages, so the flag does not jump sides when the language changes.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp), contentAlignment = Alignment.TopEnd) {
                LanguageButton(selected = strings.language, onClick = { showLanguage = true })
            }
        }
        LanguageOverlay(
            visible = showLanguage,
            selected = strings.language,
            onSelect = { scope.launch { container.progress.setLanguage(it) } },
            onClose = { showLanguage = false },
        )
    }
}

@Composable
private fun MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (badge != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(badge, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    Text(LocalStrings.current.mastered, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Tanzil's licence asks apps to credit the source with a link. */
@Composable
private fun Attribution() {
    val strings = LocalStrings.current
    val linkStyle = TextLinkStyles(
        SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
    )
    val text = buildAnnotatedString {
        append(strings.attributionQuranFrom)
        withLink(LinkAnnotation.Url("https://tanzil.net", linkStyle)) { append(strings.attributionTanzil) }
        append(strings.attributionRecitationsFrom)
        withLink(LinkAnnotation.Url("https://everyayah.com", linkStyle)) { append("everyayah.com") }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

private val AppLanguage.flag: String
    get() = when (this) {
        AppLanguage.Arabic -> "\uD83C\uDDF8\uD83C\uDDE6" // 🇸🇦
        AppLanguage.English -> "\uD83C\uDDEC\uD83C\uDDE7" // 🇬🇧
    }

/** Endonyms: they do not change with the UI language. Read by TalkBack together with the flag. */
private val AppLanguage.endonym: String
    get() = when (this) {
        AppLanguage.Arabic -> "العربية"
        AppLanguage.English -> "English"
    }

/** Shows the current language's flag; tapping it opens the [LanguageOverlay]. */
@Composable
private fun LanguageButton(selected: AppLanguage, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
    ) {
        Box(
            Modifier.semantics { contentDescription = "${strings.languageLabel}: ${selected.endonym}" },
            contentAlignment = Alignment.Center,
        ) {
            Text(selected.flag, style = MaterialTheme.typography.titleLarge)
        }
    }
}

/**
 * Full-screen, opaque language picker. Switching the language re-lays out the whole app (direction,
 * text, fonts); doing it behind this overlay hides that transition. The overlay itself is always
 * left-to-right and only shows endonyms, so it does not move while the language changes.
 */
@Composable
private fun LanguageOverlay(
    visible: Boolean,
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onClose: () -> Unit,
) {
    val strings = LocalStrings.current
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        BackHandler(onBack = onClose)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = strings.back)
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .widthIn(max = 360.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        AppLanguage.entries.forEach { lang ->
                            LanguageOption(lang, lang == selected) { if (lang != selected) onSelect(lang) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(language: AppLanguage, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().semantics { this.selected = selected },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLowest,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(language.flag, style = MaterialTheme.typography.headlineMedium)
            Text(
                language.endonym,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = scheme.primary)
        }
    }
}
