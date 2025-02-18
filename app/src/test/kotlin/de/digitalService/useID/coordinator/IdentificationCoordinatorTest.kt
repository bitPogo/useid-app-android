package de.digitalService.useID.coordinator

import android.content.Context
import android.net.Uri
import android.util.Base64
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPINDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationScanDestination
import de.digitalService.useID.ui.screens.identification.FetchMetadataEvent
import de.digitalService.useID.ui.screens.identification.ScanEvent
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class IdentificationCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIDCardManager: IDCardManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @MockK(relaxUnitFun = true)
    lateinit var mockStorageManager: StorageManagerType

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        mockkStatic(Uri::class)

        val mockUriBuilder = mockk<Uri.Builder>()

        mockkConstructor(Uri.Builder::class)

        every {
            anyConstructed<Uri.Builder>()
                .scheme("http")
                .encodedAuthority("127.0.0.1:24727")
                .appendPath("eID-Client")
                .appendQueryParameter("tcTokenURL", testTokenURL)
        } returns mockUriBuilder

        val mockedUri = mockk<Uri>()

        every { mockUriBuilder.build() } returns mockedUri

        every { mockedUri.toString() } returns testURL
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val testTokenURL = "https://token"
    private val testURL = "eid://127.0.0.1/eID-Client?tokenURL="

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ProcessCompletedSuccessfully() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testRequestAuthenticationRequestConfirmation = EIDInteractionEvent.RequestAuthenticationRequestConfirmation(
            EIDAuthenticationRequest(
                "",
                "",
                "subject",
                "",
                "",
                AuthenticationTerms.Text(""),
                "",
                mapOf()
            )
        ) {}
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val scanResults = mutableListOf<ScanEvent>()
        val scanJob = identificationCoordinator.scanEventFlow
            .onEach(scanResults::add)
            .launchIn(CoroutineScope(dispatcher))

        val fetchResults = mutableListOf<FetchMetadataEvent>()
        val fetchJob = identificationCoordinator.fetchMetadataEventFlow
            .onEach(fetchResults::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.CardRequested, scanResults.get(0))
        Assertions.assertEquals(FetchMetadataEvent.Started, fetchResults.get(0))

        testFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()

        Assertions.assertEquals(1, scanResults.size)
        Assertions.assertEquals(2, fetchResults.size)
        Assertions.assertEquals(FetchMetadataEvent.Finished, fetchResults.get(1))
        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        testFlow.value = EIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Finished, scanResults.get(1))

        scanJob.cancel()
        fetchJob.cancel()
        verify(exactly = 2) { mockAppCoordinator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ProcessCompletedSuccessfully_WithoutRequest() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Finished, results.get(1))

        job.cancel()
        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_SendEventBeforeListening_SkipFirstCardRequestedEvent() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        testFlow.value = EIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Finished, results.get(0))

        job.cancel()
        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun startIdentificationProcess_RequestPIN_WithAttempts(testValue: Int) = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.RequestPIN(testValue) {}
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.IncorrectPIN(attempts = testValue)), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestPIN_WithoutAttempts() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        testFlow.value = EIDInteractionEvent.RequestPIN(null) {}
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationPersonalPINDestination) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestCAN_Error() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.RequestCAN {}
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.PINSuspended), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestPINAndCAN_Error() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.RequestPINAndCAN { _, _ -> }
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.PINSuspended), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestPUK_Error() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.RequestPUK {}
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.PINBlocked), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestCardInsertion() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)
        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationScanDestination) }
        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestCardInsertion_WithReachedScanState() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        testFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.CardAttached, results.get(1))

        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CardDeactivatedException() = runTest {
        val testFlow: Flow<EIDInteractionEvent> = flow {
            throw IDCardInteractionException.CardDeactivated
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.CardDeactivated), results.get(0))

        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CardBlockedException() = runTest {
        val testFlow: Flow<EIDInteractionEvent> = flow {
            throw IDCardInteractionException.CardBlocked
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.CardBlocked), results.get(0))

        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_NullPointerException() = runTest {
        val testFlow: Flow<EIDInteractionEvent> = flow {
            throw NullPointerException()
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        val scanResults = mutableListOf<ScanEvent>()
        val scanJob = identificationCoordinator.scanEventFlow
            .onEach(scanResults::add)
            .launchIn(CoroutineScope(dispatcher))

        val fetchResults = mutableListOf<FetchMetadataEvent>()
        val fetchJob = identificationCoordinator.fetchMetadataEventFlow
            .onEach(fetchResults::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.Other(null)), scanResults.get(0))
        Assertions.assertEquals(FetchMetadataEvent.Error, fetchResults.get(0))

        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        scanJob.cancel()
        fetchJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onPINEntered_WithPinCallback() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        var didCallCallback = false
        testFlow.value = EIDInteractionEvent.RequestPIN(null) {
            didCallCallback = true
        }

        advanceUntilIdle()

        identificationCoordinator.onPINEntered("testPin")

        Assertions.assertTrue(didCallCallback)

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationPersonalPINDestination) }
        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onPINEntered_CalledTwice() = runTest {
        val testFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL)

        var callbackCalledCount = 0
        testFlow.value = EIDInteractionEvent.RequestPIN(null) {
            callbackCalledCount++
        }

        advanceUntilIdle()

        identificationCoordinator.onPINEntered("testPin1")
        identificationCoordinator.onPINEntered("testPin2")

        Assertions.assertEquals(1, callbackCalledCount)

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationPersonalPINDestination) }
    }

    @Test
    fun finishIdentification(){
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.finishIdentification()

        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
    }

    @Test
    fun cancelIdentification(){
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockCoroutineContextProvider,
            mockStorageManager
        )

        identificationCoordinator.cancelIdentification()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
    }
}
