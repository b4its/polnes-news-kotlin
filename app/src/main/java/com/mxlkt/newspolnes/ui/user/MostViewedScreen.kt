package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.components.LiveNewsCard
import com.mxlkt.newspolnes.components.NewsCard
import com.mxlkt.newspolnes.view.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostViewedNewsScreen(
    onNavigateBack: () -> Unit,
    onNewsClick: (Int) -> Unit,
    // Gunakan Hilt atau default factory untuk membuat ViewModel
    // Di sini menggunakan 'viewModel()' dari Compose Activity Ktx
    viewModel: NewsViewModel = viewModel()
) {
    // --- State dari ViewModel ---
    // Menggunakan LiveData.observeAsState() untuk mengamati perubahan
    val newsList by viewModel.newsList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)
    val context = LocalContext.current

    // Asumsi: View model akan mengambil berita yang sudah disortir oleh backend (views terbanyak).
    // Jika API tidak memiliki sorting, Anda harus menambahkan parameter query ke fetchNewsList
    // dan mengimplementasikannya di ApiNewsService/Repository.

    // Catatan: Karena backend menyediakan data yang sudah dipaginasi, kita hanya perlu tahu halaman berikutnya.
    // Di sini, kita akan simulasikan permintaan halaman berikutnya (page++)
    var currentPage by rememberSaveable { mutableStateOf(1) }

    // --- Side Effect: Memuat Data Saat Pertama Kali Dimuat ---
    LaunchedEffect(Unit) {
        viewModel.fetchNewsList(page = 1) // Muat halaman 1 saat awal
    }

    // Side Effect: Muat halaman baru saat currentPage berubah (kecuali halaman 1, karena sudah dimuat di LaunchedEffect)
    LaunchedEffect(currentPage) {
        if (currentPage > 1) {
            viewModel.fetchNewsList(page = currentPage)
        }
    }

    // Mengganti logika sorting manual dengan data dari ViewModel
    // Karena API kita memuat halaman demi halaman, kita tidak lagi memanipulasi List secara manual.

    // Logika hasMoreNews sederhana (asumsi: jika list yang didapat penuh, maka ada halaman berikutnya)
    // Dalam implementasi nyata, kita harus mengekstrak informasi total/next_page_url dari NewsListResponse
    // Untuk tujuan ini, saya akan menggunakan simulasi sederhana berdasarkan total data yang ada.
    // (Dalam kasus nyata, Anda perlu menyimpan PaginatedNewsData di ViewModel untuk akurasi)
    val pageLimit = 10 // Asumsi page limit di backend
    val hasMoreNews = newsList.size >= currentPage * pageLimit // Jika size sama dengan pageLimit kali halaman, anggap ada lebih banyak

    // Hanya tampilkan 10 berita terbaru (atau jumlah yang sesuai dengan pageLimit) saat dimuat
    // Kita tidak perlu take() karena ViewModel sudah mengakumulasi semua halaman yang dimuat.

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Most Viewed News",
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tampilkan error message jika ada
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearStatusMessages() },
                    title = { Text("Error") },
                    text = { Text(errorMessage!!) },
                    confirmButton = {
                        Button(onClick = { viewModel.clearStatusMessages() }) {
                            Text("OK")
                        }
                    }
                )
            }

            // Tampilkan Loading Indicator
            if (isLoading && newsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (newsList.isEmpty() && !isLoading && errorMessage == null) {
                // Tampilkan jika tidak ada data setelah loading selesai
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada berita yang dapat ditampilkan saat ini.")
                }
            } else {
                // Tampilkan Daftar Berita dan Tombol Load More
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(newsList, key = { it.id }) { newsModel ->
                        // NewsCard butuh object News. Kita harus konversi NewsModel ke News
                        // atau perbarui NewsCard untuk menerima NewsModel.
                        // Asumsi: NewsCard diubah atau menerima NewsModel yang sudah sangat mirip.
                        // Jika NewsCard tidak dapat diubah, Anda perlu fungsi konversi (NewsModel -> News)

                        // Karena NewsCard sebelumnya menggunakan model News lokal,
                        // saya akan asumsikan Anda telah memperbaruinya agar menerima NewsModel,
                        // atau kita buat konversi sederhana:

                        // News(
                        //     id = newsModel.id,
                        //     title = newsModel.title,
                        //     categoryId = newsModel.categoryId,
                        //     imageRes = 0, // Tidak ada di NewsModel
                        //     content = newsModel.content,
                        //     authorId = newsModel.authorId,
                        //     date = newsModel.created_at, // Menggunakan created_at sebagai date
                        //     views = newsModel.views,
                        //     youtubeVideoId = newsModel.linkYoutube,
                        //     status = NewsStatus.DRAFT // Asumsi default
                        // )

                        // Jika NewsCard diubah untuk menerima NewsModel, gunakan:
                        LiveNewsCard(
                            newsModel = newsModel, // *Perlu penyesuaian di NewsCard.kt*
                            onClick = { onNewsClick(newsModel.id) }
                        )
                    }

                    // Tombol "Load More"
                    item {
                        if (isLoading && newsList.isNotEmpty()) {
                            // Tampilkan loading di bagian bawah saat memuat halaman berikutnya
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (hasMoreNews && !isLoading) {
                            OutlinedButton(
                                onClick = { currentPage++ }, // Tambah nomor halaman, memicu LaunchedEffect
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("Load More (Page ${currentPage + 1})")
                            }
                        } else if (newsList.isNotEmpty()) {
                            // Tampilkan jika semua berita sudah dimuat
                            Text(
                                "Semua berita sudah dimuat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Catatan: Preview memerlukan NewsViewModel, yang mungkin sulit disiapkan tanpa framework DI (seperti Hilt/Koin).
// Untuk tujuan implementasi, Anda dapat menghapus Preview atau menggunakan ViewModel palsu.
/*
@Preview(showBackground = true)
@Composable
private fun MostViewedNewsScreenPreview() {
    MostViewedNewsScreen(
        onNavigateBack = {},
        onNewsClick = {}
    )
}
*/