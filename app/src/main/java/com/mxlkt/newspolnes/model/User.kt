package com.mxlkt.newspolnes.model

import com.google.gson.annotations.SerializedName



data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.USER
)

enum class UserRole {
    ADMIN,
    EDITOR,
    USER
}

// Model untuk request body
data class LoginRequest(
    val name: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Model untuk response sukses
data class LoginResponse(
    @SerializedName("data")
    val data: User
)

data class UpdateRoleToEditorResponse(
    val status: String,
    val message: String,
    // Asumsi properti data berisi objek User yang telah diperbarui
    @SerializedName("data")
    val user: User
)

data class RegisterResponse(
    // Properti status dan message
    val status: String,
    val message: String,

    // Properti ini yang dicari. Harus bernama 'user'.
    val user: User // <-- Pastikan namanya 'user' dengan tipe data User
)

data class UsersResponse(
    @SerializedName("data") // Jika nama di API berbeda dari nama properti
    val data: List<User>,
    val message: String?
)

// Model untuk response error
data class ErrorResponse(
    @SerializedName("message")
    val message: String
)