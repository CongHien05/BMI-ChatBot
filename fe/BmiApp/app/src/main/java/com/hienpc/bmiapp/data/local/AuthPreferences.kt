package com.hienpc.bmiapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * AuthPreferences - Quản lý lưu trữ JWT token bằng DataStore
 *
 * Sprint 1 yêu cầu chuẩn bị local storage (mock) cho token,
 * class này cung cấp API đơn giản để:
 *  - Lưu token sau khi đăng nhập/đăng ký
 *  - Đọc token khi cần gọi API bảo vệ
 *  - Xoá token khi logout
 */

// Extension DataStore gắn với Context
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_prefs"
)

class AuthPreferences(private val context: Context) {

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }

    /**
     * Lưu token và email vào DataStore
     */
    suspend fun saveToken(token: String, email: String? = null) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
            email?.let {
                prefs[KEY_USER_EMAIL] = it
            }
        }
    }

    /**
     * Lấy token dạng Flow<String?> để có thể observe nếu cần
     */
    val tokenFlow: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN]
    }

    /**
     * Lấy email dạng Flow<String?> để có thể observe nếu cần
     */
    val emailFlow: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    /**
     * Lấy token trực tiếp (synchronous) - dùng cho auto-login check
     */
    suspend fun getToken(): String? {
        return tokenFlow.first()
    }

    /**
     * Lấy email trực tiếp (synchronous) - dùng cho hiển thị
     */
    suspend fun getEmail(): String? {
        return emailFlow.first()
    }

    /**
     * Xoá token và email (dùng cho logout)
     */
    suspend fun clearToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
            prefs.remove(KEY_USER_EMAIL)
        }
    }
}


