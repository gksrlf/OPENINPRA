package com.little_wizard.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.little_wizard.myapplication.util.Coordinates;
import com.little_wizard.myapplication.util.ObjectBuffer;
import com.little_wizard.myapplication.util.S3Transfer;
import com.little_wizard.myapplication.view.MyView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DrawActivity extends AppCompatActivity implements S3Transfer.TransferCallback, DrawAdapter.ItemClickListener {
    public static final int ASYMMETRY = 1;
    public static final int SYMMETRY = 2;

    private MyView m;
    private boolean isDrawMode = false;
    Display display;
    private int viewHeight;
    private int viewWidth;
    private float line;
    Bitmap bitmap;

    private ObjectBuffer objectBuffer;
    String filepath;
    String filename;

    S3Transfer transfer;

    ProgressDialog progressDialog;
    AlertDialog dialog;
    DrawAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        filepath = getExternalCacheDir() + "/";
        filename = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
        objectBuffer = new ObjectBuffer(filepath, filename);

        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewHeight = size.y;
        viewWidth = size.x;

        transfer = new S3Transfer(this);
        transfer.setCallback(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progressDialog));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

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
        m.setMenu(menu);
        m.setUnClearMenu();
        menu.findItem(R.id.draw_confirmation).setEnabled(false);
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

            case R.id.draw_add:
                m.clear();
                return true;

            case R.id.draw_confirmation:
                m.setConfirmation(true);
                List list = m.getList();
                Bitmap bitmap = m.getCroppedImage().copy(Bitmap.Config.ARGB_8888, true);
                if(list != null){
                    List newList = new ArrayList<Coordinates>();
                    newList.addAll(list);
                    objectBuffer.push(bitmap, newList);
                    Toast.makeText(this, "추가 완료", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.draw_save: //편집한 내역들 확인, 리스트로 표시
                Log.d(this.toString(), m.getBitmap().toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                RecyclerView recycler = new RecyclerView(this);
                adapter = new DrawAdapter(this);
                adapter.setClickListener(this);

                recycler.setLayoutManager(new LinearLayoutManager(this));
                recycler.setAdapter(adapter);
                adapter.setElementList(objectBuffer.getBuffer());
                builder.setView(recycler);
                builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {

                });
                builder.setPositiveButton("UPLOAD", (dialogInterface, i) -> {
                    int pos = 0;
                    String filename = objectBuffer.getName();
                    for(ObjectBuffer.Element e : objectBuffer.getBuffer()){
                        saveFile(String.format("%s-%d.jpg", filename, pos), e.getBitmap());
                        saveFile(String.format("%s-%d.txt", filename, pos), (ArrayList<Coordinates>)e.getList());
                        upload(String.format("%s-%d",filename, pos), "jpg");
                        upload(String.format("%s-%d",filename, pos), "txt");
                        pos++;
                    }
                });
                dialog = builder.create();
                dialog.show();
                return true;

            case R.id.draw_clear:
                m.clear();
                return true;

            default:
                return super.onOptionsItemSelected(item);
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
            FileOutputStream fos = new FileOutputStream(getExternalCacheDir() + "/" + filename, true);
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
        //Bitmap result = Bitmap.createBitmap(image, m.getStartX(), m.getStartY(), m.getW(), m.getH());
        try {
            File f = new File(getExternalCacheDir().toString(), filename);
            //디렉토리 폴더가 없으면 생성함

            FileOutputStream out = new FileOutputStream(f);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
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

    private void upload(String filename, String dataType) {
        File file = new File(String.format("%s/%s.%s", getExternalCacheDir().toString(), filename, dataType));
        transfer.upload(R.string.s3_bucket, file.getName(), file);
    }

    @Override
    public void onStateChanged(TransferState state) {
        switch (state) {
            case IN_PROGRESS:
                progressDialog.show();
                break;
            case COMPLETED:
            case FAILED:
                String text = getString(state == TransferState.COMPLETED ?
                        R.string.transfer_completed : R.string.transfer_failed);
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
                break;
        }
    }

    @Override
    public void onError(int id, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onItemClick(int pos) {
        objectBuffer.remove(pos);
        List<ObjectBuffer.Element> buffer = objectBuffer.getBuffer();
        adapter.setElementList(buffer);
        if (buffer.isEmpty()) {
            dialog.dismiss();
            m.clear();
        }
    }
}