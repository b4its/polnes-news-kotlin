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

    // Gunakan LocalDensity untuk mendapatkan ukuran dalam piksel yang dibutuhkan
    val density = LocalDensity.current

    val lineHeightInPx = with(density) {
        // Mendapatkan tinggi baris dari Compose (misalnya 1.5 * fontSize) dalam Px
        textStyle.lineHeight.toPx()
    }

    val textSizeInPx = with(density) {
        // Mendapatkan ukuran font dalam Px
        textStyle.fontSize.toPx()
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                // 1. Terapkan warna teks
                setTextColor(textColor.toArgb())

                // 2. Terapkan ukuran teks. TextView menerima nilai SP dari textStyle.fontSize.value
                textSize = textStyle.fontSize.value

                // 3. PERBAIKAN SPASI BARIS:
                // Rumus setLineSpacing((lineHeightInPx - lineHeight).toFloat(), 1.0f)
                // seringkali salah karena 'lineHeight' di dalam TextView merujuk pada
                // properti internal TextView, bukan properti Compose.

                // Pilihan A (Dianjurkan): Hapus setLineSpacing untuk menggunakan default stabil TextView
                // setLineSpacing tidak dipanggil di sini.

                // Pilihan B (Jika perlu kontrol): Atur spasi ekstra berdasarkan perbedaan Px
                // Hitung spasi ekstra (leading) dalam piksel:
                val extraSpacing = (lineHeightInPx - textSizeInPx).roundToInt().toFloat()
                // setLineSpacing(extraSpacing, 1.0f) // Uncomment jika Anda yakin ingin mengontrolnya.

                linksClickable = true
            }
        },
        update = {
            // Gunakan mode parsing yang disarankan untuk konten HTML sederhana
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    )
}