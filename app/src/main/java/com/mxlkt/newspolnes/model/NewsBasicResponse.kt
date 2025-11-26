package com.mxlkt.newspolnes.model

// Response untuk GET /news/{id} atau POST/PUT/DELETE yang mengembalikan objek tunggal
data class SingleNewsResponse(
    val status: String,
    val message: String,
    val data: NewsModel
)

// Response dasar untuk operasi yang hanya mengembalikan status/pesan (seperti DELETE)
data class BasicResponse(
    val status: String,
    val message: String
)