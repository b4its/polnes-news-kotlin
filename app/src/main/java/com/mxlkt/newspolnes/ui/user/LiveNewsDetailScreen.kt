package com.mxlkt.newspolnes.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mxlkt.newspolnes.components.*
import com.mxlkt.newspolnes.model.Comment
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.utils.SessionManager
import com.mxlkt.newspolnes.view.CommentViewModel
import com.mxlkt.newspolnes.view.NewsViewModel

// Konstanta URL Dasar untuk Gambar
private const val BASE_IMAGE_URL = "https://polnes-news.b4its.tech/"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveNewsDetailScreen(
    onNavigateBack: () -> Unit,
    newsId: Int,
    newsViewModel: NewsViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // --- State NewsViewModel ---
    val newsDetail by newsViewModel.newsDetail.observeAsState(initial = null)
    val isNewsLoading by newsViewModel.isLoading.observeAsState(initial = false)
    val newsErrorMessage by newsViewModel.errorMessage.observeAsState(initial = null)

    // --- State CommentViewModel ---
    val comments by commentViewModel.commentList.observeAsState(emptyList())
    val isCommentsLoading by commentViewModel.isLoading.observeAsState(false)
    val commentErrorMessage by commentViewModel.errorMessage.observeAsState()
    val commentSuccessMessage by commentViewModel.successMessage.observeAsState()

    // --- State User & UI ---
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)
    var userRating by remember { mutableIntStateOf(0) }

    // --- LOGIC: Cek Apakah User Sudah Pernah Komen ---
    // Mencari komentar di list yang userId-nya sama dengan userId yang login
    val existingComment = remember(comments, loggedInUserId) {
        comments.find { it.userId == loggedInUserId }
    }

    // --- Side Effects ---
    LaunchedEffect(newsId) {
        // Ambil detail berita
        newsViewModel.fetchNewsDetail(newsId)
        newsViewModel.addViews(newsId)

        // Ambil komentar
        commentViewModel.fetchComments(newsId)
    }

    // Sinkronisasi Rating Awal:
    // Jika user sudah pernah rating, isi bintang input dengan rating lama
    LaunchedEffect(existingComment) {
        if (existingComment != null) {
            userRating = existingComment.rating
        } else {
            // Jika belum pernah rating (atau logout), reset ke 0
            userRating = 0
        }
    }

    // Handle Toast untuk Error Comment
    LaunchedEffect(commentErrorMessage) {
        commentErrorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            commentViewModel.clearStatusMessages()
        }
    }

    // Handle Toast untuk Success Comment
    LaunchedEffect(commentSuccessMessage) {
        commentSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            // Kita TIDAK mereset userRating ke 0 di sini agar user melihat rating barunya
            commentViewModel.clearStatusMessages()
        }
    }

    // --- Logic URL Gambar ---
    val news = newsDetail
    val fullImageUrl = news?.gambar?.let { BASE_IMAGE_URL + it }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = news?.title ?: "Detail Berita",
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->

        // Loading State (Hanya jika loading berita utama)
        if (isNewsLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Error State (Jika berita tidak ditemukan)
        if (news == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(newsErrorMessage ?: "Berita tidak ditemukan atau gagal dimuat.")
            }
            return@Scaffold
        }

        // --- KONTEN UTAMA ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Judul
            item {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }

            // 2. Gambar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = if (news.gambar.isNullOrEmpty()) {
                        "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"
                    } else {
                        "https://polnes-news.b4its.tech/public/${news.gambar}"
                    },
                    contentDescription = news.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            }

            // 3. Author & Tanggal
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AuthorDateRow(
                    authorName = news.author?.name ?: "Unknown",
                    date = StoreData.formatDate(news.created_at)
                )
            }

            // 4. Isi Berita (HTML)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HtmlText(html = news.contents ?: "", modifier = Modifier.padding(horizontal = 16.dp))
            }

            // 5. Video (Jika ada)
            if (news.linkYoutube != null && news.linkYoutube.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
                    VideoThumbnailCard(
                        youtubeVideoId = news.linkYoutube,
                        onClick = {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${news.linkYoutube}"))
                            context.startActivity(webIntent)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // 6. Input Rating (Update / Create)
            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Menampilkan input hanya jika user login (opsional: bisa juga ditampilkan tapi disable)
                // Di sini kita tampilkan, validasi login ada di tombol submit
                ArticleRatingInput(
                    currentRating = userRating,
                    // Aktifkan mode Update jika existingComment ditemukan
                    isUpdateMode = (existingComment != null),
                    onRatingSelected = { userRating = it },
                    onSubmit = {
                        val currentUserId = loggedInUserId

                        if (currentUserId == null) {
                            Toast.makeText(context, "Silakan login untuk memberi rating", Toast.LENGTH_SHORT).show()
                        } else if (userRating == 0) {
                            Toast.makeText(context, "Silakan pilih bintang terlebih dahulu", Toast.LENGTH_SHORT).show()
                        } else {
                            // LOGIKA UTAMA: Pilih Store atau Update
                            if (existingComment != null) {
                                // Jika sudah ada -> UPDATE (PATCH)
                                commentViewModel.updateComment(newsId, currentUserId, userRating)
                            } else {
                                // Jika belum ada -> STORE (POST)
                                commentViewModel.storeComment(newsId, currentUserId, userRating)
                            }
                        }
                    }
                )
            }

            // 7. Ringkasan Rating
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Ulasan Pengguna",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                RatingSummary(comments = comments)
            }

            // 8. Daftar Komentar
            if (isCommentsLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada ulasan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }
        }
    }
}

// --- Helper Composable untuk Item Komentar ---
@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.user?.name ?: "Pengguna",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = comment.rating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Diulas pada: ${comment.date}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}