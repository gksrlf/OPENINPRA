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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.little_wizard.myapplication.util.Coordinates;
import com.little_wizard.myapplication.view.MyView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;

public class DrawActivity extends AppCompatActivity {
    private MyView m;
    private boolean isDrawMode = false;
    Display display;
    private int viewHeight;
    private int viewWidth;
    MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewHeight = size.y;
        viewWidth = size.x;

        m = new MyView(this, viewHeight, viewWidth);

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
        item = menu.findItem(R.id.draw_mode);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.draw_mode:
                if(!isDrawMode){
                    item.setIcon(R.drawable.ic_baseline_done_24);
                }else{
                    item.setIcon(R.drawable.ic_baseline_edit_24);
                }
                isDrawMode = !isDrawMode;
                m.setItemMode(isDrawMode);
                return true;

            case R.id.draw_save:
                String path;
                String bitName = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
                Log.d(this.toString(), m.getBitmap().toString());
                saveBitmap(this, bitName, m.getBitmap());
                path = getFilesDir() + "/" + bitName + ".png"; // TODO::서버에 전송할 때 사진이랑 비트맵 같이 전송해야함

                Intent returnIntent = new Intent(this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("filename", bitName);
                bundle.putParcelableArrayList("coordinates", (ArrayList<Coordinates>)m.getList());
                returnIntent.putExtras(bundle);
                setResult(Activity.RESULT_OK, returnIntent);

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

        int maxResolution = 0;

        float width = source.getWidth();
        float height = source.getHeight();
        float newWidth = viewWidth;
        float newHeight = viewHeight;
        float rate = height / width;

        Log.d("rate1 rate2", String.valueOf(viewHeight / viewWidth) + ", " + String.valueOf(height / width));
        if(viewHeight / viewWidth > height / width){
            //newWidth = viewWidth;
            newHeight = (int) (viewWidth * rate);
            Log.d("rate", "1");
        }else{
            //newHeight = viewHeight;
            newWidth = (int) (viewHeight / rate);
        }
        Log.d("newwidth newheight", String.valueOf(newWidth) + ", " + String.valueOf(newHeight));
      /*  if(width > height)
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
*/
        return Bitmap.createScaledBitmap(source, (int)newWidth, (int)newHeight, true);
    }

    public boolean isDrawMode() {
        return isDrawMode;
    }
}