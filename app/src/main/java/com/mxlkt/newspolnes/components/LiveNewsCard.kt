package com.mxlkt.newspolnes.components

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
import com.mxlkt.newspolnes.model.NewsModel
import com.mxlkt.newspolnes.R

// � HARUS ADA: Definisikan BASE URL untuk gambar
private const val BASE_IMAGE_URL = "https://polnes-news.b4its.tech/"

@Composable
fun LiveNewsCard(
    newsModel: NewsModel,
    onClick: () -> Unit
) {
    // 1. Tangani Nullability Kategori (jika model data NewsModel sudah diubah menjadi nullable)
    // Jika newsModel.category atau .name null, gunakan "UNCATEGORIZED"
    val categoryName = newsModel.category?.name?.uppercase() ?: "UNCATEGORIZED"

    // 2. Buat URL Gambar lengkap
    // newsModel.gambar hanya berisi path relatif (misalnya: "media/gambar/...").
    // Kita harus menggabungkannya dengan BASE_IMAGE_URL.
    val fullImageUrl = newsModel.gambar?.let {
        BASE_IMAGE_URL + it
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp))
    ) {
        // 3. Gunakan URL Gambar Lengkap (fullImageUrl)
        AsyncImage(
            model = if (newsModel.gambar.isNullOrEmpty()) {
                "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"
            } else {
                "https://polnes-news.b4its.tech/public/${newsModel.gambar}"
            },
            contentDescription = newsModel.title,
            contentScale = ContentScale.Crop,
            // Fallback/Placeholder (Pastikan drawable ini ada di proyek Anda)
            placeholder = painterResource(id = R.drawable.category_tech),
            error = painterResource(id = R.drawable.category_economy),
            modifier = Modifier.fillMaxSize()
        )

        // Overlay hitam semi transparan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = 0.5f)
                .background(Color.Black)
        )

        // Teks kategori + judul
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = categoryName,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = newsModel.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}