package com.little_wizard.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    Matrix matrix;
    Matrix savedMatrix;
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;
    float matrixArray[];
    float savedMatrixArray[];

    PointF start = new PointF();
    PointF mid = new PointF();
    float scale = 1f;
    float oldDist = 1f, nowDist = 0;
    double oldRadian = 0, nowRadian = 0;
    double oldDegree = 0, nowDegree = 0;
    float magnification = 1f;

    private static final float MIN_ZOOM = 0.7f;
    private static final float MAX_ZOOM = 3.0f;

    public MyImageView(Context context) {
        super(context);
    }

    public void setBitmap(Bitmap bitmap, Display display) {
        Point size = new Point();
        display.getSize(size);
        matrix = new Matrix();
        savedMatrix = new Matrix();
        matrixArray = new float[9];
        savedMatrixArray = new float[9];

        this.bitmap = getResizedBitmap(bitmap, size.x, size.y);
        bitmapDrawable = new BitmapDrawable(this.bitmap);
        setImageDrawable(bitmapDrawable);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pastOldDist = 0;
        float pastNowDist = 0;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                pastOldDist = oldDist;

                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    oldRadian = Math.atan2(event.getY() - mid.y, event.getX() - mid.x);
                    oldDegree = (oldRadian * 180) / Math.PI;
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                } else if (mode == ZOOM) {
                    savedMatrix.getValues(savedMatrixArray);
                    matrix.getValues(matrixArray);
                    magnification = matrixArray[Matrix.MSCALE_X];
                    pastNowDist = nowDist;
                    nowDist = spacing(event);

                    Log.i("magnification", String.valueOf(magnification));
                    if (nowDist > 10f) {
                        matrix.set(savedMatrix);
                        scale = nowDist / oldDist;
                        Log.i("scale", String.valueOf(scale));
                        nowRadian = Math.atan2(event.getY() - mid.y, event.getX() - mid.x);
                        nowDegree = (nowRadian * 180) / Math.PI;
                        Log.i("magnification degree", String.valueOf(nowDegree));
                        /*if(magnification > 3f || magnification < 0.7f){
                            magnification = magnification>3f?3f:0.7f;
                            matrix.setValues(new float[]{
                                    magnification, savedMatrixArray[1], savedMatrixArray[2],
                                    savedMatrixArray[3], magnification, savedMatrixArray[5],
                                    savedMatrixArray[6], savedMatrixArray[7], savedMatrixArray[8]
                            });
                        }else{
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }*/
                        /*if(Math.abs(magnification) > 3f || Math.abs(magnification) < 0.7f){
                            if(magnification > 0){
                                magnification = magnification > 3f?3f:0.7f;
                            }else{
                                magnification = magnification < -3f?-3f:-0.7f;
                            }
                            matrix.postScale(magnification / scale, magnification / scale, mid.x, mid.y);
                            //oldDist = nowDist = 1f;
                            Log.i("magnification", "DSFSDFSDFSDFSDFSDF");
                        }else{
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }*/
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        matrix.postRotate((float) (nowDegree - oldDegree), mid.x, mid.y);

                        Log.i("magnification", String.format("MPERSP_0 %f MPERSP_1 %f MPERSP_2 %f", matrixArray[Matrix.MPERSP_0], matrixArray[Matrix.MPERSP_1], matrixArray[Matrix.MPERSP_2]));
                        Log.i("magnification", String.format("MSCALE_X %f MSKEW_X %f MTRANS_X %f", matrixArray[Matrix.MSCALE_X], matrixArray[Matrix.MSKEW_X], matrixArray[Matrix.MTRANS_X]));
                        Log.i("magnification", String.format("MSCALE_Y %f MSKEW_Y %f MTRANS_Y %f", matrixArray[Matrix.MSCALE_Y], matrixArray[Matrix.MSKEW_Y], matrixArray[Matrix.MTRANS_Y]));
                        Log.i("realDegree", String.valueOf(getRealDegree()));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        setImageMatrix(matrix);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int layoutWidth, int layoutHeight) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float newWidth = layoutWidth;
        float newHeight = layoutHeight;
        float rate = height / width;

        if (layoutHeight / layoutWidth > height / width) {
            newHeight = (int) (layoutWidth * rate);
            Log.d("rate", "1");
        } else {
            newWidth = (int) (layoutHeight / rate);
        }

        return Bitmap.createScaledBitmap(bitmap, (int) newWidth, (int) newHeight, true);
    }

    public float getRealScale(){
        matrix.getValues(matrixArray);
        float scaleX = matrixArray[Matrix.MSCALE_X];
        float skewY = matrixArray[Matrix.MSKEW_Y];
        return (float) Math.sqrt(scaleX * scaleX + skewY * skewY);
    }

    public float getRealDegree(){
        matrix.getValues(matrixArray);
        float scaleX = matrixArray[Matrix.MSCALE_X];
        float skewX = matrixArray[Matrix.MSKEW_X];
        return -Math.round(Math.atan2(skewX, scaleX) * (180 / Math.PI));
    }

    public float getRealRadians(){
        matrix.getValues(matrixArray);
        float scaleX = matrixArray[Matrix.MSCALE_X];
        float skewX = matrixArray[Matrix.MSKEW_X];
        return -(float)Math.atan2(skewX, scaleX);
    }

    // 사진 안에 y축 있는지 검사
    protected boolean baselineCheck(){
        float points[][] = {{0, 0}, {bitmap.getWidth(), 0},
                {0, bitmap.getHeight()}, {bitmap.getWidth(), bitmap.getHeight()}};
        float rotaionPoints[][] = {{0, 0}, {0, 0}, {0, 0}, {0, 0}};
        float degree = getRealDegree();
        float radians = getRealRadians();

        for(int i=0;i<4;i++){
            rotaionPoints[i][0] = (float)Math.cos(radians) * points[i][0] - (float)Math.sin(radians) * points[i][1];
            rotaionPoints[i][1] = (float)Math.sin(radians) * points[i][0] + (float)Math.cos(radians) * points[i][1];
        }

        if(-90 < degree && degree < 90){

        }else{

        }
        return false;
    }

    public Bitmap getResult() {
        //기존 비트맵 이미지 좌표
        float points[][] = {{0, 0}, {bitmap.getWidth(), 0},
                {0, bitmap.getHeight()}, {bitmap.getWidth(), bitmap.getHeight()}};
        float rotaionPoints[][] = {{0, 0}, {0, 0}, {0, 0}, {0, 0}};
        /*matrix.getValues(matrixArray);

        // calculate real scale
        float scaleX = matrixArray[Matrix.MSCALE_X];
        float skewY = matrixArray[Matrix.MSKEW_Y];
        float realScale = (float) Math.sqrt(scaleX * scaleX + skewY * skewY);

        // calculate the degree of rotation
        float skewX = matrixArray[Matrix.MSKEW_X];
        float realAngle = -Math.round(Math.atan2(skewX, scaleX) * (180 / Math.PI));
        float realRadians = -(float)Math.atan2(skewX, scaleX);
        Log.i("magnification", String.format("realScale : %f, realAngle : %f", realScale, realAngle));
*/
        float radians = getRealRadians();
        float degree = getRealDegree();

        float maxX, minX, maxY, minY;
        maxX = maxY = Float.MIN_VALUE;
        minX = minY = Float.MAX_VALUE;

        // 회전 후 비트맵 이미지 꼭짓점의 좌표
        // 0  1
        // 2  3
        for(int i=0;i<4;i++){
            rotaionPoints[i][0] = (float)Math.cos(radians) * points[i][0] - (float)Math.sin(radians) * points[i][1];
            rotaionPoints[i][1] = (float)Math.sin(radians) * points[i][0] + (float)Math.cos(radians) * points[i][1];

            maxX = maxX < rotaionPoints[i][0] ? rotaionPoints[i][0] : maxX;
            minX = minX > rotaionPoints[i][0] ? rotaionPoints[i][0] : minX;
            maxY = maxY < rotaionPoints[i][1] ? rotaionPoints[i][1] : maxY;
            minY = minY > rotaionPoints[i][1] ? rotaionPoints[i][1] : minY;
        }

        /*if(minX < 0){
            for(int i=0;i<4;i++){
                rotaionPoints[i][0] += Math.abs(minX);
            }
            maxX += Math.abs(minX);
            minX = 0;
        }
        if(minY < 0){
            for(int i=0;i<4;i++){
                rotaionPoints[i][1] += Math.abs(minY);
            }
            maxY += Math.abs(minY);
            minY = 0;
        }*/

        float h = maxY - minY;
        float w = maxX - minX;

        // 이미지 회전 유지한 채로 새로운 비트맵 생성
        Matrix m = new Matrix();
        m.postRotate(degree, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        m.postTranslate((w - bitmap.getWidth()) / 2, (h - bitmap.getHeight()) / 2);

        // 뒷 배경 생성
        Bitmap background = Bitmap.createBitmap((int)w, (int)h, Bitmap.Config.ARGB_8888);
        background.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(background);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(bitmap, m, null);
        return background;
    }

}
