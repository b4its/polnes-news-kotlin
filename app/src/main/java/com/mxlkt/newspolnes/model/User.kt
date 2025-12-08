package com.mxlkt.newspolnes.model

import com.google.gson.annotations.SerializedName

// DIPERBAIKI: Hapus properti 'password' (tidak boleh dikirim balik dari API)
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.USER,
    val password: String
)

enum class UserRole {
    ADMIN,
    EDITOR,
    USER
}

// Model untuk request body LOGIN
data class LoginRequest(
    val name: String,
    val password: String
)

// Model untuk request body REGISTER
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Model untuk response sukses LOGIN (mengandung objek User di properti 'data')
data class LoginResponse(
    @SerializedName("data")
    val data: User
)

// Model untuk response sukses REGISTER (mengandung objek User di properti 'user')
data class RegisterResponse(
    val status: String,
    val message: String,
    val user: User
)

data class UpdateRoleToEditorResponse(
    val status: String,
    val message: String,
    @SerializedName("data")
    val user: User
)

data class UsersResponse(
    @SerializedName("data")
    val data: List<User>,
    val message: String?
)

// Model baru untuk request body UPDATE (semua field nullable)
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: String? = null
)

// Model baru untuk response sukses UPDATE
data class UpdateUserResponse(
    val status: String,
    val message: String,
    @SerializedName("data")
    val user: UpdateUserData
)

// Model untuk data user di dalam response UPDATE (sesuai respons Laravel)
data class UpdateUserData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String, // String OK, karena ini hanya data sementara dari response
    @SerializedName("updated_at")
    val updatedAt: String
)

// Model untuk response error
data class ErrorResponse(
    @SerializedName("message")
    val message: String
)