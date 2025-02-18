@file:OptIn(ExperimentalMaterial3Api::class)

package de.digitalService.useID.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.BuildConfig
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.ButtonType
import de.digitalService.useID.ui.components.RegularBundButton
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.AccessibilityScreenDestination
import de.digitalService.useID.ui.screens.destinations.ImprintScreenDestination
import de.digitalService.useID.ui.screens.destinations.PrivacyScreenDestination
import de.digitalService.useID.ui.screens.destinations.TermsOfUseScreenDestination
import de.digitalService.useID.ui.theme.*
import javax.inject.Inject

@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen(viewModel: HomeScreenViewModelInterface = hiltViewModel<HomeScreenViewModel>()) {
    LaunchedEffect(Unit) {
        viewModel.homeScreenLaunched()
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Blue300)
        ) {
            Image(painter = painterResource(id = R.drawable.pin_brief), contentDescription = "")

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.homeScreen_titleBox_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Text(
                text = stringResource(R.string.homeScreen_titleBox_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 64.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.homeScreen_more_subtitle),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            SetupUseIdCardBox(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            MoreSettingsCardBox(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = Gray900,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SetupUseIdCardBox(viewModel: HomeScreenViewModelInterface) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Gray300),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.eids),
                contentScale = ContentScale.Inside,
                contentDescription = "",
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .padding(horizontal = 8.dp)
            )

            RegularBundButton(
                type = ButtonType.PRIMARY,
                onClick = viewModel::setupOnlineID,
                label = stringResource(R.string.homeScreen_setupOnlineID_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun MoreSettingsCardBox(viewModel: HomeScreenViewModelInterface) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Gray300),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CardButton(text = stringResource(R.string.homeScreen_more_license_button), onClick = {})
        StyledDivider()

        CardButton(
            text = stringResource(R.string.homeScreen_more_accessibilityStatement_button),
            onClick = viewModel::onAccessibilityButtonClicked
        )
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_termsOfUse_button), onClick = viewModel::onTermsOfUseButtonClicked)
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_imprint_button), onClick = viewModel::onImprintButtonClicked)
    }
}

@Composable
private fun StyledDivider() {
    Divider(
        color = Gray300,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

@Composable
private fun CardButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            color = Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

interface HomeScreenViewModelInterface {
    fun setupOnlineID()
    fun homeScreenLaunched()
    fun onPrivacyButtonClicked()
    fun onImprintButtonClicked()
    fun onAccessibilityButtonClicked()
    fun onTermsOfUseButtonClicked()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val appCoordinator: AppCoordinator,
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), HomeScreenViewModelInterface {
    override fun homeScreenLaunched() {
        appCoordinator.homeScreenLaunched(null)
    }

    override fun setupOnlineID() {
        setupCoordinator.startSetupIDCard()
    }

    override fun onPrivacyButtonClicked() {
        appCoordinator.navigate(PrivacyScreenDestination)
    }

    override fun onAccessibilityButtonClicked() {
        appCoordinator.navigate(AccessibilityScreenDestination)
    }

    override fun onTermsOfUseButtonClicked() {
        appCoordinator.navigate(TermsOfUseScreenDestination)
    }

    override fun onImprintButtonClicked() {
        appCoordinator.navigate(ImprintScreenDestination)
    }
}

private class PreviewViewModel : HomeScreenViewModelInterface {
    override fun setupOnlineID() {}
    override fun homeScreenLaunched() {}
    override fun onPrivacyButtonClicked() {}
    override fun onImprintButtonClicked() {}
    override fun onAccessibilityButtonClicked() {}
    override fun onTermsOfUseButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        HomeScreen(PreviewViewModel())
    }
}
