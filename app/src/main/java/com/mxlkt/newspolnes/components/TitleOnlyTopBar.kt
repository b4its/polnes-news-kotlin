package com.mxlkt.newspolnes.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleOnlyTopAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    // Gunakan 'CenterAlignedTopAppBar' agar judul otomatis di tengah.
    // Ini versi TopAppBar yang lebih simpel, tanpa ikon navigasi (back).
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        // Atur skema warna untuk TopAppBar ini
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary, // Warna background hijau
            titleContentColor = MaterialTheme.colorScheme.onPrimary // Pastikan warna teks judul putih
        ),
        modifier = modifier
    )
}