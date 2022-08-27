package de.digitalService.useID

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecuredSharedPreferencesFactorySpec {
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
        val expected: SharedPreferences = mockk()

        every {
            anyConstructed<MasterKey.Builder>().setKeyScheme(any()).build()
        } returns masterKey
        every {
            EncryptedSharedPreferences.create(any(), any(), any<MasterKey>(), any(), any())
        } returns expected

        // When
        val actual = SecuredSharedPreferencesFactory.getInstance(context)

        // Then
        assertSame(
            actual,
            expected,
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
