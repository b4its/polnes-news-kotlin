package com.mxlkt.newspolnes.repository

import com.mxlkt.newspolnes.api.ApiCategoryService
import com.mxlkt.newspolnes.api.ApiClient // Asumsi ApiClient memiliki instance ApiCategoryService
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.CategoryRequest
import com.mxlkt.newspolnes.model.CategoryResponse
import com.mxlkt.newspolnes.model.SingleCategoryResponse

/**
 * Repository untuk mengelola operasi data kategori,
 * bertindak sebagai perantara antara ViewModel dan ApiCategoryService.
 * Mengikuti pola: Melempar Exception jika respons tidak 2xx.
 */
class CategoryRepository(
    // Asumsi ApiClient memiliki instance ApiCategoryService yang sudah diinisialisasi
    private val apiCategoryService: ApiCategoryService = ApiClient.apiCategoryService
) {

    // 1. READ ALL - Mendapatkan semua kategori
    // Mengembalikan List<Category> atau melempar Exception
    suspend fun getAllCategories(): List<Category> {
        val response: retrofit2.Response<CategoryResponse> = apiCategoryService.getAllCategories()

        // Cek sukses dan data tidak null
        if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
            // Asumsi: data adalah List<Category>
            return response.body()!!.data!!
        }

        // Melempar exception jika respons gagal atau data null
        throw Exception("Gagal memuat daftar kategori: ${response.code()}")
    }

    // 2. CREATE - Membuat kategori baru
    // Mengembalikan Category (data tunggal yang baru dibuat) atau melempar Exception
    suspend fun createCategory(request: CategoryRequest): Category {
        val response: retrofit2.Response<SingleCategoryResponse> = apiCategoryService.createCategory(request)

        if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
            return response.body()!!.data!!
        }

        // Ambil pesan error jika ada di body yang gagal
        val errorBody = response.errorBody()?.string()
        throw Exception("Gagal membuat kategori. Code: ${response.code()}. Error: $errorBody")
    }

    // 3. UPDATE - Memperbarui kategori yang sudah ada
    // Mengembalikan Category (data tunggal yang diperbarui) atau melempar Exception
    suspend fun updateCategory(categoryId: Int, request: CategoryRequest): Category {
        val response: retrofit2.Response<SingleCategoryResponse> =
            apiCategoryService.updateCategory(categoryId, request)

        if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
            return response.body()!!.data!!
        }

        val errorBody = response.errorBody()?.string()
        throw Exception("Gagal memperbarui kategori. Code: ${response.code()}. Error: $errorBody")
    }
}