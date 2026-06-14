package com.priveat.app.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SQLiteDatabase
import java.security.SecureRandom

object DatabaseKeyProvider {
    private const val STORE_NAME = "priveat_secure_store"
    private const val DATABASE_KEY = "room_database_passphrase"

    fun databasePassphrase(context: Context): ByteArray {
        val prefs = encryptedPrefs(context)
        val existing = prefs.getString(DATABASE_KEY, null)
        val passphrase = existing ?: generatePassphrase().also {
            prefs.edit().putString(DATABASE_KEY, it).apply()
        }
        return SQLiteDatabase.getBytes(passphrase.toCharArray())
    }

    fun clear(context: Context) {
        encryptedPrefs(context).edit().clear().apply()
    }

    private fun encryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        STORE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun generatePassphrase(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
