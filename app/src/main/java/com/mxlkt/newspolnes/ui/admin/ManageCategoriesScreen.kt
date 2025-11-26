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
import com.mxlkt.newspolnes.api.ApiClient
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.AdminCategoryCard
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.Category
// � Hapus import DataService, kita akan injeksi ViewModel di sini
// import com.mxlkt.newspolnes.model.DataService
import com.mxlkt.newspolnes.model.StoreData // Asumsi ini masih digunakan untuk simulasi
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.repository.CategoryViewModelFactory
import com.mxlkt.newspolnes.view.CategoryViewModel

@Composable
fun ManageCategoriesScreen(
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Int) -> Unit
) {
    val context = LocalContext.current

    // 1. Injeksi ViewModel dan Factory
    // Kita gunakan ApiClient untuk mendapatkan Service yang dibutuhkan Factory
    val viewModel: CategoryViewModel = viewModel(
        factory = remember {
            CategoryViewModelFactory(apiCategoryService = ApiClient.apiCategoryService)
        }
    )

    // 2. Amati LiveData dari ViewModel
    // Nilai akan diperbarui secara otomatis saat data diterima
    val categories by viewModel.categories.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)
    val successMessage by viewModel.successMessage.observeAsState(initial = null)

    // State untuk menyimpan kategori yang sedang ingin dihapus
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    // Tampilkan Toast untuk error/success
    LaunchedEffect(errorMessage, successMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            // Reset error setelah ditampilkan jika diperlukan
            // viewModel.clearErrorMessage()
        }
        if (successMessage != null) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            // Reset success setelah ditampilkan
            // viewModel.clearSuccessMessage()
        }
    }


    // --- Dialog Konfirmasi Hapus ---
    if (categoryToDelete != null) {
        DeleteConfirmationDialog(
            showDialog = true,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                // TODO: GANTI INI DENGAN PANGGILAN viewModel.deleteCategory(categoryToDelete!!.id)

                // SIMULASI LAMA (Hapus ini setelah fungsi deleteCategory tersedia di ViewModel)
                val isUsed = StoreData.newsList.any { it.categoryId == categoryToDelete?.id }

                if (isUsed) {
                    Toast.makeText(context, "Cannot delete! Category is in use.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Category Deleted: ${categoryToDelete?.name} (Simulated)", Toast.LENGTH_SHORT).show()
                }
                // AKHIR SIMULASI

                categoryToDelete = null
            }
        )
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 3. Tampilkan UI berdasarkan status
            if (isLoading) {
                // Tampilkan indikator loading di tengah
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (categories.isEmpty() && errorMessage == null) {
                // Tampilkan pesan kosong jika tidak ada data dan tidak ada error
                Text(
                    "No categories found. Add one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Tampilkan daftar kategori
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { category ->
                        AdminCategoryCard(
                            category = category,
                            onEditClick = {
                                onEditCategoryClick(category.id)
                            },
                            onDeleteClick = {
                                categoryToDelete = category
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = onAddCategoryClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    }


// � Hapus kode Preview lama dan panggil ManageCategoriesScreen di Preview baru
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManageCategoriesFullPreview() {
    NewsPolnesTheme {
        // Karena Preview tidak bisa menjalankan Composable yang butuh API/ViewModel,
        // Preview harus dijalankan di environment yang disimulasikan atau dengan memanggil
        // komponen yang lebih kecil. Namun, untuk menjaga struktur, kita akan panggil
        // Composable utama.
        ManageCategoriesScreen(
            onAddCategoryClick = {},
            onEditCategoryClick = {} // Dummy action
        )
    }
}