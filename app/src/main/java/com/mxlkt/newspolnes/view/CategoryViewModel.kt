package com.mxlkt.newspolnes.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxlkt.newspolnes.api.ApiCategoryService // Import yang benar
import com.mxlkt.newspolnes.model.Category
import com.mxlkt.newspolnes.model.CategoryDto
import com.mxlkt.newspolnes.model.CategoryRequest
import kotlinx.coroutines.launch

class CategoryViewModel(
    // � KOREKSI: Hanya butuh ApiCategoryService, API Key sudah di Interceptor
    private val apiCategoryService: ApiCategoryService
) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    init {
        fetchCategories()
    }

    /**
     * Mengambil daftar kategori dari API secara asinkron.
     */
    fun fetchCategories() {
        _isLoading.value = true
        _successMessage.value = null
        viewModelScope.launch {
            try {
                // � KOREKSI: Panggil tanpa menyertakan apiKey
                val response = apiCategoryService.getAllCategories()

                if (response.isSuccessful && response.body()?.data != null) {
                    val categoryList = response.body()!!.data!!.map { dto: CategoryDto ->
                        Category(
                            id = dto.id,
                            name = dto.name,
                            gambar = 0 // Asumsi gambar = 0 adalah default
                        )
                    }
                    _categories.postValue(categoryList)
                    _errorMessage.postValue(null)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.postValue("Failed to load categories: HTTP ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    /**
     * Menyimpan kategori baru ke database (CREATE/STORE).
     */
    fun createCategory(name: String, imageUrl: String) {
        _isLoading.value = true
        _successMessage.value = null
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val request = CategoryRequest(name = name, gambar = imageUrl)
                // � KOREKSI: Panggil tanpa menyertakan apiKey
                val response = apiCategoryService.createCategory(request)

                if (response.isSuccessful && response.body()?.data != null) {
                    _successMessage.postValue("Category '$name' created successfully!")
                    fetchCategories()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.postValue("Failed to create category: HTTP ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error during creation: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memperbarui kategori yang sudah ada di database (UPDATE).
     */
    fun updateCategory(id: Int, name: String, imageUrl: String) {
        _isLoading.value = true
        _successMessage.value = null
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val request = CategoryRequest(name = name, gambar = imageUrl)
                // � KOREKSI: Panggil tanpa menyertakan apiKey
                val response = apiCategoryService.updateCategory(id, request)

                if (response.isSuccessful && response.body()?.data != null) {
                    _successMessage.postValue("Category ID $id updated successfully!")
                    fetchCategories()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.postValue("Failed to update category: HTTP ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Network error during update: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}