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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Pastikan import ini sesuai dengan struktur project Anda
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.components.LiveNewsCard
import com.mxlkt.newspolnes.view.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostViewedNewsScreen(
    onNavigateBack: () -> Unit,
    onNewsClick: (Int) -> Unit,
    viewModel: NewsViewModel = viewModel()
) {
    // --- State dari ViewModel ---
    // Mengubah LiveData menjadi State agar Compose bisa bereaksi saat data berubah
    val newsList by viewModel.newsList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)

    // --- Konfigurasi Pagination ---
    val pageSize = 5 // Jumlah data per halaman

    // State untuk halaman saat ini (disimpan agar tidak reset saat rotasi layar)
    var currentPage by rememberSaveable { mutableIntStateOf(1) }

    // Logika sederhana untuk mengecek apakah mungkin masih ada data berikutnya
    // Jika jumlah list saat ini >= (halaman * 5), asumsinya masih ada sisa data di server
    val hasMoreNews = newsList.size >= currentPage * pageSize

    // --- Side Effect 1: Muat Data Awal (Page 1) ---
    LaunchedEffect(Unit) {
        // Hanya muat ulang jika list kosong (agar tidak reload berulang kali saat navigasi balik)
        if (newsList.isEmpty()) {
            viewModel.fetchNewsMostViewedShortList(page = 1)
        }
    }

    // --- Side Effect 2: Muat Halaman Berikutnya ---
    // Terpanggil setiap kali `currentPage` berubah nilainya
    LaunchedEffect(currentPage) {
        if (currentPage > 1) {
            viewModel.fetchNewsMostViewedShortList(page = currentPage)
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Most Viewed",
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- Error Handling (Pop-up Dialog) ---
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearStatusMessages() },
                    title = { Text("Error") },
                    text = { Text(errorMessage ?: "Terjadi kesalahan") },
                    confirmButton = {
                        Button(onClick = { viewModel.clearStatusMessages() }) {
                            Text("OK")
                        }
                    }
                )
            }

            // --- Logika Tampilan Utama ---

            // Kondisi 1: Loading Awal & Data Benar-benar Kosong
            if (isLoading && newsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Kondisi 2: Data Kosong, Tidak Loading, Tidak Error
            else if (newsList.isEmpty() && !isLoading && errorMessage == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada berita yang dapat ditampilkan.")
                }
            }
            // Kondisi 3: Ada Data (Tampilkan List)
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Berita
                    items(newsList, key = { it.id }) { newsModel ->
                        LiveNewsCard(
                            newsModel = newsModel,
                            onClick = { onNewsClick(newsModel.id) }
                        )
                    }

                    // --- Bagian Bawah List (Tombol Load More / Loader Kecil) ---
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                // Tampilkan Loading kecil saat memuat halaman berikutnya (append)
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else if (hasMoreNews) {
                                // Tombol Load More
                                OutlinedButton(
                                    onClick = {
                                        if (!isLoading) {
                                            currentPage++
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Load More (+5 Data)")
                                }
                            } else {
                                // Pesan Habis
                                Text(
                                    text = "Semua berita sudah dimuat.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}