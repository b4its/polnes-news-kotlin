package com.mxlkt.newspolnes.components

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.compose.material3.MaterialTheme
import kotlin.math.roundToInt

/**
 * A helper composable that renders HTML text inside Compose using AndroidView.
 * Supports <b>, <i>, <u>, and <a> tags.
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier
) {
    val textStyle = MaterialTheme.typography.bodyLarge
    val textColor = MaterialTheme.colorScheme.onSurface
    val lineHeightInPx = with(LocalDensity.current) {
        textStyle.lineHeight.toPx().roundToInt()
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor.toArgb())
                textSize = textStyle.fontSize.value
                setLineSpacing((lineHeightInPx - lineHeight).toFloat(), 1.0f)
                linksClickable = true
            }
        },
        update = {
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    )
}
