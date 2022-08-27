package de.digitalService.useID.pinstorage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.digitalService.useID.pinstorage.PinStorageContract
import de.digitalService.useID.pinstorage.SecuredSharedPreferencesFactory
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecuredSharedPreferencesFactoryTest {
    @Test
    fun `It fulfils SecuredSharedPreferencesFactory`()  {
        val factory: Any = SecuredSharedPreferencesFactory

        assertTrue {
            factory is PinStorageContract.SecuredSharedPreferencesFactory
        }
    }

    @Test
    fun `Given getInstance is called with an Context it returns a SharedPreferences Storage`() {
        // setup
        mockkStatic(EncryptedSharedPreferences::class)
        mockkConstructor(MasterKey.Builder::class)

        // Given
        val context: Context = mockk(relaxed = true)
        val masterKey: MasterKey = mockk()
        val expectedPreferences: SharedPreferences = mockk()

        every {
            anyConstructed<MasterKey.Builder>().setKeyScheme(any()).build()
        } returns masterKey
        every {
            EncryptedSharedPreferences.create(any(), any(), any<MasterKey>(), any(), any())
        } returns expectedPreferences

        // When
        val actualPreferences = SecuredSharedPreferencesFactory.getInstance(context)

        // Then
        assertSame(
            actualPreferences,
            expectedPreferences,
        )

        verify(exactly = 1) {
            EncryptedSharedPreferences.create(
                context,
                any(),
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        verify(exactly = 1) {
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        }

        // tear down
        unmockkStatic(EncryptedSharedPreferences::class)
        unmockkConstructor(MasterKey.Builder::class)
    }
}
