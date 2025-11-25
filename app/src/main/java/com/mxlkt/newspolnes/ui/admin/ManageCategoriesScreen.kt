package com.mxlkt.newspolnes.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.components.AdminBottomNav
import com.mxlkt.newspolnes.components.AdminCategoryCard
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.components.TitleOnlyTopAppBar
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme

@Composable
fun ManageCategoriesScreen(
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Int) -> Unit // ✅ Parameter baru untuk navigasi Edit
) {
    val categories = StoreData.categoryList
    val context = LocalContext.current

    // State untuk menyimpan kategori yang sedang ingin dihapus
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    // --- Dialog Konfirmasi Hapus ---
    if (categoryToDelete != null) {
        DeleteConfirmationDialog(
            showDialog = true,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                // Simulasi: Cek apakah kategori dipakai
                val isUsed = StoreData.newsList.any { it.categoryId == categoryToDelete?.id }

                if (isUsed) {
                    Toast.makeText(context, "Cannot delete! Category is in use.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Category Deleted: ${categoryToDelete?.name}", Toast.LENGTH_SHORT).show()
                }

                categoryToDelete = null
            }
        )
    }

    // Layout Utama
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                AdminCategoryCard(
                    category = category,
                    onEditClick = {
                        // ✅ Memanggil fungsi navigasi dengan mengirim ID kategori
                        onEditCategoryClick(category.id)
                    },
                    onDeleteClick = {
                        categoryToDelete = category
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManageCategoriesFullPreview() {
    NewsPolnesTheme {
        Scaffold(
            topBar = { TitleOnlyTopAppBar(title = "Manage Categories") },
            bottomBar = {
                AdminBottomNav(currentRoute = "Categories", onItemClick = {})
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                ManageCategoriesScreen(
                    onAddCategoryClick = {},
                    onEditCategoryClick = {} // Dummy action
                )
            }
        }
    }
}