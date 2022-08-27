package de.digitalService.useID.pinstorage

import android.content.Context
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import de.digitalService.useID.pinstorage.PinStorageContract.SecuredSharedPreferencesFactory
import de.digitalService.useID.pinstorage.PinStorageContract.PinStorage
import de.digitalService.useID.pinstorage.PinStorage.PinStorageFactory
import io.mockk.every
import io.mockk.verify

class PinStorageFactoryTest {
    @Test
    fun `It fulfils PinStorageFactory`() {
        val factory: Any = PinStorageFactory(mockk())

        assertTrue {
            factory is PinStorageContract.PinStorageFactory
        }
    }

    @Test
    fun `Given getInstance is called with an Context it returns a PinStorage`() {
        // Given
        val securedSharedPreferencesFactory: SecuredSharedPreferencesFactory = mockk()
        val context: Context = mockk()

        every { securedSharedPreferencesFactory.getInstance(any()) } returns mockk()

        // When
        val actualPinStorage: Any = PinStorageFactory(securedSharedPreferencesFactory).getInstance(context)

        // Then
        assertTrue {
            actualPinStorage is PinStorage
        }

        verify(exactly = 1) {
            securedSharedPreferencesFactory.getInstance(context)
        }
    }
}
