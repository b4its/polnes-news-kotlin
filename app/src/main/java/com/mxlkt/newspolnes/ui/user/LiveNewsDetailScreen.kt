package com.mxlkt.newspolnes.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import coil.compose.AsyncImage
import com.mxlkt.newspolnes.components.*
import com.mxlkt.newspolnes.utils.SessionManager
import com.mxlkt.newspolnes.view.NewsViewModel
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.NewsModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveNewsDetailScreen(
    onNavigateBack: () -> Unit,
    newsId: Int, // ID berita dari Navigasi
    viewModel: NewsViewModel = viewModel() // INJEKSI VIEWMODEL
) {
    val context = LocalContext.current

    // Menggunakan observeAsState untuk LiveData
    val newsDetail by viewModel.newsDetail.observeAsState(initial = null)
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)

    // Logika SessionManager (Asumsi userId menggunakan StateFlow/Flow, menggunakan collectAsState)
    val sessionManager = remember { SessionManager(context) }
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    // State lokal untuk rating pengguna
    var userRating by remember { mutableIntStateOf(0) }

    // Side Effect untuk memuat data saat layar dimuat
    LaunchedEffect(newsId) {
        viewModel.fetchNewsDetail(newsId)
        // Asumsi: Jika commentsList dimuat terpisah, tambahkan di sini:
        // viewModel.fetchComments(newsId)
    }

    // --- Logic Kirim Rating (DIAKTIFKAN) ---
    // fun submitRating() {
    //     val currentUserId = loggedInUserId // Ambil nilai state secara lokal
    //
    //     if (currentUserId == null) {
    //         Toast.makeText(context, "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
    //         return
    //     }
    //
    //     if (userRating <= 0) {
    //         Toast.makeText(context, "Pilih rating bintang terlebih dahulu.", Toast.LENGTH_SHORT).show()
    //         return
    //     }
    //
    //     viewModel.submitRating(
    //         newsId = newsId,
    //         userId = currentUserId, // Menggunakan nilai yang sudah dipastikan non-null
    //         ratingValue = userRating
    //     ) { success ->
    //         if (success) {
    //             Toast.makeText(context, "Rating $userRating bintang berhasil terkirim!", Toast.LENGTH_SHORT).show()
    //             userRating = 0 // Reset rating setelah sukses
    //             // Muat ulang data (jika diperlukan untuk update tampilan rating/komentar)
    //             viewModel.fetchNewsDetail(newsId)
    //         } else {
    //             Toast.makeText(context, "Gagal mengirim rating.", Toast.LENGTH_SHORT).show()
    //         }
    //     }
    // }
    // -------------------------------------------

    Scaffold(
        topBar = {
            CommonTopBar(
                title = newsDetail?.title ?: "Article",
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->

        // Tampilkan Loading State
        if (isLoading == true) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Tampilkan Error State atau Data Not Found
        val news = newsDetail
        if (news == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(errorMessage ?: "Berita tidak ditemukan atau gagal dimuat.")
            }
            return@Scaffold
        }

        // Data ditemukan, tampilkan konten
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Bagian Judul
            item {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            // Bagian Gambar
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRXg3WCpcrYqjif0W-Gs4OYWOKu_oXoMgdPKw&s",
                    contentDescription = news.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
            }
            // Bagian Author & Tanggal
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Menggunakan safe call pada news.author
                AuthorDateRow(
                    authorName = news.author.name ?: "Unknown",
                    date = StoreData.formatDate(news.created_at)
                )
            }
            // Bagian Konten
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // ✅ PERBAIKAN: Menggunakan operator Elvis (?: "") untuk mencegah NullPointerException
                HtmlText(html = news.contents ?: "", modifier = Modifier.padding(horizontal = 16.dp))
            }
            // Bagian Video (jika ada)
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
            //
            // // BAGIAN INPUT RATING (DIKOMENTARI)
            // item {
            //     Spacer(modifier = Modifier.height(24.dp))
            //     ArticleRatingInput(
            //         currentRating = userRating,
            //         onRatingSelected = { userRating = it },
            //         onSubmit = { submitRating() },
            //         modifier = Modifier.padding(horizontal = 16.dp)
            //     )
            // }

            // // BAGIAN RINGKASAN RATING (DIKOMENTARI)
            // val allComments by viewModel.commentsList.observeAsState(initial = emptyList())
            //
            // // Filter komentar berdasarkan newsId
            // val comments = remember(newsId, allComments) {
            //     // allComments sudah non-null karena initial = emptyList()
            //     allComments.filter { it.newsId == newsId }
            // }
            //
            // item {
            //     Spacer(modifier = Modifier.height(24.dp))
            //     Text(
            //         text = "Rating Pengguna Lain (${comments.size} komentar)",
            //         style = MaterialTheme.typography.titleMedium,
            //         fontWeight = FontWeight.SemiBold,
            //         modifier = Modifier.padding(horizontal = 16.dp)
            //     )
            // }
            //
            // item {
            //     RatingSummary(comments = comments, modifier = Modifier.padding(horizontal = 16.dp))
            // }
        }
    }
}