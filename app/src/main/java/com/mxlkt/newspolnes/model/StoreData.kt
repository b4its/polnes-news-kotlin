package com.mxlkt.newspolnes.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.mxlkt.newspolnes.R // Pastikan package R sesuai dengan aplikasi Anda

object StoreData {

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(dateString: String?): String {
        // Cek null safety agar aplikasi tidak crash jika tanggal kosong
        if (dateString.isNullOrEmpty()) return "Unknown Date"

        return try {
            // --- SKENARIO 1: Format API Laravel / ISO 8601 ---
            // Format: "2025-11-30T09:47:29.000000Z"
            // Menggunakan ZonedDateTime karena ada zona waktu (Z)
            val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
            val dateApi = ZonedDateTime.parse(dateString, isoFormatter)

            // Format Output: "Sun, 30 Nov 2025"
            val outputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH)
            dateApi.format(outputFormatter)

        } catch (e: Exception) {
            // --- SKENARIO 2: Format Dummy Data Lokal ---
            // Format: "2025-11-09" (yyyy-MM-dd)
            // Menggunakan LocalDate karena tidak ada jam/zona
            try {
                val localFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                val dateLocal = LocalDate.parse(dateString, localFormatter)

                val outputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH)
                dateLocal.format(outputFormatter)
            } catch (e2: Exception) {
                // Jika masih error (format tidak dikenali), kembalikan string aslinya
                // e2.printStackTrace() // Aktifkan jika ingin melihat log error di Logcat
                dateString
            }
        }
    }

    // --- USER DATA ---

    @Suppress("EXPERIMENTAL_MUTABILITY_OF_IMMUTABLE_TYPE")
    internal var _userList = mutableListOf(
        User(id = 1, name = "Ade Darmawan", email = "ade@polnesnews.com", password = "password123", role = UserRole.EDITOR),
        User(id = 7, name = "Admin A", email = "a", password = "a", role = UserRole.ADMIN)
    )

    val userList: List<User>
        get() = _userList.toList()

    fun setUsers(users: List<User>) {
        _userList.clear()
        _userList.addAll(users)
    }

    // --- CATEGORY DATA ---

    val categoryList = listOf(
        Category(1, "Teknologi", "ini gambar"),
        Category(2, "Ekonomi", "ini gambar")
    )

    // --- NEWS DATA ---

    @Suppress("EXPERIMENTAL_MUTABILITY_OF_IMMUTABLE_TYPE")
    var newsList = mutableListOf(
        News(
            id = 1,
            title = "Inovasi Teknologi Baru di Indonesia",
            categoryId = 1,
            imageRes = R.drawable.sample_news1,
            content = "Konten berita ini <b>hanya contoh</b> untuk tampilan awal.",
            authorId = 1,
            date = "2025-11-09", // Ini format lokal (Skenario 2)
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

    val commentList = listOf(
        Comment(id = 1, newsId = 1, userId = 3, rating = 5, date = "2025-11-10"),
        Comment(id = 2, newsId = 1, userId = 2, rating = 3, date = "2025-11-11"),
        Comment(id = 3, newsId = 2, userId = 3, rating = 2, date = "2025-11-09")
    )

    // --- NOTIFICATION DATA ---

    @RequiresApi(Build.VERSION_CODES.O)
    val notifications = newsList.toList().map { news ->
        val categoryName = categoryList.find { it.id == news.categoryId }?.name ?: "Unknown"
        Notification(
            id = news.id,
            iconRes = R.drawable.ic_news,
            category = categoryName,
            title = news.title,
            date = formatDate(news.date) // Menggunakan fungsi format baru
        )
    }
}