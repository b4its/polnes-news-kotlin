package com.mxlkt.newspolnes.ui.editor

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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.model.DummyData
import com.mxlkt.newspolnes.model.News
import com.mxlkt.newspolnes.ui.theme.PolnesGreen
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.ui.theme.White
// 🟢 Import SessionManager
import com.mxlkt.newspolnes.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddANewArticleScreen(
    articleId: Int?,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    // 🟢 Ambil ID Penulis (Author ID) dari Session Manager
    val currentAuthorId = SessionManager.currentUser?.id

    // 🟢 GUARD: Jika user belum login, tampilkan pesan error
    if (currentAuthorId == null) {
        Scaffold(topBar = { CommonTopBar(title = "Error", onBack = onBackClick) }) {
            Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
                Text("Error: Anda harus login sebagai Editor untuk membuat artikel.")
            }
        }
        return
    }

    // --- Data Logic ---
    val isEditMode = articleId != null
    // Gunakan find saja, karena kita tahu currentUser pasti ada di sini
    val articleToEdit: News? = if (isEditMode) { DummyData.newsList.find { it.id == articleId } } else { null }

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
    val initialCategory = DummyData.categoryList.find { it.id == articleToEdit?.categoryId }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val categories = DummyData.categoryList

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
                // 💡 Saat onSubmitClick dipanggil, kita tahu authorId-nya adalah currentAuthorId
                onClick = { onSubmitClick() },
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
                    null // Placeholder case
                }

                if (imagePainter != null) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Article Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap to change", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    // 3. Kosong (Placeholder)
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
                modifier = Modifier.fillMaxWidth().height(250.dp)
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

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Preview ---
@Preview(name = "Add Mode", showBackground = true)
@Composable
fun AddArticleScreenPreview() {
    NewsPolnesTheme {
        AddANewArticleScreen(articleId = null, onBackClick = {}, onSubmitClick = {})
    }
}

@Preview(name = "Edit Mode", showBackground = true)
@Composable
fun EditArticleScreenPreview() {
    NewsPolnesTheme {
        AddANewArticleScreen(articleId = 1, onBackClick = {}, onSubmitClick = {})
    }
}