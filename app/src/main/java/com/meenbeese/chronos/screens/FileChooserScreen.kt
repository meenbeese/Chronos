package com.meenbeese.chronos.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

import com.meenbeese.chronos.data.PreferenceEntry

import kotlinx.coroutines.launch

@Composable
fun FileChooserScreen(
    type: String,
    preference: PreferenceEntry.StringPref?,
    onFileChosen: (name: String, uriString: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hasLaunched = remember { mutableStateOf(false) }

    val permission = when (type) {
        FileChooserType.IMAGE -> FileChooserType.READ_MEDIA_IMAGES
        FileChooserType.AUDIO -> FileChooserType.READ_MEDIA_AUDIO
        else -> null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startFileChooser(type)
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val path = uri.toString()
            coroutineScope.launch { preference?.set(context, path) }
            onFileChosen("Image File", path)
        } ?: onDismiss()
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val name = getDisplayName(context, uri) ?: "Selected File"
            val path = uri.toString()
            coroutineScope.launch { preference?.set(context, path) }
            onFileChosen(name, path)
        } ?: onDismiss()
    }

    LaunchedEffect(Unit) {
        if (!hasLaunched.value) {
            hasLaunched.value = true
            if (type == FileChooserType.IMAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else if (permission != null && ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            } else {
                startFileChooser(type, openDocumentLauncher)
            }
        }
    }
}

private fun startFileChooser(
    type: String,
    launcher: ManagedActivityResultLauncher<Array<String>, Uri?>? = null
) {
    val mimeTypes = when (type) {
        FileChooserType.IMAGE -> arrayOf(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/bmp",
            "image/heif"
        )
        FileChooserType.AUDIO -> arrayOf(
            "audio/mpeg",
            "audio/mp4",
            "audio/ogg",
            "audio/aac",
            "audio/opus",
            "audio/flac"
        )
        else -> arrayOf("*/*")
    }

    launcher?.launch(mimeTypes)
}

private fun getDisplayName(
    context: Context,
    uri: Uri
): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (index != -1) it.getString(index) else null
        } else null
    }
}

object FileChooserType {
    const val IMAGE = "image/*"
    const val AUDIO = "audio/*"
    const val READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES"
    const val READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"
}
