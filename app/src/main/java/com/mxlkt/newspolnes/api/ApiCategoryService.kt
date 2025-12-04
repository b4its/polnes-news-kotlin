package com.mxlkt.newspolnes.api

import com.mxlkt.newspolnes.model.CategoryRequest
import com.mxlkt.newspolnes.model.CategoryResponse
import com.mxlkt.newspolnes.model.SingleCategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiCategoryService {

    // --- READ ALL ---
    // HAPUS @Header("X-Api-Key") apiKey: String

    @GET("category/get")
    suspend fun getAllCategories(): Response<CategoryResponse>

    @GET("category/news/get/{id}")
    suspend fun getNewsInCategory(
        @Path("id") categoryId: Int
    ): Response<CategoryResponse>

    // --- CREATE ---
    // HAPUS @Header("X-Api-Key") apiKey: String
    @POST("category/store")
    suspend fun createCategory(
        @Body request: CategoryRequest
    ): Response<SingleCategoryResponse>

    // --- UPDATE ---
    // HAPUS @Header("X-Api-Key") apiKey: String
    @PUT("category/{id}/update")
    suspend fun updateCategory(
        @Path("id") categoryId: Int,
        @Body request: CategoryRequest
    ): Response<SingleCategoryResponse>


}