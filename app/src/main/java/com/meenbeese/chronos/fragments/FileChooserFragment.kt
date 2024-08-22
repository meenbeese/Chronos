package com.meenbeese.chronos.fragments

import android.Manifest
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
        val data = arguments
        data?.let {
            if (it.containsKey(EXTRA_PREF) && it.getSerializable(EXTRA_PREF) is PreferenceData)
                preference = it.getSerializable(EXTRA_PREF) as PreferenceData?
            if (it.containsKey(EXTRA_TYPE)) type = it.getString(EXTRA_TYPE)
        }
        val permission: String
        val requestCode: Int
        if (TYPE_AUDIO == type) {
            permission = Manifest.permission.READ_MEDIA_AUDIO
            requestCode = REQUEST_AUDIO_PERMISSION
        } else {
            permission = Manifest.permission.READ_MEDIA_IMAGES
            requestCode = REQUEST_IMAGE_PERMISSION
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) startIntent() else requestPermissions(
            arrayOf(permission),
            requestCode
        )
    }

    private fun startIntent() {
        val requestCode = if (TYPE_AUDIO == type) REQUEST_AUDIO else REQUEST_IMAGE
        val intent = Intent()
        intent.type = type
        if (TYPE_AUDIO == type) {
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.action = Intent.ACTION_GET_CONTENT
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, requestCode)
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
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    // Permission denied. Show a message asking for permission.
                    Toast.makeText(
                        requireContext(),
                        "Permission is necessary for this feature.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestPermissions(
                        arrayOf(permissions[0]),
                        requestCode
                    )
                } else {
                    // Permission hard denied twice. Show a different message.
                    Toast.makeText(
                        requireContext(),
                        "Please enable permission in settings.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                activity?.finish()
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
