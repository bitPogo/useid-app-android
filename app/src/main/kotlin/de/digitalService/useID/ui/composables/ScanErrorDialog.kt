package de.digitalService.useID.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.theme.UseIDTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanErrorAlertDialog(error: ScanError, onButtonTap: () -> Unit) {
    StandardDialog(
        title = { Text(stringResource(id = error.titleResID), style = MaterialTheme.typography.titleMedium) },
        text = { MarkdownText(markdown = stringResource(id = error.textResID), fontResource = R.font.bundes_sans_dtp_regular) },
        onButtonTap = onButtonTap
    )
}

@Preview
@Composable
private fun Preview() {
    UseIDTheme {
        ScanErrorAlertDialog(error = ScanError.PINBlocked, onButtonTap = { })
    }
}
