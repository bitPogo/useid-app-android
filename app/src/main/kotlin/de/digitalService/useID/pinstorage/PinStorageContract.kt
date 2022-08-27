package de.digitalService.useID.pinstorage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.digitalService.useID.BuildConfig

interface PinStorageContract {
    fun interface SecuredSharedPreferencesFactory {
        fun getInstance(context: Context): SharedPreferences
    }

    fun interface PinStorageFactory {
        fun getInstance(context: Context): PinStorage
    }

    interface PinStorage {
        var transportPin: String?
        var personalPin: String?

        fun clear()
    }


    companion object {
        const val PREFERENCE_FILE_NAME = BuildConfig.APPLICATION_ID + ".encryptedSharedPreferences"
        val masterKeySchema = MasterKey.KeyScheme.AES256_GCM
        val preferenceKeyEncryptionSchema = EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
        val preferenceValueEncryptionSchema = EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM

    }

    enum class PIN_FLAVOUR {
        TRANSPORT,
        PERSONAL
    }
}
