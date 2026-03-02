package com.onserver1.app.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    fun getAccessToken(): String? {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[ACCESS_TOKEN_KEY]
            }.first()
        }
    }

    fun getRefreshToken(): String? {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[REFRESH_TOKEN_KEY]
            }.first()
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    fun getUserId(): String? {
        return runBlocking {
            context.dataStore.data.map { prefs ->
                prefs[USER_ID_KEY]
            }.first()
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
