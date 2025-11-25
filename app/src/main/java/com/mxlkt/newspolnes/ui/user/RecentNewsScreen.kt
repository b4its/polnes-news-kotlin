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
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.News

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentNewsScreen(
    onNavigateBack: () -> Unit, // Aksi untuk kembali
    onNewsClick: (Int) -> Unit // Aksi saat berita diklik (membawa ID berita)
) {
    // Gunakan 'remember' agar sorting hanya dijalankan sekali
    val sortedNews = remember {
        StoreData.newsList.sortedWith(
            compareByDescending<News> { it.date } // Sortir utama: tanggal terbaru
                .thenByDescending { it.id } // Sortir kedua: ID (jika tanggal sama)
        )
    }

    // --- State untuk Pagination Sederhana ---
    var currentPage by rememberSaveable { mutableStateOf(1) } // Ingat halaman saat ini
    val pageLimit = 5 // Jumlah berita per halaman
    val totalNewsCount = sortedNews.size

    // Ambil berita sesuai halaman saat ini (misal: hal 1 = 5 berita, hal 2 = 10 berita)
    val displayedNews = sortedNews.take(currentPage * pageLimit)
    // Cek apakah masih ada berita yang belum ditampilkan
    val hasMoreNews = displayedNews.size < totalNewsCount
    // ----------------------------------------

    Scaffold(
        topBar = {
            // Pakai TopBar reusable
            CommonTopBar(
                title = "Recent News",
                onBack = onNavigateBack // Teruskan aksi 'back'
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Render daftar berita yang sudah difilter (displayedNews)
            // 'key' membantu Compose mengidentifikasi item, penting untuk performa
            items(displayedNews, key = { it.id }) { news ->
                NewsCard(
                    news = news,
                    // Teruskan ID berita ke NavGraph (pemanggil) saat diklik
                    onClick = { onNewsClick(news.id) }
                )
            }

            // Tampilkan tombol "Load More" HANYA jika masih ada berita
            if (hasMoreNews) {
                item {
                    OutlinedButton(
                        onClick = { currentPage++ }, // Tambah nomor halaman
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .padding(horizontal = 16.dp)
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
private fun RecentNewsScreenPreview() {
    // Sediakan aksi lambda kosong ({}) agar preview bisa jalan
    RecentNewsScreen(
        onNavigateBack = {},
        onNewsClick = {}
    )
}