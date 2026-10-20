package suft.android.ayahiq.ui.components

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import suft.android.ayahiq.R
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.theme.LocalQuizColors
import suft.android.ayahiq.ui.theme.TitleFont

/** Old-paper texture from the original app (light theme) or a plain dark surface. */
@Composable
fun PaperBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (LocalQuizColors.current.paperIsTextured) {
            Image(
                painter = painterResource(R.drawable.paper_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.85f),
            )
        }
        content()
    }
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
