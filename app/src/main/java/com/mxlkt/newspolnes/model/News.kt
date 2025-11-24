package com.mxlkt.newspolnes.model

data class News(
    val id: Int,
    val title: String,
    val categoryId: Int,
    val imageRes: Int,
    val content: String,
    val authorId: Int,
    val date: String,
    val views: Int,
    val youtubeVideoId: String?,
    val status: NewsStatus = NewsStatus.DRAFT
)

