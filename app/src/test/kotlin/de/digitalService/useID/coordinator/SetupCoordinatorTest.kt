package de.digitalService.useID.coordinator

import de.digitalService.useID.pinstorage.PinStorageContract.PinStorage
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockPinStorage: PinStorage

    @Test
    fun startSetupIDCard() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.startSetupIDCard()

        verify(exactly = 1) { mockPinStorage.clear() }
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPINLetterDestination) }
    }

    @Test
    fun setupWithPINLetter() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.setupWithPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupTransportPINDestination) }
    }

    @Test
    fun setupWithoutPINLetter() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.setupWithoutPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupResetPersonalPINDestination) }
    }

    @Test
    fun onTransportPINEntered() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.onTransportPINEntered()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }
    }

    @Test
    fun onPersonalPINIntroFinished() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.onPersonalPINIntroFinished()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINDestination) }
    }

    @Test
    fun onPersonalPINEntered() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.onPersonalPINEntered()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupScanDestination) }
    }

    @Test
    fun onSettingPINSucceeded() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.onSettingPINSucceeded()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupFinishDestination) }
    }

    @Test
    fun onSetupFinished_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.onSetupFinished()

        verify(exactly = 1) { mockPinStorage.clear() }
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(any()) }
    }

    @Test
    fun onSetupFinished_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSetupFinished()

        verify(exactly = 1) { mockPinStorage.clear() }
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSetupFinished_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSetupFinished()
        setupCoordinator.onSetupFinished()

        verify(exactly = 2) { mockPinStorage.clear() }
        verify(exactly = 2) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSkipSetup_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)

        setupCoordinator.onSkipSetup()

        verify(exactly = 1) { mockPinStorage.clear() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
    }

    @Test
    fun onSkipSetup_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSkipSetup()

        verify(exactly = 1) { mockPinStorage.clear() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun onSkipSetup_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.onSkipSetup()
        setupCoordinator.onSkipSetup()

        verify(exactly = 2) { mockPinStorage.clear() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl) }
    }

    @Test
    fun cancelSetup() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.cancelSetup()

        verify(exactly = 1) { mockPinStorage.clear() }
        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(testUrl) }

        setupCoordinator.cancelSetup()

        verify(exactly = 2) { mockAppCoordinator.popToRoot() }
    }

    @Test
    fun hasToken() {
        val setupCoordinator = SetupCoordinator(mockAppCoordinator, mockPinStorage)
        val testUrl = "tokenUrl"

        Assertions.assertFalse(setupCoordinator.identificationPending())

        setupCoordinator.setTCTokenURL(testUrl)

        Assertions.assertTrue(setupCoordinator.identificationPending())
    }
}
