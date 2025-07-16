package com.meenbeese.chronos.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.ext.loadAlarmSounds
import com.meenbeese.chronos.ext.loadRingtones
import com.meenbeese.chronos.views.sound.AlarmSoundChooserView
import com.meenbeese.chronos.views.sound.FileSoundChooserView
import com.meenbeese.chronos.views.sound.RingtoneSoundChooserView
import com.meenbeese.chronos.utils.AudioUtils

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Preview
@Composable
fun SoundChooserDialog(
    onDismissRequest: () -> Unit = {},
    onSoundChosen: (SoundData) -> Unit = {},
) {
    val context = LocalContext.current
    val audioUtils = AudioUtils(context)
    val alarmSounds = remember { loadAlarmSounds(context) }
    val ringtoneSounds = remember { loadRingtones(context) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            audioUtils.stopCurrentSound()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            audioUtils.stopCurrentSound()
            onDismissRequest()
        },
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .displayCutoutPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                InputChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Alarm") },
                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) }
                )
                InputChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Ringtone") },
                    leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) }
                )
                InputChip(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("File") },
                    leadingIcon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> AlarmSoundChooserView(
                    sounds = alarmSounds,
                    onSoundChosen = onSoundChosen
                )
                1 -> RingtoneSoundChooserView(
                    sounds = ringtoneSounds,
                    onSoundChosen = onSoundChosen
                )
                2 -> FileSoundChooserView(
                    onSoundChosen = onSoundChosen
                )
            }
        }
    }
}
