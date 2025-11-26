package com.mxlkt.newspolnes.domain

import com.mxlkt.newspolnes.model.CategoryDto

/**
 * Model Domain yang digunakan di UI/ViewModel (untuk gambar lokal)
 * Menggunakan konstruktor sekunder untuk konversi dari DTO.
 */
data class Category(
    val id: Int,
    val name: String,
    val imageUrl: String, // URL gambar dari API
    val imageRes: Int // Resource ID lokal (drawable/mipmap)
) {
    companion object {
        fun fromDto(dto: CategoryDto, resourceId: Int): Category {
            return Category(
                id = dto.id,
                name = dto.name,
                imageUrl = dto.gambar,
                imageRes = resourceId
            )
        }
    }
}