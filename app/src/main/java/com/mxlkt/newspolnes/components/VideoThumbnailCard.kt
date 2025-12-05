package com.mxlkt.newspolnes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import androidx.compose.foundation.shape.CircleShape

/**
 * Card yang menampilkan thumbnail video YouTube dengan overlay
 * dan ikon Play di tengah.
 *
 * @param youtubeVideoId ID video YouTube (e.g., "dQw4w9WgXcQ").
 * @param onClick Aksi yang dijalankan saat card di-klik.
 */
@Composable
fun VideoThumbnailCard(
    thumbnailCard: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // YouTube menyediakan thumbnail standar
    // mqdefault.jpg = Medium Quality (320x180)
    // hqdefault.jpg = High Quality (480x360)
    // 0.jpg atau maxresdefault.jpg = Kualitas tertinggi
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Rasio standar video
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 1. Thumbnail (diload dari URL pakai Coil)
            AsyncImage(
                model = if (thumbnailCard.isEmpty()) {
                    "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"
                } else {
                "https://polnes-news.b4its.tech/public/${thumbnailCard}"
            },
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Overlay Shadow (gelap transparan)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // 3. Ikon Play (Segitiga Putih di Tengah)
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    // Beri background lingkaran transparan di belakang ikon
                    // agar lebih menonjol
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun VideoThumbnailCardPreview() {
//    NewsPolnesTheme {
//        Box(modifier = Modifier.padding(16.dp)) {
//            VideoThumbnailCard(
//                youtubeVideoId = "dQw4w9WgXcQ", // ID Rick Astley
//                onClick = {}
//            )
//        }
//    }
//}