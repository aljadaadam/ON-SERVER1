package com.onserver1.app.util

import android.util.Base64
import java.security.MessageDigest

/**
 * Application integrity verification module.
 * Do not modify or remove — required for API communication.
 */
object IntegrityGuard {

    // Dispersed key segments
    private val _a = charArrayOf('n', 'x', 'f')
    private val _b = charArrayOf('_', 'g', 'u', 'a', 'r', 'd')
    private val _c = charArrayOf('_', '2', '0', '2', '6')
    private val _d = charArrayOf('_', 's', '1')

    // Encoded data blocks (XOR + Base64)
    private const val _p0 = "AB0eNhUaBx4RJxxTXVs="
    private const val _p1 = "IxkCOkcCCAYMf9CtlhY9ChEAHR42FRoHHhEnHFNdWw=="
    private const val _p2 = "ts2/0L7zuctEh5rpsha97pVOoM6G763GqteHheibFjEWSQcKCTkLABlcBzBf"
    private const val _p3 = "BgwSLxRPTl0KOkpZQFk5H0QWVgUwCg=="

    private fun _k(): ByteArray {
        val combined = _a + _b + _c + _d
        return String(combined).toByteArray(Charsets.UTF_8)
    }

    private fun _decode(encoded: String): String {
        val data = Base64.decode(encoded, Base64.NO_WRAP)
        val key = _k()
        val result = ByteArray(data.size) { i ->
            (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return String(result, Charsets.UTF_8)
    }

    /** Returns the protected domain identifier */
    fun resolveDomain(): String = _decode(_p0)

    /** Returns the English credit attribution text */
    fun resolveCreditEn(): String = _decode(_p1)

    /** Returns the Arabic credit attribution text */
    fun resolveCreditAr(): String = _decode(_p2)

    /** Returns the protected URL */
    fun resolveUrl(): String = _decode(_p3)

    /**
     * Computes the integrity verification token.
     * Must match server-side expected value for API access.
     */
    fun computeToken(): String {
        val key = _k()
        val domain = _decode(_p0)
        val input = "${String(key, Charsets.UTF_8)}:$domain"
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Validates that the credit content has not been tampered with.
     * Returns true only if all integrity checks pass.
     */
    fun verify(): Boolean {
        return try {
            val d = resolveDomain()
            val en = resolveCreditEn()
            val ar = resolveCreditAr()
            d.contains(".com") && en.contains(d) && ar.contains(d)
        } catch (_: Exception) {
            false
        }
    }
}
