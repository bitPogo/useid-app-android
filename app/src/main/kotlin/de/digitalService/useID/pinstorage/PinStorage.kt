package de.digitalService.useID.pinstorage

import android.content.Context
import android.content.SharedPreferences
import de.digitalService.useID.pinstorage.PinStorageContract.EncryptedSharedPreferencesFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import de.digitalService.useID.pinstorage.PinStorageContract.PIN_FLAVOUR

class PinStorage private constructor(
    private val sharedPreferences: SharedPreferences
) : PinStorageContract.PinStorage {
    private val lock = ReentrantLock()

    private fun load(
        key: PIN_FLAVOUR
    ): String? = sharedPreferences.getString(key.name, null)

    private fun save(
        key: PIN_FLAVOUR,
        value: String?, // CharArray would be better
    ) {
        lock.withLock {
            sharedPreferences
                .edit()
                .putString(key.name, value)
                .apply()
        }
    }

    override var transportPin: String?
        get() = load(PIN_FLAVOUR.TRANSPORT)
        set(value) {
            save(PIN_FLAVOUR.TRANSPORT, value)
        }

    override var personalPin: String?
        get() = load(PIN_FLAVOUR.PERSONAL)
        set(value) {
            save(PIN_FLAVOUR.PERSONAL, value)
        }

    override fun clear() {
        lock.withLock {
            sharedPreferences
                .edit()
                .clear()
                .apply()
        }
    }

    class PinStorageFactory(
        private val sharedPreferencesFactory: EncryptedSharedPreferencesFactory
    ): PinStorageContract.PinStorageFactory {
        override fun getInstance(context: Context): PinStorageContract.PinStorage {
            return PinStorage(
                sharedPreferencesFactory.getInstance(context)
            )
        }
    }
}
