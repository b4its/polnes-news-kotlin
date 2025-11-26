package com.mxlkt.newspolnes.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Diperlukan untuk viewModel()
import coil.compose.rememberAsyncImagePainter
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.components.DeleteConfirmationDialog
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.PolnesGreen
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.ui.theme.White
// Hapus import com.mxlkt.newspolnes.utils.SessionManager
import com.mxlkt.newspolnes.view.AuthViewModel // Diperlukan untuk akses data pengguna



import com.mxlkt.newspolnes.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddANewCategoryScreen(
    categoryId: Int? = null,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel() // � Injeksi AuthViewModel
) {
    // � 1. Ambil Peran Pengguna dari DataStore (Flow)
    val currentUserRoleString by authViewModel.userRole.collectAsState(initial = null)

    // Konversi string role menjadi enum UserRole
    val currentUserRole = remember(currentUserRoleString) {
        try {
            currentUserRoleString?.let { UserRole.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // � 2. GUARD: Cek Hak Akses
    if (currentUserRole != UserRole.ADMIN) {
        // Tampilkan pesan akses terbatas
        Scaffold(topBar = { CommonTopBar(title = "Access Denied", onBack = onBackClick) }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Error: Hanya Administrator yang dapat mengelola kategori. Role Anda: ${currentUserRoleString ?: "N/A"}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return // � Penting: Hentikan eksekusi Composable jika bukan Admin
    }

    // --- Logic Form (Hanya dijalankan jika user adalah Admin) ---
    val isEditMode = categoryId != null
    val categoryToEdit: Category? = remember {
        if (isEditMode) {
            StoreData.categoryList.find { it.id == categoryId }
        } else {
            null
        }
    }

    var title by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val topBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    )

    // Dialog ini tetap ada, tapi hanya dipicu jika tombol delete di NavGraph/Body diaktifkan
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { onDeleteClick() }
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CommonTopBar(
                title = if (isEditMode) "Edit Category" else "Add New Category",
                onBack = onBackClick,
                colors = topBarColors,
                windowInsets = WindowInsets(0.dp),
                actions = {
                    /* Dikosongkan sesuai permintaan user */
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onSubmitClick() },
                containerColor = PolnesGreen,
                contentColor = White
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Category")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // --- INPUT 1: TITLE ---
            Text(
                text = "Category Title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Enter category name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- INPUT 2: IMAGE PICKER ---
            Text(
                text = "Category Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { launcher.launch("image/*") }
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap to change", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                } else if (isEditMode && categoryToEdit != null) {
                    // Tampilkan Gambar Lama dari DummyData
                    Image(
                        painter = painterResource(id = R.drawable.category_tech),
                        contentDescription = "Current Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap to change", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to upload image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "* Recommended ratio 3:2 for best display.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- PREVIEW ADD MODE ---
@Preview(name = "Add Mode", showBackground = true)
@Composable
private fun AddCategoryPreview() {
    NewsPolnesTheme {
        // Catatan: Di Preview, AuthViewModel akan menggunakan nilai default (null),
        // sehingga guard biasanya tidak terlewati. Untuk Preview yang lengkap,
        // Anda mungkin perlu membuat AuthViewModel mock atau mengomentari guard sementara.
        // Tapi untuk tujuan perbaikan bug ini, versi sekarang sudah benar.
        AddANewCategoryScreen(
            categoryId = null,
            onBackClick = {},
            onSubmitClick = {}
        )
    }
}

// --- PREVIEW EDIT MODE ---
@Preview(name = "Edit Mode", showBackground = true)
@Composable
private fun EditCategoryPreview() {
    NewsPolnesTheme {
        AddANewCategoryScreen(
            categoryId = 1,
            onBackClick = {},
            onSubmitClick = {},
            onDeleteClick = {}
        )
    }
}