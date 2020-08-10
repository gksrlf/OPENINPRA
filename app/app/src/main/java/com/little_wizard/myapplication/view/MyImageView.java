package com.little_wizard.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
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

    public void setBitmap(Bitmap bitmap, Display display){
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

        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                pastOldDist = oldDist;

                if(oldDist > 10f){
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    oldRadian = Math.atan2(event.getY() - mid.y, event.getX() - mid.x);
                    oldDegree = (oldRadian * 180) / Math.PI;
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG){
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }else if(mode == ZOOM){
                    savedMatrix.getValues(savedMatrixArray);
                    matrix.getValues(matrixArray);
                    magnification = matrixArray[Matrix.MSCALE_X];
                    pastNowDist = nowDist;
                    nowDist = spacing(event);

                    Log.i("magnification", String.valueOf(magnification));
                    if(nowDist > 10f){
                        matrix.set(savedMatrix);
                        scale = nowDist / oldDist;
                        Log.i("scale", String.valueOf(scale));
                        nowRadian = Math.atan2(event.getY() - mid.y, event.getX() - mid.x);
                        nowDegree = (nowRadian * 180) / Math.PI;
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

    private float spacing(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float)Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event){
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    public Bitmap getResizedBitmap(Bitmap bitmap, int layoutWidth, int layoutHeight){
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        float newWidth = layoutWidth;
        float newHeight = layoutHeight;
        float rate = height / width;

        if(layoutHeight / layoutWidth > height / width){
            newHeight = (int) (layoutWidth * rate);
            Log.d("rate", "1");
        }else{
            newWidth = (int) (layoutHeight / rate);
        }

        return Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, true);
    }

    public Bitmap getResult(){
        //buildDrawingCache();
        //Bitmap result = getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }
}
