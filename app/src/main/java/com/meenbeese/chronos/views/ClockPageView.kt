package com.meenbeese.chronos.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ClockPageView(
    fragments: List<@Composable () -> Unit>,
    backgroundPainter: Painter,
    pageIndicatorVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundDimConstant = 0.15f
    val pagerState = rememberPagerState { fragments.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
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
            fragments[page]()
        }

        if (pageIndicatorVisible) {
            val currentPage = pagerState.currentPage
            val pageOffset = pagerState.currentPageOffsetFraction

            PageIndicatorView(
                currentPage = currentPage,
                pageOffset = pageOffset,
                pageCount = fragments.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }
    }
}
