package com.mxlkt.newspolnes.data

import com.mxlkt.newspolnes.api.ApiClient
import com.mxlkt.newspolnes.api.ApiNewsService
import com.mxlkt.newspolnes.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class NewsRepository(
    // Menggunakan instance authenticated untuk POST/DELETE dan public untuk GET
    private val apiNewsServicePublic: ApiNewsService = ApiClient.apiNewsServicePublic,
    private val apiNewsServices: ApiNewsService = ApiClient.apiNewsService
) {

    // 1. GET Daftar Berita (Publik)
    suspend fun getNewsList(page: Int = 1): NewsListResponse {
        val response = apiNewsServicePublic.getNewsList(page)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        // Melempar exception jika respons gagal
        throw Exception("Gagal memuat daftar berita: ${response.code()}")
    }

    suspend fun getRecentViewFirst(): SingleNewsResponse {
        val response = apiNewsServicePublic.getRecentViewFirst()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal memuat berita terbaru pertama: ${response.code()}")
    }

    /**
     * Mengambil Berita Paling Banyak Dilihat (Most Viewed) pertama (tunggal).
     */
    suspend fun getMostViewedFirst(): SingleNewsResponse {
        val response = apiNewsServicePublic.getMostViewedFirst()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal memuat berita paling banyak dilihat pertama: ${response.code()}")
    }

    /**
     * Mengambil Berita Paling Banyak Dirating (Most Rated) pertama (tunggal).
     */
    suspend fun getMostRatedFirst(): SingleNewsResponse {
        val response = apiNewsServicePublic.getMostRatedFirst()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal memuat berita paling banyak dirating pertama: ${response.code()}")
    }

    // 1. GET Daftar Berita (Publik)
    suspend fun getMostViewedList(page: Int = 1): NewsListResponse {
        val response = apiNewsServicePublic.getMostViewedList(page)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        // Melempar exception jika respons gagal
        throw Exception("Gagal memuat daftar berita: ${response.code()}")
    }

    // 1. GET Daftar Berita (Publik)
    suspend fun getMostRatedList(page: Int = 1): NewsListResponse {
        val response = apiNewsServicePublic.getMostRatedList(page)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        // Melempar exception jika respons gagal
        throw Exception("Gagal memuat daftar berita: ${response.code()}")
    }

    // 2. GET Detail Berita (Publik)
    suspend fun getNewsDetail(newsId: Int): SingleNewsResponse {
        val response = apiNewsServicePublic.getNewsDetail(newsId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal memuat detail berita: ${response.code()}")
    }

    // 3. POST Buat Berita Baru (Terotentikasi) - Tanpa Gambar
    suspend fun createNews(request: NewsCreateRequest): SingleNewsResponse {
        val response = apiNewsServices.createNews(request)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal membuat berita: ${response.code()}")
    }

    suspend fun addViews(newsId: Int): SingleNewsResponse {
        val response = apiNewsServices.addViewNews(newsId = newsId)

        if (response.isSuccessful && response.body() != null) {
            // Berdasarkan respons API Laravel, kita mengembalikan data berita yang sudah diupdate.
            // Respons sukses biasanya mengandung 'data' berita lengkap dengan 'newViews'.
            return response.body()!!
        }

        // Tangani error, misalnya 404 Not Found (seperti yang ditangkap di sisi Laravel)
        val errorBody = response.errorBody()?.string()
        throw Exception("Gagal menambahkan views untuk berita ID $newsId. Code: ${response.code()}. Error: $errorBody")
    }

    // 4. POST Update Berita (Terotentikasi) - Dengan/Tanpa Gambar
    suspend fun updateNews(
        newsId: Int,
        title: String,
        contents: String,
        authorId: Int,
        categoryId: Int?,
        linkYoutube: String?,
        status: String?,
        imageFile: File? // File gambar, bisa null
    ): SingleNewsResponse {
        // Konversi data ke RequestBody
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = contents.toRequestBody("text/plain".toMediaTypeOrNull())
        val authorIdPart = authorId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        // Data yang nullable
        val categoryIdPart = categoryId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val linkYoutubePart = linkYoutube?.toRequestBody("text/plain".toMediaTypeOrNull())
        val statusPart = status?.toRequestBody("text/plain".toMediaTypeOrNull())

        // Bagian Multipart untuk gambar (jika ada)
        val imagePart = imageFile?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            // Nama field: "gambar" sesuai API yang umum (asumsi)
            // Namun, di ApiNewsService, nama parameternya adalah `image: MultipartBody.Part?`
            // dan Retrofit akan menggunakan nama parameter (kecuali ditimpa).
            // Saya asumsikan nama field di API adalah "gambar" atau "image"
            // Mari kita gunakan "gambar" (sesuai NewsModel)
            MultipartBody.Part.createFormData("gambar", file.name, requestFile)
        }

        val response = apiNewsServices.updateNews(
            newsId = newsId,
            title = titlePart,
            content = contentPart,
            authorId = authorIdPart,
            categoryId = categoryIdPart,
            linkYoutube = linkYoutubePart,
            status = statusPart,
            image = imagePart
        )

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal update berita: ${response.code()}")
    }

    // 5. DELETE Hapus Berita (Terotentikasi)
    suspend fun deleteNews(newsId: Int): BasicResponse {
        val response = apiNewsServices.deleteNews(newsId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception("Gagal menghapus berita: ${response.code()}")
    }
}