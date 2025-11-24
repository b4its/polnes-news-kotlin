package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.components.NewsCard
import com.mxlkt.newspolnes.model.DummyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostRatedNewsScreen(
    onNavigateBack: () -> Unit,
    onNewsClick: (Int) -> Unit
) {
    // 🟢 LOGIKA SORTING BERDASARKAN RATING TERTINGGI
    val sortedNews = remember {
        DummyData.newsList.sortedByDescending { news ->
            // 1. Ambil semua komentar untuk berita ini
            val relatedComments = DummyData.commentList.filter { it.newsId == news.id }

            // 2. Hitung rata-rata rating
            if (relatedComments.isNotEmpty()) {
                relatedComments.map { it.rating }.average()
            } else {
                0.0 // Jika tidak ada rating, anggap 0
            }
        }
    }

    // --- Pagination (Sama seperti RecentNews) ---
    var currentPage by rememberSaveable { mutableIntStateOf(1) }
    val pageLimit = 5
    val totalNewsCount = sortedNews.size
    val displayedNews = sortedNews.take(currentPage * pageLimit)
    val hasMoreNews = displayedNews.size < totalNewsCount

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Most Rated News",
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(displayedNews, key = { it.id }) { news ->
                NewsCard(
                    news = news,
                    onClick = { onNewsClick(news.id) }
                )
            }

            if (hasMoreNews) {
                item {
                    OutlinedButton(
                        onClick = { currentPage++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Load More")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MostRatedNewsScreenPreview() {
    MostRatedNewsScreen(onNavigateBack = {}, onNewsClick = {})
}