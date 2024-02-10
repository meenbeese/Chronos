package me.jfenn.alarmio.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatDialog

import com.afollestad.aesthetic.Aesthetic.Companion.get


abstract class AestheticDialog(context: Context?) : AppCompatDialog(context) {
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        get()
            .colorPrimary()
            .take(1)
            .subscribe { integer: Int? ->
                findViewById<View>(android.R.id.content)!!.setBackgroundColor(
                    integer!!
                )
            }
    }
}
