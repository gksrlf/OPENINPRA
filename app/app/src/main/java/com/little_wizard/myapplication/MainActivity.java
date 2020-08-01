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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.little_wizard.myapplication.util.Coordinates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1;
    private static final int GET_COORDINATES = 2;
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

                        Intent intent = new Intent(this, DrawActivity.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setData(Uri.fromFile(setFile));
                        startActivityForResult(intent, GET_COORDINATES);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                break;
            case GET_COORDINATES:
                if(resultCode == Activity.RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Log.d(this.toString(), bundle.getParcelableArrayList("coordinates").toString());
                    String filename = bundle.getString("filename");
                    ArrayList<Coordinates> list = bundle.getParcelableArrayList("coordinates");
                    saveFile(filename, list);
                    readFile(filename);
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


}