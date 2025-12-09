package com.mxlkt.newspolnes.api

import com.mxlkt.newspolnes.model.BasicResponse
import com.mxlkt.newspolnes.model.BasicResponses
import com.mxlkt.newspolnes.model.CategoryRequest
import com.mxlkt.newspolnes.model.CategoryResponse
import com.mxlkt.newspolnes.model.NewsByCategoryResponse
import com.mxlkt.newspolnes.model.SingleCategoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiCategoryService {

    // --- READ ALL ---
    // HAPUS @Header("X-Api-Key") apiKey: String

    @GET("category/get")
    suspend fun getAllCategories(): Response<CategoryResponse>

    @GET("news/category/get/{id}")
    suspend fun getNewsInCategory(
        @Path("id") categoryId: Int,
        @Query("page") page: Int? = 1 // Opsional: untuk handle pagination halaman 2, 3, dst.
    ): Response<NewsByCategoryResponse>

    // --- CREATE ---
    // HAPUS @Header("X-Api-Key") apiKey: String
    @Multipart
    @POST("category/store")
    suspend fun createCategory(
        // Untuk data teks (nama kategori), gunakan RequestBody
        @Part("name") name: RequestBody,

        // Untuk file fisik (gambar), gunakan MultipartBody.Part
        @Part gambar: MultipartBody.Part
    ): Response<SingleCategoryResponse>

    // --- UPDATE ---
    // HAPUS @Header("X-Api-Key") apiKey: String
    @Multipart
    @POST("category/{id}/update") // Gunakan POST agar file terbaca
    suspend fun updateCategory(
        @Path("id") categoryId: Int,

        // Trik Method Spoofing agar Laravel menganggap ini PUT
        @Part("_method") method: RequestBody,

        // Data teks (nama)
        @Part("name") name: RequestBody,

        // File gambar (Nullable, karena user mungkin tidak ganti gambar)
        @Part gambar: MultipartBody.Part?
    ): Response<SingleCategoryResponse>

    // DELETE Category
    // Pastikan URL-nya cocok dengan route Laravel (misal: "category/{id}" atau "category/delete/{id}")
    @DELETE("category/delete/{id}")
    suspend fun deleteCategory(
        @Path("id") categoryId: Int
    ): Response<BasicResponses>

}