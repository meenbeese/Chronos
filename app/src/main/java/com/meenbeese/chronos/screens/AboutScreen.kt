package com.meenbeese.chronos.screens

import android.widget.Toast

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.CircleCropTransformation

import com.meenbeese.chronos.R

@Composable
fun AboutScreen(
    onOpenUrl: (String) -> Unit,
    onSendEmail: (String) -> Unit,
    version: String,
    year: Int
) {
    val context = LocalContext.current
    val textStyle16 = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
    val textStyle14 = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(R.mipmap.ic_launcher_round)
                    .transformations(CircleCropTransformation())
                    .build()
            ),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 48.dp)
        )

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )

        Text(
            text = stringResource(R.string.app_description),
            style = textStyle16,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )

        Text(
            text = stringResource(R.string.feature_list),
            style = textStyle16,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )

        Text(
            text = stringResource(R.string.made_with_love),
            style = textStyle16,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 44.dp)
        )

        Text(
            text = version,
            style = textStyle14,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.engage_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        ) {
            EngagementItem(
                iconRes = R.drawable.ic_github,
                label = stringResource(R.string.fork_github),
                color = MaterialTheme.colorScheme.primary
            ) {
                onOpenUrl("https://github.com/meenbeese/Chronos")
            }

            EngagementItem(
                iconRes = R.drawable.ic_link,
                label = stringResource(R.string.visit_website),
                color = MaterialTheme.colorScheme.primary
            ) {
                onOpenUrl("https://kuzey.is-a.dev")
            }

            EngagementItem(
                iconRes = R.drawable.ic_email,
                label = stringResource(R.string.send_email),
                color = MaterialTheme.colorScheme.primary
            ) {
                onSendEmail("kuzeybilgin@proton.me")
            }
        }

        EngagementItem(
            iconRes = R.drawable.ic_copyright,
            label = stringResource(R.string.copyright_info, year),
            color = Color.Unspecified
        ) {
            Toast.makeText(context, context.getString(R.string.copyright_info, year), Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun EngagementItem(
    @DrawableRes iconRes: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(top = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = color,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
