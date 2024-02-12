package me.jfenn.alarmio.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import me.jfenn.alarmio.data.PreferenceData;


public class FileChooserActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 284;
    private static final int REQUEST_AUDIO = 285;
    private static final int REQUEST_IMAGE_PERMISSION = 726;
    private static final int REQUEST_AUDIO_PERMISSION = 727;

    public static final String EXTRA_TYPE = "james.alarmio.FileChooserActivity.EXTRA_TYPE";
    public static final String EXTRA_PREF = "james.alarmio.FileChooserActivity.EXTRA_PREFERENCE";
    public static final String TYPE_IMAGE = "image/*";
    public static final String TYPE_AUDIO = "audio/*";

    private PreferenceData preference;
    private String type = TYPE_IMAGE;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = getIntent();
        if (data != null) {
            if (data.hasExtra(EXTRA_PREF) && data.getSerializableExtra(EXTRA_PREF) instanceof PreferenceData)
                preference = (PreferenceData) data.getSerializableExtra(EXTRA_PREF);
            if (data.hasExtra(EXTRA_TYPE))
                type = data.getStringExtra(EXTRA_TYPE);
        }

        String permission;
        int requestCode;
        if (TYPE_AUDIO.equals(type)) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
            requestCode = REQUEST_AUDIO_PERMISSION;
        } else {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
            requestCode = REQUEST_IMAGE_PERMISSION;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            startIntent();
        else
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private void startIntent() {
        int requestCode = TYPE_AUDIO.equals(type) ? REQUEST_AUDIO : REQUEST_IMAGE;
        Intent intent = new Intent();
        intent.setType(type);
        if (TYPE_AUDIO.equals(type)) {
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION || requestCode == REQUEST_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startIntent();
            } else {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    // Permission denied. Show a message asking for permission.
                    Toast.makeText(this, "Permission is necessary for this feature.", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{permissions[0]}, requestCode);
                } else {
                    // Permission hard denied twice. Show a different message.
                    Toast.makeText(this, "Please enable permission in settings.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            String path = data.getDataString();
            if (TYPE_IMAGE.equals(type)) {
                Cursor cursor = null;

                try {
                    cursor = getContentResolver().query(Objects.requireNonNull(data.getData()), null, null, null, null);

                    String documentId;
                    if (cursor != null) {
                        cursor.moveToFirst();
                        documentId = cursor.getString(0);
                        documentId = documentId.substring(documentId.lastIndexOf(":") + 1);
                        cursor.close();
                    } else {
                        finish();
                        return;
                    }

                    cursor = getContentResolver().query(MediaStore.Images.Media.getContentUri("external"), null, MediaStore.Images.Media._ID + " = ? ", new String[]{documentId}, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "An error has occurred when choosing media.", Toast.LENGTH_SHORT).show();
                } finally {
                    if (cursor != null && !cursor.isClosed())
                        cursor.close();
                }
            }

            preference.setValue(this, path);
        } else if (requestCode == REQUEST_AUDIO && resultCode == RESULT_OK && data != null && TYPE_AUDIO.equals(type)) {
            String name = null;
            Cursor cursor = null;

            try {
                getContentResolver().takePersistableUriPermission(Objects.requireNonNull(data.getData()), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cursor = getContentResolver().query(data.getData(), null, null, null, null);

                String documentId;
                if (cursor != null) {
                    cursor.moveToFirst();
                    documentId = cursor.getString(0);
                    documentId = documentId.substring(documentId.lastIndexOf(":") + 1);
                    cursor.close();
                } else {
                    finish();
                    return;
                }

                cursor = getContentResolver().query(MediaStore.Audio.Media.getContentUri("external"), null, MediaStore.Audio.Media._ID + " = ? ", new String[]{documentId}, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "An error has occurred when choosing media.", Toast.LENGTH_SHORT).show();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            }

            if (name != null && name.length() > 0)
                data.putExtra("name", name);
            setResult(RESULT_OK, data);
        }

        finish();
    }
}
