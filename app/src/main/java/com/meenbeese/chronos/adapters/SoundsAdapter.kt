package com.meenbeese.chronos.adapters

import android.view.ViewGroup

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.views.SoundItemView

class SoundsAdapter(
    private val chronos: Chronos,
    private val sounds: List<SoundData>
) : RecyclerView.Adapter<SoundsAdapter.ViewHolder>() {

    private var currentlyPlaying = -1
    private var listener: SoundChooserListener? = null

    fun setListener(listener: SoundChooserListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(composeView)
    }

    @UnstableApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val composeView = holder.composeView
        val isNoneOption = position == 0

        composeView.setContent {
            if (isNoneOption) {
                SoundItemView(
                    icon = painterResource(R.drawable.ic_ringtone_disabled),
                    title = chronos.getString(R.string.title_sound_none),
                    modifier = Modifier.clickable {
                        listener?.onSoundChosen(null)
                    }
                )
            } else {
                val sound = sounds[position - 1]
                val isPlaying = sound.isPlaying(chronos)

                SoundItemView(
                    icon = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    title = sound.name,
                    modifier = Modifier.clickable {
                        val currentPosition = holder.bindingAdapterPosition
                        val currentSound = sounds[currentPosition - 1]
                        currentlyPlaying = if (currentSound.isPlaying(chronos) || currentlyPlaying == currentPosition) {
                            currentSound.stop(chronos)
                            -1
                        } else {
                            currentSound.preview(chronos)
                            if (currentlyPlaying >= 0 && currentlyPlaying < itemCount) {
                                sounds.getOrNull(currentlyPlaying - 1)?.stop(chronos)
                                notifyItemChanged(currentlyPlaying)
                            }
                            currentPosition
                        }
                        notifyItemChanged(currentPosition)
                    }
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return sounds.size + 1
    }

    class ViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView)
}
