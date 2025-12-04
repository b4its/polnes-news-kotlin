package com.mxlkt.newspolnes.repository

import com.mxlkt.newspolnes.api.ApiClient
import com.mxlkt.newspolnes.model.CommentListResponse
import com.mxlkt.newspolnes.model.CommentRequest
import com.mxlkt.newspolnes.model.SingleCommentResponse

class CommentRepository {
    // Mengambil instance service dari ApiClient
    private val apiCommentService = ApiClient.apiCommentService

    /**
     * Mengambil daftar komentar berdasarkan newsId.
     * Menggunakan Result<T> untuk menangani Success atau Failure.
     */
    suspend fun getComments(newsId: Int): Result<CommentListResponse> {
        return try {
            val response = apiCommentService.getComments(newsId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                // Menangani error dari server (misal 404, 500)
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            // Menangani error jaringan atau parsing
            Result.failure(e)
        }
    }

    /**
     * Mengirim komentar/rating baru.
     * @param newsId ID Berita
     * @param userId ID User yang berkomentar
     * @param rating Nilai rating
     */
    suspend fun storeComment(newsId: Int, userId: Int, rating: Int): Result<SingleCommentResponse> {
        return try {
            // Membungkus data ke dalam CommentRequest sesuai definisi model
            val request = CommentRequest(
                    userId = userId,
                    rating = rating
            )

            val response = apiCommentService.storeComment(newsId, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Failed to post comment: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateComment(newsId: Int, userId: Int, rating: Int): Result<SingleCommentResponse> {
        return try {
            val request = CommentRequest(
                userId = userId,
                rating = rating
            )

            // Panggil endpoint PATCH
            val response = apiCommentService.updateComment(newsId, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                // Handle error (misal 404 jika user belum pernah komen)
                Result.failure(Exception("Failed to update: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}