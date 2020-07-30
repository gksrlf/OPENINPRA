package com.little_wizard.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.little_wizard.myapplication.view.MyView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;

public class DrawActivity extends AppCompatActivity {
    MyView m;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        m = new MyView(this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        Context context = getApplicationContext();
        try{

            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
            bitmap = resizeBitmapImage(bitmap);
            m.setBackgrountBitmap(bitmap);
        }catch(IOException e){
            Toast.makeText(context, "오류", Toast.LENGTH_LONG);
        }
        setContentView(m);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.draw_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.draw_save:
                String path;
                String bitName = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
                Log.d(this.toString(), m.getBitmap().toString());
                saveBitmap(this, bitName, m.getBitmap());
                path = getFilesDir() + "/" + bitName + ".png"; // TODO::서버에 전송할 때 사진이랑 비트맵 같이 전송해야함
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void saveBitmap(Context context, String bitName, Bitmap mBitmap) {//  ww  w.j  a va 2s.c  o  m

        File f = new File(context.getFilesDir() + "/" + bitName + ".png");
        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap resizeBitmapImage(Bitmap source){
        DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);

        int maxResolution = 0;

        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if(width > display.widthPixels && height > display.heightPixels){

        }else{

        }
        //TODO: 이미지 로드할 때 화면에 꽉차게 가져와야함
        if(width > height)
        {
            maxResolution = display.widthPixels;
            if(maxResolution < width)
            {
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        }
        else
        {
            maxResolution = display.heightPixels;
            if(maxResolution < height)
            {
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
}