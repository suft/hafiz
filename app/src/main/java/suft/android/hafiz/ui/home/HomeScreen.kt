package suft.android.hafiz.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
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
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import suft.android.hafiz.AppContainer
import suft.android.hafiz.data.Progress
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.domain.MushafEdition
import suft.android.hafiz.ui.components.AppTitle
import suft.android.hafiz.ui.components.hafizCardColors
import suft.android.hafiz.ui.components.pressScale
import suft.android.hafiz.ui.components.rememberToday
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.i18n.SystemAppLanguage
import suft.android.hafiz.ui.icons.HafizIcons
import suft.android.hafiz.ui.theme.InterFont
import suft.android.hafiz.ui.theme.NaskhFont

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
    val today = rememberToday()

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
                Spacer(Modifier.height(56.dp))
                AppTitle()
                Text(
                    strings.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))

                HomeHero(progress, today, onOpenPlay)
                Spacer(Modifier.height(12.dp))

                val overall = progress.overall
                val guideTitle = strings.howToPlay
                val statsSubtitle = if (overall.total == 0) {
                    strings.statisticsHint
                } else {
                    strings.statisticsSummary(overall.percent, overall.total)
                }
                if (LocalDensity.current.fontScale >= 1.5f) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeTile(
                            HafizIcons.Help, guideTitle, strings.howToPlayHint, onOpenGuide,
                            Modifier.fillMaxWidth(),
                        )
                        HomeTile(
                            HafizIcons.Statistics, strings.statistics, statsSubtitle, onOpenStats,
                            Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HomeTile(
                            HafizIcons.Help, guideTitle, strings.howToPlayHint, onOpenGuide,
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                        HomeTile(
                            HafizIcons.Statistics, strings.statistics, statsSubtitle, onOpenStats,
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
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
                    Icon(HafizIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CornerButton(
                    strings.settings,
                    onClick = { showSettings = true },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(HafizIcons.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

/** The Play entry: a large primary-coloured card with today's numbers once there are any. */
@Composable
private fun HomeHero(progress: Progress, today: Long, onClick: () -> Unit) {
    val strings = LocalStrings.current
    val scheme = MaterialTheme.colorScheme
    val source = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.fillMaxWidth().pressScale(source),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = scheme.primary, contentColor = scheme.onPrimary),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.play, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        strings.playHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = 0.85f),
                    )
                }
                Box(Modifier.size(56.dp).background(scheme.onPrimary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(HafizIcons.Play, contentDescription = null, modifier = Modifier.size(32.dp), tint = scheme.primary)
                }
            }
            if (progress.overall.total > 0) {
                Spacer(Modifier.height(16.dp))
                val stats = listOf(
                    strings.number(progress.currentDayStreak(today)) to strings.currentStreak,
                    strings.number(progress.answersToday(today)) to strings.questionsToday,
                    strings.percent(progress.overall.percent) to strings.accuracy,
                )
                if (LocalDensity.current.fontScale >= 1.5f) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stats.forEach { (value, label) -> HeroStat(value, label, Modifier.fillMaxWidth()) }
                    }
                } else {
                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        stats.forEach { (value, label) -> HeroStat(value, label, Modifier.weight(1f).fillMaxHeight()) }
                    }
                }
            }
        }
    }
}

/** One number and its label on a translucent pill inside [HomeHero]. */
@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = onPrimary.copy(alpha = 0.12f),
        contentColor = onPrimary,
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = onPrimary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

/** A small secondary entry on the main menu: icon, title and one line of hint. */
@Composable
private fun HomeTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val source = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = source,
        modifier = modifier.pressScale(source),
        colors = hafizCardColors(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(40.dp).background(scheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = scheme.onPrimaryContainer)
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
}

/** Endonyms, shown on the language tiles and read by TalkBack: they do not change with the UI language. */
private val AppLanguage.endonym: String
    get() = when (this) {
        AppLanguage.Arabic -> "العربية"
        AppLanguage.English -> "English"
    }

/** 48 dp round button pinned to a top corner of the main menu. */
@Composable
private fun CornerButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = source,
        modifier = modifier.size(48.dp).pressScale(source),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                    Icon(HafizIcons.Close, contentDescription = strings.back)
                }
            }
        }
    }
}

/**
 * Full-screen, opaque settings page: the app language (each language's own name, forced LTR so the tiles keep their
 * places when the language switches) and the mushaf edition that Where's the Ayah? uses. Choices apply
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
            HafizIcons.Settings,
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
    val source = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .size(112.dp)
            .pressScale(source)
            .clip(shape)
            .selectable(
                selected = selected,
                interactionSource = source,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = language.endonym },
        shape = shape,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow,
        border = if (selected) BorderStroke(2.dp, scheme.primary) else null,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Sized in dp so a large system font cannot push the name out of its tile.
            Text(
                language.endonym,
                fontSize = with(LocalDensity.current) { 22.dp.toSp() },
                fontFamily = if (language == AppLanguage.Arabic) NaskhFont else InterFont,
                fontWeight = FontWeight.Bold,
                color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
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
    val source = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .pressScale(source)
            .clip(shape)
            .selectable(
                selected = selected,
                interactionSource = source,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = shape,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow,
        border = if (selected) BorderStroke(2.dp, scheme.primary) else null,
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
 * Credits as cards in three groups: the sources (Tanzil's licence asks for a link; the mutashabihat come from
 * Aswaatul Qurraa; the ayah positions of both mushaf prints come from quran.com), the people, and the privacy
 * policy. A card with a link is one tap target that opens it in the browser.
 */
@Composable
private fun CreditsOverlay(visible: Boolean, onClose: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }
    FullScreenOverlay(visible, onClose, closeAlignment = AbsoluteAlignment.TopLeft) {
        Icon(
            HafizIcons.Info,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(strings.credits, style = MaterialTheme.typography.headlineSmall)
        if (!version.isNullOrBlank()) {
            Text(
                "${strings.appName} · ${strings.versionLabel(version)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CreditGroup(strings.creditsSourcesHeading) {
            CreditCard(
                strings.creditsQuranText,
                strings.creditsTanzil,
                note = strings.creditsTanzilLicence,
                url = "https://tanzil.net",
            )
            CreditCard(strings.creditsRecitations, "everyayah.com", url = "https://everyayah.com")
            CreditCard(
                strings.creditsMutashabihat,
                strings.creditsAswaatulQurraa,
                url = "https://www.aswaatulqurraa.com/mutashabihat/",
            )
            CreditCard(
                strings.creditsMushafLayout,
                "quran.com",
                note = strings.creditsMushafEditions,
                url = "https://quran.com",
            )
        }
        CreditGroup(strings.creditsPeopleHeading) {
            CreditCard(strings.creditsInspiredBy, "AlrazyLabs")
            CreditCard(strings.creditsDeveloper, strings.developerName)
        }
        CreditGroup(strings.creditsLegalHeading) {
            CreditCard(strings.privacyPolicy, "suft.github.io/hafiz", url = PRIVACY_POLICY_URL)
        }
    }
}

/** A heading above its cards. */
@Composable
private fun CreditGroup(heading: String, cards: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(heading, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        cards()
    }
}

/** One credit: label, source and an optional note, centred; with a [url] the whole card opens it. */
@Composable
private fun CreditCard(label: String, source: String, note: String? = null, url: String? = null) {
    val strings = LocalStrings.current
    val uriHandler = LocalUriHandler.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (url == null) Modifier else Modifier.pressScale(interaction))
            .clip(shape)
            .then(
                if (url == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = ripple(),
                        onClickLabel = strings.opensInBrowser,
                        role = Role.Button,
                    ) { uriHandler.openUri(url) }
                },
            ),
        shape = shape,
        color = scheme.surfaceContainerLow,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                Modifier.padding(horizontal = 44.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                Text(
                    source,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (url != null) scheme.primary else scheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                if (note != null) {
                    Text(
                        note,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (url != null) {
                Icon(
                    HafizIcons.OpenExternal,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp).size(20.dp),
                    tint = scheme.primary,
                )
            }
        }
    }
}
