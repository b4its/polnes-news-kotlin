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

    @GET("news/get/published")
    suspend fun getNewsPublished(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    @GET("news/get/draft")
    suspend fun getNewsDrafted(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    @GET("news/get/review")
    suspend fun getNewsReview(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    @GET("news/get/most_view/long")
    suspend fun getMostViewedLongList(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    @GET("news/get/most_view/short")
    suspend fun getMostViewedShortList(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>


    @GET("news/get/most_rated/long")
    suspend fun getMostRatedLongList(
        @Query("page") page: Int = 1 // Untuk pagination
        // TIDAK PERLU @Header("X-Api-Key") karena akses publik
    ): Response<NewsListResponse>

    @GET("news/get/most_rated/short")
    suspend fun getMostRatedShortList(
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
    @Multipart // Wajib: Menandakan request ini mendukung upload file
    @POST("news/post")
    suspend fun createNews(
        // Bagian Teks (Harus RequestBody)
        @Part("title") title: RequestBody,
        @Part("contents") content: RequestBody,       // Sesuai PHP: 'contents'
        @Part("categoryId") categoryId: RequestBody,  // Sesuai PHP: 'categoryId'
        @Part("authorId") authorId: RequestBody,      // Sesuai PHP: 'authorId'
        @Part("linkYoutube") linkYoutube: RequestBody?, // Sesuai PHP: 'linkYoutube'
        @Part("status") status: RequestBody?,         // Sesuai PHP: 'status'

        // Bagian File Gambar (MultipartBody.Part)
        // Note: Nama "gambar" diatur saat membuat MultipartBody di Repository,
        // bukan di anotasi @Part ini untuk file.
        @Part gambar: MultipartBody.Part?,
        @Part thumbnail: MultipartBody.Part?
    ): Response<SingleNewsResponse>   // Catatan: Route Laravel memiliki '/news/' tambahan. Saya sesuaikan di sini.

    @Multipart // Wajib: Menandakan request ini mendukung upload file
    @POST("news/admin/post")
    suspend fun createNewsAdmin(
        // Bagian Teks (Harus RequestBody)
        @Part("title") title: RequestBody,
        @Part("contents") content: RequestBody,       // Sesuai PHP: 'contents'
        @Part("categoryId") categoryId: RequestBody,  // Sesuai PHP: 'categoryId'
        @Part("authorId") authorId: RequestBody,      // Sesuai PHP: 'authorId'
        @Part("linkYoutube") linkYoutube: RequestBody?, // Sesuai PHP: 'linkYoutube'
        @Part("status") status: RequestBody?,         // Sesuai PHP: 'status'

        // Bagian File Gambar (MultipartBody.Part)
        // Note: Nama "gambar" diatur saat membuat MultipartBody di Repository,
        // bukan di anotasi @Part ini untuk file.
        @Part gambar: MultipartBody.Part?,
        @Part thumbnail: MultipartBody.Part?
    ): Response<SingleNewsResponse>

    // 4. POST (Update - Update Berita dengan Gambar/Multipart)
    // Route: POST /api/news/news/{id} -> update
    @POST("news/add/views/{id}")
    suspend fun addViewNews(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int,
    ): Response<SingleNewsResponse>





    @POST("news/admin/update/status/published/{id}")
    suspend fun updatePublishStatus(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int,
    ): Response<SingleNewsResponse>


    @POST("news/admin/update/status/draft/{id}")
    suspend fun updateDraftStatus(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int,
    ): Response<SingleNewsResponse>


    @POST("news/admin/update/status/pending_review/{id}")
    suspend fun updateReviewStatus(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int,
    ): Response<SingleNewsResponse>


    // Route: POST /api/news/news/{id} -> update
    @Multipart
    @POST("news/post/{id}")
    suspend fun updateNews(
        @Path("id") newsId: Int,
        @Part("title") title: RequestBody,
        @Part("contents") content: RequestBody,
        @Part("authorId") authorId: RequestBody,
        @Part("categoryId") categoryId: RequestBody?,
        @Part("linkYoutube") linkYoutube: RequestBody?,
        @Part("status") status: RequestBody?,
        @Part image: MultipartBody.Part?, // File Gambar Utama
        @Part thumbnail: MultipartBody.Part? // <--- TAMBAHKAN INI
    ): Response<SingleNewsResponse>

    // 5. DELETE (Destroy - Hapus Berita)
    // Route: DELETE /api/news/news/{id} -> destroy
    @DELETE("news/delete/{id}")
    suspend fun deleteNews(
        // API Key diasumsikan ada di Interceptor
        @Path("id") newsId: Int
    ): Response<BasicResponse> // Menggunakan BasicResponse untuk respons sederhana

}