package suft.android.hafiz.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import suft.android.hafiz.domain.GameMode
import suft.android.hafiz.domain.GameStyle
import suft.android.hafiz.ui.components.AppTopBar
import suft.android.hafiz.ui.i18n.LocalStrings
import suft.android.hafiz.ui.modes.RevelationIcon
import suft.android.hafiz.ui.modes.icon
import suft.android.hafiz.ui.styles.icon
import suft.android.hafiz.ui.theme.LocalQuizColors

/** "How to play": the question styles, game modes, levels, statistics and settings, in cards like the rest of the menu. */
@Composable
fun GuideScreen(onBack: () -> Unit) {
    val strings = LocalStrings.current
    val quizColors = LocalQuizColors.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.howToPlay, onBack = onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                GuideSection(strings.guideStylesHeading) {
                    GameStyle.entries.forEach { style ->
                        GuideCard {
                            GuideEntry(style.icon, strings.styleTitle(style), strings.styleSubtitle(style))
                        }
                    }
                }

                GuideSection(strings.gameModes) {
                    GameMode.entries.forEach { mode ->
                        GuideCard {
                            GuideEntry(mode.icon, strings.gameModeTitle(mode), strings.gameModeSubtitle(mode))
                        }
                    }
                    GuideCard { GuideEntry(RevelationIcon, strings.revelationTitle, strings.revelationSubtitle) }
                }

                GuideSection(strings.guideLevelsHeading) {
                    GuideCard {
                        Text(
                            strings.guideLevels,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                        )
                        GameStyle.entries.forEach { style ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = quizColors.correct,
                                    modifier = Modifier.size(24.dp),
                                )
                                Text(
                                    strings.guideMastery(strings.styleTitle(style), style.questionsPerRound, style.masteryScore),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                GuideSection(strings.guideStatsHeading) {
                    GuideCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            IconBadge(Icons.Filled.Insights)
                            Text(
                                strings.guideStats,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                GuideSection(strings.settings) {
                    GuideCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            IconBadge(Icons.Filled.Settings)
                            Text(
                                strings.guideSettings,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A titled group of cards: the heading sits above, with an accent rule under it. */
@Composable
private fun GuideSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() },
            )
            HorizontalDivider(thickness = 2.dp, color = LocalQuizColors.current.gold, modifier = Modifier.widthIn(max = 72.dp))
        }
        content()
    }
}

@Composable
private fun GuideCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content,
    )
}

/** An icon, a title and a description side by side, as in the menu cards. */
@Composable
private fun GuideEntry(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconBadge(icon)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector) {
    Box(
        Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
