package com.mxlkt.newspolnes.model

import com.google.gson.annotations.SerializedName

/**
 * Model data yang merefleksikan objek Category yang dikembalikan oleh API
 * 'gambar' (String) adalah URL dari gambar.
 */
data class Category(
    val id: Int,
    val name: String,
    // Perhatikan nama kolom di Laravel adalah 'gambar'
    val gambar: String?
)

/**
 * Model data untuk permintaan POST/PUT (CREATE/UPDATE)
 */
data class CategoryRequest(
    val name: String,
    val gambar: String // URL gambar yang dikirim ke API
)

/**
 * Struktur Respons JSON dari endpoint showAllCategory
 */
data class CategoryResponse(
    val status: String,
    val message: String,
    val count: Int?, // Boleh null jika ada error
    @SerializedName("data") // Mengambil list CategoryDto dari field 'data'
    val data: List<Category>?
)

/**
 * Struktur Respons JSON dari endpoint store/update
 */
data class SingleCategoryResponse(
    val status: String,
    val message: String,
    @SerializedName("data")
    val data: Category?
)