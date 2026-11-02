package suft.android.hafiz.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import suft.android.hafiz.R
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.theme.LocalQuizColors
import suft.android.hafiz.ui.theme.TitleFont

/** Plain paper-coloured background (dark surface in the dark theme) behind every screen. */
@Composable
fun PaperBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalStrings.current.back)
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
        }
    }
}

/** The gold ornamental rule from the original main menu. */
@Composable
fun OrnamentDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.divider_ornament),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(LocalQuizColors.current.gold),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(18.dp)
                .padding(vertical = 2.dp),
        )
    }
}

/** The app name in the Rakkas title face, as on the main menu and the load screen. */
@Composable
fun AppTitle(modifier: Modifier = Modifier) {
    Text(
        text = LocalStrings.current.appName,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium.copy(fontFamily = TitleFont, fontSize = 52.sp),
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
