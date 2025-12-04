package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Diperlukan untuk AndroidViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.CategoryCard
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.components.UserBottomNav
import com.mxlkt.newspolnes.model.Category // Import Model Category
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel // Import ViewModel

// ASUMSI: Anda masih memiliki StoreData.kt untuk Preview
// ASUMSI: Anda masih memiliki komponen CategoryCard, TitleOnlyTopAppBar, UserBottomNav

/**
 * Layar yang menampilkan daftar semua kategori berita untuk pengguna.
 */
@Composable
fun CategoriesScreen(
    onCategoryClick: (Int) -> Unit // Menerima ID Kategori (Int)
) {
    // 1. Injeksi ViewModel
    // Karena CategoryViewModel adalah AndroidViewModel, kita perlu context dari compose.
    val context = LocalContext.current
    val viewModel: CategoryViewModel = viewModel(
        // Factory dihilangkan karena CategoryViewModel yang baru dibuat adalah AndroidViewModel
        // yang hanya membutuhkan Application (yang disediakan oleh viewModel() secara default
        // ketika berada dalam konteks Application/Activity).
    )

    // 2. Amati LiveData dari ViewModel yang baru dibuat
    // Menggunakan LiveData: categoryList, isLoading, errorMessage
    val categories by viewModel.categoryList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)

    // 3. Panggil fungsi untuk mengambil data ketika komponen pertama kali disusun
    LaunchedEffect(Unit) {
        // Nama fungsi yang benar di ViewModel yang baru dibuat: fetchAllCategories()
        viewModel.fetchAllCategories()
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Tampilkan indikator loading
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Tampilkan error (jika ada)
            else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gagal memuat kategori: ${errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchAllCategories() }) {
                        Text("Coba Lagi")
                    }
                }
            }

            // Tampilkan pesan kosong
            else if (categories.isEmpty()) {
                Text(
                    text = "Tidak ada kategori ditemukan.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Tampilkan daftar kategori
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = {
                                // Mengirim ID Kategori (Int) untuk navigasi ke daftar berita
                                onCategoryClick(category.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }


// --- Preview menggunakan Dummy Data ---

// Model dummy untuk Preview, karena kita tidak punya StoreData.kt
private val dummyCategoryList = listOf(
    Category(1, "Politik", "url_gambar_1"),
    Category(2, "Olahraga", "url_gambar_2"),
    Category(3, "Teknologi", "url_gambar_3"),
    Category(4, "Ekonomi", "url_gambar_4"),
)

@Preview(showBackground = true, name = "Categories Screen Preview")
@Composable
private fun CategoriesScreenPreview() {
    Scaffold(
        topBar = {
            TitleOnlyTopAppBar(title = "Categories")
        },
        bottomBar = {
            UserBottomNav(
                currentRoute = "Categories",
                onItemClick = {}
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(dummyCategoryList) { category ->
                CategoryCard(category = category, onClick = { /* no op */ })
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}