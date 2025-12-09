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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.AccountInfoCard
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.model.UserRole
import com.mxlkt.newspolnes.ui.theme.*
import com.mxlkt.newspolnes.utils.SessionManager
// Import ViewModel
import com.mxlkt.newspolnes.view.NewsViewModel
import com.mxlkt.newspolnes.view.UserViewModel
import com.mxlkt.newspolnes.view.UserListState
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel // PENTING: Import ini ditambahkan

@Composable
fun AdminDashboardScreen(
    newsViewModel: NewsViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loggedInUserId by sessionManager.userId.collectAsState(initial = null)

    // --- 1. Fetch Data saat pertama kali load ---
    LaunchedEffect(Unit) {
        newsViewModel.fetchNewsList(1)
        newsViewModel.fetchNewsMostRatedShortList()
        userViewModel.fetchAllUsers()
        categoryViewModel.fetchAllCategories()
    }

    // --- 2. Observasi State dari ViewModel ---
    val newsList by newsViewModel.newsList.observeAsState(emptyList())
    val categoryList by categoryViewModel.categoryList.observeAsState(emptyList())

    // Handling User State (Sealed Class)
    val userState = userViewModel.userListState.value
    val userList = remember(userState) {
        if (userState is UserListState.Success) userState.users else emptyList()
    }

    // Identifikasi User yang Login
    val currentUser = remember(loggedInUserId, userList) {
        userList.find { it.id == loggedInUserId }
    }

    // --- 3. Perhitungan Statistik ---
    val stats by remember(newsList, userList) {
        derivedStateOf {
            val pending = newsList.count { it.status == "pending_review" || it.status == "pending_deletion" }
            val published = newsList.count { it.status == "published" }
            val totalViews = newsList.sumOf { it.views }

            // Cek role user (Case insensitive)
            val editors = userList.count { it.role == UserRole.EDITOR || it.role.name.equals("editor", true) }
            val readers = userList.count { it.role == UserRole.USER || it.role.name.equals("user", true) }

            mapOf(
                "pending" to pending,
                "published" to published,
                "views" to totalViews,
                "editors" to editors,
                "readers" to readers
            )
        }
    }

    // Data Chart Kategori (Top 5)
    val categoryViewsMap = remember(categoryList, newsList) {
        categoryList.map { cat ->
            val views = newsList.filter { it.categoryId == cat.id }.sumOf { it.views }
            cat.name to views
        }.sortedByDescending { it.second }
            .take(5)
            .filter { it.second > 0 }
    }

    // Top News (Most Viewed)
    val topNews = remember(newsList) {
        newsList.sortedByDescending { it.views }.take(3)
    }

    // Top Editors (by Views)
    val topEditors = remember(userList, newsList) {
        userList.filter { it.role == UserRole.EDITOR || it.role.name.equals("editor", true) }
            .map { editor ->
                val totalViews = newsList.filter { it.authorId == editor.id }.sumOf { it.views }
                editor to totalViews
            }
            .sortedByDescending { it.second }
            .take(3)
    }

    // === UI DISPLAY ===
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Info Akun
        AccountInfoCard(
            fullName = currentUser?.name ?: "Loading...",
            role = currentUser?.role?.name ?: "Administrator",
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- SECTION 1: SYSTEM OVERVIEW ---
            Text(
                text = "System Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Row 1
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Need Review",
                    count = stats["pending"].toString(),
                    containerColor = StatusPendingBg, // Pastikan warna ini ada di Theme.kt atau ganti Color(0xFFFFE0B2)
                    contentColor = StatusPendingText
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Published",
                    count = stats["published"].toString(),
                    containerColor = StatusPublishedBg, // Pastikan warna ini ada di Theme.kt atau ganti Color(0xFFC8E6C9)
                    contentColor = StatusPublishedText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Editors",
                    count = stats["editors"].toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Views",
                    count = stats["views"].toString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 2: CATEGORY ANALYTICS ---
            Text(
                text = "Top Categories by Views",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (categoryViewsMap.isEmpty()) {
                Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SimplePieChart(data = categoryViewsMap)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 3: MOST VIEWED NEWS ---
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

            // --- SECTION 4: TOP PERFORMING EDITORS ---
            SectionHeader(title = "Top Editors (by Views)", icon = Icons.Outlined.Person)
            if (topEditors.isEmpty()) {
                Text("No data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topEditors.forEachIndexed { index, data ->
                        TopEditorItemCard(
                            rank = index + 1,
                            user = data.first,
                            metricLabel = "${data.second} Views"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// =========================================================================
// UI COMPONENTS DEFINITIONS (Tambahkan ini agar error Unresolved hilang)
// =========================================================================

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
fun TopEditorItemCard(rank: Int, user: User, metricLabel: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    // Jika StatusPendingBg error, ganti dengan Color(0xFFFFE0B2)
                    .background(StatusPendingBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "#$rank", fontWeight = FontWeight.Bold, color = StatusPendingText)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.SemiBold)
                Text(text = user.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(
                text = metricLabel,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
        // Chart Bulat
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

        // Legend (Keterangan Warna)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.forEachIndexed { index, pair ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(chartColors.getOrElse(index) { Color.Gray })
                    )
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