package com.little_wizard.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.little_wizard.myapplication.view.MyView;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class DrawActivity extends AppCompatActivity {
    public static final int ASYMMETRY = 1;
    public static final int SYMMETRY = 2;

    private MyView m;
    private boolean isDrawMode = false;
    Display display;
    private int viewHeight;
    private int viewWidth;
    private float line;
    Bitmap bitmap;

    TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        initClient();

        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewHeight = size.y;
        viewWidth = size.x;

        Intent intent = getIntent();
        Context context = getApplicationContext();
        Uri uri = intent.getData();
        if(intent.getStringExtra("mode").equals("asymmetry")){//비대칭일 때 갤러리 사진 Uri 가져옴
            m = new MyView(this, viewHeight, viewWidth, ASYMMETRY);
        }else{ // 대칭일 때 byte array 가져옴, 축 설정
            m = new MyView(this, viewHeight, viewWidth, SYMMETRY);
        }
        line = intent.getFloatExtra("line", Float.MIN_VALUE);
        try{
            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(source);
            bitmap = resizeBitmapImage(bitmap);
            m.setBackgroundBitmap(bitmap);
            m.setLine(line);
        }catch(IOException e){
            Toast.makeText(context, "오류", Toast.LENGTH_LONG);
        }
        setContentView(m);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.draw_menu, menu);
        menu.findItem(R.id.draw_save).setEnabled(false);
        menu.findItem(R.id.draw_save).setVisible(false);
        m.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.draw_undo:
                m.undo();
                return true;

            case R.id.draw_mode:
                if(!isDrawMode){
                    item.setIcon(R.drawable.ic_baseline_pan_tool_24);
                }else{
                    item.setIcon(R.drawable.ic_baseline_edit_24);
                }
                isDrawMode = !isDrawMode;
                m.setItemMode(isDrawMode);
                return true;

            case R.id.draw_confirmation:
                m.setConfirmation(true);
                return true;
            case R.id.draw_save:
                String path;
                String bitName = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
                Log.d(this.toString(), m.getBitmap().toString());
                saveBitmap(this, bitName, m.getBitmap());
                path = getFilesDir() + "/" + bitName + ".png"; // TODO::서버에 전송할 때 사진이랑 비트맵 같이 전송해야함

                ArrayList<Coordinates> list = (ArrayList<Coordinates>)m.getList();
                saveFile(bitName, list);
                //saveFile(bitName, image)
                //readFile(bitName);
                upload(getExternalCacheDir() + "/" + bitName + ".txt");
                finish();
                return true;

            case R.id.draw_clear:
                m.clear();
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
        float width = source.getWidth();
        float height = source.getHeight();
        float newWidth = viewWidth;
        float newHeight = viewHeight;
        float rate = height / width;

        if(viewHeight / viewWidth > height / width){
            newHeight = (int) (viewWidth * rate);
        }else{
            newWidth = (int) (viewHeight / rate);
        }

        line = (line * newWidth) / width;
        return Bitmap.createScaledBitmap(source, (int)newWidth, (int)newHeight, true);
    }

    public boolean isDrawMode() {
        return isDrawMode;
    }

    // 좌표 리스트를 텍스트 파일로 생성
    public void saveFile(String filename, ArrayList<Coordinates> list) {
        try {
            File dir = new File(getExternalCacheDir().toString());
            //디렉토리 폴더가 없으면 생성함
            if (!dir.exists()) {
                dir.mkdir();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(getExternalCacheDir() + "/" + filename + ".txt", true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (Coordinates c : list) {
                writer.write(String.format("%f;%f;\n", c.getX(), c.getY()));
                writer.flush();
            }

            writer.close();
            fos.close();
        } catch (IOException e) {

        }
    }

    // 서버에 보낼 텍스쳐 생성 후 저장
    public Bitmap saveFile(String filename, Bitmap image) {
        Bitmap result = Bitmap.createBitmap(image, m.getStartX(), m.getStartY(), m.getW(), m.getH());
        try {
            File f = new File(getExternalCacheDir().toString(), filename);
            //디렉토리 폴더가 없으면 생성함

            FileOutputStream out = new FileOutputStream(f);
            result.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (Exception e){

        }
        return null;
    }

    public void readFile(String filename) {
        try {
            File file = new File(getExternalCacheDir() + "/" + filename + ".txt");
            FileReader reader = new FileReader(file);
            char[] buffer = new char[20];

            while (reader.read(buffer) != -1) {
                Log.d("FileReader", String.valueOf(buffer));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, filename + "이 존재하지 않음", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
        }
    }

    private void initClient() {
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
                    Toast.makeText(getApplicationContext(), "전송 완료.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "전송 실패.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}