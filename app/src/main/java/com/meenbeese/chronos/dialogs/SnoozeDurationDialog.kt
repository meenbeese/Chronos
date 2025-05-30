package com.meenbeese.chronos.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter

import androidx.activity.ComponentDialog

import com.meenbeese.chronos.databinding.DialogSnoozeDurationBinding

class SnoozeDurationDialog(
    context: Context,
    private val names: Array<CharSequence?>,
    private val listener: OnSnoozeDurationSelectedListener
) : ComponentDialog(context) {

    interface OnSnoozeDurationSelectedListener {
        fun onSnoozeDurationSelected(which: Int)
    }

    private var _binding: DialogSnoozeDurationBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogSnoozeDurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, names)
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, which, _ ->
            listener.onSnoozeDurationSelected(which)
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun show() {
        super.show()
        val window = window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onStop() {
        super.onStop()
        _binding = null
    }
}
