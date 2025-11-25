package com.mxlkt.newspolnes.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.mxlkt.newspolnes.R // Asumsikan Anda memiliki file R.java untuk resource drawable

// --- ASUMSI MODEL DATA (Wajib ada di project Anda) ---
// enum class UserRole { USER, EDITOR, ADMIN }
// data class User(...)
// data class Category(...)
// data class News(...)
// data class Comment(...)
// data class Notification(...)
// enum class NewsStatus { DRAFT, PENDING_REVIEW, PUBLISHED, ARCHIVED }
// ----------------------------------------------------

/**
 * Objek singleton untuk menyediakan data (bisa dummy atau dimuat)
 * untuk kebutuhan preview, testing, dan pengembangan awal.
 */
object StoreData {

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(dateString: String): String {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
            val outputFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val date = LocalDate.parse(dateString, inputFormatter)
            return date.format(outputFormatter)
        } catch (e: Exception) {
            return dateString
        }
    }

    // PERUBAHAN KRITIS: userList diubah menjadi 'var' agar dapat diperbarui
    // Gunakan mutableList untuk memungkinkan perubahan.
    @Suppress("EXPERIMENTAL_MUTABILITY_OF_IMMUTABLE_TYPE")
    internal var _userList = mutableListOf(
        User(id = 1, name = "Ade Darmawan", email = "ade@polnesnews.com", password = "password123", role = UserRole.EDITOR),
        User(id = 7, name = "Admin A", email = "a", password = "a", role = UserRole.ADMIN)
    )

    // Getter publik untuk userList (mengembalikan List<User> yang aman)
    val userList: List<User>
        get() = _userList.toList()

    /**
     * Fungsi untuk memperbarui daftar pengguna dengan data yang dimuat dari API.
     */
    fun setUsers(users: List<User>) {
        // Hapus data lama dan tambahkan data baru dari API
        _userList.clear()
        _userList.addAll(users)
        // Opsional: Anda mungkin ingin memicu LiveData/StateFlow di sini jika StoreData digunakan di luar Compose.
    }



    // Daftar kategori dummy (tetap val listOf)
    val categoryList = listOf(
        Category(1, "Teknologi", R.drawable.category_tech),
        Category(2, "Ekonomi", R.drawable.category_economy)
    )

    // PERUBAHAN KRITIS: Mengubah dari 'val listOf' menjadi 'var mutableListOf'
    @Suppress("EXPERIMENTAL_MUTABILITY_OF_IMMUTABLE_TYPE")
    var newsList = mutableListOf(
        News(
            id = 1,
            title = "Inovasi Teknologi Baru di Indonesia",
            categoryId = 1,
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

    // Daftar komentar/rating dummy
    val commentList = listOf(
        Comment(id = 1, newsId = 1, userId = 3, rating = 5, date = "2025-11-10"),
        Comment(id = 2, newsId = 1, userId = 2, rating = 3, date = "2025-11-11"),
        Comment(id = 3, newsId = 2, userId = 3, rating = 2, date = "2025-11-09")
    )

    @RequiresApi(Build.VERSION_CODES.O)
    val notifications = newsList.toList().map { news ->
        val categoryName = categoryList.find { it.id == news.categoryId }?.name ?: "Unknown"
        Notification(
            id = news.id,
            iconRes = R.drawable.ic_news,
            category = categoryName,
            title = news.title,
            date = formatDate(news.date)
        )
    }
}