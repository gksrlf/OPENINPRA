package com.little_wizard.tdc.ui.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import com.little_wizard.tdc.R;
import com.little_wizard.tdc.util.draw.Coordinates;
import com.little_wizard.tdc.util.draw.DrawQueue;

import java.util.ArrayList;
import java.util.List;

public class MyView extends View {
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private final int DROW = 3;

    public static final int ASYMMETRY = 1;
    public static final int SYMMETRY = 2;

    private static int pointCount = 0;
    private final int pick = 3;

    private DrawQueue drawQueue;

    private int mode;
    private boolean isDrawMode;

    private Path drawPath, viewPath;
    private Paint drawPaint, viewPaint, canvasPaint;
    private int paintColor = 0xFFFF0000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap, originalBitmap;
    private int paintWidth = 5;

    private float width;
    private float height;
    private float originalWidth;
    private float mPosX;
    private float mPosY;

    private float magnification = 1f;

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
    private int photo_mode;
    private float line;
    private float originalLine;

    ArrayList<Coordinates> list = new ArrayList<>();

    public MyView(Context context, int displayHeight, int displayWidth, int mode) {
        super(context);
        setupDrawing();
        this.displayHeight = displayHeight;
        this.displayWidth = displayWidth;
        drawQueue = new DrawQueue();
        confirmation = false;
        photo_mode = mode;
    }

    private void setupDrawing() {
        drawPath = new Path();
        viewPath = new Path();

        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setStrokeWidth(paintWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        viewPaint = new Paint();
        viewPaint.setColor(paintColor);
        //viewPaint.setAntiAlias(true);
        viewPaint.setStrokeWidth(paintWidth);
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
        viewPaint.setStrokeWidth(paintWidth);
        canvas.drawPath(viewPath, viewPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float absX = 1 / magnification * (event.getX() - mPosX);
        float absY = 1 / magnification * (event.getY() - mPosY);
        Log.d("onTouchEvent absolute", String.format("%f, %f", absX, absY));
        Bitmap previousBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //ArrayList<Coordinates> list = new ArrayList<>();
        Log.d("confirmation", String.format("%b", confirmation));
        Coordinates lastPoint = drawQueue.getLastPoint();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mode == NONE) {
                    Log.d("onTouchEvent Event", "ACTION_DOWN");
                    if (!isDraggable(event)) break;
                    if (isDrawMode && !confirmation) { //TODO: 대칭일때 첫번째 점 처리
                        if(!isInPicture(event)) break;
                        if (lastPoint != null) {
                            drawPath.moveTo(lastPoint.getX(), lastPoint.getY());
                            viewPath.moveTo(lastPoint.getX() * magnification + mPosX, lastPoint.getY() * magnification + mPosY);
                        } else { // 첫번째 터치
                            startX = absX;
                            startY = absY;
                            if (photo_mode == SYMMETRY) {
                                drawPath.moveTo(originalLine , absY);
                                drawPath.lineTo(absX, absY);
                            } else {
                                drawPath.moveTo(absX, absY);
                            }
                            viewPath.moveTo(event.getX(), event.getY());
                        }
                        list.add(new Coordinates(absX, absY));
                        mode = DROW;
                    } else {
                        posX1 = (int) event.getX();
                        posY1 = (int) event.getY();
                        offsetX = posX1 - mPosX;
                        offsetY = posY1 - mPosY;

                        mode = DRAG;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    if (isDraggable(event)) {
                        mPosX = posX2 - offsetX;
                        mPosY = posY2 - offsetY;
                        posX2 = (int) event.getX();
                        posY2 = (int) event.getY();
                        if (Math.abs(posX2 - posX1) > 20 || Math.abs(posY2 - posY1) > 20) {
                            posX1 = posX2;
                            posY1 = posY2;
                        }
                    }
                } else if (mode == DROW && !confirmation) {
                    if (isInPicture(event)) {
                        viewPath.lineTo(event.getX(), event.getY());
                        pointCount++;
                        if (pointCount % pick == 0) {
                            drawPath.lineTo(absX, absY);
                            list.add(new Coordinates(absX, absY));
                            pointCount = 0;
                            Log.d("getPointer", String.valueOf(absX) + "," + String.valueOf(absY));
                        }
                    } else { //draw상태에서 사진 범위 넘어갔을 때
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
                    magnification = width / originalWidth;
                    float pastDist = newDist;
                    newDist = spacing(event);
                    if (newDist - oldDist > 20) {  // zoom in
                        if (magnification > 3f) {
                            newDist = pastDist;
                        } else {
                            scale = (float) Math.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (height * height + width * width));
                            mPosY = mPosY - (height * scale / 2);
                            mPosX = mPosX - (width * scale / 2);

                            height = height * (1 + scale);
                            width = width * (1 + scale);
                            line = (originalLine * width) / originalWidth;

                            oldDist = newDist;
                        }

                    } else if (oldDist - newDist > 20) {  // zoom out
                        if (magnification <= 1f) {
                            newDist = pastDist;
                        } else {
                            scale = (float) Math.sqrt(((newDist - oldDist) * (newDist - oldDist)) / (height * height + width * width));
                            scale = 0 - scale;
                            mPosY = mPosY - (height * scale / 2);
                            mPosX = mPosX - (width * scale / 2);

                            height = height * (1 + scale);
                            width = width * (1 + scale);
                            line = (originalLine * width) / originalWidth;

                            oldDist = newDist;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mode == DROW && !confirmation) {
                    list.add(new Coordinates(absX, absY));
                    drawPath.lineTo(absX, absY);
                    viewPath.lineTo(event.getX(), event.getY());
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    viewPath.reset();
                    drawQueue.push(previousBitmap, list);
                    list.clear();
                    if(drawQueue.isClear()){
                        setClearMenu();
                    }else{
                        setUnClearMenu();
                    }
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

    public void setBackgroundBitmap(Bitmap bitmap) {
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
        originalWidth = width = canvasBitmap.getWidth();
        height = canvasBitmap.getHeight();
        if (displayHeight / displayWidth > height / width) { //가로가꽉참
            mPosY = (displayHeight - height) / 2;
        } else {
            mPosX = (displayWidth - width) / 2;
        }
        drawQueue.push(bitmap, null);
        line = originalLine;
        scale = 1f;
    }

    public void setItemMode(boolean mode) {
        isDrawMode = mode;
    }

    public List getList() {
        return drawQueue.getResult();
    }

    public void undo() {
        Bitmap previousBitmap = drawQueue.getPreviousBitmap().copy(Bitmap.Config.ARGB_8888, true);
        drawQueue.undo();

        drawCanvas.save();
        drawCanvas.drawBitmap(previousBitmap, 0, 0, null);
        drawCanvas.restore();
        invalidate();
        setConfirmation(false);
        if(drawQueue.isClear()){
            activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
        }
    }

    public void clear() {
        drawQueue.clear();
        setConfirmation(false);
        activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
        setBackgroundBitmap(originalBitmap);
        invalidate();
    }

    protected boolean isInPicture(MotionEvent e) {
        if (photo_mode == ASYMMETRY) {
            return (mPosX <= e.getX() && e.getX() <= mPosX + width && mPosY <= e.getY() && e.getY() <= mPosY + height) ? true : false;
        } else {
            return (mPosX <= e.getX() && e.getX() <= mPosX + line && mPosY <= e.getY() && e.getY() <= mPosY + height) ? true : false;
        }
    }

    protected boolean isDraggable(MotionEvent e) {
        return (mPosX <= e.getX() && e.getX() <= mPosX + width && mPosY <= e.getY() && e.getY() <= mPosY + height) ? true : false;
    }

    public void setMenu(Menu menu) {
        activityMenu = menu;
    }

    public void setConfirmation(Boolean status) {
        confirmation = status;
        if (status == true) {
            Bitmap previousBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
            ArrayList<Coordinates> point = new ArrayList<>();
            setClearMenu();
            Coordinates lastPoint = drawQueue.getLastPoint();
            //TODO: 대칭일때 마지막 점 처리 (완료)
            drawPath.moveTo(lastPoint.getX(), lastPoint.getY());
            if (photo_mode == ASYMMETRY) {
                point.add(new Coordinates(startX, startY));
                drawPath.lineTo(startX, startY);
            } else {
                point.add(new Coordinates(originalLine, lastPoint.getY()));
                drawPath.lineTo(originalLine, lastPoint.getY());
            }
            drawCanvas.drawPath(drawPath, drawPaint);

            drawQueue.push(previousBitmap, point);
            drawPath.reset();
            invalidate();
        } else {
            setUnClearMenu();
        }
    }

    public void setClearMenu(){
        activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
        activityMenu.findItem(R.id.draw_confirmation).setVisible(false);
        activityMenu.findItem(R.id.draw_add).setEnabled(true);
        activityMenu.findItem(R.id.draw_add).setVisible(true);
        activityMenu.findItem(R.id.draw_save).setEnabled(true);
        activityMenu.findItem(R.id.draw_save).setVisible(true);
    }

    public void setUnClearMenu(){
        activityMenu.findItem(R.id.draw_confirmation).setEnabled(true);
        activityMenu.findItem(R.id.draw_confirmation).setVisible(true);
        activityMenu.findItem(R.id.draw_add).setEnabled(false);
        activityMenu.findItem(R.id.draw_add).setVisible(false);
        activityMenu.findItem(R.id.draw_save).setEnabled(false);
        activityMenu.findItem(R.id.draw_save).setVisible(false);
    }

    public void setLine(float line) {
        originalLine = this.line = line;
    }

    public int getStartX(){
        return drawQueue.getStartX();
    }

    public int getStartY(){
        return drawQueue.getStartY();
    }

    public int getH(){
        return drawQueue.getHeight();
    }

    public int getW(){
        return drawQueue.getWidth();
    }

    public Bitmap getCroppedImage(){
        return Bitmap.createBitmap(originalBitmap, drawQueue.getStartX(), drawQueue.getStartY(), drawQueue.getWidth(), drawQueue.getHeight());
    }
}