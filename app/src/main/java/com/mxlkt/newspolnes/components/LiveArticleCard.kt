package com.mxlkt.newspolnes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Import Coil untuk gambar dari URL
import coil.compose.AsyncImage
// Import model dari API
import com.mxlkt.newspolnes.model.NewsModel
// Import model lokal yang masih dibutuhkan untuk Status dan StoreData (jika StoreData masih ada untuk formatDate)
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.R // Asumsi: untuk placeholder
import com.mxlkt.newspolnes.ui.theme.*

@Composable
fun ArticleCard(
    // � PERUBAHAN 1: Menerima NewsModel
    article: NewsModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // � PERUBAHAN 2: Menentukan status dari string API ke NewsStatus (jika diperlukan)
    // Karena NewsModel memiliki 'status: String', kita perlu mengkonversi atau langsung pakai string.
    // Jika StatusChip butuh NewsStatus, kita perlu konversi:
    val newsStatus = try {
        NewsStatus.valueOf(article.status.uppercase())
    } catch (e: IllegalArgumentException) {
        NewsStatus.DRAFT // Default jika status tidak dikenali
    }

    val isLocked = newsStatus == NewsStatus.PENDING_REVIEW ||
            newsStatus == NewsStatus.PENDING_DELETION ||
            newsStatus == NewsStatus.PENDING_UPDATE

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = !isLocked) { onEdit() }
    ) {
        Column {
            // --- BAGIAN ATAS: GAMBAR & INFO ---
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 1. Gambar Thumbnail
                // � PERUBAHAN 3: Menggunakan AsyncImage untuk memuat URL gambar
                AsyncImage(
                    model = article.gambar,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.category_economy),
                    error = painterResource(id = R.drawable.category_economy),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Info Artikel
                Column(modifier = Modifier.weight(1f)) {
                    // Judul
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tanggal
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            // � PERUBAHAN 4: Menggunakan created_at (atau updated_at) dari NewsModel
                            text = StoreData.formatDate(article.created_at), // Gunakan created_at
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Chip
                    StatusChip(status = newsStatus) // Menggunakan NewsStatus yang sudah dikonversi
                }
            }

            // --- BAGIAN BAWAH: TOMBOL AKSI (OUTLINE) ---
            Divider(color = Color.Gray.copy(alpha = 0.1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // ... (Tombol Edit dan Delete tidak berubah, hanya menggunakan isLocked) ...

                // Tombol Edit
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isLocked,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Tombol Delete
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isLocked,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }
}

// Preview dihapus atau diubah untuk menggunakan NewsModel dummy
// (Ini membutuhkan implementasi NewsModel dummy yang lengkap)