package suft.android.hafiz.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import suft.android.hafiz.data.localEpochDay
import suft.android.hafiz.data.millisUntilNextLocalDay
import suft.android.hafiz.domain.AppLanguage
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.icons.HafizIcons
import suft.android.hafiz.ui.theme.LatinTitleFont
import suft.android.hafiz.ui.theme.TitleFont
import kotlin.math.roundToInt

/** Plain background colour behind every screen. */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(HafizIcons.Back, contentDescription = LocalStrings.current.back)
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

/** A big tappable card used for menu entries; [badge] (with [badgeLabel] beside or under it) shows the progress. */
@Composable
fun MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String?,
    badgeLabel: String?,
    onClick: () -> Unit,
) {
    // With a large system font the trailing badge squeezes the title into one-word lines; put it under the text.
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val source = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.fillMaxWidth().pressScale(source),
        colors = hafizCardColors(),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (largeText && badge != null) {
                    Text(
                        if (badgeLabel != null) "$badge $badgeLabel" else badge,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            if (!largeText && badge != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(badge, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    if (badgeLabel != null) {
                        Text(badgeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (badge == null) {
                Icon(
                    HafizIcons.Chevron,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A title face with its size and how far its box reaches above and below the baseline (all in sp). */
private class TitleFace(val family: FontFamily, val size: TextUnit, val above: TextUnit, val below: TextUnit)

// Tuned by eye so both names look equally big and sit the same distance from the tagline: Rakkas draws the Arabic
// tails below the baseline, while Marhey's empty descender would otherwise leave a big gap under "Hafiz".
private val ArabicTitle = TitleFace(TitleFont, 56.sp, above = 40.sp, below = 17.sp)
private val EnglishTitle = TitleFace(LatinTitleFont, 42.sp, above = 34.sp, below = 12.sp)

/** Sizes the box from the last baseline, so a face's own ascent and descent add no space above or below the text. */
private fun Modifier.boxFromBaseline(above: TextUnit, below: TextUnit) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val top = above.roundToPx()
    layout(placeable.width, top + below.roundToPx()) { placeable.place(0, top - placeable[LastBaseline]) }
}

/** The app name in the Rakkas (Arabic) or Marhey (English) title face, as on the main menu and the load screen. */
@Composable
fun AppTitle(modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val face = if (strings.language == AppLanguage.English) EnglishTitle else ArabicTitle
    Text(
        text = strings.appName,
        modifier = modifier.boxFromBaseline(face.above, face.below),
        style = MaterialTheme.typography.displayMedium.copy(fontFamily = face.family, fontSize = face.size),
        color = MaterialTheme.colorScheme.primary,
    )
}

/**
 * [verticalScroll] plus a thin thumb along the end edge, so a pane that holds more than it shows says so. The thumb
 * is drawn only while there is something to scroll, dimmer at rest and brighter while the pane moves. It is drawn over
 * the pane's end edge: leave a few dp of end padding inside if the content would otherwise run underneath it.
 * [inset] keeps the thumb off the edge and off the ends of the track (more for a pane with rounded corners).
 */
@Composable
fun Modifier.indicatedVerticalScroll(state: ScrollState, inset: Dp = 2.dp): Modifier {
    val color = MaterialTheme.colorScheme.onSurface
    val alpha by animateFloatAsState(if (state.isScrollInProgress) 0.6f else 0.3f, label = "scrollIndicator")
    return this
        .drawWithContent {
            drawContent()
            val range = state.maxValue
            if (range <= 0) return@drawWithContent
            val thickness = 3.dp.toPx()
            val gap = inset.toPx()
            val track = (size.height - 2 * gap).coerceAtLeast(0f)
            val length = (track * track / (track + range)).coerceIn(24.dp.toPx().coerceAtMost(track), track)
            val top = gap + (track - length) * state.value / range
            val left = if (layoutDirection == LayoutDirection.Rtl) gap else size.width - gap - thickness
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(thickness, length),
                cornerRadius = CornerRadius(thickness / 2),
                alpha = alpha,
            )
        }
        .verticalScroll(state)
}

/** Card colours for every card-like surface: white in light, a step above the background in dark. */
@Composable
fun hafizCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

/** True when the system's animation scale is 0 ("Remove animations"). */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/** Shrinks the element slightly while it is pressed and springs it back; off when animations are off. */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduce = rememberReduceMotion()
    val scale by animateFloatAsState(
        if (pressed && !reduce) 0.97f else 1f,
        spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** A determinate progress bar with rounded ends and no end-of-track dot. */
@Composable
fun RoundedProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) = LinearProgressIndicator(
    progress = progress,
    modifier = modifier.fillMaxWidth().height(height),
    color = color,
    trackColor = trackColor,
    strokeCap = StrokeCap.Round,
    gapSize = 0.dp,
    drawStopIndicator = {},
)

/** Local epoch day that updates at midnight and whenever the screen resumes. */
@Composable
fun rememberToday(): Long {
    var today by remember { mutableLongStateOf(localEpochDay()) }
    LifecycleResumeEffect(Unit) {
        today = localEpochDay()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(millisUntilNextLocalDay() + 1_000)
            today = localEpochDay()
        }
    }
    return today
}

/** Counts from 0 up to [target] when first shown (and between later targets); snaps when animations are off. */
@Composable
fun animatedCount(target: Int): Int {
    val reduce = rememberReduceMotion()
    val value = remember { Animatable(if (reduce) target.toFloat() else 0f) }
    LaunchedEffect(target) {
        if (reduce) {
            value.snapTo(target.toFloat())
        } else {
            value.animateTo(target.toFloat(), tween(700, easing = FastOutSlowInEasing))
        }
    }
    return value.value.roundToInt()
}

/** The transition between questions: the new content fades and slides up in, the old one fades out quickly. */
fun <S> AnimatedContentTransitionScope<S>.questionSwap(reduceMotion: Boolean): ContentTransform =
    if (reduceMotion) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        (fadeIn(tween(220, delayMillis = 90)) + slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it / 10 })
            .togetherWith(fadeOut(tween(90)))
            .using(SizeTransform(clip = false))
    }
