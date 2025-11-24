package com.mxlkt.newspolnes.model

enum class NewsStatus {
    DRAFT,              // Masih di HP Editor, belum dikirim
    PUBLISHED,          // Tayang untuk umum
    PENDING_REVIEW,     // Artikel BARU sedang dicek Admin
    REJECTED,           // Ditolak Admin

    // TAMBAHAN PENTING:
    PENDING_DELETION,   // Artikel SUDAH tayang, tapi Editor minta hapus (perlu ACC Admin)
    PENDING_UPDATE      // Artikel SUDAH tayang, tapi ada revisi yang perlu ACC Admin
}