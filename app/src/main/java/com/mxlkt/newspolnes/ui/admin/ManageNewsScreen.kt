package com.mxlkt.newspolnes.ui.admin

import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mxlkt.newspolnes.components.ConfirmationDialog
import com.mxlkt.newspolnes.components.StatusChip
import com.mxlkt.newspolnes.model.NewsModel
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.ui.theme.ActionDeleteIcon
import com.mxlkt.newspolnes.ui.theme.White
import com.mxlkt.newspolnes.view.NewsViewModel
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageNewsScreen(
    viewModel: NewsViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    onAddArticleClick: () -> Unit,      // Callback Navigasi Tambah
    onEditArticleClick: (Int) -> Unit   // Callback Navigasi Edit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ViewModel State ---
    val newsList by viewModel.newsList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val successMessage by viewModel.successMessage.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    // --- UI State ---
    val tabs = listOf("Needs Review", "All News")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var searchQuery by remember { mutableStateOf("") }

    // --- State Pagination API (DIPISAH) ---
    // Halaman khusus untuk tab Needs Review
    var reviewPage by remember { mutableIntStateOf(1) }
    // Halaman khusus untuk tab All News
    var allNewsPage by remember { mutableIntStateOf(1) }

    // --- Action Dialog States ---
    var articleToDelete by remember { mutableStateOf<NewsModel?>(null) }
    var articleToReview by remember { mutableStateOf<NewsModel?>(null) }

    // --- Initial Load ---
    LaunchedEffect(Unit) {
        categoryViewModel.fetchAllCategories()
    }

    // Reset dan Fetch data saat tab berubah
    LaunchedEffect(pagerState.currentPage) {
        searchQuery = "" // Reset search saat ganti tab

        // Logika Reset Page saat ganti tab agar data fresh dari halaman 1
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
            articleToReview = null // Close review dialog
            viewModel.clearStatusMessages()

            // Refresh list current tab sesuai halaman 1 agar update terlihat
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

    // Fungsi Load More yang sudah dipisah logikanya
    fun loadMore() {
        if (pagerState.currentPage == 0) {
            reviewPage++
            viewModel.fetchReviewNewsList(reviewPage)
        } else {
            allNewsPage++
            viewModel.fetchNewsList(allNewsPage)
        }
    }

    // --- MAIN UI START ---
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            // Tampilkan tombol Tambah hanya di tab "All News" (Tab 1)
            if (pagerState.currentPage == 1) {
                FloatingActionButton(
                    onClick = onAddArticleClick,
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

            // Loading Bar (Top)
            if (isLoading && articleToReview == null && articleToDelete == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Pager & List
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->

                // Client-side filtering
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
                                        onEditArticleClick(article.id)
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

                        // Load More Button
                        // SYARAT MUNCUL:
                        // 1. Tidak sedang mencari (searchQuery kosong)
                        // 2. Data yang ada di list saat ini cukup banyak (misal >= 5)
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
                            // Spacer agar item terakhir tidak tertutup FAB
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SUB-COMPOSABLES ----------------

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