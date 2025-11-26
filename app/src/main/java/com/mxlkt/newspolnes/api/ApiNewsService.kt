package com.mxlkt.newspolnes.api
//
//interface ApiNewsService {
//
//    // 1. GET (Index - Daftar Berita)
//    @GET("news")
//    suspend fun getNewsList(
//        @Header("X-Api-Key") apiKey: String, // API Key diperlukan
//        @Query("page") page: Int = 1 // Untuk pagination
//    ): Response<NewsListResponse>
//
//    // 2. GET (Show - Detail Berita)
//    @GET("news/{id}")
//    suspend fun getNewsDetail(
//        @Header("X-Api-Key") apiKey: String, // API Key diperlukan
//        @Path("id") newsId: Int
//    ): Response<SingleNewsResponse>
//
//    // 3. POST (Store - Tambah Berita)
//    @POST("news")
//    suspend fun createNews(
//        @Header("X-Api-Key") apiKey: String,
//        @Body newsData: NewsCreateRequest // Mengirim data sebagai JSON (jika tanpa file)
//    ): Response<SingleNewsResponse>
//
//    // **4. POST (Update - Update Berita dengan Gambar/Multipart)**
//    // Digunakan POST untuk mendukung upload file
//    @Multipart
//    @POST("news/{id}")
//    suspend fun updateNews(
//        @Header("X-Api-Key") apiKey: String,
//        @Path("id") newsId: Int,
//        @Part("title") title: RequestBody,
//        @Part("content") content: RequestBody,
//        @Part("authorId") authorId: RequestBody,
//        @Part image: MultipartBody.Part? // Gambar sebagai Multipart
//        // Tambahkan part lain yang diperlukan (categoryId, linkYoutube, status)
//    ): Response<SingleNewsResponse>
//
//
//    // 5. DELETE (Destroy - Hapus Berita)
//    @DELETE("news/{id}")
//    suspend fun deleteNews(
//        @Header("X-Api-Key") apiKey: String,
//        @Path("id") newsId: Int
//    ): Response<BasicResponse> // Asumsi BasicResponse adalah data class {val status: String, val message: String}
//}
//
//// Data Class tambahan untuk Store/Update response
//data class SingleNewsResponse(
//    val status: String,
//    val message: String,
//    val data: NewsModel
//)
//
//// Data Class untuk request body jika tanpa file upload
//data class NewsCreateRequest(
//    val title: String,
//    val categoryId: Int,
//    val content: String,
//    val authorId: Int,
//    // ... lainnya
//)