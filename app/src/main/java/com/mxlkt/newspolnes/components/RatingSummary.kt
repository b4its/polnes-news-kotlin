package com.mxlkt.newspolnes.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.model.Comment

/**
 * Menampilkan ringkasan rata-rata rating untuk artikel,
 * berdasarkan daftar komentar user.
 */
@Composable
fun RatingSummary(
    comments: List<Comment>,
    modifier: Modifier = Modifier
) {
    if (comments.isEmpty()) {
        Text(
            text = "Belum ada rating untuk artikel ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        return
    }

    val totalRatings = comments.size
    // Menghitung rata-rata, pastikan tidak dibagi 0 (handled by isEmpty check above)
    val averageRating = comments.sumOf { it.rating }.toFloat() / totalRatings

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Angka Rata-rata
            Column {
                Text(
                    text = "%.1f".format(averageRating),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dari $totalRatings ulasan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Visualisasi Bintang Rata-rata
            Row {
                (1..5).forEach { index ->
                    val icon = when {
                        averageRating >= index -> Icons.Filled.Star
                        averageRating >= index - 0.5 -> Icons.Filled.StarHalf
                        else -> Icons.Outlined.StarOutline
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}