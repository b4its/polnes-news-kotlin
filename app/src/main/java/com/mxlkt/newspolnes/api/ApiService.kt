package com.mxlkt.newspolnes.api

import com.mxlkt.newspolnes.model.*
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Interface untuk endpoint API yang sudah ada (Users dan Auth)
interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("users/get")
    suspend fun getAllUsers(): Response<UsersResponse>

    @GET("users/{id}/role_editor")
    suspend fun updateRoleToEditor(
        @Path("id") userId: Int
    ): Response<UpdateRoleToEditorResponse>
}


// Objek Singleton untuk Retrofit dan konfigurasi
object ApiClient {
    private const val BASE_URL = "https://polnes-news.b4its.tech/api/"
    private const val API_KEY = "gueKece11" // Kunci API

    // Penyimpanan Token Otentikasi (Bearer Token)
    // **Harus di-set setelah login berhasil**
    @Volatile
    var authToken: String? = null

    // 1. Logging Interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. Interceptor untuk menambahkan Header X-Api-Key
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("X-Api-Key", API_KEY)
            .build()
        // println("Debug: Sending request with X-Api-Key: $API_KEY") // Log di konsol
        chain.proceed(newRequest)
    }

    // 3. Interceptor untuk menambahkan Header Authorization (Bearer Token)
    // Digunakan untuk endpoint yang membutuhkan otentikasi (misalnya: POST, DELETE news)
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // Tambahkan header Authorization HANYA jika authToken tersedia
        authToken?.let { token ->
            builder.header("Authorization", "Bearer $token")
            // println("Debug: Adding Bearer Token: $token") // Log di konsol
        }

        chain.proceed(builder.build())
    }

    // --- OKHTTP CLIENTS ---

    // OkHttpClient untuk endpoint yang hanya memerlukan API Key (misalnya: Login, Register, GET Users)
    private val okHttpClientPublic: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // OkHttpClient untuk endpoint yang memerlukan API Key dan Auth Token (misalnya: POST News, DELETE News)
    private val okHttpClientAuthenticated: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(authInterceptor) // Interceptor Token Otentikasi
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // --- RETROFIT INSTANCE ---

    // Retrofit Instance untuk permintaan publik/otentikasi dasar
    private val retrofitPublic: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientPublic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit Instance untuk permintaan yang memerlukan Token Otentikasi (gunakan client authenticated)
    private val retrofitAuthenticated: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientAuthenticated)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instance ApiService (untuk Login, Register, GET Users)
    val apiService: ApiService by lazy {
        retrofitPublic.create(ApiService::class.java)
    }

    // Instance ApiNewsService (untuk GET News yang publik)
    // Karena GET News bersifat publik, kita bisa gunakan retrofitPublic
    val apiNewsServicePublic: ApiNewsService by lazy {
        retrofitPublic.create(ApiNewsService::class.java)
    }

    // Instance ApiNewsService (untuk POST/DELETE News yang terotentikasi)
    // Untuk POST, PUT, DELETE, sebaiknya gunakan instance ini agar token terkirim
    val apiNewsService: ApiNewsService by lazy {
        retrofitAuthenticated.create(ApiNewsService::class.java)
    }

    // Instance ApiCategoryService yang sudah ada
    // Asumsi: Jika ApiCategoryService tidak memerlukan Token Otentikasi, gunakan retrofitPublic
    val apiCategoryService: ApiCategoryService by lazy {
        retrofitPublic.create(ApiCategoryService::class.java)
    }
}