package com.mxlkt.newspolnes.model

data class Comment(
    val id: Int,
    val newsId: Int,
    val userId: Int,
    val rating: Int,
    val date: String
)