package com.mxlkt.newspolnes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.CategoryRequest
import com.mxlkt.newspolnes.repository.CategoryRepository
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola data kategori, menyesuaikan pola dari NewsViewModel.
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    // Instance Repository (Diinisialisasi secara sederhana)
    private val repository = CategoryRepository()

    // --- LiveData untuk State UI ---

    // Daftar Kategori (berisi list data kategori)
    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    // State Loading
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Pesan Error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Pesan Sukses untuk operasi CUD
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // Data kategori tunggal hasil operasi CREATE/UPDATE
    private val _singleCategory = MutableLiveData<Category?>()
    val singleCategory: LiveData<Category?> = _singleCategory


    // --- Fungsi API ---

    /**
     * Memuat daftar semua kategori.
     */
    fun fetchAllCategories() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Repository mengembalikan List<Category> atau melempar Exception
                val categories: List<Category> = repository.getAllCategories()
                _categoryList.value = categories
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat kategori: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Membuat kategori baru.
     */
    fun createCategory(request: CategoryRequest) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
        viewModelScope.launch {
            try {
                // Repository mengembalikan Category data atau melempar Exception
                val newCategory: Category = repository.createCategory(request)

                _successMessage.value = "Kategori ${newCategory.name} berhasil dibuat!"
                _singleCategory.value = newCategory

                // Opsional: Perbarui daftar di UI dengan data baru (jika perlu)
                fetchAllCategories()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal membuat kategori: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memperbarui kategori yang sudah ada.
     */
    fun updateCategory(categoryId: Int, request: CategoryRequest) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null
        viewModelScope.launch {
            try {
                // Repository mengembalikan Category data atau melempar Exception
                val updatedCategory: Category = repository.updateCategory(categoryId, request)

                _successMessage.value = "Kategori ${updatedCategory.name} berhasil diperbarui!"
                _singleCategory.value = updatedCategory

                // Opsional: Perbarui daftar di UI dengan data yang diperbarui
                fetchAllCategories()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui kategori: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fungsi untuk mereset pesan status
     */
    fun clearStatusMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}