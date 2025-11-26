package com.mxlkt.newspolnes.model

data class NewsListResponse(
    val status: String,
    val message: String,
    val data: PaginatedNewsData
)

data class PaginatedNewsData(
    val current_page: Int,
    val data: List<NewsModel>, // Daftar berita utama
    val first_page_url: String?,
    val from: Int,
    val last_page: Int,
    val last_page_url: String?,
    val next_page_url: String?,
    val path: String,
    val per_page: Int,
    val prev_page_url: String?,
    val to: Int,
    val total: Int
)