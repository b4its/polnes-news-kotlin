package com.mxlkt.newspolnes.view

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mxlkt.newspolnes.api.ApiClient
import com.mxlkt.newspolnes.model.UpdateUserRequest
import com.mxlkt.newspolnes.model.ErrorResponse
import com.mxlkt.newspolnes.model.LoginRequest
import com.mxlkt.newspolnes.model.RegisterRequest
import com.mxlkt.newspolnes.model.StoreData // Asumsi StoreData ada
import com.mxlkt.newspolnes.model.UpdateUserData
import com.mxlkt.newspolnes.model.User
import com.mxlkt.newspolnes.utils.SessionManager // Asumsi SessionManager ada
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

// Sealed class untuk mengelola state UI Otentikasi
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class SuccessLogin(val user: User) : AuthState()
    data class SuccessRegister(val user: User) : AuthState()
    data class SuccessUpdate(val user: UpdateUserData) : AuthState()
    data class Error(val message: String) : AuthState()
    // Tambahkan SuccessDelete jika API memiliki fungsi hapus user
}

/**
 * ViewModel yang bertanggung jawab HANYA untuk operasi Autentikasi dan pengelolaan sesi.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val apiServicePublic = ApiClient.apiService
    private val sessionManager = SessionManager(application)

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // --- Data Sesi dari DataStore (Flow) ---
    val userName = sessionManager.userName
    val userEmail = sessionManager.userEmail
    val userId = sessionManager.userId
    val isLoggedIn = sessionManager.isLoggedIn
    val userRole = sessionManager.userRole

    suspend fun checkLoginStatus(): Boolean {
        return isLoggedIn.first()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // ------------------------------------
    // LOGIN
    // ------------------------------------
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Gunakan apiServicePublic
                val response = apiServicePublic.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val user = response.body()?.data

                    if (user != null) {
                        // Jika API mengembalikan token (dan Anda mengambilnya di model/interceptor)
                        // Anda harus menyimpannya di ApiClient.authToken di sini,
                        // tetapi untuk saat ini kita biarkan ApiClient yang mengelola melalui interceptor.

                        sessionManager.saveLoginSession(
                            isLoggedIn = true,
                            userId = user.id,
                            userName = user.name,
                            userEmail = user.email,
                            userRole = user.role.name // Menggunakan .name untuk Enum Role
                        )
                        _authState.value = AuthState.SuccessLogin(user)
                    } else {
                        _authState.value = AuthState.Error("Login berhasil, tetapi respons data kosong.")
                    }
                } else {
                    handleApiError(response.errorBody()?.string(), "Gagal Login. Periksa kredensial Anda.")
                }
            } catch (e: IOException) {
                _authState.value = AuthState.Error("Kesalahan jaringan: Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan tak terduga: ${e.message}")
            }
        }
    }

    // ------------------------------------
    // REGISTER
    // ------------------------------------
    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Semua kolom harus diisi.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password minimal 6 karakter.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Gunakan apiServicePublic
                val response = apiServicePublic.register(RegisterRequest(name, email, password))

                if (response.isSuccessful) {
                    val user: User? = response.body()?.user

                    if (user != null) {
                        StoreData._userList.add(user) // Hati-hati dengan mutable list global
                        sessionManager.saveLoginSession(
                            isLoggedIn = true,
                            userId = user.id,
                            userName = user.name,
                            userEmail = user.email,
                            userRole = user.role.name
                        )
                        _authState.value = AuthState.SuccessRegister(user)
                    } else {
                        _authState.value = AuthState.Error("Registrasi berhasil, tetapi respons data tidak valid.")
                    }

                } else {
                    handleApiError(response.errorBody()?.string(), "Registrasi gagal: Kode ${response.code()}.")
                }

            } catch (e: IOException) {
                _authState.value = AuthState.Error("Kesalahan jaringan: Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan tak terduga: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // ------------------------------------
    // UPDATE USER
    // ------------------------------------
    fun updateUser(userId: Int, request: UpdateUserRequest) {
        // Pengecekan awal sudah benar: setidaknya satu field harus diisi.
        if (request.name == null && request.email == null && request.password == null && request.role == null) {
            _authState.value = AuthState.Error("Setidaknya satu field harus diisi untuk update.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Ambil ID pengguna saat ini dari Session Manager (menggunakan first() untuk mendapatkan nilai pertama DataStore)
                val currentUserId = sessionManager.userId.first()

                // Jika rute Anda di API diubah menjadi GET dan Anda menggunakan @Query, Anda harus memanggilnya seperti ini:

                val response = apiServicePublic.updateUser(
                    userId = userId,
                    name = request.name,
                    email = request.email,
                    password = request.password,
                    role = request.role
                )


                // **Tetap menggunakan Panggilan PUT yang Benar (Sesuai Struktur Kode Anda)**
//                val response = apiServicePublic.updateUser(userId, request)

                if (response.isSuccessful) {
                    val updatedUser = response.body()?.user

                    if (updatedUser != null) {

                        // Cek apakah user yang di-update adalah user yang sedang login
                        if (userId == currentUserId) {
                            // Perbarui data sesi hanya jika user yang di-update adalah user saat ini
                            sessionManager.saveLoginSession(
                                isLoggedIn = true,
                                userId = updatedUser.id,
                                userName = updatedUser.name,
                                userEmail = updatedUser.email,
                                userRole = updatedUser.role // Role di UpdateUserData adalah String
                            )
                        }

                        // Kirim status sukses
                        _authState.value = AuthState.SuccessUpdate(updatedUser)
                    } else {
                        // Penanganan respons yang sukses tetapi body kosong
                        _authState.value = AuthState.Error("Update berhasil, tetapi respons data pengguna kosong.")
                    }
                } else {
                    // Penanganan error API (400, 401, 405, dll.)
                    // Asumsi handleApiError() akan mengatur _authState.value = AuthState.Error(...)
                    handleApiError(response.errorBody()?.string(), "Gagal Update User. Kode: ${response.code()}.")
                }

            } catch (e: IOException) {
                // Kesalahan jaringan (timeout, koneksi terputus)
                _authState.value = AuthState.Error("Kesalahan jaringan: Periksa koneksi internet Anda.")
            } catch (e: Exception) {
                // Kesalahan lainnya (parsing, dll.)
                _authState.value = AuthState.Error("Terjadi kesalahan tak terduga: ${e.message ?: "Unknown Error"}")
            } finally {
                // (Opsional) Tambahkan penanganan selesai jika diperlukan
            }
        }
    }
    // ------------------------------------
    // LOGOUT
    // ------------------------------------
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            ApiClient.authToken = null // Kosongkan token saat logout
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Helper function untuk menangani error response dari API
     */
    private fun handleApiError(errorBodyString: String?, defaultMessage: String) {
        val errorMessage = try {
            errorBodyString?.let {
                Gson().fromJson(it, ErrorResponse::class.java).message
            } ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
        _authState.value = AuthState.Error(errorMessage)
    }
}