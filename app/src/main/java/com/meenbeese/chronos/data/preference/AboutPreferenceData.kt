package com.meenbeese.chronos.data.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity.CENTER
import android.widget.Toast

import androidx.activity.ComponentActivity

import com.meenbeese.chronos.R
import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.activities.MainActivity.Companion.LAYOUT_FLAG

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
        window.setFlags(LAYOUT_FLAG, LAYOUT_FLAG)
        setContentView(AboutPage(this)
            .setImage(R.mipmap.ic_launcher)
            .setDescription("Chronos\n\n${DESCRIPTION.trim()}")
            .addItem(Element().setTitle(VERSION).setGravity(CENTER))
            .addGroup("Engage with Chronos")
            .addGitHub("meenbeese/Chronos", "Fork GitHub repo")
            .addWebsite(WEBSITE, "Visit my website")
            .addEmail(EMAIL, "Send me an email")
            .addItem(copyrightElement())
            .create()
        )
    }

    private fun copyrightElement(): Element {
        val copyrightText = "Copyright $YEAR Meenbeese"
        return Element()
            .setTitle(copyrightText)
            .setIconDrawable(R.drawable.ic_copyright)
            .setAutoApplyIconTint(true)
            .setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color)
            .setIconNightTint(android.R.color.white)
            .setGravity(CENTER)
            .setOnClickListener {
                Toast.makeText(this@AboutActivity, copyrightText, Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val DESCRIPTION =
                "Simple, yet customizable alarm clock app focused on simplicity, usability and design.\n\n" +
                "• Custom backgrounds & ringtones\n" +
                "• No unnecessary permissions\n" +
                "• Dark, Light, AMOLED themes\n" +
                "• Granular controls everywhere\n" +
                "• Unique, minimal, efficient design\n" +
                "• Portrait and landscape orientation\n" +
                "• Countless default ringtones\n\n\n\n" +
                "Made with ❤ in Canada."
        const val EMAIL = "meenbeese@tutanota.com"
        const val VERSION = "Version ${BuildConfig.VERSION_NAME}"
        const val WEBSITE = "https://meenbeese.is-a.dev"
        val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    }
}
