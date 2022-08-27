package de.digitalService.useID.pinstorage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.digitalService.useID.pinstorage.PinStorageContract.Companion.PREFERENCE_FILE_NAME
import de.digitalService.useID.pinstorage.PinStorageContract.Companion.preferenceKeyEncryptionSchema
import de.digitalService.useID.pinstorage.PinStorageContract.Companion.preferenceValueEncryptionSchema

object SecuredSharedPreferencesFactory : PinStorageContract.SecuredSharedPreferencesFactory {
    private fun resolveMasterKey(
        context: Context
    ): MasterKey =  MasterKey.Builder(context).setKeyScheme(PinStorageContract.masterKeySchema).build()

    override fun getInstance(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFERENCE_FILE_NAME,
            resolveMasterKey(context),
            preferenceKeyEncryptionSchema,
            preferenceValueEncryptionSchema
        )
    }

}
