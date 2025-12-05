package com.mxlkt.newspolnes.ui.user

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.components.*
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    onNavigateBack: () -> Unit,
    newsId: Int
) {
    val news = remember(newsId) { StoreData.newsList.find { it.id == newsId } }
    val author = remember(news?.authorId) { StoreData.userList.find { it.id == news?.authorId } }
    val comments = remember(newsId) { StoreData.commentList.filter { it.newsId == newsId } }

    val context = LocalContext.current
    var userRating by remember { mutableIntStateOf(0) }

    // � PERBAIKAN: Mengambil data user yang sedang login menggunakan SessionManager
    val sessionManager = remember { SessionManager(context) }
    // Mengumpulkan User ID dari DataStore (Flow -> State)
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    // Mencari objek User lengkap di StoreData berdasarkan ID yang sedang login
    val currentUser = remember(loggedInUserId) {
        loggedInUserId?.let { id -> StoreData.userList.find { it.id == id } }
    }

    // � Logic Kirim Rating
    fun submitRatingToDatabase() {
        if (currentUser == null) {
            // Jika user ID belum tersedia atau user belum login
            Toast.makeText(context, "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        // DATA YANG AKAN DIKIRIM KE BACKEND:
        val userId = currentUser.id
        val newsIdToSend = newsId
        val ratingValue = userRating

        // � TODO: Tambahkan logic untuk menyimpan rating baru ke StoreData.commentList di sini
        // Perlu diperhatikan bahwa StoreData.commentList harus berupa MutableList agar bisa dimodifikasi.

        // Simulasi Kirim
        Toast.makeText(
            context,
            "Rating $ratingValue bintang dari ${currentUser.name} terkirim!",
            Toast.LENGTH_SHORT
        ).show()

        userRating = 0
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Article",
                onBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (news == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Berita tidak ditemukan.")
            }
            return@Scaffold
        }

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
                Image(
                    painter = painterResource(id = news.imageRes),
                    contentDescription = news.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
            }
            // Bagian Author & Tanggal
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AuthorDateRow(authorName = author?.name ?: "Unknown", date = news.date)
            }
            // Bagian Konten
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HtmlText(html = news.content, modifier = Modifier.padding(horizontal = 16.dp))
            }
            // Bagian Video (jika ada)
            if (news.youtubeVideoId != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp))
                    VideoThumbnailCard(
                        thumbnailCard = "abc",
                        onClick = {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("abc"))
                            context.startActivity(webIntent)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // BAGIAN INPUT RATING
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ArticleRatingInput(
                    currentRating = userRating,
                    onRatingSelected = { userRating = it },
                    onSubmit = { submitRatingToDatabase() }
                )
            }

            // BAGIAN RINGKASAN RATING
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Rating Pengguna Lain",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                RatingSummary(comments = comments)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewsDetailScreenPreview() {
    NewsDetailScreen(onNavigateBack = {}, newsId = 1)
}