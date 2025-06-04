package com.meenbeese.chronos.data.preference

import android.app.Activity
import android.content.Context
import android.view.View

import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData

import kotlinx.coroutines.runBlocking

class ColorPrefererenceData(
    val context: Context,
    nameRes: Int,
) : CustomPreferenceData(nameRes) {

    override fun getValueName(holder: ViewHolder): String? {
        val color = PreferenceData.BACKGROUND_COLOR.getValue<Int>(context)
        return String.format("#%06X", 0xFFFFFF and color)
    }

    override fun onClick(holder: ViewHolder) {
        if (context is Activity) {
            MaterialColorPickerDialog
                .Builder(context)
                .setTitle(R.string.title_picker_dialog)
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(PreferenceData.BACKGROUND_COLOR.getValue<Int>(context))
                .setColorListener { color, _ ->
                    runBlocking {
                        PreferenceData.BACKGROUND_COLOR.setValue(context, color)
                    }
                }
                .show()
        }
    }

    override fun bindViewHolder(holder: ViewHolder) {
        super.bindViewHolder(holder)
        val color = PreferenceData.BACKGROUND_COLOR.getValue<Int>(context)
        val colorPreview = holder.binding.root.findViewById<View>(R.id.color_preview)
        colorPreview?.setBackgroundColor(color)
    }
}
