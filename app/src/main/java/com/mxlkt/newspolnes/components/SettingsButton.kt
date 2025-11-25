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
// Tambahkan ikon Logout jika belum ada
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.ui.draw.shadow

/**
 * Composable reusable untuk satu baris item di halaman Settings.
 */
@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    // Parameter baru untuk mengubah warna konten (Teks dan Ikon).
    // Nilai default-nya adalah warna onSurface agar teks normal.
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    // Tentukan warna latar belakang ikon dan warna tint ikon berdasarkan contentColor.
    val isErrorState = contentColor == MaterialTheme.colorScheme.error

    // 1. Tentukan warna Ikon di dalam lingkaran
    val iconTint = if (isErrorState) {
        contentColor // Jika error, ikon berwarna merah (error color)
    } else {
        Color.White // Default-nya, ikon berwarna putih
    }

    // 2. Tentukan warna Latar Belakang Lingkaran Ikon
    val iconBgColor = if (isErrorState) {
        // Jika error, gunakan warna errorContainer (lebih muda)
        MaterialTheme.colorScheme.errorContainer
    } else {
        // Warna Hijau Utama (Default)
        Color(0xFF038900)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Row untuk susun Ikon (kiri) dan Teks (kanan)
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kiri: Ikon dengan background lingkaran
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor), // Menggunakan warna latar belakang yang ditentukan
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconTint // Menggunakan warna ikon yang ditentukan
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Jarak

            // Kanan: Teks label
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                // Teks menggunakan contentColor yang dioper
                color = contentColor
            )
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, name = "Tombol Normal")
@Composable
private fun SettingsButtonNormalPreview() {
    SettingsButton(
        text = "Help and Feedback",
        icon = Icons.Outlined.Help,
        onClick = {}
    )
}

@Preview(showBackground = true, name = "Tombol Logout (Error State)")
@Composable
private fun SettingsButtonLogoutPreview() {
    // Tombol Logout menggunakan error state
    SettingsButton(
        text = "Logout",
        icon = Icons.Outlined.Logout,
        onClick = {},
        contentColor = MaterialTheme.colorScheme.error // Menggunakan warna error
    )
}