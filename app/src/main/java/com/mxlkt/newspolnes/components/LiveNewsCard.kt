package com.mxlkt.newspolnes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import yang diperlukan untuk memuat gambar dari URL
import coil.compose.AsyncImage
import com.mxlkt.newspolnes.model.NewsModel // Import NewsModel dari API
import com.mxlkt.newspolnes.R // Asumsi: R.drawable.placeholder ada

@Composable
fun LiveNewsCard(
    // � PERUBAHAN 1: Menerima NewsModel dari API
    newsModel: NewsModel,
    onClick: () -> Unit
) {
    // � PERUBAHAN 2: Mengambil nama kategori langsung dari relasi NewsModel
    val categoryName = newsModel.category.name

    // Kita tidak lagi butuh StoreData di sini karena NewsModel sudah membawa relasi Category dan Author

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Menggunakan rasio aspek 3:2, yang umum untuk gambar berita
            .aspectRatio(3f / 2f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp))
    ) {
        // � PERUBAHAN 3: Menggunakan AsyncImage untuk memuat gambar dari URL (newsModel.gambar)
        AsyncImage(
            model = newsModel.gambar, // URL atau path gambar dari API
            contentDescription = newsModel.title,
            contentScale = ContentScale.Crop,
            // Opsional: Tampilkan placeholder jika gambar null/sedang dimuat
            placeholder = painterResource(id = R.drawable.category_tech),
            error = painterResource(id = R.drawable.category_economy),
            modifier = Modifier.fillMaxSize()
        )

        // Overlay hitam semi transparan di bawah 25% (untuk kontras teks)
        // Dibuat lebih besar agar judul panjang tetap terlihat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Meningkatkan area overlay sedikit (misal 35%)
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = 0.5f) // Alpha dinaikkan sedikit
                .background(Color.Black)
        )

        // Teks kategori + judul
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = categoryName.uppercase(), // Teks kategori di-uppercase agar menonjol
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = newsModel.title,
                color = Color.White,
                fontSize = 15.sp, // Ukuran teks sedikit diperbesar
                fontWeight = FontWeight.Bold, // Judul dibuat lebih tebal
                maxLines = 2, // Izinkan 2 baris untuk judul
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ⚠️ PREVIEW SULIT DILAKUKAN karena butuh data model relasional lengkap
// Biasanya data palsu untuk preview diletakkan di file terpisah.

// @Preview(showBackground = true)
// @Composable
// fun NewsCardPreview() {
// // Menghapus preview yang bergantung pada StoreData.newsList lama
// // Anda harus membuat NewsModel dummy jika ingin preview berjalan
// }

// Anda perlu membuat file NewsModel dummy atau menggunakan data dari ViewModel
// jika Anda ingin menampilkan preview.