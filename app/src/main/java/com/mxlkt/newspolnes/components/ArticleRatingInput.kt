package com.mxlkt.newspolnes.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Komponen input rating 5 bintang.
 * Mendukung tombol submit dan state hoisting.
 * Updated: Mendukung mode Update vs Create.
 */
@Composable
fun ArticleRatingInput(
    modifier: Modifier = Modifier,
    currentRating: Int,              // Data rating saat ini
    isUpdateMode: Boolean = false,   // TRUE jika user sudah pernah rating
    onRatingSelected: (Int) -> Unit, // Event saat bintang diklik
    onSubmit: () -> Unit             // Event saat tombol kirim diklik
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
            // Ubah teks judul berdasarkan mode
            Text(
                text = if (isUpdateMode) "Rating Anda" else "Beri Rating Artikel Ini",
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

            // --- Tombol Kirim / Update ---
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = currentRating > 0 // Tombol mati jika belum pilih bintang
            ) {
                // Ubah teks tombol berdasarkan mode
                Text(if (isUpdateMode) "Update Rating" else "Kirim Rating")
            }
        }
    }
}