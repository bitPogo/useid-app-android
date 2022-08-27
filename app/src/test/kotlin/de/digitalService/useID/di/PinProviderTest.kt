package de.digitalService.useID.di

import de.digitalService.useID.pinstorage.PinStorageContract
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PinProviderTest {
    @Test
    fun `It contains a EncryptedSharedPreferencesFactory`() {
        val factory: Any = PinProvider.provideEncryptedSharedPreferencesFactory()

        assertTrue {
            factory is PinStorageContract.EncryptedSharedPreferencesFactory
        }
    }

    @Test
    fun `It contains a PinStorage`() {
        val storage: Any = PinProvider.providePinStorage(
            context = mockk(relaxed = true),
            encryptedSharedPreferencesFactory = mockk(relaxed = true)
        )

        assertTrue {
            storage is PinStorageContract.PinStorage
        }
    }
}
