package com.meenbeese.chronos.data.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.meenbeese.chronos.R
import com.meenbeese.chronos.utils.DimenUtils.getStatusBarHeight

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

    companion object {
        var versionName: String? = null
    }

    init {
        versionName = context.packageManager?.getPackageInfo(context.packageName, 0)?.versionName
    }
}

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val aboutPage: View = AboutPage(this)
            .setImage(R.mipmap.ic_launcher)
            .setDescription("Chronos".plus("\n\n").plus(DESCRIPTION.trim()))
            .addItem(Element().setTitle(VERSION).setGravity(1))
            .addGroup("Connect with us")
            .addGitHub("meenbeese/Chronos")
            .addWebsite("https://meenbeese.is-a.dev")
            .addEmail("meenbeese@tutanota.com")
            .addItem(copyrightsElement())
            .create()
        aboutPage.setPadding(0, getStatusBarHeight(), 0, 0)
        setContentView(aboutPage)
    }

    private fun copyrightsElement(): Element {
        val copyrightsElement = Element()
        val copyrightText = "Copyright $YEAR Meenbeese"
        copyrightsElement.setTitle(copyrightText)
        copyrightsElement.setIconDrawable(R.drawable.ic_copyright)
        copyrightsElement.setAutoApplyIconTint(true)
        copyrightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color)
        copyrightsElement.setIconNightTint(android.R.color.white)
        copyrightsElement.setGravity(Gravity.CENTER)
        copyrightsElement.setOnClickListener {
            Toast.makeText(
                this@AboutActivity,
                copyrightText,
                Toast.LENGTH_SHORT
            ).show()
        }
        return copyrightsElement
    }

    companion object {
        const val DESCRIPTION =
                "Simple, yet customizable alarm clock app that is focused on simplicity, usability and modern design.\n\n" +
                "• Custom backgrounds & ringtones\n" +
                "• No unnecessary permissions\n" +
                "• Dark, Light, AMOLED themes\n" +
                "• Granular controls everywhere\n" +
                "• Unique, minimal, efficient design\n" +
                "• Portrait and landscape orientation\n" +
                "• Countless default ringtones\n\n\n\n" +
                "Made with ❤ in Canada."
        val VERSION = "Version ${AboutPreferenceData.versionName}"
        val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    }
}
