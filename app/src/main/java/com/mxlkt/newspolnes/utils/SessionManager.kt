package com.mxlkt.newspolnes.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Nama file DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * Kelas yang bertanggung jawab untuk menyimpan dan mengambil data sesi pengguna
 * menggunakan Preference DataStore.
 */
class SessionManager(private val context: Context) {

    // Kunci-kunci (Keys) untuk DataStore
    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role") // <--- TAMBAHAN UNTUK ROLE
    }

    // Mengambil status login (Flow<Boolean>)
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    // Mengambil User ID (Flow<Int?>)
    val userId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }

    // Mengambil Nama Pengguna (Flow<String?>)
    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME]
        }

    // Mengambil Email Pengguna (Flow<String?>)
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_EMAIL]
        }

    // Mengambil Role Pengguna (Flow<String?>)
    val userRole: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ROLE]
        }


    // Fungsi untuk menyimpan data sesi login (dengan role)
    suspend fun saveLoginSession(
        isLoggedIn: Boolean,
        userId: Int,
        userName: String,
        userEmail: String,
        userRole: String // <--- PARAMETER BARU: ROLE
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_NAME] = userName
            preferences[PreferencesKeys.USER_EMAIL] = userEmail
            preferences[PreferencesKeys.USER_ROLE] = userRole // <--- SIMPAN ROLE
        }
    }

    // Fungsi untuk menghapus semua data sesi (Logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}