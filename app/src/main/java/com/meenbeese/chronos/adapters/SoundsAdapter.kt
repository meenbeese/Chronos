package com.meenbeese.chronos.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.databinding.ItemSoundBinding
import com.meenbeese.chronos.interfaces.SoundChooserListener

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
        val binding = ItemSoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @UnstableApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.binding.title.setText(R.string.title_sound_none)
            holder.binding.icon.setOnClickListener(null)
            holder.binding.root.setOnClickListener {
                listener?.onSoundChosen(null)
            }
            setPlaying(holder, isPlaying = false, isAnimated = false)
            holder.binding.icon.setImageResource(R.drawable.ic_ringtone_disabled)
            val color = ContextCompat.getColor(holder.binding.root.context, R.color.textColorPrimary)
            holder.binding.icon.setColorFilter(color)
        } else {
            val sound = sounds[position - 1]
            holder.binding.title.text = sound.name
            holder.binding.icon.setOnClickListener {
                val position1 = holder.bindingAdapterPosition
                val sound1 = sounds[position1 - 1]
                currentlyPlaying = if (sound1.isPlaying(chronos) || currentlyPlaying == position1) {
                    sound1.stop(chronos)
                    -1
                } else {
                    sound1.preview(chronos)
                    if (currentlyPlaying >= 0) {
                        sounds[currentlyPlaying - 1].stop(chronos)
                        notifyItemChanged(currentlyPlaying)
                    }
                    position1
                }
                setPlaying(holder, currentlyPlaying == position1, true)
            }
            holder.binding.root.setOnClickListener {
                listener?.onSoundChosen(sounds[holder.bindingAdapterPosition - 1])
            }
            setPlaying(holder, sound.isPlaying(chronos), false)
        }
    }

    @SuppressLint("CheckResult")
    private fun setPlaying(holder: ViewHolder, isPlaying: Boolean, isAnimated: Boolean) {
        val color = ContextCompat.getColor(holder.binding.root.context, R.color.textColorPrimary)

        if (isAnimated) {
            val drawable = AnimatedVectorDrawableCompat.create(
                chronos, if (isPlaying) R.drawable.ic_play_to_pause else R.drawable.ic_pause_to_play
            )
            if (drawable != null) {
                holder.binding.icon.setImageDrawable(drawable)
                holder.binding.icon.setColorFilter(color)
                drawable.start()
                return
            }
        }
        holder.binding.icon.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        holder.binding.icon.setColorFilter(color)
    }

    override fun getItemCount(): Int {
        return sounds.size + 1
    }

    class ViewHolder(val binding: ItemSoundBinding) : RecyclerView.ViewHolder(binding.root)
}
