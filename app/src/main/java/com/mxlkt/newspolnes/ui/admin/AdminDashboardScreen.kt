package com.mxlkt.newspolnes.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxlkt.newspolnes.components.AccountInfoCard
import com.mxlkt.newspolnes.model.DummyData
import com.mxlkt.newspolnes.model.News
import com.mxlkt.newspolnes.model.NewsStatus
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.*
import com.mxlkt.newspolnes.utils.SessionManager // 🟢 Import SessionManager

@Composable
fun AdminDashboardScreen(
    // Parameter dihapus, karena currentUser diambil dari SessionManager
) {
    // 🟢 Ambil user dari SessionManager
    val currentUser = SessionManager.currentUser

    // --- 1. Hitung Data Statistik Global ---
    val pendingNews = DummyData.newsList.count {
        it.status == NewsStatus.PENDING_REVIEW || it.status == NewsStatus.PENDING_DELETION
    }
    val publishedNews = DummyData.newsList.count { it.status == NewsStatus.PUBLISHED }
    val totalViews = DummyData.newsList.sumOf { it.views }
    val totalEditors = DummyData.userList.count { it.role == UserRole.EDITOR }
    val totalReaders = DummyData.userList.count { it.role == UserRole.USER }
    val totalCategories = DummyData.categoryList.size

    // --- 2. Data Analitik ---
    val categoryViewsMap = remember {
        DummyData.categoryList.map { category ->
            val viewsInCategory = DummyData.newsList
                .filter { it.categoryId == category.id }
                .sumOf { it.views }
            category.name to viewsInCategory
        }.filter { it.second > 0 }
    }

    val topNews = remember { DummyData.newsList.sortedByDescending { it.views }.take(3) }

    val topRatedNews = remember {
        DummyData.newsList.map { news ->
            val ratings = DummyData.commentList.filter { it.newsId == news.id }.map { it.rating }
            val avgRating = if (ratings.isNotEmpty()) ratings.average() else 0.0
            news to avgRating
        }.sortedByDescending { it.second }.take(3)
    }

    val topEditors = remember {
        DummyData.userList.filter { it.role == UserRole.EDITOR }.map { editor ->
            val newsByEditor = DummyData.newsList.filter { it.authorId == editor.id }
            val ratings = DummyData.commentList.filter { comment ->
                newsByEditor.any { it.id == comment.newsId }
            }.map { it.rating }
            val avg = if (ratings.isNotEmpty()) ratings.average() else 0.0
            Pair(editor, avg)
        }.sortedByDescending { it.second }.take(3)
    }

    // === LAYOUT UTAMA ===
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Info Akun
        AccountInfoCard(
            fullName = currentUser?.name ?: "Admin",
            role = "Administrator",
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- SECTION 1: SYSTEM OVERVIEW ---
            Text(
                text = "System Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Row 1: Status Berita
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Need Review",
                    count = pendingNews.toString(),
                    containerColor = StatusPendingBg,
                    contentColor = StatusPendingText
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Published",
                    count = publishedNews.toString(),
                    containerColor = StatusPublishedBg,
                    contentColor = StatusPublishedText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Users
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Editors",
                    count = totalEditors.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Readers",
                    count = totalReaders.toString(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Engagement
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Views",
                    count = totalViews.toString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Categories",
                    count = totalCategories.toString(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 2: CATEGORY ENGAGEMENT ---
            Text(
                text = "Category Engagement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (categoryViewsMap.isEmpty()) {
                Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SimplePieChart(data = categoryViewsMap)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 3: TOP TRENDING (VIEWS) ---
            SectionHeader(title = "Most Viewed News", icon = Icons.Outlined.Visibility)
            if (topNews.isEmpty()) {
                Text("No news available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topNews.forEachIndexed { index, news ->
                        SimpleRankCard(
                            rank = index + 1,
                            title = news.title,
                            metricValue = "${news.views} views",
                            metricColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 4: TOP RATED (RATING) ---
            SectionHeader(title = "Highest Rated Stories", icon = Icons.Outlined.ThumbUp)
            if (topRatedNews.isEmpty()) {
                Text("No ratings yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topRatedNews.forEachIndexed { index, (news, rating) ->
                        SimpleRankCard(
                            rank = index + 1,
                            title = news.title,
                            metricValue = String.format("★ %.1f", rating),
                            metricColor = Color(0xFFFFC107) // Emas
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 5: TOP EDITORS ---
            SectionHeader(title = "Top Editors", icon = Icons.Outlined.Person)
            if (topEditors.isEmpty()) {
                Text("No editors data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topEditors.forEachIndexed { index, data ->
                        TopEditorItemCard(rank = index + 1, user = data.first, rating = data.second)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ------------------------------------------------
// UI COMPONENTS
// ------------------------------------------------

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
fun AdminStatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
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
fun SimpleRankCard(
    rank: Int,
    title: String,
    metricValue: String,
    metricColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                text = metricValue,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = metricColor
            )
        }
    }
}

@Composable
fun TopEditorItemCard(rank: Int, user: User, rating: Double) {
    // 🟢 Tentukan Warna Editor
    val bgColor = StatusPendingBg
    val contentColor = StatusPendingText

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🟢 Icon Rank (Fix Warna Editor)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor), // Background Biru Pucat
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor // Icon Biru Tua
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Rating
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", rating),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Avg Rating",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SimplePieChart(data: List<Pair<String, Int>>) {
    val total = data.sumOf { it.second }.toFloat()
    val chartColors = listOf(
        Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFFF9800), Color(0xFF2196F3), Color(0xFFE91E63)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = -90f
                data.forEachIndexed { index, pair ->
                    val sweepAngle = (pair.second / total) * 360f
                    val color = chartColors.getOrElse(index) { Color.Gray }
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 40f)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = total.toInt().toString(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Views",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // LEGEND SECTION (Fix Box Error)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.forEachIndexed { index, pair ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(chartColors.getOrElse(index) { Color.Gray }),
                        contentAlignment = Alignment.Center
                    ) {} // 🟢 FIX: Tambahkan blok konten kosong
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = pair.first,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${pair.second} views",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun AdminDashboardPreview() {
    NewsPolnesTheme {
        AdminDashboardScreen()
    }
}