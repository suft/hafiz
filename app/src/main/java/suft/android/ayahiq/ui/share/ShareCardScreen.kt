package suft.android.ayahiq.ui.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import suft.android.ayahiq.R
import suft.android.ayahiq.domain.ArabicText
import suft.android.ayahiq.domain.Quran
import suft.android.ayahiq.ui.i18n.LocalStrings
import suft.android.ayahiq.ui.components.AppTopBar
import suft.android.ayahiq.ui.theme.CordobaFont
import suft.android.ayahiq.ui.theme.NaskhFont
import suft.android.ayahiq.ui.theme.QuranFont

/**
 * Decorative frames from the original app, with the fraction of each side taken by the border
 * (measured from the bitmaps, plus a small margin).
 */
private enum class Frame(
    @DrawableRes val res: Int,
    val aspect: Float,
    val start: Float,
    val top: Float,
    val end: Float,
    val bottom: Float,
) {
    Green(R.drawable.frame_2, 480f / 640f, 0.16f, 0.12f, 0.16f, 0.12f),
    Arabesque(R.drawable.frame_5, 409f / 561f, 0.22f, 0.16f, 0.22f, 0.16f),
    Gold(R.drawable.frame_4, 600f / 770f, 0.18f, 0.14f, 0.18f, 0.14f),
    Classic(R.drawable.frame_3, 500f / 711f, 0.16f, 0.13f, 0.17f, 0.13f),
    Columns(R.drawable.frame_1, 374f / 490f, 0.13f, 0.07f, 0.13f, 0.07f),
}

private enum class CardFont { Quran, Cordoba }

@Composable
fun ShareCardScreen(quran: Quran, ayahs: IntRange, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var frameIndex by rememberSaveable { mutableIntStateOf(0) }
    var fontIndex by rememberSaveable { mutableIntStateOf(0) }
    var sharing by remember { mutableStateOf(false) }
    val frame = Frame.entries[frameIndex]
    val font = CardFont.entries[fontIndex]

    val strings = LocalStrings.current
    val body = remember(ayahs) {
        ayahs.joinToString(" ") { quran.text(it) + " " + ArabicText.ayahMarker(quran.ayahNumberInSurah(it)) }
    }
    val reference = remember(ayahs, strings.language) { quran.reference(ayahs, strings.language) }

    val layer = rememberGraphicsLayer()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppTopBar(title = strings.shareAyah, onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Very long ayahs get a taller card so the text stays readable.
            val aspect = if (body.length > 450) frame.aspect * 0.7f else frame.aspect
            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        drawLayer(layer)
                    }
                    .background(Color.White),
            ) {
                Image(
                    painter = painterResource(frame.res),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
                // The frame insets were measured for right-to-left; the card looks the same in every UI language.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    BoxWithConstraints(
                        Modifier
                            .fillMaxSize()
                            .padding(0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = maxWidth * frame.start,
                                    end = maxWidth * frame.end,
                                    top = maxHeight * frame.top,
                                    bottom = maxHeight * frame.bottom,
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            BasicText(
                                text = body,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .fillMaxWidth(),
                                style = TextStyle(
                                    fontFamily = if (font == CardFont.Quran) QuranFont else CordobaFont,
                                    color = Color(0xFF1F1A10),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 1.9.em,
                                    textDirection = TextDirection.Rtl,
                                ),
                                autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 30.sp, stepSize = 0.5.sp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                reference,
                                style = TextStyle(fontFamily = NaskhFont, fontSize = 13.sp, color = Color(0xFF0B5D3B)),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(strings.frame, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Frame.entries.forEachIndexed { i, f ->
                    Image(
                        painter = painterResource(f.res),
                        contentDescription = strings.frameNumber(i + 1),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                BorderStroke(
                                    if (i == frameIndex) 3.dp else 1.dp,
                                    if (i == frameIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                ),
                                RoundedCornerShape(8.dp),
                            )
                            .clickable { frameIndex = i },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(strings.font, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardFont.entries.forEachIndexed { i, f ->
                    androidx.compose.material3.FilterChip(
                        selected = i == fontIndex,
                        onClick = { fontIndex = i },
                        label = {
                            Text(
                                when (f) {
                                    CardFont.Quran -> strings.fontMushaf
                                    CardFont.Cordoba -> strings.fontCordoba
                                },
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (sharing) return@Button
                    sharing = true
                    scope.launch {
                        try {
                            val bitmap = layer.toImageBitmap().asAndroidBitmap()
                            shareImage(context, bitmap, text = "$body\n[$reference]", chooserTitle = strings.shareAyah)
                        } finally {
                            sharing = false
                        }
                    }
                },
                enabled = !sharing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(strings.shareImage)
            }
        }
    }
}

private suspend fun shareImage(context: Context, bitmap: Bitmap, text: String, chooserTitle: String) {
    val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        File(dir, "ayah.png").also { f ->
            // Hardware bitmaps can't always be compressed directly; copy to a software one.
            val software = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            f.outputStream().use { software.compress(Bitmap.CompressFormat.PNG, 100, it) }
            software.recycle()
        }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
