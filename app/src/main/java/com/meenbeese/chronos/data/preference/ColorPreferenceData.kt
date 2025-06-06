package com.meenbeese.chronos.data.preference

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup

import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.databinding.ItemPreferenceColorBinding

import kotlinx.coroutines.runBlocking

class ColorPreferenceData(
    private val context: Context,
    private val nameRes: Int,
) : BasePreferenceData<ColorPreferenceData.ViewHolder>() {

    fun onClick(holder: ViewHolder) {
        if (context is Activity) {
            MaterialColorPickerDialog
                .Builder(context)
                .setTitle(R.string.title_picker_dialog)
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(PreferenceData.BACKGROUND_COLOR.getValue<Int>(context))
                .setColorListener { color, _ ->
                    runBlocking {
                        PreferenceData.BACKGROUND_COLOR.setValue(context, color)

                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 12f
                            setColor(color)
                            setStroke(1, 0xFF888888.toInt())
                        }
                        holder.binding.colorPreview.background = drawable
                    }
                }
                .show()
        }
    }

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding = ItemPreferenceColorBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun bindViewHolder(holder: ViewHolder) {
        holder.binding.name.setText(nameRes)

        val color = PreferenceData.BACKGROUND_COLOR.getValue<Int>(context)

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(color)
            setStroke(1, 0xFF888888.toInt())
        }

        holder.binding.colorPreview.background = drawable
        holder.itemView.setOnClickListener { onClick(holder) }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(val binding: ItemPreferenceColorBinding) : BasePreferenceData.ViewHolder(binding.root)
}
