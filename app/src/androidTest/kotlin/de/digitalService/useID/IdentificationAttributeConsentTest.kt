package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.identification.IdentificationAttributeConsent
import de.digitalService.useID.ui.screens.identification.IdentificationAttributeConsentViewModel
import de.digitalService.useID.ui.screens.identification.ProviderInfoDialogContent
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationAttributeConsentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun correctUsage() {
        val viewModel: IdentificationAttributeConsentViewModel = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.idCardAttribute_DG01,
            R.string.idCardAttribute_DG02,
            R.string.idCardAttribute_DG03,
            R.string.idCardAttribute_DG04,
            R.string.idCardAttribute_DG05,
        )

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes
        every { viewModel.shouldShowInfoDialog } returns false

        composeTestRule.activity.setContent {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        testRequiredReadAttributes.forEach { testId ->
            val attributeText = composeTestRule.activity.getString(testId)
            composeTestRule.onNodeWithText(attributeText, substring = true).assertIsDisplayed()
        }

        val buttonText = composeTestRule.activity.getString(R.string.identification_attributeConsent_pinButton)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { viewModel.onPINButtonTapped() }
    }


    @Test
    fun dialogTest() {
        val viewModel: IdentificationAttributeConsentViewModel = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.idCardAttribute_DG01,
            R.string.idCardAttribute_DG02,
            R.string.idCardAttribute_DG03,
            R.string.idCardAttribute_DG04,
            R.string.idCardAttribute_DG05,
        )

        val testIssue = "ISSUE"
        val testIssueUrl = "ISSUE_URL"
        val testSubject = "SUBJECT"
        val testSubjectUrl = "SUBJECT_URL"
        val testTerms = "TERMS"

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes

        every { viewModel.shouldShowInfoDialog } returns true
        every { viewModel.infoDialogContent } returns ProviderInfoDialogContent(
            testIssue,
            testIssueUrl,
            testSubject,
            testSubjectUrl,
            testTerms,
        )

        composeTestRule.activity.setContent {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        testRequiredReadAttributes.forEach { testId ->
            val attributeText = composeTestRule.activity.getString(testId)
            composeTestRule.onNodeWithText(attributeText, substring = true).assertIsDisplayed()
        }

        composeTestRule.onNodeWithText(testIssue).assertIsDisplayed()
        composeTestRule.onNodeWithText(testIssueUrl).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(testSubject).assertCountEquals(2)
        composeTestRule.onNodeWithText(testSubjectUrl).assertIsDisplayed()
        composeTestRule.onNodeWithText(testTerms).assertIsDisplayed()

        val cancelButton = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithContentDescription(cancelButton).performClick()

        verify(exactly = 1) { viewModel.onInfoDialogDismissalRequest() }
    }
}
