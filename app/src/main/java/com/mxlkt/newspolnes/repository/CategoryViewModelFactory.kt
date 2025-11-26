package com.mxlkt.newspolnes.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mxlkt.newspolnes.api.ApiCategoryService
import com.mxlkt.newspolnes.view.CategoryViewModel

class CategoryViewModelFactory(
    // � KOREKSI: Hanya butuh ApiCategoryService
    private val apiCategoryService: ApiCategoryService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            // ✅ Membuat instance CategoryViewModel hanya dengan satu argumen
            return CategoryViewModel(apiCategoryService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}