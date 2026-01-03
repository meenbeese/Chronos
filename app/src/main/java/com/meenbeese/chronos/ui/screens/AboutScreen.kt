package com.meenbeese.chronos.ui.screens

import android.graphics.Paint
import android.widget.Toast

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.CircleCropTransformation

import com.meenbeese.chronos.R

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlin.random.Random

@Composable
fun AboutScreen(
    onOpenUrl: (String) -> Unit,
    onSendEmail: (String) -> Unit,
    version: String,
    year: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showHearts = remember { mutableStateOf(false) }
    val textStyle16 = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
    val textStyle14 = MaterialTheme.typography.bodySmall.copy(
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
    val copyrightText = stringResource(R.string.copyright_info, year)
    val madeWithLove = stringResource(R.string.made_with_love)
    val (beforeHeart, afterHeart) = remember(madeWithLove) {
        val parts = madeWithLove.split("❤️", limit = 2)
        parts.getOrElse(0) { "" } to parts.getOrElse(1) { "" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                .size(160.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 64.dp)
        )

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 44.dp)
        ) {
            Text(
                text = beforeHeart,
                style = textStyle16,
            )
            Text(
                text = "❤️",
                style = textStyle16,
                modifier = Modifier.clickable {
                    showHearts.value = true
                    scope.launch {
                        delay(3000)
                        showHearts.value = false
                    }
                }
            )
            Text(
                text = afterHeart,
                style = textStyle16,
            )
        }

        Text(
            text = version,
            style = textStyle14,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.engage_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 44.dp)
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
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Toast.makeText(context, copyrightText, Toast.LENGTH_SHORT).show()
        }
    }

    AnimatedVisibility(
        visible = showHearts.value,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -100 }),
        exit = fadeOut(),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        FallingHeartsOverlay()
    }
}

@Composable
fun EngagementItem(
    @DrawableRes iconRes: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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

@Composable
fun FallingHeartsOverlay() {
    val heartCount = 100

    val hearts = remember {
        List(heartCount) {
            mutableStateOf(
                HeartState(
                    x = Math.random().toFloat(),
                    scale = (0.6f..1.2f).random(),
                    rotation = (-25f..25f).random(),
                    duration = (2500..4500).random()
                )
            )
        }
    }

    val animatables = remember {
        hearts.map { Animatable(0f) }
    }

    hearts.forEachIndexed { index, state ->
        LaunchedEffect(index) {
            delay((0L..1500L).random())
            animatables[index].animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = state.value.duration,
                    easing = FastOutSlowInEasing
                )
            )
            animatables[index].snapTo(0f)
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        hearts.forEachIndexed { index, state ->
            val heart = state.value
            drawContext.canvas.nativeCanvas.apply {
                save()
                translate(
                    heart.x * size.width,
                    animatables[index].value * size.height
                )
                rotate(heart.rotation)
                scale(heart.scale, heart.scale)
                drawText(
                    "❤️",
                    0f,
                    0f,
                    Paint().apply { textSize = 48f }
                )
                restore()
            }
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    Random.nextFloat() * (endInclusive - start) + start

private data class HeartState(
    val x: Float,
    val scale: Float,
    val rotation: Float,
    val duration: Int,
)
