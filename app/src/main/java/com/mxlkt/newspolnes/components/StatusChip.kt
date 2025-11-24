package com.mxlkt.newspolnes.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxlkt.newspolnes.model.NewsStatus

@Composable
fun StatusChip(status: NewsStatus) {
    // Tentukan Warna Background dan Warna Teks berdasarkan Status
    val (bgColor, textColor) = when (status) {
        NewsStatus.DRAFT -> Pair(
            Color(0xFFE0E0E0), // Abu-abu muda
            Color(0xFF424242)  // Abu-abu tua
        )
        NewsStatus.PUBLISHED -> Pair(
            Color(0xFFE8F5E9), // Hijau sangat muda
            Color(0xFF2E7D32)  // Hijau tua
        )
        NewsStatus.PENDING_REVIEW -> Pair(
            Color(0xFFFFF3E0), // Oranye muda
            Color(0xFFEF6C00)  // Oranye tua
        )
        NewsStatus.REJECTED -> Pair(
            Color(0xFFFFEBEE), // Merah muda
            Color(0xFFC62828)  // Merah tua
        )
        NewsStatus.PENDING_DELETION -> Pair(
            Color(0xFFFCE4EC), // Pink kemerahan (Warning Hapus)
            Color(0xFF880E4F)  // Merah maroon
        )
        NewsStatus.PENDING_UPDATE -> Pair(
            Color(0xFFE3F2FD), // Biru muda
            Color(0xFF1565C0)  // Biru tua
        )
    }

    // Tentukan Teks Label agar lebih rapi (bukan PENDING_REVIEW tapi Pending Review)
    val label = when (status) {
        NewsStatus.DRAFT -> "Draft"
        NewsStatus.PUBLISHED -> "Published"
        NewsStatus.PENDING_REVIEW -> "Pending Review"
        NewsStatus.REJECTED -> "Rejected"
        NewsStatus.PENDING_DELETION -> "Request Delete"
        NewsStatus.PENDING_UPDATE -> "Request Update"
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50), // Bentuk kapsul
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}