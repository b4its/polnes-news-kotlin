package com.mxlkt.newspolnes.repository

import android.util.Log
import com.mxlkt.newspolnes.api.ApiCategoryService
import com.mxlkt.newspolnes.api.ApiClient // Asumsi ApiClient memiliki instance ApiCategoryService
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.CategoryRequest
import com.mxlkt.newspolnes.model.CategoryResponse
import com.mxlkt.newspolnes.model.SingleCategoryResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

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
    suspend fun createCategory(name: String, imageFile: File): Category {

        // 1. Persiapkan 'name' (String -> RequestBody)
        // Gunakan "text/plain" karena ini hanya teks biasa
        val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())

        // 2. Persiapkan 'gambar' (File -> MultipartBody.Part)
        // Tentukan tipe konten file (misal image/jpeg atau image/*)
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())

        // "gambar" adalah key yang harus sama persis dengan validasi di Controller Laravel
        val gambarPart = MultipartBody.Part.createFormData("gambar", imageFile.name, requestFile)

        // 3. Panggil API Service (yang sudah menggunakan @Multipart)
        val response: retrofit2.Response<SingleCategoryResponse> =
            apiCategoryService.createCategory(nameRequestBody, gambarPart)

        // 4. Cek Response (Sama seperti logika Anda sebelumnya)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.data != null) {
                return body.data
            }
        }

        // 5. Error Handling
        val errorBody = response.errorBody()?.string()
        throw Exception("Gagal membuat kategori. Code: ${response.code()}. Error: $errorBody")
    }

    suspend fun updateCategory(categoryId: Int, name: String, imageFile: File?): Category {

        // Log untuk memastikan fungsi terpanggil
        Log.d("CategoryRepo", "Mengupdate kategori ID: $categoryId, Nama: $name, File: ${imageFile?.name ?: "Tidak ada"}")

        // A. Siapkan _method="PUT" (Spoofing)
        val methodBody = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())

        // B. Siapkan Name
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())

        // C. Siapkan Gambar (Opsional)
        var gambarPart: MultipartBody.Part? = null

        if (imageFile != null) {
            // Cek apakah file benar-benar ada secara fisik
            if (imageFile.exists()) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                gambarPart = MultipartBody.Part.createFormData("gambar", imageFile.name, requestFile)
            } else {
                Log.e("CategoryRepo", "File gambar tidak ditemukan di path: ${imageFile.absolutePath}")
            }
        }

        // D. Panggil API
        // Perbaikan: Pastikan nama parameter argumen cocok dengan definisi di ApiService
        // Jika di interface: fun updateCategory(@Path("id") id: Int, ...) -> gunakan id = categoryId
        // Jika di interface: fun updateCategory(@Path("id") categoryId: Int, ...) -> gunakan categoryId = categoryId
        val response = apiCategoryService.updateCategory(
            categoryId = categoryId, // Sesuaikan ini dengan nama parameter di interface Anda
            method = methodBody,
            name = nameBody,
            gambar = gambarPart
        )

        // E. Cek Response Sukses
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.data != null) {
                Log.d("CategoryRepo", "Update sukses: ${body.data.name}")
                return body.data
            }
        }

        // F. Handle Error dengan Parsing JSON Laravel
        // Laravel biasanya mengirim error format: { "message": "...", "errors": {...} }
        val errorString = response.errorBody()?.string()
        var cleanErrorMessage = "Terjadi kesalahan (Code: ${response.code()})"

        if (!errorString.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(errorString)
                // Ambil pesan utama dari Laravel
                val message = jsonObject.optString("message", "Gagal memperbarui data")

                // Opsional: Jika ada detail validasi (misal: "The name has already been taken")
                val errors = jsonObject.optJSONObject("errors")
                val detailError = if (errors != null) {
                    // Ambil error pertama dari field 'name' atau 'gambar'
                    val nameErr = errors.optJSONArray("name")?.optString(0)
                    val imgErr = errors.optJSONArray("gambar")?.optString(0)
                    nameErr ?: imgErr ?: ""
                } else ""

                cleanErrorMessage = if (detailError.isNotEmpty()) "$message: $detailError" else message

            } catch (e: Exception) {
                Log.e("CategoryRepo", "Gagal parsing error JSON: ${e.message}")
                cleanErrorMessage += " - Raw: $errorString"
            }
        }

        Log.e("CategoryRepo", "Update gagal: $cleanErrorMessage")
        throw Exception(cleanErrorMessage)
    }


    suspend fun deleteCategory(id: Int): String {
        // Panggil API
        val response = apiCategoryService.deleteCategory(id)

        // Cek Sukses
        if (response.isSuccessful && response.body() != null) {
            // Kembalikan pesan dari server (misal: "Kategori berhasil dihapus")
            return response.body()!!.message
        }

        // Handle Error
        val errorBody = response.errorBody()?.string()
        throw Exception("Gagal menghapus kategori. Code: ${response.code()}. Error: $errorBody")
    }



}