package com.mxlkt.newspolnes.model
import com.google.gson.annotations.SerializedName

data class News(
    val id: Int,
    val title: String,
    val categoryId: Int,
    val imageRes: Int,
    val content: String,
    val authorId: Int?,
    val date: String,
    val views: Int,
    val youtubeVideoId: String?,
    val status: NewsStatus = NewsStatus.DRAFT
)

data class NewsModel(
    val id: Int,
    val title: String,
    val categoryId: Int,
    val gambar: String?, // URL atau path gambar (bisa null)
    val contents: String,
    val authorId: Int,
    val views: Int,
    val thumbnail: String?, // Bisa null
    val linkYoutube: String?, // Bisa null
    val status: String,
    val created_at: String,
    val updated_at: String,
    // Relasi yang disertakan (dari with(['author:id,name', 'category:id,name']))
    val author: AuthorModel?,
    val category: CategoryModel?
)

data class AuthorModel(
    val id: Int,
    val name: String
)

data class CategoryModel(
    val id: Int,
    val name: String
)


// --- Model Permintaan/Request ---
// Digunakan untuk POST /news (createNews) tanpa file
data class NewsCreateRequest(
    val title: String,
    @SerializedName("categoryId")
    val categoryId: Int?, // Boleh nullable
    val content: String,
    @SerializedName("authorId")
    val authorId: Int,
    val linkYoutube: String?,
    val status: String? // Dibuat nullable karena server bisa memiliki nilai default 'draft'
)