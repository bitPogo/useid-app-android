package de.digitalService.useID.pinstorage

import android.content.Context
import android.content.SharedPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import de.digitalService.useID.pinstorage.PinStorageContract.PIN_FLAVOUR
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals

class PinStorageTest {
    private val fixture = kotlinFixture()
    private val sharedPreferences: SharedPreferences = mockk()
    private val context: Context = mockk()

    @BeforeEach
    fun setUp() {
        clearMocks(sharedPreferences, context)
    }

    @Test
    fun `It binds a TransportPin`() {
        // Given
        val pin: String? = fixture.fixture()

        every { sharedPreferences.getString(any(), any()) } returns pin

        // When
        val actual = PinStorage.PinStorageFactory { sharedPreferences }
            .getInstance(context)
            .transportPin

        // Then
        assertEquals(
            pin,
            actual,
        )
        verify(exactly = 1) {
            sharedPreferences.getString(PIN_FLAVOUR.TRANSPORT.name, null)
        }
    }

    @Test
    fun `It saves a given TransportPin`() {
        // Given
        val pin: String? = fixture.fixture()
        val editor: SharedPreferences.Editor = mockk(relaxUnitFun = true)

        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        // When
        PinStorage.PinStorageFactory { sharedPreferences }
            .getInstance(context)
            .transportPin = pin

        // Then
        verifyOrder {
            editor.putString(PIN_FLAVOUR.TRANSPORT.name, pin)
            editor.apply()
        }
    }

    @Test
    fun `It binds a PersonalPin`() {
        // Given
        val pin: String = fixture.fixture()

        every { sharedPreferences.getString(any(), any()) } returns pin

        // When
        val actual = PinStorage.PinStorageFactory { sharedPreferences }
            .getInstance(context)
            .personalPin

        // Then
        assertEquals(
            pin,
            actual,
        )
        verify(exactly = 1) {
            sharedPreferences.getString(PIN_FLAVOUR.PERSONAL.name, null)
        }
    }

    @Test
    fun `It saves a given PersonalPin`() {
        // Given
        val pin: String = fixture.fixture()
        val editor: SharedPreferences.Editor = mockk(relaxUnitFun = true)

        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        // When
        PinStorage.PinStorageFactory { sharedPreferences }
            .getInstance(context)
            .personalPin = pin

        // Then
        verifyOrder {
            editor.putString(PIN_FLAVOUR.PERSONAL.name, pin)
            editor.apply()
        }
    }

    @Test
    fun `Given clear is called it removes all pins`() {
        // Given
        val editor: SharedPreferences.Editor = mockk(relaxUnitFun = true)

        every { sharedPreferences.edit() } returns editor
        every { editor.clear() } returns editor

        // When
        PinStorage.PinStorageFactory { sharedPreferences }
            .getInstance(context)
            .clear()

        // Then
        verifyOrder {
            editor.clear()
            editor.apply()
        }
    }
}
