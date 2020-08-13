package com.little_wizard.tdc.ui.draw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.little_wizard.tdc.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;

public class EditActivity extends AppCompatActivity {
    MyImageView imageView;
    Context context;
    String TAG = getClass().getSimpleName();
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageView = new MyImageView(this);
        context = getApplicationContext();

        Intent intent = getIntent();
        Uri uri = intent.getData();
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(), uri);
            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setBitmap(bitmap, getWindowManager().getDefaultDisplay());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_LONG);
        }

        setContentView(imageView);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout) inflater.inflate(R.layout.line, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(linear, params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_done: {
                Bitmap bitmap = imageView.getResult();
                if (bitmap != null) {
                    String bitName = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
                    saveBitmap(this, bitName, bitmap);

                    Intent intent = new Intent(this, DrawActivity.class);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra("mode", "symmetry");
                    intent.putExtra("line", imageView.getLinePosX());
                    intent.setData(Uri.fromFile(new File(filePath)));
                    startActivity(intent);
                } else Log.d(TAG, "result == null");
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveBitmap(Context context, String bitName, Bitmap mBitmap) {//  ww  w.j  a va 2s.c  o  m

        File f = new File(getExternalCacheDir() + "/" + bitName + ".png");
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
        filePath = f.getPath();
    }
}