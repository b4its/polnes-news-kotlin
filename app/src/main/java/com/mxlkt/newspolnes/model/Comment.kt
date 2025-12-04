package com.mxlkt.newspolnes.model

import com.google.gson.annotations.SerializedName

// Model untuk data user yang disertakan dalam respons Comment (relasi user:id,name)
data class CommentUser(
    val id: Int,
    val name: String
)

// Model utama untuk menyimpan data rating/komentar
data class Comment(
    val id: Int,
    @SerializedName("newsId") val newsId: Int, // Menggunakan SerializedName karena di Laravel menggunakan newsId/userId
    @SerializedName("userId") val userId: Int,
    val rating: Int,
    @SerializedName("created_at") val date: String, // Mapping Laravel's 'created_at' ke 'date' di Kotlin
    val user: CommentUser? = null // Data user, optional (hanya ada di endpoint GET)
)

// Request Body untuk POST /api/comment/store/{newsId}
data class CommentRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("rating") val rating: Int
)

// Response Meta data untuk GET /api/comment/get/{newsId}
data class CommentMeta(
    @SerializedName("total_ratings") val totalRatings: Int,
    @SerializedName("average_rating") val averageRating: Double // Menggunakan Double untuk rating rata-rata
)

// Response umum untuk operasi CREATE (storeComment)
data class SingleCommentResponse(
    val status: String,
    val message: String,
    val data: Comment
)

// Response untuk GET /api/comment/get/{newsId} (List komentar)
data class CommentListResponse(
    val status: String,
    val message: String,
    val meta: CommentMeta,
    val data: List<Comment>
)