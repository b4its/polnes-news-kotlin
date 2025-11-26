package com.mxlkt.newspolnes.api

import com.mxlkt.newspolnes.model.LoginRequest
import com.mxlkt.newspolnes.model.LoginResponse
import com.mxlkt.newspolnes.model.RegisterRequest
import com.mxlkt.newspolnes.model.RegisterResponse
import com.mxlkt.newspolnes.model.UpdateRoleToEditorResponse
import com.mxlkt.newspolnes.model.UsersResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// Interface untuk endpoint API
interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("users/get")
    suspend fun getAllUsers(): Response<UsersResponse>

    @GET("users/{id}/role_editor") // Path disesuaikan dengan route Laravel dan menggunakan placeholder {id}
    suspend fun updateRoleToEditor(
        @Path("id") userId: Int // Mengambil ID dari path URL
    ): Response<UpdateRoleToEditorResponse>
}

// Objek Singleton untuk Retrofit dan konfigurasi
object ApiClient {
    // Pastikan BASE_URL sudah sesuai dengan alamat server Laravel Anda
    private const val BASE_URL = "https://passanger.b4its.tech/api/"

    // Kunci API HARUS SAMA PERSIS dengan PRIVATE_API_KEY di Laravel .env
    private const val API_KEY = "gueKece11"

    // 1. Logging Interceptor untuk mencetak Request/Response di Logcat (Level BODY)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. Interceptor untuk menambahkan Header X-Api-Key
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Penting: Menggunakan "X-Api-Key" (Kapital) agar sesuai dengan controller Laravel
        val newRequest = originalRequest.newBuilder()
            .header("X-Api-Key", API_KEY)
            .build()

        // Log sederhana di konsol Java (bisa dilihat di Run, bukan Logcat)
        println("Debug: Sending request with X-Api-Key: $API_KEY")

        chain.proceed(newRequest)
    }

    // 3. DEFINISIKAN OKHTTPCLIENT DENGAN INTERCEPTOR
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Logging Interceptor harus ditambahkan agar kita bisa melihat Header yang dikirim
            .addInterceptor(loggingInterceptor)
            // Interceptor Kunci API
            .addInterceptor(apiKeyInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 4. GUNAKAN OKHTTPCLIENT DI RETROFIT
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instance ApiService yang akan digunakan di ViewModel
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    } // Instance ApiService yang akan digunakan di ViewModel

    // Instance ApiCategoryService yang baru
    val apiCategoryService: ApiCategoryService by lazy {
        retrofit.create(ApiCategoryService::class.java)
    }

}