package com.mxlkt.newspolnes.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxlkt.newspolnes.components.AccountInfoCard
import com.mxlkt.newspolnes.model.DummyData
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.ui.theme.*
// 🟢 Import SessionManager
import com.mxlkt.newspolnes.utils.SessionManager

@Composable
fun EditorDashboardScreen(
    // 🟢 Parameter editorId SUDAH DIHAPUS
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // 🟢 1. Ambil Data Editor dari SessionManager
    val currentUser = SessionManager.currentUser

    // 🟢 2. Filter Berita Milik Editor Tersebut
    // Menggunakan remember(currentUser) agar data refresh jika user berubah
    val myArticles = remember(currentUser) {
        DummyData.newsList.filter { it.authorId == currentUser?.id }
    }

    // --- 3. Hitung Statistik ---
    val totalViews = myArticles.sumOf { it.views }

    val approvedCount = myArticles.count { it.status == NewsStatus.PUBLISHED }

    val pendingCount = myArticles.count {
        it.status == NewsStatus.PENDING_REVIEW || it.status == NewsStatus.PENDING_UPDATE
    }

    // Hitung Rating Rata-rata (Dari tabel comment yang newsId-nya milik editor ini)
    val ratings = remember(myArticles) {
        DummyData.commentList.filter { comment ->
            myArticles.any { it.id == comment.newsId }
        }.map { it.rating }
    }
    val averageRating = if (ratings.isNotEmpty()) ratings.average() else 0.0

    // Top 3 Artikel Saya
    val myTopArticles = remember(myArticles) {
        myArticles.sortedByDescending { it.views }.take(3)
    }

    // Teks Performa & Warna
    val (perfText, perfColor, perfIcon) = when {
        averageRating >= 4.5 -> Triple("Excellent Work!", PolnesGreen, Icons.Outlined.EmojiEvents)
        averageRating >= 3.5 -> Triple("Great Effort!", Color(0xFFFFA000), Icons.Filled.ThumbUp)
        else -> Triple("Keep Improving!", Color.Gray, Icons.Outlined.Insights)
    }

    // === LAYOUT UTAMA ===
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Header Akun
        AccountInfoCard(
            fullName = currentUser?.name ?: "Editor",
            role = "Editor",
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // --- SECTION 1: OVERVIEW STATS ---
            Text(
                text = "Performance Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Row 1: Views & Rating
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EditorStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Views",
                    value = totalViews.toString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                EditorStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Avg Rating",
                    value = String.format("%.1f", averageRating),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    icon = Icons.Default.Star
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Published & Pending
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EditorStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Published",
                    value = approvedCount.toString(),
                    containerColor = StatusPublishedBg, // Pastikan warna ini ada di Theme atau ganti Color.Green
                    contentColor = StatusPublishedText
                )
                EditorStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Pending",
                    value = pendingCount.toString(),
                    containerColor = StatusPendingBg, // Pastikan warna ini ada di Theme atau ganti Color.Yellow
                    contentColor = StatusPendingText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 2: PERFORMANCE INSIGHT ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = perfIcon,
                        contentDescription = null,
                        tint = perfColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Performance Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = perfText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 3: TOP ARTICLES ---
            SectionHeader(title = "Your Top Articles", icon = Icons.Outlined.Visibility)

            if (myTopArticles.isEmpty()) {
                Text("No articles yet.", color = Color.Gray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    myTopArticles.forEachIndexed { index, news ->
                        EditorRankCard(
                            rank = index + 1,
                            title = news.title,
                            views = news.views
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ------------------------------------------------
// HELPER COMPONENTS
// ------------------------------------------------

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun EditorStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EditorRankCard(rank: Int, title: String, views: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$views views",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorDashboardScreenPreview() {
    NewsPolnesTheme {
        EditorDashboardScreen(
            currentRoute = "editor_dashboard",
            onNavigate = {}
        )
    }
}