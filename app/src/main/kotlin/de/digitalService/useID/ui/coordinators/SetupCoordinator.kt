package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.pinstorage.PinStorageContract
import de.digitalService.useID.ui.screens.destinations.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(
    private val appCoordinator: AppCoordinator,
    private val pinStorage: PinStorageContract.PinStorage
) {
    private var tcTokenURL: String? = null

    fun setTCTokenURL(tcTokenURL: String) {
        this.tcTokenURL = tcTokenURL
    }

    fun identificationPending(): Boolean {
        return this.tcTokenURL != null
    }

    fun startSetupIDCard() {
        pinStorage.clear()
        appCoordinator.navigate(SetupPINLetterDestination)
    }

    fun setupWithPINLetter() {
        appCoordinator.navigate(SetupTransportPINDestination)
    }

    fun setupWithoutPINLetter() {
        appCoordinator.navigate(SetupResetPersonalPINDestination)
    }

    fun onTransportPINEntered() {
        appCoordinator.navigate(SetupPersonalPINIntroDestination)
    }

    fun onPersonalPINIntroFinished() {
        appCoordinator.navigate(SetupPersonalPINDestination)
    }

    fun onPersonalPINEntered() {
        appCoordinator.navigate(SetupScanDestination)
    }

    fun onSettingPINSucceeded() {
        appCoordinator.navigate(SetupFinishDestination)
    }

    fun onSetupFinished() {
        appCoordinator.setIsNotFirstTimeUser()
        handleSetupEnded()
    }

    fun onBackToHome() {
        appCoordinator.popToRoot()
    }

    fun onSkipSetup() {
        handleSetupEnded()
    }

    fun cancelSetup() {
        pinStorage.clear()
        appCoordinator.popToRoot()
        tcTokenURL = null
    }

    private fun handleSetupEnded() {
        pinStorage.clear()

        tcTokenURL?.let {
            appCoordinator.startIdentification(it)
            tcTokenURL = null
        } ?: run {
            appCoordinator.popToRoot()
        }
    }
}
