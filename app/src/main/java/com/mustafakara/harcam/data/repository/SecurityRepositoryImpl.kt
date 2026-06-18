package com.mustafakara.harcam.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mustafakara.harcam.domain.repository.SecurityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-lock security backed by EncryptedSharedPreferences — architecture.md §5, design.md §11.
 * Only a salted hash of the PIN is stored, never the PIN itself.
 */
@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : SecurityRepository {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "harcam_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun isLockEnabled(): Boolean = prefs.contains(KEY_PIN_HASH)

    override suspend fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == hash(pin)
    }

    override suspend fun disableLock() {
        prefs.edit().remove(KEY_PIN_HASH).remove(KEY_BIOMETRIC).apply()
    }

    override fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC, false)

    override suspend fun setBiometricEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest("$SALT$pin".toByteArray()).joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val SALT = "harcam.v1."
    }
}
