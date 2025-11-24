package com.mxlkt.newspolnes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable reusable untuk menampilkan kartu info akun sederhana.
 * Menampilkan ikon, nama lengkap, dan role pengguna.
 */
@Composable
fun AccountInfoCard(
    modifier: Modifier = Modifier,
    fullName: String,
    role: String
) {
    // Card sebagai container utama, memberikan efek bayangan (elevation)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), // Beri padding di luar card
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // Row untuk menyusun item secara horizontal (Ikon di kiri, Teks di kanan)
        Row(
            modifier = Modifier.padding(16.dp), // Padding di dalam card
            verticalAlignment = Alignment.CenterVertically // Pusatkan item secara vertikal
        ) {

            // Bagian ikon profile di sebelah kiri
            // Kita pakai Box di sini agar bisa membuat background lingkaran
            // di belakang ikonnya. Ini trik umum di Compose.
            Box(
                modifier = Modifier
                    .size(48.dp) // Ukuran pasti untuk lingkaran
                    .clip(CircleShape) // Buat jadi bulat
                    .background(Color(0xFFA3E5A6)), // Warna background lingkaran
                contentAlignment = Alignment.Center // Pusatkan ikon di dalam Box
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    tint = Color(0xFF50AE5E), // Warna ikonnya
                    modifier = Modifier.size(32.dp) // Ukuran ikon lebih kecil dari Box
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Jarak antara ikon dan teks

            // Bagian teks di sebelah kanan
            // Column untuk menyusun teks secara vertikal (Nama di atas, Role di bawah)
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray, // Role dibuat abu-abu agar tidak terlalu menonjol
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountInfoCardPreview() {
    // Fungsi private ini hanya untuk menampilkan preview di Android Studio
    AccountInfoCard(fullName = "User Full Name", role = "User")
}