package com.meenbeese.chronos.fragments

import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.meenbeese.chronos.data.PreferenceData


class FileChooserFragment : Fragment() {
    private var preference: PreferenceData? = null
    private var type: String? = TYPE_IMAGE

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { data ->
            preference = data.getSerializable(EXTRA_PREF, PreferenceData::class.java)
            type = data.getString(EXTRA_TYPE)
        }

        val (permission, requestCode) = when (type) {
            TYPE_AUDIO -> READ_MEDIA_AUDIO to REQUEST_AUDIO_PERMISSION
            else -> READ_MEDIA_IMAGES to REQUEST_IMAGE_PERMISSION
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            startIntent()
        } else {
            requestPermissions(arrayOf(permission), requestCode)
        }
    }

    private fun startIntent() {
        val requestCode = if (TYPE_AUDIO == type) REQUEST_AUDIO else REQUEST_IMAGE
        val intent = Intent().apply {
            type = this@FileChooserFragment.type
            action = if (TYPE_AUDIO == this@FileChooserFragment.type) {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Intent.ACTION_OPEN_DOCUMENT
            } else {
                Intent.ACTION_GET_CONTENT
            }
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, requestCode)
    }

    private fun handlePermissionDenied(permission: String, requestCode: Int) {
        if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(requireContext(), "Permission is necessary for this feature.", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            Toast.makeText(requireContext(), "Please enable permission in settings.", Toast.LENGTH_SHORT).show()
        }
        activity?.finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION || requestCode == REQUEST_IMAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startIntent()
            } else {
                handlePermissionDenied(permissions[0], requestCode)
            }
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == ComponentActivity.RESULT_OK && data != null) {
            var path = data.dataString
            if (TYPE_IMAGE == type) {
                var cursor: Cursor? = null
                try {
                    cursor = requireContext().contentResolver.query(data.data!!, null, null, null, null)
                    var documentId: String
                    if (cursor != null && cursor.moveToFirst()) {
                        documentId = cursor.getString(0)
                        documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
                        cursor.close()
                    } else {
                        activity?.finish()
                        return
                    }
                    cursor = requireContext().contentResolver.query(
                        MediaStore.Images.Media.getContentUri("external"),
                        null,
                        MediaStore.Images.Media._ID + " = ? ",
                        arrayOf(documentId),
                        null
                    )
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                            if (columnIndex != -1) path = it.getString(columnIndex)
                        }
                        it.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "An error has occurred when choosing media.",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    if (cursor != null && !cursor.isClosed) cursor.close()
                }
            }
            preference?.setValue(requireContext(), path)
        } else if (requestCode == REQUEST_AUDIO && resultCode == ComponentActivity.RESULT_OK && data != null && TYPE_AUDIO == type) {
            var name: String? = null
            var cursor: Cursor? = null
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    data.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                cursor = requireContext().contentResolver.query(data.data!!, null, null, null, null)
                var documentId: String
                if (cursor != null) {
                    cursor.moveToFirst()
                    documentId = cursor.getString(0)
                    documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
                    cursor.close()
                } else {
                    activity?.finish()
                    return
                }
                cursor = requireContext().contentResolver.query(
                    MediaStore.Audio.Media.getContentUri("external"),
                    null,
                    MediaStore.Audio.Media._ID + " = ? ",
                    arrayOf(documentId),
                    null
                )
                cursor?.let {
                    it.moveToFirst()
                    val columnIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    if (columnIndex != -1) name = it.getString(columnIndex)
                    it.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "An error has occurred when choosing media.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                if (cursor != null && !cursor.isClosed) cursor.close()
            }
            if (!name.isNullOrEmpty()) data.putExtra("name", name)
            activity?.setResult(ComponentActivity.RESULT_OK, data)
        }
        activity?.finish()
    }

    companion object {
        const val EXTRA_PREF = "extra_pref"
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_IMAGE = "image/*"
        const val TYPE_AUDIO = "audio/*"
        const val REQUEST_IMAGE_PERMISSION = 1001
        const val REQUEST_AUDIO_PERMISSION = 1002
        const val REQUEST_IMAGE = 1003
        const val REQUEST_AUDIO = 1004

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
