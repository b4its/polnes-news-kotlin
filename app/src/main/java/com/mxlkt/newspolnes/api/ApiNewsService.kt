package com.mxlkt.newspolnes.api

import com.mxlkt.newspolnes.model.* // Import semua model yang dibutuhkan
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiNewsService {
    // 1. GET (Index - Daftar Berita) - Akses Publik
    // Route: GET /api/news -> index
    @GET("news")
    suspend fun getNewsList(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    @GET("news/get/most_view/long")
    suspend fun getMostViewedList(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>


    @GET("news/get/most_rated/long")
    suspend fun getMostRatedList(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    // 2. GET (Show - Detail Berita) - Akses Publik
    // Route: GET /api/news/{id} -> show
    @GET("news/get/recent_news/first")
    suspend fun getRecentViewFirst(): Response<SingleNewsResponse>

    @GET("news/get/most_view/first")
    suspend fun getMostViewedFirst(): Response<SingleNewsResponse>

    @GET("news/get/most_rated/first")
    suspend fun getMostRatedFirst(): Response<SingleNewsResponse>

    // 2. GET (Show - Detail Berita) - Akses Publik
    // Route: GET /api/news/{id} -> show
    @GET("news/{id}")
    suspend fun getNewsDetail(
        @Path("id") newsId: Int
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<SingleNewsResponse>


    @GET("news/category/get/{id}")
    suspend fun getNewsInCategory(
        @Path("id") categoryId: Int
    ): Response<CategoryResponse>

    // --- Operasi Tulis (Memerlukan API Key/Otentikasi) ---

    // 3. POST (Store - Tambah Berita) - Mengirim data JSON saja (tanpa file gambar)
    // Route: POST /api/news/news/ -> store
   // Catatan: Route Laravel memiliki '/news/' tambahan. Saya sesuaikan di sini.
    @POST("news/post")
    suspend fun createNews(
        // API Key diasumsikan ada di Interceptor
        @Body newsData: NewsCreateRequest
    ): Response<SingleNewsResponse>

    // 4. POST (Update - Update Berita dengan Gambar/Multipart)
    // Route: POST /api/news/news/{id} -> update
    @POST("news/add/views/{id}")
    suspend fun addViewNews(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int,
    ): Response<SingleNewsResponse>


    // Route: POST /api/news/news/{id} -> update
    @Multipart
    @POST("news/post/{id}")
    suspend fun updateNews(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int,
        // Semua data non-file harus dikirim sebagai RequestBody (untuk Multipart)
        @Part("title") title: RequestBody,
        @Part("contents") content: RequestBody,
        @Part("authorId") authorId: RequestBody,
        @Part("categoryId") categoryId: RequestBody?, // Tambahan
        @Part("linkYoutube") linkYoutube: RequestBody?, // Tambahan
        @Part("status") status: RequestBody?, // Tambahan
        @Part image: MultipartBody.Part? // File Gambar
    ): Response<SingleNewsResponse>

    // 5. DELETE (Destroy - Hapus Berita)
    // Route: DELETE /api/news/news/{id} -> destroy
    @DELETE("news/delete/{id}")
    suspend fun deleteNews(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int
    ): Response<BasicResponse> // Menggunakan BasicResponse untuk respons sederhana
}