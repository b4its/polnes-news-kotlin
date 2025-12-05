package com.mxlkt.newspolnes.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.AdminCategoryCard
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
// Mengganti import ke path yang benar
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel
// Menghapus import ApiClient karena tidak diperlukan di Compose
// Menghapus import StoreData sesuai permintaan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Int) -> Unit
) {
    val context = LocalContext.current

    // 1. Injeksi ViewModel (AndroidViewModel)
    // Factory dihilangkan karena CategoryViewModel adalah AndroidViewModel
    val viewModel: CategoryViewModel = viewModel()

    // 2. Amati LiveData dari ViewModel
    // Menggunakan nama LiveData yang benar: categoryList
    val categories by viewModel.categoryList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)
    val successMessage by viewModel.successMessage.observeAsState(initial = null)

    // State untuk menyimpan kategori yang sedang ingin dihapus
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    // 3. Panggil fungsi untuk mengambil data saat komponen pertama kali disusun
    LaunchedEffect(Unit) {
        viewModel.fetchAllCategories()
    }

    // 4. Handle side effect: Tampilkan Toast dan reset status
    LaunchedEffect(errorMessage, successMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            // Reset error setelah ditampilkan
            viewModel.clearStatusMessages()
        }
        if (successMessage != null) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            // Reset success setelah ditampilkan
            viewModel.clearStatusMessages()
        }
    }

    // --- Dialog Konfirmasi Hapus ---
    if (categoryToDelete != null) {
        DeleteConfirmationDialog(
            showDialog = true,
            title = "Hapus Kategori",
            message = "Apakah Anda yakin ingin menghapus kategori '${categoryToDelete!!.name}'?",
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                val idToDelete = categoryToDelete!!.id
                categoryToDelete = null

                // TODO: PANGGIL FUNGSI DELETE KATEGORI DI VIEWMODEL
                /*
                viewModel.deleteCategory(idToDelete)
                */
                Toast.makeText(context, "TODO: Panggil deleteCategory($idToDelete)", Toast.LENGTH_SHORT).show()

                // CATATAN: Fungsi deleteCategory harus diimplementasikan di CategoryViewModel.
                // Fungsi deleteCategory harus memanggil Repository, dan Repository
                // harus memiliki fungsi DELETE.
            }
        )
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Tampilkan indikator loading di tengah
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Tampilkan error jika ada (selain loading)
            else if (errorMessage != null) {
                // Tampilkan pesan error jika pemuatan gagal
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading categories. Please retry.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchAllCategories() }) {
                        Text("Refresh Data")
                    }
                }
            }
            // Tampilkan pesan kosong jika tidak ada data
            else if (categories.isEmpty()) {
                Text(
                    "No categories found. Add one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Tampilkan daftar kategori
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { category ->
                        AdminCategoryCard(
                            category = category,
                            onEditClick = { onEditCategoryClick(category.id) },
                            onDeleteClick = { categoryToDelete = category } // Trigger dialog
                        )
                    }
                }
            }
        }
    }


// --- Preview (Menggunakan Dummy Data jika perlu) ---

// Dummy data untuk Preview (jika tidak menggunakan StoreData)
private val dummyCategoryList = listOf(
    Category(1, "Teknologi", "url_dummy_1"),
    Category(2, "Ekonomi", "url_dummy_2"),
    Category(3, "Politik", "url_dummy_3"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManageCategoriesFullPreview() {
    NewsPolnesTheme {
        Scaffold(
            topBar = {
                TitleOnlyTopAppBar(title = "Manage Categories (Preview)")
            },
            bottomBar = {
                AdminBottomNav(currentRoute = "Categories", onItemClick = {})
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dummyCategoryList) { category ->
                    AdminCategoryCard(
                        category = category,
                        onEditClick = { /* no op */ },
                        onDeleteClick = { /* no op */ }
                    )
                }
            }
        }
    }
}