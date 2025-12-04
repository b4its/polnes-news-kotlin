package com.mxlkt.newspolnes.components.liveData

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
// Diperlukan untuk memuat gambar dari URL (Anda harus menambahkan dependency Coil/Glide)
import coil.compose.rememberAsyncImagePainter
import com.mxlkt.newspolnes.R // Asumsi Anda punya R.drawable.placeholder_image
import com.mxlkt.newspolnes.model.NewsModel // Import NewsModel dari API
// import com.mxlkt.newspolnes.model.StoreData // Hapus atau abaikan impor StoreData jika sudah tidak digunakan

// LiveNewsCard harus menerima categoryName yang sudah diproses dari atas
@Composable
fun LiveNewsCard(
    newsModel: NewsModel, // Menerima NewsModel dari API
    categoryName: String, // � Menerima nama kategori sebagai string
    onClick: () -> Unit
) {
    // Menghapus logika pencarian StoreData di sini. CategoryName sudah diberikan.
    // val categoryName = StoreData.categoryList.find { it.id == newsModel.categoryId }?.name ?: "Unknown"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp))
    ) {
        // --- Menggunakan Coil untuk memuat gambar dari URL ---

        AsyncImage(
            model = if (newsModel.gambar.isNullOrEmpty()) {
                "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"
            } else {
                "https://polnes-news.b4its.tech/public/${newsModel.gambar}"
            }, // **Ganti dengan URL gambar dari model Anda**
            contentDescription = newsModel.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            // Optional: Tambahkan placeholder, error, atau fallback jika diperlukan
            // placeholder = painterResource(R.drawable.placeholder_image),
            // error = painterResource(R.drawable.error_image),
            // fallback = painterResource(R.drawable.empty_image),
        )

        // Overlay hitam semi transparan di bawah 25%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = 0.4f)
                .background(Color.Black)
        )

        // Teks kategori + judul
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = categoryName, // Menggunakan parameter categoryName
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = newsModel.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Preview harus dihapus atau diubah untuk menggunakan NewsModel palsu
/*
@Preview(showBackground = true)
@Composable
fun LiveNewsCardPreview() {
    // Anda perlu membuat NewsModel palsu untuk Preview
    // val dummyNewsModel = NewsModel(...)
    // LiveNewsCard(newsModel = dummyNewsModel, onClick = {})
}
*/