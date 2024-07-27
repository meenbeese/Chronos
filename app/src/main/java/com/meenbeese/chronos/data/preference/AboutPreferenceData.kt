package com.meenbeese.chronos.data.preference

import android.content.Context

import androidx.fragment.app.FragmentActivity

import com.meenbeese.chronos.R
import com.meenbeese.chronos.fragments.AboutFragment


/**
 * A preference item that opens the application's about screen.
 */
class AboutPreferenceData(private val context: Context) : CustomPreferenceData(R.string.title_about) {
    override fun getValueName(holder: ViewHolder): String? = null

    override fun onClick(holder: ViewHolder) {
        if (context is FragmentActivity) {
            val fragmentManager = context.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment, AboutFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }
}
