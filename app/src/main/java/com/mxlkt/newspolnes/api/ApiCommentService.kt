package com.mxlkt.newspolnes.api

import com.mxlkt.newspolnes.model.CommentListResponse
import com.mxlkt.newspolnes.model.CommentRequest
import com.mxlkt.newspolnes.model.SingleCommentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiCommentService {

    /**
     * POST /api/comment/store/{newsId}
     * Menambahkan rating/komentar baru ke suatu berita.
     * Membutuhkan: X-Api-Key (biasanya ditambahkan via Interceptor Retrofit), userId, dan rating (di Body).
     * @param newsId ID Berita yang akan diberi rating.
     * @param request Body request yang berisi userId dan rating.
     */
    @POST("comment/store/{newsId}")
    suspend fun storeComment(
        @Path("newsId") newsId: Int,
        @Body request: CommentRequest
    ): Response<SingleCommentResponse>

    @PATCH("comment/update/{newsId}")
    suspend fun updateComment(
        @Path("newsId") newsId: Int,
        @Body request: CommentRequest
    ): Response<SingleCommentResponse>

    /**
     * GET /api/comment/get/{newsId}
     * Menampilkan semua rating/komentar untuk suatu berita, termasuk data user.
     * Membutuhkan: X-Api-Key.
     * @param newsId ID Berita yang ingin dilihat komentarnya.
     */
    @GET("comment/get/{newsId}")
    suspend fun getComments(
        @Path("newsId") newsId: Int
    ): Response<CommentListResponse>
}