package com.meenbeese.chronos.dialogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager

import androidx.activity.ComponentDialog

import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.databinding.DialogBackgroundPermissionsBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackgroundPermissionsDialog(context: Context) : ComponentDialog(context) {
    private var _binding: DialogBackgroundPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogBackgroundPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener { dismiss() }
        binding.okButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                PreferenceData.INFO_BACKGROUND_PERMISSIONS.setValue(context, true)

                withContext(Dispatchers.Main) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    dismiss()
                }
            }
        }
    }

    override fun show() {
        super.show()
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
