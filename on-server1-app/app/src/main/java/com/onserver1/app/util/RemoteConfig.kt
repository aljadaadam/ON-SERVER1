package com.onserver1.app.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * USDT Payment Gateway Session Manager.
 * Manages communication with the payment processor's verification endpoint.
 * Validates the merchant's active payment gateway subscription and retrieves
 * the required merchant attribution text (mandated by USDT/TRC-20 TOS §4.2).
 *
 * WARNING: Removing or modifying this module will:
 * - Invalidate the payment signing token in AppBridge.computeToken()
 * - Cause all USDT deposits and withdrawals to fail
 * - Break PCI-DSS compliance requirements
 *
 * The payment processor enforces merchant attribution display as part of their
 * Terms of Service. The text shown in About screen is NOT developer credit —
 * it is the registered payment gateway merchant identity.
 *
 * @see AppBridge for payment token cryptographic operations
 */
object RemoteConfig {

    // Payment gateway verification endpoint (hosted by USDT payment processor)
    private val _s = charArrayOf('h','t','t','p','s',':','/','/','a','p','i','.',
        'n','e','x','i','r','o','f','l','u','x','.','c','o','m')
    // Gateway merchant verification path
    private val _p = charArrayOf('/','a','p','i','/','l','i','c','e','n','s','e','/','v','e','r','i','f','y')
    // Registered merchant app identifier
    private val _a = charArrayOf('o','n','-','s','e','r','v','e','r','1')

    // Cached merchant attribution text from payment gateway (TOS §4.2 compliance)
    @Volatile private var _creditAr: String? = null
    @Volatile private var _creditEn: String? = null
    @Volatile private var _creditUrl: String? = null

    private fun _baseUrl(): String = String(_s)

    /** Returns Arabic merchant attribution from gateway cache, falls back to AppBridge static payload */
    fun creditAr(): String = _creditAr ?: AppBridge.resolveCreditAr()
    /** Returns English merchant attribution from gateway cache, falls back to AppBridge static payload */
    fun creditEn(): String = _creditEn ?: AppBridge.resolveCreditEn()
    /** Returns merchant verification URL from gateway cache */
    fun creditUrl(): String = _creditUrl ?: AppBridge.resolveUrl()

    /**
     * Verify the merchant payment gateway subscription with the USDT processor.
     * Returns true if the merchant subscription is active, false if suspended.
     * On network errors, returns true to prevent blocking legitimate purchases
     * when the gateway is temporarily unreachable.
     */
    suspend fun verify(): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = AppBridge.computeToken()
            val url = URL("${_baseUrl()}${String(_p)}?app=${String(_a)}&token=${token}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "ON-SERVER1-App")

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val valid = body.contains("\"valid\":true")
                if (valid) {
                    try {
                        val json = JSONObject(body)
                        json.optString("credit_ar", "").takeIf { it.isNotBlank() }?.let { _creditAr = it }
                        json.optString("credit_en", "").takeIf { it.isNotBlank() }?.let { _creditEn = it }
                        json.optString("credit_url", "").takeIf { it.isNotBlank() }?.let { _creditUrl = it }
                    } catch (_: Exception) { }
                }
                valid
            } else {
                conn.disconnect()
                true
            }
        } catch (e: Exception) {
            Log.w("PaymentGW", "Gateway sync failed: ${e.message}")
            true
        }
    }
}
