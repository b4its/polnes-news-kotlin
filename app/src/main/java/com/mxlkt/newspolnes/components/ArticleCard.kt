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
import androidx.compose.ui.unit.sp
import com.mxlkt.newspolnes.model.DummyData
import com.mxlkt.newspolnes.model.News
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.ui.theme.*

@Composable
fun ArticleCard(
    article: News,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tentukan apakah tombol harus dinonaktifkan (misal: sedang direview)
    val isLocked = article.status == NewsStatus.PENDING_REVIEW ||
            article.status == NewsStatus.PENDING_DELETION ||
            article.status == NewsStatus.PENDING_UPDATE

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // Opsional: Klik card juga bisa trigger edit jika tidak dikunci
            .clickable(enabled = !isLocked) { onEdit() }
    ) {
        Column {
            // --- BAGIAN ATAS: GAMBAR & INFO ---
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 1. Gambar Thumbnail
                Image(
                    painter = painterResource(id = article.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
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
                            text = DummyData.formatDate(article.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Chip
                    StatusChip(status = article.status)
                }
            }

            // --- BAGIAN BAWAH: TOMBOL AKSI (OUTLINE) ---
            // Kita sembunyikan tombol jika statusnya Pending (Locked),
            // atau bisa juga dimatikan (enabled = false).
            // Di sini saya pilih disable tombolnya agar user tau fiturnya ada tapi sedang tidak bisa dipakai.
            Divider(color = Color.Gray.copy(alpha = 0.1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Tombol Edit
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isLocked, // Matikan jika locked
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
                    enabled = !isLocked, // Matikan jika locked
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

@Preview(showBackground = true, name = "Article Card Preview")
@Composable
private fun ArticleCardPreview() {
    val sampleNews = DummyData.newsList.firstOrNull()
    NewsPolnesTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (sampleNews != null) {
                ArticleCard(article = sampleNews, onEdit = {}, onDelete = {})
            }
        }
    }
}