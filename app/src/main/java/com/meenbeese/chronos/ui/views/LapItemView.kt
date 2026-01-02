package com.meenbeese.chronos.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.nav.destinations.LapData
import com.meenbeese.chronos.utils.FormatUtils

@Composable
fun LapItemView(
    lap: LapData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.title_lap_number, lap.number),
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(
                R.string.title_lap_time,
                FormatUtils.formatMillis(lap.lapTime)
            ),
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(
                R.string.title_total_time,
                FormatUtils.formatMillis(lap.totalTime)
            ),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
