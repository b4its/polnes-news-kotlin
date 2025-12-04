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

// � Konstanta URL Dasar untuk Gambar (Pastikan ini sesuai dengan server Anda)
private const val BASE_IMAGE_URL = "https://polnes-news.b4its.tech/"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveNewsDetailScreen(
    onNavigateBack: () -> Unit,
    newsId: Int,
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current

    val newsDetail by viewModel.newsDetail.observeAsState(initial = null)
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val errorMessage by viewModel.errorMessage.observeAsState(initial = null)

    val sessionManager = remember { SessionManager(context) }
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    var userRating by remember { mutableIntStateOf(0) }

    LaunchedEffect(newsId) {
        viewModel.fetchNewsDetail(newsId)
        viewModel.addViews(newsId)
    }

    // Mengambil objek NewsModel yang aman (setelah check null)
    val news = newsDetail

    // � Logic untuk URL Gambar: Menggabungkan BASE_URL dengan path relatif
    val fullImageUrl = news?.gambar?.let {
        BASE_IMAGE_URL + it
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = news?.title ?: "Article",
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
                    // � GUNAKAN URL YANG SUDAH DIGABUNGKAN
                    model = if (news.gambar.isNullOrEmpty()) {
                        "https://www.internetcepat.id/wp-content/uploads/2023/12/20602785_6325254-scaled-1.jpg"
                    } else {
                        "https://polnes-news.b4its.tech/public/${news.gambar}"
                    },
                    contentDescription = news.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
            }
            // Bagian Author & Tanggal
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AuthorDateRow(
                    // � PERBAIKAN KRITIS: Menggunakan safe call (?.) pada news.author
                    // untuk mengakses 'name' dan elvis operator (?:) untuk fallback.
                    authorName = news.author?.name ?: "Unknown",
                    date = StoreData.formatDate(news.created_at)
                )
            }
            // Bagian Konten
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Menggunakan operator Elvis (?: "") untuk fallback jika contents null
                HtmlText(html = "${news.contents}" ?: "", modifier = Modifier.padding(horizontal = 16.dp))
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
            // Bagian Rating yang dikomentari
        }
    }
}