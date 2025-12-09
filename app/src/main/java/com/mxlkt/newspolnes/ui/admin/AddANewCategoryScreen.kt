package com.mxlkt.newspolnes.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.News
import com.mxlkt.newspolnes.ui.theme.PolnesGreen
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.ui.theme.White
import com.mxlkt.newspolnes.view.AuthViewModel
import com.mxlkt.newspolnes.model.UserRole
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddANewArticleScreen(
    articleId: Int?,
    onBackClick: () -> Unit,
    //  PERUBAHAN 1: Callback onSubmitClick membawa data keluar
    onSubmitClick: (String, String, String, Int?, Uri?) -> Unit,
    //  PERUBAHAN 2: Callback untuk request hapus
    onRequestDelete: (Int) -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    // --- Auth Logic ---
    val currentAuthorId by authViewModel.userId.collectAsState(initial = null)
    val currentUserRoleString by authViewModel.userRole.collectAsState(initial = null)

    val currentUserRole = remember(currentUserRoleString) {
        try {
            currentUserRoleString?.let { UserRole.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    val isAuthorized = currentAuthorId != null &&
            (currentUserRole == UserRole.EDITOR || currentUserRole == UserRole.ADMIN)

    if (!isAuthorized) {
        Scaffold(topBar = { CommonTopBar(title = "Access Denied", onBack = onBackClick) }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Error: Anda harus login sebagai Editor/Admin untuk membuat artikel.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // --- Data Logic ---
    val isEditMode = articleId != null

    val articleToEdit: News? = if (isEditMode) {
        StoreData.newsList.find { it.id == articleId }
    } else {
        null
    }

    // State Form
    var title by remember { mutableStateOf(articleToEdit?.title ?: "") }
    var content by remember { mutableStateOf(articleToEdit?.content ?: "") }
    var youtubeLink by remember { mutableStateOf(articleToEdit?.youtubeVideoId ?: "") }

    // State Image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    // State Category
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val initialCategory = StoreData.categoryList.find { it.id == articleToEdit?.categoryId }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val categories = StoreData.categoryList

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CommonTopBar(
                title = if (isEditMode) "Edit Article" else "Add a New Article",
                onBack = onBackClick,
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //  PERUBAHAN 3: Kirim data ke NavGraph saat tombol ditekan
                    if (title.isNotEmpty() && content.isNotEmpty() && selectedCategory != null) {
                        onSubmitClick(
                            title,
                            content,
                            youtubeLink,
                            selectedCategory?.id,
                            selectedImageUri
                        )
                    } else {
                        // Opsional: Handle jika data belum lengkap (misal field merah)
                    }
                },
                containerColor = PolnesGreen,
                contentColor = White
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Submit Article")
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- INPUT JUDUL ---
            Text("Title", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Add title...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- INPUT GAMBAR ---
            Text("Image", style = MaterialTheme.typography.titleMedium)

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
                val imagePainter = if (selectedImageUri != null) {
                    rememberAsyncImagePainter(selectedImageUri)
                } else if (isEditMode && articleToEdit != null) {
                    painterResource(id = articleToEdit.imageRes)
                } else {
                    null
                }

                if (imagePainter != null) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Article Image",
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
                    // Placeholder
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


            // --- INPUT KATEGORI ---
            Text("Category", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = { selectedCategory = category; categoryDropdownExpanded = false }
                        )
                    }
                }
            }

            // --- INPUT KONTEN ---
            Text("Text", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start writing your article") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            // --- INPUT VIDEO ---
            Text("Video (Youtube Link)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = youtubeLink,
                onValueChange = { youtubeLink = it },
                placeholder = { Text("Add a Youtube Link (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            //  PERUBAHAN 4: Tombol Request Delete (Hanya muncul saat Edit)
            if (isEditMode && articleId != null) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onRequestDelete(articleId) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request to Delete Article")
                }
                Text(
                    text = "*Deleting requires Admin approval",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- PREVIEW DI-UPDATE UNTUK MENGHINDARI ERROR ---
