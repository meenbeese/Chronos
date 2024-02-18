package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import me.jfenn.attribouter.Attribouter


/**
 * A preference item that opens the application's about screen.
 */
class AboutPreferenceData : CustomPreferenceData(R.string.title_about) {

    override fun getValueName(holder: ViewHolder): String? = null

    override fun onClick(holder: ViewHolder) {
        Attribouter.from(holder.context)
                .withGitHubToken(null)
                .show()
    }
}
