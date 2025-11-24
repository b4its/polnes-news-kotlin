package com.mxlkt.newspolnes.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder // Gunakan StarBorder bawaan default
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Komponen input rating 5 bintang.
 * Sekarang mendukung tombol submit dan state hoisting.
 */
@Composable
fun ArticleRatingInput(
    modifier: Modifier = Modifier,
    currentRating: Int,              // 🟢 Data rating diterima dari luar
    onRatingSelected: (Int) -> Unit, // 🟢 Event saat bintang diklik
    onSubmit: () -> Unit             // 🟢 Event saat tombol kirim diklik
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Beri Rating Artikel Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // --- Baris Bintang ---
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..5).forEach { index ->
                    IconButton(
                        onClick = { onRatingSelected(index) },
                        modifier = Modifier.size(48.dp) // Ukuran touch area
                    ) {
                        Icon(
                            imageVector = if (index <= currentRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Rate $index",
                            tint = if (index <= currentRating) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp) // Ukuran ikon visual
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Tombol Kirim ---
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = currentRating > 0 // Tombol mati jika belum pilih bintang
            ) {
                Text("Kirim Rating")
            }
        }
    }
}