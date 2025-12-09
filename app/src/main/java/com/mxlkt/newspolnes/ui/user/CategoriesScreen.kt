package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.CategoryCard
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.components.UserBottomNav
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel

/**
 * Layar yang menampilkan daftar semua kategori berita.
 */
@Composable
fun CategoriesScreen(
    // HAPUS: categoryId: Int (Tidak diperlukan untuk layar list utama)
    // UBAH: Callback menerima Object Category (agar bisa ambil ID & Name di NavGraph)
    onCategoryClick: (Category) -> Unit
) {
    // 1. Injeksi ViewModel
    val viewModel: CategoryViewModel = viewModel()

    // 2. Amati LiveData
    val categories by viewModel.categoryList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)

    // 3. Panggil API saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchAllCategories()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // A. Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // B. Error Message
        else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gagal memuat kategori: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchAllCategories() }) {
                    Text("Coba Lagi")
                }
            }
        }

        // C. Empty State
        else if (categories.isEmpty()) {
            Text(
                text = "Tidak ada kategori ditemukan.",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // D. List Data
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = {
                            // PENTING: Kirim object category ke callback
                            onCategoryClick(category)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// --- Preview Section ---

// Dummy data untuk preview
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
        topBar = { TitleOnlyTopAppBar(title = "Categories") },
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