package com.mxlkt.newspolnes.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mxlkt.newspolnes.components.CommonTopBar
import com.mxlkt.newspolnes.components.LiveNewsCard
import com.mxlkt.newspolnes.components.LiveNewsCategoryCard
import com.mxlkt.newspolnes.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectedScreen(
    categoryId: Int,
    categoryName: String,
    onNavigateBack: () -> Unit,
    onNewsClick: (Int) -> Unit,

    // HAPUS NewsViewModel, Cukup pakai satu saja
    categoryViewModel: CategoryViewModel = viewModel()
) {
    // 1. OBSERVE STATE (PERBAIKAN DI SINI)
    // Gunakan 'categoryViewModel.newsInCategoryList' (sesuai nama variabel di ViewModel yg kita buat tadi)
    // BUKAN NewsViewModel
    val newsList by categoryViewModel.newsInCategoryList.observeAsState(emptyList())

    val isLoading by categoryViewModel.isLoading.observeAsState(false)
    val errorMessage by categoryViewModel.errorMessage.observeAsState()

    // 2. TRIGGER API CALL
    LaunchedEffect(categoryId) {
        if (categoryId > 0) {
            categoryViewModel.fetchNewsByCategory(categoryId)
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = categoryName.ifEmpty { "Kategori" },
                onBack = onNavigateBack
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // A. Loading
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // B. Error
            else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Terjadi kesalahan",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            // C. Success
            else {
                if (newsList.isEmpty()) {
                    Text(
                        text = "Belum ada berita di kategori ini.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(newsList) { news ->
                            LiveNewsCategoryCard(
                                newsModel = news,
                                onClick = { onNewsClick(news.id) },
                                categoryNameOverride = categoryName
                            )
                        }
                    }
                }
            }
        }
    }
}