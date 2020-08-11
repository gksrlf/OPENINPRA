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

        initClient();
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
        Bundle bundle;
        switch(requestCode){
            case PICK_FROM_ALBUM:
                if(resultCode == Activity.RESULT_OK){
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

                        final String[] menu = {"Symmetry", "Asymmetry"};
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("Photo type");
                        alertDialogBuilder.setItems(menu, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent;
                                switch(i){
                                    case 0:
                                        intent = new Intent(getApplicationContext(), EditActivity.class);
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setData(Uri.fromFile(setFile));
                                        startActivityForResult(intent, PHOTO_TYPE_SYMMETRY);
                                        break;
                                    case 1:
                                        intent = new Intent(getApplicationContext(), DrawActivity.class);
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setData(Uri.fromFile(setFile));
                                        startActivityForResult(intent, GET_COORDINATES);
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
            case PHOTO_TYPE_SYMMETRY:
                if(resultCode == Activity.RESULT_OK){
                    Bitmap bitmap = data.getParcelableExtra("image");
                    float pos = data.getFloatExtra("line", Float.MIN_VALUE);

                    Intent intent = new Intent(getApplicationContext(), DrawActivity.class);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setData(Uri.fromFile(setFile));
                    startActivityForResult(intent, GET_COORDINATES);
                    //TODO: 대칭인사진일 때 : bitmap 전송              비대칭인사진일 때 : URI전송 구별
                    break;
                }
                break;
            case GET_COORDINATES:
                if(resultCode == Activity.RESULT_OK){
                    bundle = data.getExtras();
                    Log.d(this.toString(), bundle.getParcelableArrayList("coordinates").toString());
                    String filename = bundle.getString("filename");
                    ArrayList<Coordinates> list = bundle.getParcelableArrayList("coordinates");
                    saveFile(filename, list);
                    readFile(filename);
                    upload(getFilesDir() + "/" + filename + ".txt");
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

    public void saveFile(String filename, ArrayList<Coordinates> list){
        try{
            File dir = new File (getFilesDir().toString());
            //디렉토리 폴더가 없으면 생성함
            if(!dir.exists()){
                dir.mkdir();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(getFilesDir()+"/"+filename+".txt", true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (Coordinates c : list) {
                writer.write(String.format("%f;%f;\n", c.getX(), c.getY()));
                writer.flush();
            }

            writer.close();
            fos.close();
        }catch(IOException e){

        }
    }

    public void readFile(String filename){
        try{
            File file = new File(getFilesDir()+"/"+filename+".txt");
            FileReader reader = new FileReader(file);
            char [] buffer = new char[20];

            while(reader.read(buffer) != -1){
                Log.d("FileReader", String.valueOf(buffer));
            }
            reader.close();
        }catch(FileNotFoundException e){
            Toast.makeText(this, filename+"이 존재하지 않음", Toast.LENGTH_LONG).show();
        }catch (IOException e){
        }
    }

    private void initClient(){
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:13fe9921-fc1f-49ab-b998-c59c0a367efe", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
        transferUtility = TransferUtility.builder().s3Client(s3).context(this).build();
        TransferNetworkLossHandler.getInstance(this);
    }

    private void upload(String path) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progressDialog));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
        progressDialog.show();

        File file = new File(path);
        TransferObserver transferObserver = transferUtility.upload(
                "imagebucket20200724",
                file.getName(),
                file
        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(this.toString(), "onStateChanged: " + id + ", " + state.toString());
                if (state == TransferState.COMPLETED) {
                    Toast.makeText(MainActivity.this, "전송 완료.", Toast.LENGTH_SHORT).show();
                }
                if (state == TransferState.COMPLETED || state == TransferState.FAILED) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float v = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percent = (int) v;
                Log.d(this.toString(), "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percent + "%");
            }

            @Override
            public void onError(int id, Exception e) {
                Log.e(this.toString(), Objects.requireNonNull(e.getMessage()));
                Toast.makeText(MainActivity.this, "전송 실패.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}