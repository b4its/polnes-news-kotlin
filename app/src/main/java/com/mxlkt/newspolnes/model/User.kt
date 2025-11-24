package com.mxlkt.newspolnes.model

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
