package com.little_wizard.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1;
    private final int PERMISSIONS_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button select_picture = findViewById(R.id.select_picture);
        select_picture.setOnClickListener(new pictureClickListener());
    }

    private class pictureClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String[] permissions = {Manifest.permission.CAMERA
                    , Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSIONS_REQUEST);

            Intent albumIntent = new Intent(Intent.ACTION_PICK);
            albumIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(albumIntent, PICK_FROM_ALBUM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            Uri photoUri = data.getData();
            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                File setFile = new File(cursor.getString(column_index));

                Intent intent = new Intent(this, DrawActivity.class);
                intent.setAction(Intent.ACTION_SEND);
                intent.setData(Uri.fromFile(setFile));
                startActivity(intent);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean success = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    success = false;
                    break;
                }
            }
        }
    }

}