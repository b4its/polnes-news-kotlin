package com.mxlkt.newspolnes.view

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mxlkt.newspolnes.api.ApiClient
import com.mxlkt.newspolnes.model.ErrorResponse
import com.mxlkt.newspolnes.model.StoreData
import com.mxlkt.newspolnes.model.User
import kotlinx.coroutines.launch
import java.io.IOException

// Sealed class untuk mengelola state UI Daftar Pengguna
sealed class UserListState {
    object Idle : UserListState()
    object Loading : UserListState()
    data class Success(val users: List<User>) : UserListState()
    data class Error(val message: String) : UserListState()
}

sealed class UpdateRoleState {
    object Idle : UpdateRoleState()
    object Loading : UpdateRoleState()
    data class Success(val user: User, val message: String) : UpdateRoleState()
    data class Error(val message: String) : UpdateRoleState()
}

/**
 * ViewModel yang bertanggung jawab HANYA untuk data Daftar Pengguna.
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.apiService

    // State untuk Daftar Pengguna (Sudah ada)
    private val _userListState = mutableStateOf<UserListState>(UserListState.Idle)
    val userListState: State<UserListState> = _userListState

    // State BARU untuk Update Role
    private val _updateRoleState = mutableStateOf<UpdateRoleState>(UpdateRoleState.Idle)
    val updateRoleState: State<UpdateRoleState> = _updateRoleState


    fun resetState() {
        _userListState.value = UserListState.Idle
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _userListState.value = UserListState.Loading
            try {
                // Panggilan ke endpoint GET("users/get") yang sudah didefinisikan di ApiService
                val response = apiService.getAllUsers()

                if (response.isSuccessful) {
                    // Pastikan penanganan null-safety untuk response.body()
                    val users = response.body()?.data ?: emptyList()
                    StoreData.setUsers(users)
                    _userListState.value = UserListState.Success(users)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Gagal mengambil daftar pengguna (Code: ${response.code()})"
                    }
                    _userListState.value = UserListState.Error(errorMessage)
                }
            } catch (e: IOException) {
                // Kesalahan jaringan
                _userListState.value = UserListState.Error("Kesalahan jaringan: Periksa koneksi Anda.")
            } catch (e: Exception) {
                // Kesalahan tak terduga
                _userListState.value = UserListState.Error("Terjadi kesalahan tak terduga: ${e.message}")
            }
        }
    }



    fun updateRole(userId: Int) {
        viewModelScope.launch {
            _updateRoleState.value = UpdateRoleState.Loading
            try {
                val response = apiService.updateRoleToEditor(userId)

                if (response.isSuccessful) {
                    val body = response.body()
                    val updatedUser = body?.user // Ambil objek User yang diperbarui

                    if (updatedUser != null) {
                        _updateRoleState.value = UpdateRoleState.Success(
                            user = updatedUser,
                            message = body.message
                        )
                        // Opsional: Perbarui daftar pengguna lokal atau muat ulang
                        // fetchAllUsers()
                    } else {
                        _updateRoleState.value = UpdateRoleState.Error("Respon sukses, tapi data pengguna kosong.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "Gagal memperbarui role (Code: ${response.code()})"
                    }
                    _updateRoleState.value = UpdateRoleState.Error(errorMessage)
                }

            } catch (e: IOException) {
                _updateRoleState.value = UpdateRoleState.Error("Kesalahan jaringan: Periksa koneksi Anda.")
            } catch (e: Exception) {
                _updateRoleState.value = UpdateRoleState.Error("Terjadi kesalahan tak terduga: ${e.message}")
            }
        }
    }

    // Fungsi untuk mereset state update role
    fun resetUpdateRoleState() {
        _updateRoleState.value = UpdateRoleState.Idle
    }


}