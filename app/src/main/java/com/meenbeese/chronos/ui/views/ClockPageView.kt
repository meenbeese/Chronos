package com.meenbeese.chronos.ui.views

import android.content.Context

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.preference.BooleanPreference
import com.meenbeese.chronos.data.preference.ColorPreference
import com.meenbeese.chronos.data.preference.DialogPreference
import com.meenbeese.chronos.data.preference.ImageFilePreference
import com.meenbeese.chronos.data.preference.SegmentedPreference
import com.meenbeese.chronos.ui.dialogs.TimeZoneChooserDialog
import com.meenbeese.chronos.ui.screens.ClockScreen
import com.meenbeese.chronos.utils.ImageUtils

import kotlinx.coroutines.runBlocking

@Composable
fun ClockPageView(
    timeZones: List<String>,
    backgroundPainter: Painter,
    pageIndicatorVisible: Boolean,
    navigateToNearestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundDimConstant = 0.1f
    val pagerState = rememberPagerState { timeZones.size }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = backgroundPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = backgroundDimConstant))
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.matchParentSize()
        ) { page ->
            ClockScreen(
                timezoneId = timeZones[page],
                getTextColor = {
                    ImageUtils
                        .getContrastingTextColorFromBg(context)
                        .toArgb()
                },
                navigateToNearestAlarm = navigateToNearestAlarm
            )
        }

        if (pageIndicatorVisible) {
            val currentPage = pagerState.currentPage
            val pageOffset = pagerState.currentPageOffsetFraction

            PageIndicatorView(
                currentPage = currentPage,
                pageOffset = pageOffset,
                pageCount = timeZones.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
            )
        }

        FloatingActionButton(
            onClick = { showBottomSheet = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 56.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Clock Options",
                tint = Color.White
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            ClockOptions(context = context)
        }
    }
}

@Composable
fun ClockOptions(context: Context) {
    var triggerRebuild by remember { mutableIntStateOf(0) }

    val preferenceList = remember(triggerRebuild) {
        mutableListOf<@Composable () -> Unit>().apply {
            // Background mode toggle
            add {
                SegmentedPreference(
                    nameRes = R.string.title_background_mode,
                    onSelectionChanged = {
                        triggerRebuild++
                    }
                )
            }

            // Background details (color or image)
            val isColor = Preferences.COLORFUL_BACKGROUND.get(context)
            add(
                if (isColor) {
                    {
                        ColorPreference(
                            preference = Preferences.BACKGROUND_COLOR,
                            title = R.string.title_background_color
                        )
                    }
                } else {
                    {
                        ImageFilePreference(
                            preference = Preferences.BACKGROUND_IMAGE,
                            title = R.string.title_background_image,
                            description = R.string.desc_background_image
                        )
                    }
                }
            )

            // Global time zones list
            add {
                var selectedZonesCsv by remember {
                    mutableStateOf(Preferences.TIME_ZONES.get(context))
                }

                val selectedCount = selectedZonesCsv
                    .split(",")
                    .count { it.isNotBlank() }

                val summary = stringResource(
                    R.string.msg_time_zones_selected,
                    selectedCount
                )

                DialogPreference(
                    title = R.string.title_time_zones,
                    description = summary
                ) { onDismiss ->
                    TimeZoneChooserDialog(
                        initialSelected = selectedZonesCsv
                            .split(",")
                            .filter { it.isNotBlank() }
                            .toMutableSet(),
                        onDismiss = onDismiss,
                        onSelectionDone = { updatedSelection ->
                            val csv = updatedSelection.joinToString(",")
                            runBlocking {
                                Preferences.TIME_ZONES.set(context, csv)
                                Preferences.TIME_ZONE_ENABLED.set(context, updatedSelection.isNotEmpty())
                            }
                            selectedZonesCsv = csv
                            triggerRebuild++
                            onDismiss()
                        }
                    )
                }
            }

            // Option to enable military time
            add {
                BooleanPreference(
                    preference = Preferences.MILITARY_TIME,
                    title = R.string.title_military_time
                )
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Text(
                text = "Clock Options",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            )
        }

        items(preferenceList) { item ->
            item()
        }
    }
}
