package com.mxlkt.newspolnes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- WARNA UTAMA ---
val PolnesGreen = Color(0xFF038900)
val PolnesGreenLight = Color(0xFF6DDB6B)
val PolnesGreenDark = Color(0xFF00390A)

// --- WARNA NETRAL ---
val LightGray = Color(0xFFF7F7F7)
val DarkGray = Color(0xFF1C1B1F)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// --- Warna Khusus Notifikasi ---
val NotifBackgroundLight = Color(0xFFA3E5A6)
val NotifIconTintLight = Color(0xFF50AE5E)
val NotifBackgroundDark = Color(0xFF1B5E20)
val NotifIconTintDark = Color(0xFFC8E6C9)


// --- WARNA SEMANTIK UNTUK STATUS DAN AKSI ---
val StatusDraftBg = Color(0xFFFFF9C4)      // Kuning Pucat
val StatusDraftText = Color(0xFFFFA000)    // Orange
val StatusPublishedBg = Color(0xFFC8E6C9)  // Hijau Pucat
val StatusPublishedText = Color(0xFF2E7D32)  // Hijau Tua
val StatusPendingBg = Color(0xFFBBDEFB)    // Biru Pucat
val StatusPendingText = Color(0xFF1565C0)  // Biru Tua

// � (KODE LAMA DIHAPUS) �
// val ActionEditBg = Color(0xFF2196F3)
// val ActionDeleteBg = Color(0xFFF44336)
// val ActionOnColor = Color.White

// � (KODE BARU DITAMBAHKAN) �
// Warna untuk Tombol Aksi (Gaya Chip)
val ActionEditBg = Color(0xFFFFFFCC)     // Kuning Pucat (Latar)
val ActionEditIcon = Color(0xFFFFD700)    // Kuning Tua (Ikon)
val ActionDeleteBg = Color(0xFFFFCCBC)   // Orange Pucat (Latar)
val ActionDeleteIcon = Color(0xFFF4511E)  // Orange Tua (Ikon)


// --- Color Scheme ---
val LightColorScheme = lightColorScheme(
    primary = PolnesGreen,
    onPrimary = White,
    background = LightGray,
    onBackground = Black,
    surface = White,
    onSurface = Black
)

val DarkColorScheme = darkColorScheme(
    primary = PolnesGreenLight,
    onPrimary = PolnesGreenDark,
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = White
    /* ... */
)