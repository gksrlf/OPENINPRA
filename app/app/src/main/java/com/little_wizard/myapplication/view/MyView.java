package com.little_wizard.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class MyView extends View {
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int MULTI = 2;
    private int mode;

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFFFF0000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private ScaleGestureDetector scaleDetector;
    private float mScaleFactor = 1.0f;
    private Matrix matrix;

    private int mPosX;
    private int mPosY;

    public MyView(Context context){

        super(context);
        setupDrawing();
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener(){
           @Override
           public boolean onScale(ScaleGestureDetector detector){
               mScaleFactor *= detector.getScaleFactor();
               mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

               invalidate();
               return true;
           }
        });

        matrix = new Matrix();
    }

    private void setupDrawing(){

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        //drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(25);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged( w, h, oldw, oldh);
        //canvasBitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888);
        //drawCanvas = new Canvas(canvasBitmap);
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
        canvas.restore();
        //drawCanvas.scale()
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        float touchX = 1 / mScaleFactor * event.getX();
        float touchY = 1 / mScaleFactor * event.getY();
        int pointer = event.getPointerCount();

        Log.d("onTouchEvent", String.valueOf((mScaleFactor)));
        Log.d("onTouchEvent touchX", String.valueOf(touchX));
        Log.d("onTouchEvent touchY", String.valueOf(touchY));
        Log.d("onTouchEvent focusX", String.valueOf(scaleDetector.getFocusX()));
        Log.d("onTouchEvent currentSpanX", String.valueOf(scaleDetector.getCurrentSpanX()));

        switch (event.getAction()) {
            //TODO: 평행 이동 구현해야함
            case MotionEvent.ACTION_DOWN:
                if(pointer == 1) {
                    drawPath.moveTo(touchX, touchY);
                    mode = DRAG;
                }else if(pointer == 2){
                    mode = MULTI;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG) {
                    drawPath.lineTo(touchX, touchY);
                }else if(mode == MULTI){

                }
                break;
            case MotionEvent.ACTION_UP:
                if(mode == DRAG) {
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                }
                mode = NONE;
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public Bitmap getBitmap(){
        return canvasBitmap;
    }

    public void setBackgrountBitmap(Bitmap bitmap){
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
    }
/*
    public class MovingUnit{
        //이미지
        private Bitmap Image;

        private float X;
        private float Y;

        private float Width;
        private float Height;

        //처음 이미지를 선택했을 때, 이미지의 X,Y 값과 클릭 지점 간의 거리
        private float offsetX;
        private float offsetY;

        // 드래그시 좌표 저장

        int posX1=0, posX2=0, posY1=0, posY2=0;

        // 핀치시 두좌표간의 거리 저장

        float oldDist = 1f;
        float newDist = 1f;

        // 드래그 모드인지 핀치줌 모드인지 구분
        static final int NONE = 0;
        static final int DRAG = 1;
        static final int ZOOM = 2;

        int mode = NONE;


        //Image를 인자로 받는다.
        public MovingUnit(Bitmap Image) {
            // TODO Auto-generated constructor stub
            this.Image=Image;

            setSize(Image.getHeight(),Image.getWidth());
            setXY(0,0);

        }

        public void TouchProcess(MotionEvent event) {
            float touchX = event.getX();
            float touchY = event.getY();

            int act = event.getAction();
            switch(act & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치
                    if(InObject(event.getX(), event.getY())){//손가락 터치 위치가 이미지 안에 있으면 DragMode가 시작된다.
                        posX1 = (int) event.getX();
                        posY1 = (int) event.getY();
                        offsetX=posX1-X;
                        offsetY=posY1-Y;

                        Log.d("zoom", "mode=DRAG" );

                        mode = DRAG;
                    }
                    mode = DRAG;
                    drawPath.moveTo(touchX, touchY);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(mode == DRAG) {   // 드래그 중이면, 이미지의 X,Y값을 변환시키면서 위치 이동.
                        X=posX2-offsetX;
                        Y=posY2-offsetY;
                        posX2 = (int) event.getX();
                        posY2 = (int) event.getY();
                        if(Math.abs(posX2-posX1)>20 || Math.abs(posY2-posY1)>20) {
                            posX1 = posX2;
                            posY1 = posY2;
                            Log.d("drag","mode=DRAG");
                        }
                    if(mode == DRAG){
                        drawPath.lineTo(touchX, touchY);
                    } else if (mode == ZOOM) {    // 핀치줌 중이면, 이미지의 거리를 계산해서 확대를 한다.
                        newDist = spacing(event);

                        if (newDist - oldDist > 20) {  // zoom in
                            float scale = (float)Math.sqrt(((newDist-oldDist)*(newDist-oldDist))/(Height*Height + Width * Width));
                            Y=Y-(Height*scale/2);
                            X=X-(Width*scale/2);

                            Height=Height*(1+scale);
                            Width=Width*(1+scale);

                            oldDist = newDist;

                        } else if(oldDist - newDist > 20) {  // zoom out
                            float scale = (float)Math.sqrt(((newDist-oldDist)*(newDist-oldDist))/(Height*Height + Width * Width));
                            scale=0-scale;
                            Y=Y-(Height*scale/2);
                            X=X-(Width*scale/2);

                            Height=Height*(1+scale);
                            Width=Width*(1+scale);

                            oldDist = newDist;
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
                case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
                    if(mode == DRAG){
                        drawPath.lineTo(touchX, touchY);
                        drawCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                    }
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
                    mode = ZOOM;
                    newDist = spacing(event);
                    oldDist = spacing(event);
                    Log.d("zoom", "newDist=" + newDist);
                    Log.d("zoom", "oldDist=" + oldDist);
                    Log.d("zoom", "mode=ZOOM");

                    break;
                case MotionEvent.ACTION_CANCEL:
                default :
                    break;
            }

        }
        //Rect 형태로 넘겨준다.
        public Rect getRect(){
            Rect rect=new Rect();
            rect.set((int)X,(int)Y, (int)(X+Width), (int)(Y+Height));
            return rect;
        }

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float)Math.sqrt(x * x + y * y);

        }
        public boolean InObject(float eventX,float eventY){
            if(eventX<(X+Width+30) &&  eventX>X-30 && eventY<Y+Height+30 &&="" eventY="">Y-30){
                return true;
            }
            return false;
        }
        public void setSize(float Height,float Width){
            this.Height=Height;
            this.Width=Width;

        }
        public void setXY(float X, float Y){
            this.X=X;
            this.Y=Y;
        }
        public Bitmap getImage(){
            return Image;
        }
    }

    //출처: https://90000e.tistory.com/15 [구만이의 어린 프로그래밍]*/
}
