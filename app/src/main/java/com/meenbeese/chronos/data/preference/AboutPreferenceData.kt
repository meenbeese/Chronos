package com.meenbeese.chronos.data.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast

import androidx.activity.ComponentActivity

import com.meenbeese.chronos.R
import com.meenbeese.chronos.utils.DimenUtils.getStatusBarHeight
import com.meenbeese.chronos.BuildConfig

import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

import java.util.Calendar


/**
 * A preference item that opens the application's about screen.
 */
class AboutPreferenceData(private val context: Context) : CustomPreferenceData(R.string.title_about) {
    override fun getValueName(holder: ViewHolder): String? = null

    override fun onClick(holder: ViewHolder) {
        val intent = Intent(context, AboutActivity::class.java)
        context.startActivity(intent)
    }
}

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val aboutPage: View = AboutPage(this)
            .setImage(R.mipmap.ic_launcher)
            .setDescription("Chronos\n\n".plus(DESCRIPTION.trim()))
            .addItem(Element().setTitle(VERSION).setGravity(1))
            .addGroup("Connect with us")
            .addGitHub("meenbeese/Chronos")
            .addWebsite("https://meenbeese.is-a.dev")
            .addEmail(EMAIL)
            .addItem(copyrightsElement())
            .create()
        aboutPage.setPadding(0, getStatusBarHeight(), 0, 0)
        setContentView(aboutPage)
    }

    private fun copyrightsElement(): Element {
        val copyrightText = "Copyright $YEAR Meenbeese"
        val copyrightsElement = Element()
            .setTitle(copyrightText)
            .setIconDrawable(R.drawable.ic_copyright)
            .setAutoApplyIconTint(true)
            .setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color)
            .setIconNightTint(android.R.color.white)
            .setGravity(Gravity.CENTER)
            .setOnClickListener {
            Toast.makeText(
                this@AboutActivity,
                copyrightText,
                Toast.LENGTH_SHORT
            ).show()
        }
        return copyrightsElement
    }

    companion object {
        const val EMAIL = "meenbeese@tutanota.com"
        const val DESCRIPTION =
                "Simple, yet customizable alarm clock app focused on simplicity, usability and modern design.\n\n" +
                "• Custom backgrounds & ringtones\n" +
                "• No unnecessary permissions\n" +
                "• Dark, Light, AMOLED themes\n" +
                "• Granular controls everywhere\n" +
                "• Unique, minimal, efficient design\n" +
                "• Portrait and landscape orientation\n" +
                "• Countless default ringtones\n\n\n\n" +
                "Made with ❤ in Canada."
        const val VERSION = "Version ${BuildConfig.VERSION_NAME}"
        val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    }
}
