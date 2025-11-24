package com.mxlkt.newspolnes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Help
import androidx.compose.ui.draw.shadow
/**
 * Composable reusable untuk satu baris item di halaman Settings.
 * Dibuat agar terlihat seperti Card tapi sebenarnya Box yang bisa diklik.
 */
@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    // Kita pakai Box sebagai container utama agar bisa
    // menerapkan .clickable di seluruh area.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow( // <-- TAMBAHKAN SHADOW DI SINI
                elevation = 4.dp, // Sesuaikan ketebalan shadow
                shape = MaterialTheme.shapes.medium // Bentuk shadow harus SAMA dgn clip
            )
            .clip(MaterialTheme.shapes.medium) // Bentuknya rounded (seperti Card)
            .clickable { onClick() } // Efek ripple akan menyebar di seluruh Box
            .background(MaterialTheme.colorScheme.surface) // Warna background (seperti Card)
    ) {
        // Row untuk susun Ikon (kiri) dan Teks (kanan)
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kiri: Ikon dengan background lingkaran hijau
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF038900)), // Warna hijau utama
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text, // Deskripsi = teks tombolnya
                    tint = Color.White // Ikonnya dibuat putih agar kontras
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Jarak

            // Kanan: Teks label
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsButtonPreview() {
    SettingsButton(
        text = "Help and Feedback",
        icon = Icons.Outlined.Help,
        onClick = {}
    )
}