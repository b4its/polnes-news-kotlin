package com.mxlkt.newspolnes.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mxlkt.newspolnes.components.ArticleCard
import com.mxlkt.newspolnes.components.ConfirmationDialog
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.News
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.ui.theme.ActionDeleteIcon
import com.mxlkt.newspolnes.ui.theme.NewsPolnesTheme
import com.mxlkt.newspolnes.ui.theme.White
import com.mxlkt.newspolnes.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourArticleScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // � VARIABEL KRITIS: State untuk memaksa Recomposition setelah data StoreData diubah
    var dataVersion by remember { mutableStateOf(0) }

    val userId by sessionManager.userId.collectAsState(initial = null)
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = false)

    if (!isLoggedIn || userId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 2. Filter Artikel: allEditorArticles bergantung pada userId dan dataVersion
    val allEditorArticles = remember(userId, dataVersion) {
        val currentUserId = userId
        if (currentUserId != null) {
            // Membaca ulang data setiap kali dataVersion berubah
            StoreData.newsList.filter { it.authorId == currentUserId }
        } else {
            emptyList()
        }
    }

    // 3. State Search
    var searchQuery by remember { mutableStateOf("") }
    var articleToDelete by remember { mutableStateOf<News?>(null) }

    // 4. Logic Filter Search
    val displayedArticles = remember(searchQuery, allEditorArticles) {
        if (searchQuery.isBlank()) {
            allEditorArticles
        } else {
            allEditorArticles.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("article_form") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Article", tint = White)
            }
        }
    ) { innerPadding ->

        // Dialog Konfirmasi Hapus
        if (articleToDelete != null) {
            val article = articleToDelete!!
            ConfirmationDialog(
                title = "Hapus Artikel?",
                text = "Apakah Anda yakin ingin mengajukan penghapusan untuk artikel '${article.title}'?",
                confirmButtonColor = ActionDeleteIcon,
                confirmButtonText = "Hapus",
                dismissButtonText = "Batal",
                onDismiss = { articleToDelete = null },
                onConfirm = {
                    coroutineScope.launch {
                        val index = StoreData.newsList.indexOfFirst { it.id == article.id }
                        if (index != -1) {
                            val updatedArticle = article.copy(status = NewsStatus.PENDING_DELETION)

                            // Baris ini sekarang aman karena StoreData.newsList adalah var mutableListOf
                            StoreData.newsList[index] = updatedArticle

                            // � Paksa Recomposition dengan mengubah dataVersion
                            dataVersion++
                        }
                        articleToDelete = null
                    }
                }
            )
        }

        // Layout Utama (Column)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your articles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
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
                    disabledContainerColor = Color.White,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Konten List
            if (displayedArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "You haven't written any articles yet." else "No articles found.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding() + 80.dp,
                        start = 0.dp,
                        end = 0.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedArticles, key = { it.id }) { article ->

                        // Logic Status Badge (Overlay)
                        val (statusMessage, statusColor) = when (article.status) {
                            NewsStatus.PENDING_REVIEW -> "Menunggu Review" to MaterialTheme.colorScheme.tertiary
                            NewsStatus.PENDING_DELETION -> "Menunggu Hapus" to MaterialTheme.colorScheme.error
                            NewsStatus.PENDING_UPDATE -> "Menunggu Edit" to MaterialTheme.colorScheme.secondary
                            NewsStatus.REJECTED -> "Ditolak Admin" to MaterialTheme.colorScheme.error
                            NewsStatus.DRAFT -> "Draft" to Color.Gray
                            else -> null to Color.Transparent
                        }

                        // Tentukan apakah artikel bisa di-edit/hapus
                        val canEditOrDelete = article.status in listOf(
                            NewsStatus.DRAFT,
                            NewsStatus.REJECTED,
                            NewsStatus.PUBLISHED
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            ArticleCard(
                                article = article,
                                onEdit = {
                                    if (canEditOrDelete) {
                                        navController.navigate("article_form?articleId=${article.id}")
                                    }
                                },
                                onDelete = {
                                    if (canEditOrDelete) {
                                        articleToDelete = article
                                    }
                                }
                            )

                            // Badge Overlay
                            if (statusMessage != null && article.status != NewsStatus.PUBLISHED) {
                                Surface(
                                    color = statusColor.copy(alpha = 0.9f),
                                    contentColor = White,
                                    shape = MaterialTheme.shapes.medium.copy(
                                        topEnd = androidx.compose.foundation.shape.CornerSize(0.dp),
                                        bottomStart = androidx.compose.foundation.shape.CornerSize(8.dp)
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (article.status == NewsStatus.REJECTED) Icons.Default.Close else Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = statusMessage,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                        // Jarak antar item
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun YourArticleScreenPreview() {
    NewsPolnesTheme {
        YourArticleScreen(navController = rememberNavController())
    }
}