package com.little_wizard.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.little_wizard.myapplication.util.Coordinates;
import com.little_wizard.myapplication.view.MyImageView;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {
    MyImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageView = new MyImageView(this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        try{
            ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(), uri);
            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setBitmap(bitmap, getWindowManager().getDefaultDisplay());
        }catch(IOException e){
            Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_LONG);
        }

        setContentView(imageView);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout)inflater.inflate(R.layout.line, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(linear, params);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.edit_done:
                Bitmap result = imageView.getResult();
                if(result != null){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("image", result);
                    intent.putExtra("line", imageView.getLinePosX());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}