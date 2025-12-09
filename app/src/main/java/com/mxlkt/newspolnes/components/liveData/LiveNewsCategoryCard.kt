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

private const val BASE_IMAGE_URL = "https://polnes-news.b4its.tech/"

@Composable
fun LiveNewsCategoryCard(
    newsModel: NewsModel,
    onClick: () -> Unit,
    // [BARU] Parameter opsional untuk memaksa nama kategori
    categoryNameOverride: String? = null
) {
    // LOGIKA BARU:
    // 1. Cek apakah ada override? Jika ada pakai itu.
    // 2. Jika tidak, coba ambil dari newsModel.
    // 3. Jika null juga, baru pakai "UNCATEGORIZED".
    val displayCategoryName = categoryNameOverride?.uppercase()
        ?: newsModel.category?.name?.uppercase()
        ?: "UNCATEGORIZED"

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
        AsyncImage(
            model = if (newsModel.gambar.isNullOrEmpty()) {
                // Gambar Default jika JSON tidak ada gambar
                "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"
            } else {
                "https://polnes-news.b4its.tech/public/${newsModel.gambar}"
            },
            contentDescription = newsModel.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.category_tech), // Pastikan drawable ini ada
            error = painterResource(id = R.drawable.category_economy),     // Pastikan drawable ini ada
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = 0.5f)
                .background(Color.Black)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                // Gunakan variable hasil logika baru di atas
                text = displayCategoryName,
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