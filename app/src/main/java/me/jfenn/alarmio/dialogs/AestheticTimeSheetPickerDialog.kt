package me.jfenn.alarmio.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle

import com.afollestad.aesthetic.Aesthetic.Companion.get

import me.jfenn.alarmio.Alarmio
import me.jfenn.timedatepickers.dialogs.TimeSheetPickerDialog


class AestheticTimeSheetPickerDialog : TimeSheetPickerDialog {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, hourOfDay: Int, minute: Int) : super(context, hourOfDay, minute)

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        get()
            .textColorPrimary()
            .take(1)
            .subscribe { integer: Int? -> primaryTextColor = integer!! }
        get()
            .textColorSecondary()
            .take(1)
            .subscribe { integer: Int? -> secondaryTextColor = integer!! }
        get().colorPrimary()
            .take(1)
            .subscribe { integer: Int? ->
                backgroundColor = integer!!
                primaryBackgroundColor = integer
                secondaryBackgroundColor = integer
            }
        get().colorAccent()
            .take(1)
            .subscribe { integer: Int? ->
                selectionColor = integer!!
                selectionTextColor = if ((context.applicationContext as Alarmio).activityTheme == Alarmio.THEME_AMOLED) Color.BLACK else Color.WHITE
            }
    }
}
