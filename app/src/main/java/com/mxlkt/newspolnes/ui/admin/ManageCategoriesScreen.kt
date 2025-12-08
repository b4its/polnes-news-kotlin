package com.mxlkt.newspolnes.ui.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.utils.FileUtils
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel
import java.io.File

// --- 1. Helper Function (Persis seperti di News) ---
private fun getFullImageUrl(path: String?): String {
    return if (path.isNullOrEmpty()) {
        "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg" // Placeholder
    } else {
        "https://polnes-news.b4its.tech/public/$path" // Domain Anda
    }
}

@Composable
fun ManageCategoriesScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CategoryViewModel = viewModel()

    // --- STATE ---
    val categories by viewModel.categoryList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)
    val successMessage by viewModel.successMessage.observeAsState(initial = null)

    var searchQuery by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var selectedCategoryForEdit by remember { mutableStateOf<Category?>(null) }

    // --- PAGINATION / LOAD MORE LOGIC ---
    val itemsPerPage = 3
    var displayedCount by remember { mutableIntStateOf(itemsPerPage) }

    // Filter Logic
    val allMatchingCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val visibleCategories = remember(allMatchingCategories, displayedCount) {
        allMatchingCategories.take(displayedCount)
    }

    LaunchedEffect(searchQuery) { displayedCount = itemsPerPage }

    LaunchedEffect(Unit) { viewModel.fetchAllCategories() }

    LaunchedEffect(errorMessage, successMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessages()
        }
        if (successMessage != null) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessages()
            showFormDialog = false
            selectedCategoryForEdit = null
        }
    }

    // --- UI CONTENT ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search categories...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Clear") }
                    }
                },
                singleLine = true
            )

            // List Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading && categories.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (categories.isEmpty() && !isLoading) {
                    Text("No categories found.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(visibleCategories) { category ->
                            // Panggil Component Card di bawah
                            AdminCategoryCard(
                                category = category,
                                onEditClick = {
                                    selectedCategoryForEdit = category
                                    showFormDialog = true
                                },
                                onDeleteClick = { categoryToDelete = category }
                            )
                        }

                        // Load More Button
                        if (displayedCount < allMatchingCategories.size) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { displayedCount += itemsPerPage }) {
                                        Text("Load More (${allMatchingCategories.size - visibleCategories.size} remaining)")
                                        Icon(Icons.Default.KeyboardArrowDown, null)
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading Overlay
                if (isLoading && categories.isNotEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = {
                selectedCategoryForEdit = null
                showFormDialog = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add Category")
        }
    }

    // --- DIALOGS ---
    if (categoryToDelete != null) {
        DeleteConfirmationDialog(
            showDialog = true,
            title = "Hapus Kategori",
            message = "Yakin hapus '${categoryToDelete!!.name}'?",
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                viewModel.deleteCategory(categoryToDelete!!.id)
                categoryToDelete = null
            }
        )
    }

    if (showFormDialog) {
        CategoryFormDialog(
            category = selectedCategoryForEdit,
            onDismiss = {
                showFormDialog = false
                selectedCategoryForEdit = null
            },
            onSave = { name, file ->
                if (selectedCategoryForEdit == null) {
                    if (file != null) viewModel.createCategory(name, file)
                    else Toast.makeText(context, "Gambar wajib diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateCategory(selectedCategoryForEdit!!.id, name, file)
                }
            }
        )
    }
}

// --- 2. ADMIN CATEGORY CARD (Implementasi Image 'Sama-Sama Gambar') ---
@Composable
fun AdminCategoryCard(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Gunakan Helper Function di sini
    // Pastikan field di model Category namanya 'imagePath' atau sesuaikan dengan 'gambar'
    val imageUrl = getFullImageUrl(category.gambar)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IMAGE (Persis News) ---
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // --- TEXT ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- ACTIONS ---
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFDA8C1F))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// --- FORM DIALOG (Tetap sama, logic Image Picker) ---
@Composable
fun CategoryFormDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, File?) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    // Preview Logic: Jika ada file baru pilih itu, jika tidak pakai URL lama
    var previewModel by remember {
        mutableStateOf<Any?>(getFullImageUrl(category?.gambar))
    }

    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            previewModel = uri
            selectedFile = FileUtils.getFileFromUri(context, uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Tambah Kategori" else "Edit Kategori") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Image Preview
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Preview",
                        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedButton(
                    onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedFile == null) "Pilih Gambar" else "Ganti Gambar")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotEmpty()) onSave(name, selectedFile)
                else Toast.makeText(context, "Nama wajib diisi", Toast.LENGTH_SHORT).show()
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}