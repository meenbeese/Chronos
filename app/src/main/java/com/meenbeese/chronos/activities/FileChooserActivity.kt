package com.meenbeese.chronos.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.meenbeese.chronos.data.PreferenceData


class FileChooserActivity : ComponentActivity() {
    private var preference: PreferenceData? = null
    private var type: String? = TYPE_IMAGE
    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent
        data?.let {
            if (it.hasExtra(EXTRA_PREF) && it.getSerializableExtra(EXTRA_PREF) is PreferenceData)
                preference = it.getSerializableExtra(
                    EXTRA_PREF
                ) as PreferenceData?
            if (it.hasExtra(EXTRA_TYPE)) type = it.getStringExtra(EXTRA_TYPE)
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
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) startIntent() else ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            requestCode
        )
    }

    private fun startIntent() {
        val requestCode = if (TYPE_AUDIO == type) REQUEST_AUDIO else REQUEST_IMAGE
        val intent = Intent()
        intent.setType(type)
        if (TYPE_AUDIO == type) {
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT)
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
                        this,
                        "Permission is necessary for this feature.",
                        Toast.LENGTH_SHORT
                    ).show()
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            permissions[0]
                        ), requestCode
                    )
                } else {
                    // Permission hard denied twice. Show a different message.
                    Toast.makeText(
                        this,
                        "Please enable permission in settings.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                finish()
            }
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            var path = data.dataString
            if (TYPE_IMAGE == type) {
                var cursor: Cursor? = null
                try {
                    cursor = contentResolver.query(data.data!!, null, null, null, null)
                    var documentId: String
                    if (cursor != null && cursor.moveToFirst()) {
                        documentId = cursor.getString(0)
                        documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
                        cursor.close()
                    } else {
                        finish()
                        return
                    }
                    cursor = contentResolver.query(
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
                        this,
                        "An error has occurred when choosing media.",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    if (cursor != null && !cursor.isClosed) cursor.close()
                }
            }
            preference?.setValue(this, path)
        } else if (requestCode == REQUEST_AUDIO && resultCode == RESULT_OK && data != null && TYPE_AUDIO == type) {
            var name: String? = null
            var cursor: Cursor? = null
            try {
                contentResolver.takePersistableUriPermission(
                    data.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                cursor = contentResolver.query(data.data!!, null, null, null, null)
                var documentId: String
                if (cursor != null) {
                    cursor.moveToFirst()
                    documentId = cursor.getString(0)
                    documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
                    cursor.close()
                } else {
                    finish()
                    return
                }
                cursor = contentResolver.query(
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
                    this,
                    "An error has occurred when choosing media.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                if (cursor != null && !cursor.isClosed) cursor.close()
            }
            if (!name.isNullOrEmpty()) data.putExtra("name", name)
            setResult(RESULT_OK, data)
        }
        finish()
    }

    companion object {
        private const val REQUEST_IMAGE = 284
        private const val REQUEST_AUDIO = 285
        private const val REQUEST_IMAGE_PERMISSION = 726
        private const val REQUEST_AUDIO_PERMISSION = 727
        const val EXTRA_TYPE = "meenbeese.chronos.FileChooserActivity.EXTRA_TYPE"
        const val EXTRA_PREF = "meenbeese.chronos.FileChooserActivity.EXTRA_PREFERENCE"
        const val TYPE_IMAGE = "image/*"
        const val TYPE_AUDIO = "audio/*"
    }
}
