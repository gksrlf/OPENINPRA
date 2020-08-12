package com.little_wizard.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.little_wizard.myapplication.util.Coordinates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1;
    private static final int GET_COORDINATES = 2;
    private static final int PHOTO_TYPE_SYMMETRY = 3;
    private static final int PHOTO_TYPE_ASYMMETRY = 4;

    private final int PERMISSIONS_REQUEST = 1001;

    TransferUtility transferUtility;

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
        switch (requestCode) {
            case PICK_FROM_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    Uri photoUri = data.getData();
                    Cursor cursor = null;

                    try {
                        String[] proj = {MediaStore.Images.Media.DATA};

                        assert photoUri != null;
                        cursor = getContentResolver().query(photoUri, proj, null, null, null);

                        assert cursor != null;
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                        cursor.moveToFirst();

                        File setFile = new File(cursor.getString(column_index));

                        final String[] menu = {"Symmetry", "Asymmetry"};
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("Photo type");
                        alertDialogBuilder.setItems(menu, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent;
                                switch (i) {
                                    case 0:
                                        intent = new Intent(getApplicationContext(), EditActivity.class);
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setData(Uri.fromFile(setFile));
                                        startActivity(intent);
                                        break;
                                    case 1:
                                        intent = new Intent(getApplicationContext(), DrawActivity.class);
                                        intent.putExtra("mode", "asymmetry");
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setData(Uri.fromFile(setFile));
                                        startActivity(intent);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                break;
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