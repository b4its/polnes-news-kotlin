package com.mxlkt.newspolnes.model

data class Notification(
    val id: Int,
    val iconRes: Int,
    val category: String,
    val title: String,
    val date: String
)
