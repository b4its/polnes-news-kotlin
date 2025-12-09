package com.mxlkt.newspolnes.model

import com.google.gson.annotations.SerializedName

/**
 * Model data utama Category.
 */
data class Category(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    // Menggunakan @SerializedName memastikan key JSON tetap 'gambar'
    @SerializedName("gambar")
    val gambar: String?
)

/**
 * Request Model untuk CREATE/UPDATE.
 * Catatan: Jika 'gambar' di sini hanyalah string URL, ini sudah benar.
 * Jika Anda berniat mengupload FILE gambar asli, Anda harus menggunakan Multipart (lihat catatan di bawah).
 */
data class CategoryRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("gambar")
    val gambar: String // Kirim string URL (bukan file binary)
)

/**
 * Respons untuk List Category (Show All)
 */
data class CategoryResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("count")
    val count: Int? = 0, // Default 0 jika null

    @SerializedName("data")
    val data: List<Category>? = emptyList() // Default empty list agar aman
)

/**
 * Respons untuk Single Category (Store/Update/Show One)
 */
data class SingleCategoryResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Category?
)

data class BasicResponses(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String
)



data class NewsPagination(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("last_page")
    val lastPage: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("data")
    val newsList: List<NewsModel> // Data beritanya ada di sini
)

/**
 * Response KHUSUS untuk endpoint 'newsInCategory'
 */
data class NewsByCategoryResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("category_name")
    val categoryName: String, // Menangkap nama kategori dari PHP

    @SerializedName("count")
    val count: Int,

    @SerializedName("data")
    val data: NewsPagination // Objek paginasi, bukan List<Category>
)