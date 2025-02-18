package de.digitalService.useID.ui.screens.noNfc

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.Blue200
import de.digitalService.useID.ui.theme.Blue900
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun NoNfcScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            color = Blue200
        ) {
            Image(painter = painterResource(id = R.drawable.eids), contentDescription = "")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(38.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.noNfc_info_title),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.noNfc_info_body),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            val annotatedString = buildAnnotatedString {
                val str = stringResource(R.string.noNfc_moreInformation_link)
                append(str)
                addStyle(
                    style = SpanStyle(Blue900, fontWeight = FontWeight.Bold),
                    0,
                    str.length
                )
            }

            ClickableText(
                text = annotatedString,
                onClick = {},
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        NoNfcScreen()
    }
}
