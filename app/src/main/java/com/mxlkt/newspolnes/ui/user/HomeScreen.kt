package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.components.NewsCard
import com.mxlkt.newspolnes.components.PolnesTopAppBar
import com.mxlkt.newspolnes.components.SectionHeader
import com.mxlkt.newspolnes.components.UserBottomNav
import com.mxlkt.newspolnes.model.DummyData

@Composable
fun HomeScreen(
    onViewAllRecent: () -> Unit,
    onViewAllMostViewed: () -> Unit,
    onViewAllMostRated: () -> Unit, // 🟢 Parameter Baru
    onNewsClick: (Int) -> Unit
) {
    // Data Dummy Existing
    val latestNews = DummyData.newsList.sortedByDescending { it.date }.firstOrNull()
    val topViewedNews = DummyData.newsList.sortedByDescending { it.views }.firstOrNull()

    // 🟢 Cari 1 Berita dengan Rating Tertinggi untuk ditampilkan di Home
    val topRatedNews = remember {
        DummyData.newsList.maxByOrNull { news ->
            val comments = DummyData.commentList.filter { it.newsId == news.id }
            if (comments.isNotEmpty()) comments.map { it.rating }.average() else 0.0
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Recent News ---
        item {
            SectionHeader(
                title = "Recent News",
                subtitle = "Here is the latest news from Polnes News",
                onViewAllClick = onViewAllRecent
            )
        }
        item {
            if (latestNews != null) {
                NewsCard(news = latestNews, onClick = { onNewsClick(latestNews.id) })
            } else {
                Text("No recent news available.", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // --- 2. Most Viewed News ---
        item {
            SectionHeader(
                title = "Most Viewed News",
                subtitle = "Most frequently viewed news on Polnes News",
                onViewAllClick = onViewAllMostViewed
            )
        }
        item {
            if (topViewedNews != null) {
                NewsCard(news = topViewedNews, onClick = { onNewsClick(topViewedNews.id) })
            } else {
                Text("No most viewed news available.", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // --- 3. 🟢 Most Rated News (BARU) ---
        item {
            SectionHeader(
                title = "Most Rated News",
                subtitle = "Top rated news by our community",
                onViewAllClick = onViewAllMostRated
            )
        }
        item {
            if (topRatedNews != null) {
                NewsCard(news = topRatedNews, onClick = { onNewsClick(topRatedNews.id) })
            } else {
                Text("No rated news available.", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Preview 1 ---
@Preview(showBackground = true, name = "Hanya Konten")
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onViewAllRecent = {},
        onViewAllMostViewed = {},
        onViewAllMostRated = {}, // 🟢
        onNewsClick = {}
    )
}

// --- Preview 2 ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Full App")
@Composable
fun FullHomeScreenPreview() {
    Scaffold(
        topBar = { PolnesTopAppBar() },
        bottomBar = { UserBottomNav(currentRoute = "Home", onItemClick = {}) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HomeScreen(
                onViewAllRecent = {},
                onViewAllMostViewed = {},
                onViewAllMostRated = {}, // 🟢
                onNewsClick = {}
            )
        }
    }
}