package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.liveData.LiveNewsCard
import com.mxlkt.newspolnes.components.PolnesTopAppBar
import com.mxlkt.newspolnes.components.SectionHeader
import com.mxlkt.newspolnes.components.UserBottomNav
import com.mxlkt.newspolnes.view.NewsViewModel
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel // Import CategoryViewModel

@Composable
fun HomeScreen(
    onViewAllRecent: () -> Unit,
    onViewAllMostViewed: () -> Unit,
    onViewAllMostRated: () -> Unit,
    onNewsClick: (Int) -> Unit,
    viewModel: NewsViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel() // Mengambil instance Category ViewModel
) {
    // 1. Amati LiveData dari ViewModel Berita
    val recentNews by viewModel.recentNewsFirst.observeAsState()
    val mostViewedNews by viewModel.mostViewedFirst.observeAsState()
    val mostRatedNews by viewModel.mostRatedFirst.observeAsState()

    // 2. Amati LiveData dari CategoryViewModel
    val categoryList by categoryViewModel.categoryList.observeAsState(initial = emptyList())
    val isCategoryLoading by categoryViewModel.isLoading.observeAsState(initial = false)

    // 3. State untuk kategori yang dipilih (null = Semua kategori)
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    // Fungsi untuk mendapatkan nama kategori
    fun getCategoryName(categoryId: Int?): String {
        return categoryList.find { it.id == categoryId }?.name ?: "Unknown"
    }

    // 4. Muat data saat Composable pertama kali atau kategori berubah
    LaunchedEffect(selectedCategoryId) {
        // Panggil API untuk memuat kategori jika belum dimuat
        if (categoryList.isEmpty()) {
            categoryViewModel.fetchAllCategories()
        }

        // Panggil fungsi pemuatan berita (asumsi Anda memiliki fungsi filter di NewsViewModel)
        // Di sini saya akan memanggil fetchRecentViewFirst() yang tidak berfilter,
        // namun di aplikasi nyata, Anda akan memanggil fungsi:
        // viewModel.fetchRecentViewFirst(selectedCategoryId)

        viewModel.fetchRecentViewFirst()

        // Asumsi: Most Viewed dan Most Rated tidak difilter berdasarkan kategori di layar ini
        viewModel.fetchMostViewedFirst()
        viewModel.fetchMostRatedFirst()
    }

    // Asumsi isLoading hanya berlaku untuk daftar berita di bagian bawah
    val isAnyNewsLoading = recentNews == null && mostViewedNews == null && mostRatedNews == null

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Menampilkan loading global jika tidak ada data berita yang berhasil dimuat
        if (isAnyNewsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(color = Color(0xFF007bff))
                }
            }
        }


        // --- 1. Recent News (Sekarang difilter oleh selectedCategoryId) ---
        item {
            SectionHeader(
                title = if (selectedCategoryId != null) "Recent News (${getCategoryName(selectedCategoryId)})" else "Recent News",
                subtitle = "Here is the latest news from Polnes News",
                onViewAllClick = onViewAllRecent
            )
        }
        item {
            val latestNews = recentNews
            if (latestNews != null) {
                // Meneruskan nama kategori yang sudah dicari ke LiveNewsCard
                val categoryName = getCategoryName(latestNews.categoryId)
                LiveNewsCard(
                    newsModel = latestNews,
                    categoryName = categoryName, // Parameter categoryName yang baru
                    onClick = { onNewsClick(latestNews.id) }
                )
            } else if (!isAnyNewsLoading) {
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
            val topViewedNews = mostViewedNews
            if (topViewedNews != null) {
                // Meneruskan nama kategori yang sudah dicari ke LiveNewsCard
                val categoryName = getCategoryName(topViewedNews.categoryId)
                LiveNewsCard(
                    newsModel = topViewedNews,
                    categoryName = categoryName, // Parameter categoryName yang baru
                    onClick = { onNewsClick(topViewedNews.id) }
                )
            } else if (!isAnyNewsLoading) {
                Text("No most viewed news available.", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // --- 3. Most Rated News ---
        item {
            SectionHeader(
                title = "Most Rated News",
                subtitle = "Top rated news by our community",
                onViewAllClick = onViewAllMostRated
            )
        }
        item {
            val topRatedNews = mostRatedNews
            if (topRatedNews != null) {
                // Meneruskan nama kategori yang sudah dicari ke LiveNewsCard
                val categoryName = getCategoryName(topRatedNews.categoryId)
                LiveNewsCard(
                    newsModel = topRatedNews,
                    categoryName = categoryName, // Parameter categoryName yang baru
                    onClick = { onNewsClick(topRatedNews.id) }
                )
            } else if (!isAnyNewsLoading) {
                Text("No rated news available.", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Preview 1 (Preview Sederhana tidak bisa menggunakan ViewModel) ---
/* Peringatan: Komponen yang menggunakan `viewModel()` tidak dapat dipreview tanpa penyedia,
    sehingga kita akan menonaktifkan Preview yang memerlukan ViewModel untuk kode ini.
    Jika Anda ingin melihat hasilnya, jalankan aplikasi di emulator atau perangkat.
*/
// @Preview(showBackground = true, name = "Hanya Konten")
// @Composable
// fun HomeScreenPreview() {
//     HomeScreen(
//         onViewAllRecent = {},
//         onViewAllMostViewed = {},
//         onViewAllMostRated = {},
//         onNewsClick = {}
//     )
// }


// --- Preview 2 (Full App Preview - Menggunakan struktur Scaffold) ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Full App Structure")
@Composable
fun FullHomeScreenPreview() {
    // Karena Preview tidak dapat menyediakan ViewModel yang sesungguhnya,
    // kita hanya akan menampilkan kerangka UI di sini.
    Scaffold(
        topBar = { PolnesTopAppBar() },
        bottomBar = { UserBottomNav(currentRoute = "Home", onItemClick = {}) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                text = "Home Screen Content (Requires Live Data)",
                modifier = Modifier.padding(16.dp)
            )
            // Di lingkungan nyata, HomeScreen akan dimuat di sini.
        }
    }
}