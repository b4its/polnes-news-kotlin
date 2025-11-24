package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.components.NewsCard
import com.mxlkt.newspolnes.model.DummyData
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectedScreen(
    categoryName: String, // Menerima nama kategori sebagai judul TopBar
    onNavigateBack: () -> Unit,
    onNewsClick: (Int) -> Unit // Untuk navigasi ke NewsDetail
) {
    // 1. Cari ID Kategori
    val category = DummyData.categoryList.find { it.name == categoryName }
    val categoryId = category?.id ?: 0

    // 2. Filter Berita berdasarkan ID Kategori
    // Jika ID tidak valid (0), tampilkan semua berita (atau array kosong)
    val filteredNewsList = if (categoryId > 0) {
        DummyData.newsList.filter { it.categoryId == categoryId }
    } else {
        emptyList()
    }

    // Tentukan judul TopBar, default ke "Unknown" jika categoryName tidak ditemukan
    val screenTitle = categoryName.ifEmpty { "Category" }

    Scaffold(
        topBar = {
            // Menggunakan CommonTopBar dengan nama kategori sebagai judul
            CommonTopBar(
                title = screenTitle,
                onBack = onNavigateBack // Menggunakan fungsi back
            )
        }
    ) { paddingValues ->
        // Tampilkan daftar berita yang sudah difilter
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredNewsList) { news ->
                NewsCard(
                    news = news,
                    onClick = { onNewsClick(news.id) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySelectedScreenPreview() {
    NewsPolnesTheme {
        CategorySelectedScreen(
            categoryName = "Teknologi",
            onNavigateBack = {},
            onNewsClick = {}
        )
    }
}