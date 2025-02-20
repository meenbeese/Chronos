package com.meenbeese.chronos.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.meenbeese.chronos.data.PreferenceData

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FileChooserFragment : Fragment() {
    private var preference: PreferenceData? = null
    private var type: String? = TYPE_IMAGE
    private var callback: ((String, String) -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startIntent()
        } else {
            handlePermissionDenied()
        }
    }

    private val startForResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            handleActivityResult(result.data!!)
        }
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { data ->
            preference = data.getSerializable(EXTRA_PREF, PreferenceData::class.java)
            type = data.getString(EXTRA_TYPE)
        }

        val permission = when (type) {
            TYPE_AUDIO -> READ_MEDIA_AUDIO
            else -> READ_MEDIA_IMAGES
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            startIntent()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun startIntent() {
        val intent = Intent().apply {
            type = when (type) {
                TYPE_IMAGE -> "image/*"
                TYPE_AUDIO -> "audio/*"
                else -> "*/*"
            }
            action = run {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Intent.ACTION_OPEN_DOCUMENT
            }
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, when (type) {
                TYPE_IMAGE -> arrayOf("image/png", "image/jpeg", "image/webp")
                TYPE_AUDIO -> arrayOf("audio/mpeg", "audio/mp4", "audio/opus")
                else -> arrayOf("*/*")
            })
        }
        startForResultLauncher.launch(intent)
    }

    private fun handlePermissionDenied() {
        Toast.makeText(requireContext(), "Permission is necessary for this feature.", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun handleActivityResult(data: Intent) {
        val uri: Uri? = data.data
        if (uri != null) {
            if (TYPE_IMAGE == type) {
                handleImageResult(uri)
            } else if (TYPE_AUDIO == type) {
                handleAudioResult(uri)
            }
        } else {
            Toast.makeText(requireContext(), "No file selected.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleImageResult(uri: Uri) {
        var path: String? = null
        var cursor: Cursor? = null
        try {
            cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                if (columnIndex != -1) path = cursor.getString(columnIndex)
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "An error has occurred when choosing media.", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
        }
        GlobalScope.launch {
            preference?.setValue(requireContext(), path)
        }
        callback?.invoke("Image File", path ?: "")
        parentFragmentManager.popBackStack()
    }

    private fun handleAudioResult(uri: Uri) {
        var name: String? = null
        var cursor: Cursor? = null
        try {
            requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                if (columnIndex != -1) name = cursor.getString(columnIndex)
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "An error has occurred when choosing media.", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
        }
        if (!name.isNullOrEmpty()) {
            callback?.invoke(name, uri.toString())
        }
        parentFragmentManager.popBackStack()
    }

    fun setCallback(callback: (String, String) -> Unit) {
        this.callback = callback
    }

    companion object {
        const val EXTRA_PREF = "extra_pref"
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_IMAGE = "image/*"
        const val TYPE_AUDIO = "audio/*"
        const val READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES"
        const val READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

        fun newInstance(preference: PreferenceData?, type: String?): FileChooserFragment {
            val fragment = FileChooserFragment()
            val args = Bundle()
            args.putSerializable(EXTRA_PREF, preference)
            args.putString(EXTRA_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }
}
