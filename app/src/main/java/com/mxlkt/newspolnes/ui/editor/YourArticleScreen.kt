package com.mxlkt.newspolnes.ui.editor

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.mxlkt.newspolnes.components.ArticleCard
import com.mxlkt.newspolnes.components.ConfirmationDialog
import com.mxlkt.newspolnes.model.NewsCreateRequest
import com.mxlkt.newspolnes.model.NewsModel
import com.mxlkt.newspolnes.ui.theme.ActionDeleteIcon
import com.mxlkt.newspolnes.ui.theme.White
import com.mxlkt.newspolnes.utils.SessionManager
import com.mxlkt.newspolnes.view.NewsViewModel
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourArticleScreen(
    navController: NavHostController,
    viewModel: NewsViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId by sessionManager.userId.collectAsState(initial = null)

    // --- State Data ---
    val newsList by viewModel.newsList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val successMessage by viewModel.successMessage.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    // --- State Categories ---
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())

    // --- State UI Lokal ---
    var searchQuery by remember { mutableStateOf("") }
    var articleToDelete by remember { mutableStateOf<NewsModel?>(null) }

    // --- State Modal (Bottom Sheet) ---
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- Form State ---
    var isEditMode by remember { mutableStateOf(false) }
    var currentEditingId by remember { mutableStateOf<Int?>(null) }

    // Input Fields Standard
    var titleInput by remember { mutableStateOf("") }
    var youtubeLinkInput by remember { mutableStateOf("") }

    // --- State Images ---
    var selectedThumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Rich Text Editor State
    val richTextState = rememberRichTextState()

    // Dropdown Logic
    var categoryIdInput by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // --- Initial Effects ---
    LaunchedEffect(Unit) {
        viewModel.fetchNewsList(1)
        categoryViewModel.fetchAllCategories()
    }

    LaunchedEffect(isEditMode, currentEditingId, showBottomSheet) {
        if (showBottomSheet) {
            if (!isEditMode) {
                if (richTextState.toHtml().isEmpty()) {
                    richTextState.clear()
                }
            }
        }
    }

    val myArticles = remember(newsList, userId, searchQuery) {
        if (userId == null) emptyList()
        else {
            val userArticles = newsList.filter { it.authorId == userId }
            if (searchQuery.isBlank()) userArticles
            else userArticles.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Handle Toast Messages
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showBottomSheet = false
            viewModel.fetchNewsList(1)
            viewModel.clearStatusMessages()
            richTextState.clear()
            // Reset input
            titleInput = ""
            selectedCategoryName = ""
            categoryIdInput = ""
            youtubeLinkInput = ""
            selectedImageUri = null
            selectedThumbnailUri = null
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessages()
        }
    }

    // --- Helper Functions ---
    fun openAddModal() {
        isEditMode = false
        currentEditingId = null
        titleInput = ""
        richTextState.clear()
        categoryIdInput = ""
        selectedCategoryName = ""
        youtubeLinkInput = ""
        selectedImageUri = null
        selectedThumbnailUri = null
        showBottomSheet = true
    }

    fun openEditModal(article: NewsModel) {
        isEditMode = true
        currentEditingId = article.id

        // 1. Panggil detail terbaru dari server (Optional, good practice)
        viewModel.fetchNewsDetail(article.id)

        // 2. Isi form dengan data awal dari list
        titleInput = article.title
        richTextState.setHtml(article.contents)
        youtubeLinkInput = article.linkYoutube ?: ""
        categoryIdInput = article.categoryId.toString()

        // Cari nama kategori untuk dropdown
        val currentCategory = categories.find { it.id == article.categoryId }
        selectedCategoryName = currentCategory?.name ?: ""

        // Reset gambar input
        selectedImageUri = null
        selectedThumbnailUri = null

        showBottomSheet = true
    }

    // --- Launchers ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> selectedImageUri = uri }

    val thumbnailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> selectedThumbnailUri = uri }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddModal() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Article", tint = White)
            }
        }
    ) { innerPadding ->

        if (articleToDelete != null) {
            ConfirmationDialog(
                title = "Hapus Artikel?",
                text = "Apakah Anda yakin ingin menghapus '${articleToDelete?.title}'?",
                confirmButtonColor = ActionDeleteIcon,
                confirmButtonText = "Hapus",
                dismissButtonText = "Batal",
                onDismiss = { articleToDelete = null },
                onConfirm = {
                    articleToDelete?.let { viewModel.deleteNews(it.id) }
                    articleToDelete = null
                }
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (isEditMode) "Edit Artikel" else "Tulis Artikel Baru",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 1. Judul
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Artikel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Kategori
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            if (categories.isEmpty()) {
                                DropdownMenuItem(text = { Text("Memuat kategori...") }, onClick = { })
                            } else {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategoryName = category.name
                                            categoryIdInput = category.id.toString()
                                            isCategoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Youtube
                    OutlinedTextField(
                        value = youtubeLinkInput,
                        onValueChange = { youtubeLinkInput = it },
                        label = { Text("Link Youtube (Opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Thumbnail
                    Text("Thumbnail (Wajib/Opsional)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { thumbnailPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(text = if (selectedThumbnailUri != null) "Ganti Thumbnail" else "Pilih Thumbnail", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        if (selectedThumbnailUri != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thumbnail siap!", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Rich Editor
                    Text("Isi Artikel", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    // Toolbar
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }) {
                            Icon(Icons.Default.FormatBold, "Bold")
                        }
                    }

                    OutlinedRichTextEditor(
                        state = richTextState,
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        label = { Text("Tulis konten di sini...") },
                        colors = RichTextEditorDefaults.outlinedRichTextEditorColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Gambar Utama
                    Text("Gambar Utama (Opsional)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) {
                            Text(text = if (selectedImageUri != null) "Ganti Gambar" else "Pilih Gambar", color = Color.Black)
                        }
                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gambar dipilih!", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // 7. Tombol Simpan (Logic Inti)
                    Button(
                        onClick = {
                            val contentHtml = richTextState.toHtml()

                            // Validasi dasar UI
                            if (titleInput.isBlank() || contentHtml.isBlank()) {
                                Toast.makeText(context, "Mohon lengkapi Judul dan Konten", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val authId = userId ?: 0

                            // Konversi URI ke File
                            val fileImage = selectedImageUri?.let { uriToFile(context, it) }
                            val fileThumbnail = selectedThumbnailUri?.let { uriToFile(context, it) }

                            if (isEditMode && currentEditingId != null) {
                                // --- LOGIKA UPDATE ---

                                // 1. Ambil data original untuk fallback
                                val originalArticle = newsList.find { it.id == currentEditingId }

                                // 2. Tentukan Status:
                                // Gunakan status asli jika ada, jika tidak default ke "draft".
                                // PENTING: .lowercase() untuk menghindari error 422 "The selected status is invalid"
                                val statusToSend = (originalArticle?.status ?: "draft").lowercase()

                                // 3. Tentukan Category ID:
                                // Jika input dropdown valid, gunakan itu. Jika tidak, gunakan ID lama.
                                val catIdToSend = categoryIdInput.toIntOrNull() ?: originalArticle?.categoryId

                                // 4. Tentukan Youtube Link:
                                // Karena kita sudah pre-fill input saat openEditModal, kita percaya input user.
                                // Jika input kosong, berarti user menghapusnya (atau memang kosong).
                                val youtubeToSend = youtubeLinkInput.ifBlank { null }

                                viewModel.updateNews(
                                    newsId = currentEditingId!!,
                                    title = titleInput,
                                    content = contentHtml,
                                    authorId = authId,
                                    categoryId = catIdToSend,
                                    linkYoutube = youtubeToSend,
                                    status = statusToSend, // <--- Perbaikan Utama (Lowercase)
                                    imageFile = fileImage,
                                    thumbnailFile = fileThumbnail
                                )
                            } else {
                                // --- LOGIKA CREATE ---
                                val catId = categoryIdInput.toIntOrNull()
                                val request = NewsCreateRequest(
                                    title = titleInput,
                                    content = contentHtml,
                                    categoryId = catId,
                                    authorId = authId,
                                    linkYoutube = youtubeLinkInput.ifBlank { null },
                                    status = "draft"
                                )
                                viewModel.createNews(request, fileImage, fileThumbnail)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = White)
                        } else {
                            Text(if (isEditMode) "Simpan Perubahan" else "Buat Artikel")
                        }
                    }
                }
            }
        }

        // --- Konten List Artikel ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ... (Search bar dan LazyColumn sama seperti kode asli) ...
            if (isLoading && !showBottomSheet) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (userId == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Silakan login terlebih dahulu.") }
            } else if (myArticles.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada artikel.") }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 80.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myArticles, key = { it.id }) { article ->
                        ArticleItemWithBadge(
                            article = article,
                            onEdit = { openEditModal(article) },
                            onDelete = { articleToDelete = article }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ArticleItemWithBadge(
    article: NewsModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val status = article.status.uppercase()
    val (statusMessage, statusColor) = when (status) {
        "PENDING_REVIEW" -> "Menunggu Review" to MaterialTheme.colorScheme.tertiary
        "PENDING_DELETION" -> "Menunggu Hapus" to MaterialTheme.colorScheme.error
        "PENDING_UPDATE" -> "Menunggu Edit" to MaterialTheme.colorScheme.secondary
        "REJECTED" -> "Ditolak" to MaterialTheme.colorScheme.error
        "DRAFT" -> "Draft" to Color.Gray
        "PUBLISHED" -> "Terbit" to Color.Green.copy(alpha = 0.7f)
        else -> null to Color.Transparent
    }

    val canEditOrDelete = status in listOf("DRAFT", "REJECTED", "PUBLISHED")

    Box(modifier = Modifier.fillMaxWidth()) {
        ArticleCard(
            article = article,
            onEdit = if (canEditOrDelete) onEdit else {{}},
            onDelete = if (canEditOrDelete) onDelete else {{}}
        )

        if (statusMessage != null && status != "PUBLISHED") {
            Surface(
                color = statusColor.copy(alpha = 0.9f),
                contentColor = White,
                shape = MaterialTheme.shapes.medium.copy(topEnd = CornerSize(0.dp), bottomStart = CornerSize(8.dp)),
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (status == "REJECTED") Icons.Default.Close else Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = statusMessage, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// --- Helper: Convert URI to File ---
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}