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
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.News

@Composable
fun NewsCard(
    news: News,
    onClick: () -> Unit
) {
    val categoryName = StoreData.categoryList.find { it.id == news.categoryId }?.name ?: "Unknown"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = painterResource(id = news.imageRes),
            contentDescription = news.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
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
                text = categoryName, // ✅ pakai nama kategori
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = news.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsCardPreview() {
    val sampleNews = StoreData.newsList[0]
    NewsCard(news = sampleNews, onClick = {})
}
