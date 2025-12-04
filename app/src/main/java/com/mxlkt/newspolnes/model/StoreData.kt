package com.mxlkt.newspolnes.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.mxlkt.newspolnes.R // Asumsikan Anda memiliki file R.java untuk resource drawable
// Hapus semua import Compose dan ViewModel (Composable, viewModel, observeAsState, CategoryViewModel)

/**
 * Objek singleton untuk menyediakan data (bisa dummy atau dimuat)
 * untuk kebutuhan preview, testing, dan pengembangan awal.
 */

// Hapus fungsi DataService Composable yang salah
/*
@Composable
fun DataService(
    viewModel: CategoryViewModel = viewModel()
): List<Category> {
    val categoryList: List<Category> by viewModel.categoryList.observeAsState(initial = emptyList())
    viewModel.fetchAllCategories() // Ini adalah penyebab loop/bug jika di sini
    return categoryList
}
*/

object StoreData {

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(dateString: String): String {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
            // Menggunakan Locale Indonesia untuk format output
            val outputFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("in", "ID"))
            val date = LocalDate.parse(dateString, inputFormatter)
            return date.format(outputFormatter)
        } catch (e: Exception) {
            // Log error jika format gagal
            return dateString
        }
    }

    // --- USER DATA ---

    // Menggunakan var mutableListOf untuk memungkinkan perubahan, seperti pada NewsRepository
    @Suppress("EXPERIMENTAL_MUTABILITY_OF_IMMUTABLE_TYPE")
    internal var _userList = mutableListOf(
        // Catatan: Model User, UserRole, News, Comment, dan Notification harus ada di file lain (atau di sini jika belum)
        User(id = 1, name = "Ade Darmawan", email = "ade@polnesnews.com", password = "password123", role = UserRole.EDITOR),
        User(id = 7, name = "Admin A", email = "a", password = "a", role = UserRole.ADMIN)
    )

    // Getter publik untuk userList (mengembalikan List<User> yang aman, tidak dapat diubah)
    val userList: List<User>
        get() = _userList.toList()

    /**
     * Fungsi untuk memperbarui daftar pengguna dengan data yang dimuat dari API.
     */
    fun setUsers(users: List<User>) {
        _userList.clear()
        _userList.addAll(users)
    }

    // --- CATEGORY DATA ---

    // Daftar kategori dummy (tetap val listOf karena hanya untuk dummy)
    val categoryList = listOf(
        Category(1, "Teknologi", "ini gambar"),
        Category(2, "Ekonomi", "ini gambar")
    )

    // --- NEWS DATA ---

    // Menggunakan var mutableListOf untuk memungkinkan perubahan
    @Suppress("EXPERIMENTAL_MUTABILITY_OF_IMMUTABLE_TYPE")
    var newsList = mutableListOf(
        News(
            id = 1,
            title = "Inovasi Teknologi Baru di Indonesia",
            categoryId = 1,
            // Asumsi R.drawable.sample_news1 ada
            imageRes = R.drawable.sample_news1,
            content = "Konten berita ini <b>hanya contoh</b> untuk tampilan awal.",
            authorId = 1,
            date = "2025-11-09",
            views = 4,
            youtubeVideoId = "dQw4w9WgXcQ",
            status = NewsStatus.PUBLISHED
        ),
        News(
            id = 2,
            title = "Ekonomi Dunia Mulai Pulih Pasca Krisis",
            categoryId = 2,
            imageRes = R.drawable.sample_news2,
            content = "Isi berita contoh kedua.",
            authorId = 2,
            date = "2025-11-08",
            views = 12,
            youtubeVideoId = null,
            status = NewsStatus.PENDING_REVIEW
        )
    )

    // --- COMMENT DATA ---

    // Daftar komentar/rating dummy
    val commentList = listOf(
        // Asumsi model Comment ada
        Comment(id = 1, newsId = 1, userId = 3, rating = 5, date = "2025-11-10"),
        Comment(id = 2, newsId = 1, userId = 2, rating = 3, date = "2025-11-11"),
        Comment(id = 3, newsId = 2, userId = 3, rating = 2, date = "2025-11-09")
    )

    // --- NOTIFICATION DATA ---

    @RequiresApi(Build.VERSION_CODES.O)
    val notifications = newsList.toList().map { news ->
        val categoryName = categoryList.find { it.id == news.categoryId }?.name ?: "Unknown"
        // Asumsi model Notification ada
        Notification(
            id = news.id,
            iconRes = R.drawable.ic_news, // Asumsi R.drawable.ic_news ada
            category = categoryName,
            title = news.title,
            date = formatDate(news.date)
        )
    }
}

// Catatan: Anda perlu memastikan model data berikut didefinisikan di suatu tempat (kemungkinan di file ini juga)
// data class User(...)
// enum class UserRole
// data class News(...)
// enum class NewsStatus
// data class Comment(...)
// data class Notification(...)