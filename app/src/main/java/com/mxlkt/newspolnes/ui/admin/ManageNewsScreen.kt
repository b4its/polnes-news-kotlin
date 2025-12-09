package com.mxlkt.newspolnes.ui.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.mxlkt.newspolnes.components.ConfirmationDialog
import com.mxlkt.newspolnes.components.StatusChip
import com.mxlkt.newspolnes.model.NewsCreateRequest
import com.mxlkt.newspolnes.model.NewsModel
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.ui.theme.ActionDeleteIcon
import com.mxlkt.newspolnes.ui.theme.White
import com.mxlkt.newspolnes.utils.SessionManager // Pastikan import ini ada
import com.mxlkt.newspolnes.view.NewsViewModel
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

// --- Helper Functions ---
private fun getAuthorName(article: NewsModel): String {
    return article.author?.name ?: "Unknown Author (${article.authorId})"
}

private fun getFullImageUrl(path: String?): String {
    return if (path.isNullOrEmpty()) {
        "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg" // Placeholder
    } else {
        "https://polnes-news.b4its.tech/public/$path" // Sesuaikan domain Anda
    }
}

// Helper: Convert URI to File (Dicopy dari referensi Anda)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageNewsScreen(
    viewModel: NewsViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    onAddArticleClick: () -> Unit,      // Callback ini mungkin tidak dipakai jika pakai Modal Lokal
    onEditArticleClick: (Int) -> Unit   // Callback ini kita override logic-nya pakai Modal Lokal
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Session Manager (Untuk Author ID) ---
    val sessionManager = remember { SessionManager(context) }
    val userId by sessionManager.userId.collectAsState(initial = null)

    // --- ViewModel State ---
    val newsList by viewModel.newsList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val successMessage by viewModel.successMessage.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    // --- Categories State ---
    val categories by categoryViewModel.categoryList.observeAsState(emptyList())

    // --- UI State ---
    val tabs = listOf("Needs Review", "All News")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var searchQuery by remember { mutableStateOf("") }

    // --- State Pagination API ---
    var reviewPage by remember { mutableIntStateOf(1) }
    var allNewsPage by remember { mutableIntStateOf(1) }

    // --- Action Dialog States ---
    var articleToDelete by remember { mutableStateOf<NewsModel?>(null) }
    var articleToReview by remember { mutableStateOf<NewsModel?>(null) }

    // --- FORM / MODAL STATE (Ditambahkan) ---
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isEditMode by remember { mutableStateOf(false) }
    var currentEditingId by remember { mutableStateOf<Int?>(null) }

    // Input Fields
    var titleInput by remember { mutableStateOf("") }
    var youtubeLinkInput by remember { mutableStateOf("") }

    // Dropdown Logic
    var categoryIdInput by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Image Pickers State
    var selectedThumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Rich Text Editor State
    val richTextState = rememberRichTextState()

    // --- Launchers ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> selectedImageUri = uri }

    val thumbnailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> selectedThumbnailUri = uri }

    // --- Initial Load ---
    LaunchedEffect(Unit) {
        categoryViewModel.fetchAllCategories()
    }

    // Reset dan Fetch data saat tab berubah
    LaunchedEffect(pagerState.currentPage) {
        searchQuery = ""
        if (pagerState.currentPage == 0) {
            reviewPage = 1
            viewModel.fetchReviewNewsList(reviewPage)
        } else {
            allNewsPage = 1
            viewModel.fetchNewsList(allNewsPage)
        }
    }

    // Handle Toast Messages & Refresh
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            articleToReview = null
            showBottomSheet = false // Tutup modal form jika sukses
            viewModel.clearStatusMessages()

            // Clear Form
            richTextState.clear()
            titleInput = ""
            selectedCategoryName = ""
            categoryIdInput = ""
            youtubeLinkInput = ""
            selectedImageUri = null
            selectedThumbnailUri = null

            // Refresh list
            if (pagerState.currentPage == 0) {
                reviewPage = 1
                viewModel.fetchReviewNewsList(1)
            } else {
                allNewsPage = 1
                viewModel.fetchNewsList(1)
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessages()
        }
    }

    fun loadMore() {
        if (pagerState.currentPage == 0) {
            reviewPage++
            viewModel.fetchReviewNewsList(reviewPage)
        } else {
            allNewsPage++
            viewModel.fetchNewsList(allNewsPage)
        }
    }

    // --- Form Logic Helpers ---
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

    fun openEditModalLocal(article: NewsModel) {
        isEditMode = true
        currentEditingId = article.id

        // Populate Data
        titleInput = article.title
        richTextState.setHtml(article.contents)
        youtubeLinkInput = article.linkYoutube ?: ""
        categoryIdInput = article.categoryId.toString()

        val currentCategory = categories.find { it.id == article.categoryId }
        selectedCategoryName = currentCategory?.name ?: ""

        selectedImageUri = null
        selectedThumbnailUri = null

        showBottomSheet = true
    }

    // --- MAIN UI START ---
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            if (pagerState.currentPage == 1) {
                FloatingActionButton(
                    onClick = { openAddModal() }, // Panggil modal lokal
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Article")
                }
            }
        }
    ) { innerPadding ->

        // ---------------- DIALOGS ----------------

        // 1. Delete Confirmation
        if (articleToDelete != null) {
            ConfirmationDialog(
                title = "Hapus Berita?",
                text = "Yakin ingin menghapus '${articleToDelete?.title}'?",
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

        // 2. Review Dialog (Approve/Reject)
        if (articleToReview != null) {
            ReviewArticleDialog(
                article = articleToReview!!,
                onDismiss = { articleToReview = null },
                onApprove = {
                    // Sama seperti sebelumnya...
                    viewModel.updateNews(
                        newsId = articleToReview!!.id,
                        title = articleToReview!!.title,
                        content = articleToReview!!.contents,
                        authorId = articleToReview!!.authorId,
                        categoryId = articleToReview!!.categoryId,
                        linkYoutube = articleToReview!!.linkYoutube,
                        status = "published",
                        imageFile = null,
                        thumbnailFile = null
                    )
                },
                onReject = {
                    // Sama seperti sebelumnya...
                    viewModel.updateNews(
                        newsId = articleToReview!!.id,
                        title = articleToReview!!.title,
                        content = articleToReview!!.contents,
                        authorId = articleToReview!!.authorId,
                        categoryId = articleToReview!!.categoryId,
                        linkYoutube = articleToReview!!.linkYoutube,
                        status = "rejected",
                        imageFile = null,
                        thumbnailFile = null
                    )
                }
            )
        }

        // 3. BOTTOM SHEET FORM (ADD/EDIT NEWS)
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

                    // Title
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Artikel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Dropdown
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
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
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
                    Spacer(modifier = Modifier.height(8.dp))

                    // Youtube
                    OutlinedTextField(
                        value = youtubeLinkInput,
                        onValueChange = { youtubeLinkInput = it },
                        label = { Text("Link Youtube (Opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Rich Text Editor Toolbar (LENGKAP)
                    Text("Isi Artikel", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Toolbar Row
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }) {
                            Icon(Icons.Default.FormatBold, "Bold")
                        }
                        IconButton(onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }) {
                            Icon(Icons.Default.FormatItalic, "Italic")
                        }
                        IconButton(onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }) {
                            Icon(Icons.Default.FormatUnderlined, "Underline")
                        }
                        // Separator
                        Box(Modifier.width(1.dp).height(24.dp).background(Color.Gray).align(Alignment.CenterVertically))

                        IconButton(onClick = { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left)) }) {
                            Icon(Icons.Default.FormatAlignLeft, "Align Left")
                        }
                        IconButton(onClick = { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) }) {
                            Icon(Icons.Default.FormatAlignCenter, "Align Center")
                        }
                        IconButton(onClick = { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right)) }) {
                            Icon(Icons.Default.FormatAlignRight, "Align Right")
                        }
                    }

                    // Editor
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

                    // Images Section
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // Thumbnail Button
                        Column {
                            Button(
                                onClick = { thumbnailPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Text(if (selectedThumbnailUri != null) "Ganti Thumbnail" else "Thumbnail", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        // Main Image Button
                        Column {
                            Button(
                                onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                            ) {
                                Text(if (selectedImageUri != null) "Ganti Cover" else "Gambar Cover", color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val contentHtml = richTextState.toHtml()
                            if (titleInput.isBlank() || contentHtml.isBlank()) {
                                Toast.makeText(context, "Mohon lengkapi Judul dan Konten", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val authId = userId ?: 0 // Menggunakan ID Admin yang login
                            val fileImage = selectedImageUri?.let { uriToFile(context, it) }
                            val fileThumbnail = selectedThumbnailUri?.let { uriToFile(context, it) }

                            if (isEditMode && currentEditingId != null) {
                                // UPDATE LOGIC (Same as editor)
                                val originalArticle = newsList.find { it.id == currentEditingId }
                                val statusToSend = (originalArticle?.status ?: "draft").lowercase()
                                val catIdToSend = categoryIdInput.toIntOrNull() ?: originalArticle?.categoryId
                                val youtubeToSend = youtubeLinkInput.ifBlank { null }

                                viewModel.updateNews(
                                    newsId = currentEditingId!!,
                                    title = titleInput,
                                    content = contentHtml,
                                    authorId = authId,
                                    categoryId = catIdToSend,
                                    linkYoutube = youtubeToSend,
                                    status = statusToSend,
                                    imageFile = fileImage,
                                    thumbnailFile = fileThumbnail
                                )
                            } else {
                                // CREATE LOGIC
                                val catId = categoryIdInput.toIntOrNull()
                                val request = NewsCreateRequest(
                                    title = titleInput,
                                    content = contentHtml,
                                    categoryId = catId,
                                    authorId = authId,
                                    linkYoutube = youtubeLinkInput.ifBlank { null },
                                    status = "published" // Admin create biasanya langsung Published? Atau Draft? Saya set Published biar beda
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
                            Text(if (isEditMode) "Simpan Perubahan" else "Terbitkan Berita")
                        }
                    }
                }
            }
        }

        // ---------------- PAGE CONTENT ----------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    )
                }
            }

            // Search Bar (Hanya muncul di All News / Tab 1)
            if (pagerState.currentPage == 1) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search news title...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Loading Bar
            if (isLoading && articleToReview == null && articleToDelete == null && !showBottomSheet) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Pager & List
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->

                val filteredList = remember(newsList, searchQuery) {
                    if (searchQuery.isBlank()) newsList
                    else newsList.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                getAuthorName(it).contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredList.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Article, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text("No data found.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { article ->
                            AdminNewsItem(
                                article = article,
                                isReviewMode = (page == 0),
                                onActionClick = {
                                    if (page == 0) {
                                        articleToReview = article
                                    } else {
                                        // Panggil modal edit lokal
                                        openEditModalLocal(article)
                                    }
                                },
                                onDeleteClick = { articleToDelete = article },
                                onUnpublishClick = {
                                    viewModel.updateDraftStatus(article.id)
                                },
                                onReviewClick = {
                                    viewModel.updateReviewStatus(article.id)
                                }
                            )
                        }

                        // Load More Logic
                        if (searchQuery.isBlank() && filteredList.size >= 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        TextButton(onClick = { loadMore() }) {
                                            Text("Load More")
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        } else {
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SUB-COMPOSABLES ----------------
// ... (AdminNewsItem dan ReviewArticleDialog sama persis seperti sebelumnya) ...
@Composable
fun AdminNewsItem(
    article: NewsModel,
    isReviewMode: Boolean,
    onActionClick: () -> Unit,      // Aksi Edit
    onDeleteClick: () -> Unit,      // Aksi Hapus
    onUnpublishClick: () -> Unit,    // Aksi Draft/Unpublish
    onReviewClick: () -> Unit
) {
    val authorName = getAuthorName(article)
    val imageUrl = getFullImageUrl(article.gambar)

    val statusEnum = try {
        NewsStatus.valueOf(article.status.uppercase())
    } catch (e: Exception) {
        NewsStatus.DRAFT
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isReviewMode) Modifier.clickable { onActionClick() } else Modifier)
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                // Image
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Content Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = StoreData.formatDate(article.created_at), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = authorName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(status = statusEnum)
                }

                if (isReviewMode) {
                    Box(modifier = Modifier.height(80.dp).padding(start = 8.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Review", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (!isReviewMode) {
                Divider(color = Color.Gray.copy(alpha = 0.1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (article.status.equals("draft", ignoreCase = true)) {
                        OutlinedButton(
                            onClick = onReviewClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1958C2)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Pending, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = onActionClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDA8C1F)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onUnpublishClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Draft")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = onActionClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDA8C1F)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewArticleDialog(
    article: NewsModel,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val authorName = getAuthorName(article)
    val imageUrl = getFullImageUrl(article.gambar)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().background(Color.LightGray)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                    ) { Icon(Icons.Default.Close, null, tint = Color.White) }
                }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                    val statusUpper = article.status.uppercase()
                    val reqColor = if (statusUpper == "PENDING_DELETION") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    val reqText = when(statusUpper) {
                        "PENDING_DELETION" -> "REQUEST: DELETION"
                        "PENDING_UPDATE" -> "REQUEST: UPDATE"
                        else -> "REQUEST: PUBLISH"
                    }
                    Text(reqText, style = MaterialTheme.typography.labelLarge, color = reqColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("By $authorName • ${StoreData.formatDate(article.created_at)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(article.contents, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Reject") }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (article.status.uppercase() == "PENDING_DELETION") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (article.status.uppercase() == "PENDING_DELETION") "Confirm Delete" else "Approve")
                    }
                }
            }
        }
    }
}