package com.mxlkt.newspolnes.api
//
//import com.mxlkt.newspolnes.model.LoginRequest
//import com.mxlkt.newspolnes.model.LoginResponse
//import com.mxlkt.newspolnes.model.RegisterRequest
//import com.mxlkt.newspolnes.model.RegisterResponse
//import com.mxlkt.newspolnes.model.UpdateRoleToEditorResponse
//import com.mxlkt.newspolnes.model.UsersResponse
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Path
//
//interface ApiCategoryService {
//    @POST("category/get")
//    suspend fun login(@Body request: CategoryRequest): Response<CategoryResponse>
////
////    @POST("register")
////    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
////
////    @GET("users/get")
////    suspend fun getAllUsers(): Response<UsersResponse>
////
////    @GET("users/{id}/role_editor") // Path disesuaikan dengan route Laravel dan menggunakan placeholder {id}
////    suspend fun updateRoleToEditor(
////        @Path("id") userId: Int // Mengambil ID dari path URL
////    ): Response<UpdateRoleToEditorResponse>
//}