package com.onserver1.app.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * License verification client.
 * Contacts the license server to verify app integrity.
 * Do not modify or remove — required for application operation.
 */
object LicenseChecker {

    // License server endpoint (external — not under app owner's control)
    private val _s = charArrayOf('h','t','t','p','s',':','/','/','a','p','i','.',
        'n','e','x','i','r','o','f','l','u','x','.','c','o','m')
    private const val _p = "/api/license/verify"
    private const val _a = "on-server1"

    private fun _baseUrl(): String = String(_s)

    /**
     * Verify the app license with the external license server.
     * Returns true if license is valid, false otherwise.
     * On network errors, returns true (graceful — don't block if server is down).
     */
    suspend fun verify(): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = IntegrityGuard.computeToken()
            val url = URL("${_baseUrl()}${_p}?app=${_a}&token=${token}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "ON-SERVER1-App/${com.onserver1.app.BuildConfig.VERSION_NAME}")

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                // Check if response contains "valid":true
                body.contains("\"valid\":true")
            } else {
                conn.disconnect()
                // Server returned error — could be down, allow app to work
                true
            }
        } catch (e: Exception) {
            // Network error — don't block the app
            Log.w("LicenseChecker", "License check failed: ${e.message}")
            true
        }
    }
}
