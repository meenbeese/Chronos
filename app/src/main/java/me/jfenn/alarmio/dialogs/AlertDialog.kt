package me.jfenn.alarmio.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView

import me.jfenn.alarmio.R


class AlertDialog(context: Context?) : AestheticDialog(context), View.OnClickListener {
    private var title: String? = null
    private var content: String? = null
    private var listener: Listener? = null
    fun setTitle(title: String?): AlertDialog {
        this.title = title
        return this
    }

    fun setContent(content: String?): AlertDialog {
        this.content = content
        return this
    }

    fun setListener(listener: Listener?): AlertDialog {
        this.listener = listener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_alert)
        val titleView = findViewById<TextView>(R.id.title)
        val bodyView = findViewById<TextView>(R.id.body)
        val okView = findViewById<TextView>(R.id.ok)
        val cancelView = findViewById<TextView>(R.id.cancel)
        if (title != null) titleView?.text = title else titleView?.visibility = View.GONE
        bodyView?.text = content
        okView?.setOnClickListener(this)
        if (listener != null) {
            cancelView?.setOnClickListener(this)
        } else {
            cancelView?.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        dismiss()
        listener?.onDismiss(this, v.id == R.id.ok)
    }

    interface Listener {
        fun onDismiss(dialog: AlertDialog?, ok: Boolean)
    }
}
