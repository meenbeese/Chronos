package com.meenbeese.chronos.dialogs

import android.content.Context

import me.jfenn.timedatepickers.dialogs.SheetPickerDialog
import me.jfenn.timedatepickers.views.LinearTimePickerView


open class TimeSheetPickerDialog : SheetPickerDialog<LinearTimePickerView> {
    constructor(context: Context?) : super(context, LinearTimePickerView(context))

    constructor(context: Context?, hourOfDay: Int, minute: Int) : super(context, LinearTimePickerView(context)) {
        view.setTime(hourOfDay, minute)
    }
}