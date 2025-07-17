package com.meenbeese.chronos.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.launch

@Composable
fun HomeBottomSheet(
    tabs: List<String>,
    initialTabIndex: Int,
    onTabChanged: (Int) -> Unit,
    heightFraction: Float = 0.5f,
    isTablet: Boolean = false,
    pagerContent: @Composable (Int) -> Unit,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val peekHeight = screenHeightDp * heightFraction

    val pagerState = rememberPagerState(
        initialPage = initialTabIndex,
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()
    val currentTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    LaunchedEffect(currentTabIndex) {
        onTabChanged(currentTabIndex)
    }

    val tabContent: @Composable ColumnScope.() -> Unit = {
        CustomTabView(
            tabs = tabs,
            selectedTabIndex = currentTabIndex,
            onTabSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) { page ->
            pagerContent(page)
        }
    }

    if (isTablet) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            content = tabContent
        )
    } else {
        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState
        )

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetDragHandle = null,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    content = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            )
                        }

                        tabContent()
                    }
                )
            }
        ) { /* No content behind the sheet */ }
    }
}
