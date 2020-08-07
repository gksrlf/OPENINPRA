package com.little_wizard.myapplication.view;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.little_wizard.myapplication.R;
import com.little_wizard.myapplication.util.Coordinates;
import com.little_wizard.myapplication.util.DrawQueue;

import java.util.ArrayList;
import java.util.List;

public class MyView extends View {
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private final int DROW = 3;

    private static int pointCount = 0;
    private final int pick = 5;

    private DrawQueue drawQueue;

    private int mode;
    private boolean isDrawMode;

    private Path drawPath, viewPath;
    private Paint drawPaint, viewPaint, canvasPaint;
    private int paintColor = 0xFFFF0000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap, originalBitmap;
    private List list;
    private int paintWidth = 20;

    private float width;
    private float height;
    private float originalWidth;
    private float originalHeight;
    private float mPosX;
    private float mPosY;

    private float magification = 1f;

    private float offsetX;
    private float offsetY;

    private float posX1, posX2, posY1, posY2;
    private float oldDist = 1f;
    private float newDist = 1f;
    private float scale = 1f;

    private int displayHeight;
    private int displayWidth;
    private float startX, startY;

    Menu activityMenu;
    private boolean confirmation;

    public MyView(Context context, int displayHeight, int displayWidth) {
        super(context);
        setupDrawing();
        this.displayHeight = displayHeight;
        this.displayWidth = displayWidth;
        drawQueue = new DrawQueue();
    }

    private void setupDrawing() {
        drawPath = new Path();
        viewPath = new Path();

        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        //drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        viewPaint = new Paint();
        viewPaint.setColor(paintColor);
        //viewPaint.setAntiAlias(true);
        viewPaint.setStrokeWidth(20);
        viewPaint.setStyle(Paint.Style.STROKE);
        viewPaint.setStrokeJoin(Paint.Join.ROUND);
        viewPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        Log.d("scale", String.valueOf(scale));
        Rect dst = new Rect((int) mPosX, (int) mPosY, (int) (mPosX + width), (int) (mPosY + height));
        canvas.drawBitmap(canvasBitmap, null, dst, canvasPaint);
        viewPaint.setStrokeWidth(paintWidth * magification);
        canvas.drawPath(viewPath, viewPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float absX = 1 / magification * (event.getX() - mPosX);
        float absY = 1 / magification * (event.getY() - mPosY);
        Log.d("onTouchEvent absolute", String.format("%f, %f", absX, absY));
        Bitmap previousBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        ArrayList<Coordinates> list = new ArrayList<>();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mode == NONE) {
                    Log.d("onTouchEvent Event", "ACTION_DOWN");
                    if(!isInPicture(event)) break;
                    if (isDrawMode && confirmation == false) {
                        Coordinates lastPoint = drawQueue.getLastPoint();
                        if(lastPoint != null){
                            drawPath.moveTo(lastPoint.getX(), lastPoint.getY());
                            viewPath.moveTo(lastPoint.getX() * magification + mPosX, lastPoint.getY() * magification + mPosY);
                        }else{
                            startX = absX;
                            startY = absY;
                            drawPath.moveTo(absX, absY);
                            viewPath.moveTo(event.getX(), event.getY());
                        }
                        list.add(new Coordinates(absX, absY));

                        /*pointCount++;
                        if (pointCount % pick == 0) {
                            pointCount = 0;
                            Log.d("getPointer", String.valueOf(absX) + "," + String.valueOf(absY));
                        }*/

                        mode = DROW;
                    } else {
                        posX1 = (int) event.getX();
                        posY1 = (int) event.getY();
                        offsetX = posX1 - mPosX;
                        offsetY = posY1 - mPosY;

                        Log.d("zoom", "mode=DRAG");

                        mode = DRAG;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    if(isInPicture(event)){
                        mPosX = posX2 - offsetX;
                        mPosY = posY2 - offsetY;
                        posX2 = (int) event.getX();
                        posY2 = (int) event.getY();
                        if (Math.abs(posX2 - posX1) > 20 || Math.abs(posY2 - posY1) > 20) {
                            posX1 = posX2;
                            posY1 = posY2;
                            Log.d("drag", "mode=DRAG");
                        }
                    }
                    /*
                    float pmPosX = mPosX;
                    float pmPosY = mPosX;

                    mPosX=posX2-offsetX;
                    mPosY=posY2-offsetY;

                    float minX = 0, maxX = 0, minY = 0, maxY = 0;

                    if(displayWidth < width){
                        minX = displayWidth - width;
                    }else{
                        maxX = displayWidth - width;
                    }
                    if(displayHeight < height){
                        minY = displayHeight - height;
                    }else{
                        maxY = displayHeight - height;
                    }

                    if(minX <= mPosX && mPosX <= maxX) {
                        posX2 = (int) event.getX();
                        if (Math.abs(posX2 - posX1) > 20) {
                            posX1 = posX2;
                            Log.d("drag", "mode=DRAG");
                        }
                    }else{
                        mPosX = pmPosX;
                    }
                    if(minY <= mPosY && mPosY <= maxY) {
                        posY2 = (int) event.getY();
                        if (Math.abs(posY2 - posY1) > 20) {
                            posY1 = posY2;
                            Log.d("drag", "mode=DRAG");
                        }
                    }else{
                        mPosY = pmPosY;
                    }*/

                } else if (mode == DROW && confirmation == false) {
                    if(isInPicture(event)){
                        viewPath.lineTo(event.getX(), event.getY());
                        pointCount++;
                        if (pointCount % pick == 0) {
                            drawPath.lineTo(absX, absY);
                            list.add(new Coordinates(absX, absY));
                            pointCount = 0;
                            Log.d("getPointer", String.valueOf(absX) + "," + String.valueOf(absY));
                        }
                    }else{ //draw상태에서 사진 범위 넘어갔을 때
                        mode = NONE;
                        list.add(new Coordinates(absX, absY));
                        drawPath.lineTo(absX, absY);
                        viewPath.lineTo(event.getX(), event.getY());
                        drawCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                        viewPath.reset();
                        drawQueue.push(previousBitmap, list);
                    }
                } else {
                    magification = width / originalWidth;
                    float pastDist = newDist;
                    newDist = spacing(event);
                    if (newDist - oldDist > 20) {  // zoom in
                        if (magification > 3f) {
                            newDist = pastDist;
                        } else {
                            scale = (float) Math.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (height * height + width * width));
                            mPosY = mPosY - (height * scale / 2);
                            mPosX = mPosX - (width * scale / 2);

                            height = height * (1 + scale);
                            width = width * (1 + scale);

                            oldDist = newDist;
                        }

                    } else if (oldDist - newDist > 20) {  // zoom out
                        if (magification <= 1f) {
                            newDist = pastDist;
                        } else {
                            scale = (float) Math.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (height * height + width * width));
                            scale = 0 - scale;
                            mPosY = mPosY - (height * scale / 2);
                            mPosX = mPosX - (width * scale / 2);

                            height = height * (1 + scale);
                            width = width * (1 + scale);

                            oldDist = newDist;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mode == DROW && confirmation == false) {
                    list.add(new Coordinates(absX, absY));
                    drawPath.lineTo(absX, absY);
                    viewPath.lineTo(event.getX(), event.getY());
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    viewPath.reset();
                    drawQueue.push(previousBitmap, list);
                }
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                newDist = spacing(event);
                oldDist = spacing(event);
                break;
            default:
                return false;
        }
        invalidate();

        Log.d("sibal", String.format("mPosX: %f /mPosY: %f /width: %f /height: %f /offsetX: %f /offsetY: %f", mPosX, mPosY, width, height, offsetX, offsetY));
        return true;
    }

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return oldDist;
        }
    }

    public Bitmap getBitmap() {
        return canvasBitmap;
    }

    public void setBackgrountBitmap(Bitmap bitmap) {
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
        originalWidth = width = canvasBitmap.getWidth();
        originalHeight = height = canvasBitmap.getHeight();
        if (displayHeight / displayWidth > height / width) { //가로가꽉참
            mPosY = (displayHeight - height) / 2;
        } else {
            mPosX = (displayWidth - width) / 2;
        }
        drawQueue.push(bitmap, null);
    }

    public void setItemMode(boolean mode) {
        isDrawMode = mode;
    }

    public List getList() {
        Log.d("getListFunc", list.toString());
        return drawQueue.getResult();
    }

    public void undo(){
        Bitmap previousBitmap = drawQueue.getPreviousBitmap().copy(Bitmap.Config.ARGB_8888, true);
        drawQueue.undo();

        drawCanvas.save();
        drawCanvas.drawBitmap(previousBitmap, 0, 0, null);
        drawCanvas.restore();
        invalidate();
        setConfirmation(false);
        if(drawQueue.size() <= 1){
            activityMenu.getItem(R.id.draw_undo).setEnabled(false);
        }
    }
    public void clear(){
        drawQueue.clear();
        setBackgrountBitmap(originalBitmap);
        invalidate();
    }

    protected boolean isInPicture(MotionEvent e){
        return (mPosX <= e.getX() && e.getX() <= mPosX + width && mPosY <= e.getY() && e.getY() <= mPosY + height)?true:false;
    }

    public void setMenu(Menu menu){
        activityMenu = menu;
    }

    protected void setStatusMenuItem(int itemId, Boolean status){
        activityMenu.getItem(itemId).setEnabled(status);
        switch(itemId){
            case R.id.draw_undo:
                break;
            case R.id.draw_save:
                break;
        }
    }
    public void setConfirmation(Boolean status){
        confirmation = status;
        if(status == true){
            Bitmap previousBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
            ArrayList<Coordinates> lastPoint = new ArrayList<>();
            lastPoint.add(new Coordinates(startX, startY));

            activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
            activityMenu.findItem(R.id.draw_confirmation).setVisible(false);
            activityMenu.findItem(R.id.draw_save).setEnabled(true);
            activityMenu.findItem(R.id.draw_save).setVisible(true);

            drawPath.moveTo(startX, startY);
            drawCanvas.drawPath(drawPath, drawPaint);
            drawQueue.push(previousBitmap, lastPoint);
            drawPath.reset();
            invalidate();
        }else{
            activityMenu.findItem(R.id.draw_confirmation).setEnabled(true);
            activityMenu.findItem(R.id.draw_confirmation).setVisible(true);
            activityMenu.findItem(R.id.draw_save).setEnabled(false);
            activityMenu.findItem(R.id.draw_save).setVisible(false);
        }
    }
}