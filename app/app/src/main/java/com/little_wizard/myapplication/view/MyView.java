package com.little_wizard.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class MyView extends View {
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private final int DROW = 3;

    private int mode;
    private boolean isDrawMode;

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFFFF0000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private ScaleGestureDetector scaleDetector;
    private float mScaleFactor = 1.0f;

    private float width;
    private float height;
    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;

    private float offsetX;
    private float offsetY;

    private float posX1, posX2, posY1, posY2;
    private float oldDist = 1f;
    private float newDist = 1f;

    private int displayHeight;
    private int displayWidth;

    public MyView(Context context, int displayHeight, int displayWidth){
        super(context);
        setupDrawing();
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
           @Override
           public boolean onScale(ScaleGestureDetector detector){
               //final float scale = detector.getScaleFactor();
               mScaleFactor *= detector.getScaleFactor();
               mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 5.0f));
               invalidate();
               return true;
           }
        });
        this.displayHeight = displayHeight;
        this.displayWidth = displayWidth;
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        super.onTouchEvent(event);
        float touchX = 1 / mScaleFactor * event.getX();
        float touchY = 1 / mScaleFactor * event.getY();

        int pointer = event.getPointerCount();
        Log.d("pointer", String.valueOf(pointer));
        Log.d("pointer x1, y1", String.valueOf(event.getX(0)) +", "+String.valueOf(event.getY(0)));
        /*

        Log.d("onTouchEvent pointer", String.valueOf(pointer));
        Log.d("onTouchEvent span : Previous", String.valueOf(scaleDetector.getPreviousSpan()));
        Log.d("onTouchEvent span : Current", String.valueOf(scaleDetector.getCurrentSpan()));
        Log.d("time", String.valueOf(scaleDetector.getTimeDelta()));*/
    switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //TODO: 평행 이동 구현해야함
            case MotionEvent.ACTION_DOWN:
                if(mode == NONE) {
                    Log.d("onTouchEvent Event", "ACTION_DOWN");
                    if(isDrawMode){
                        //drawPath.moveTo(touchX, touchY);
                        drawPath.moveTo(touchX - mPosX, touchY - mPosY);
                        mode = DROW;
                    }else{
                        mLastTouchX = touchX;
                        mLastTouchY = touchY;
                        mode = DRAG;
                    }
                    posX1 = touchX;
                    posY1 = touchY;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG) {
                    final float dx = touchX - mLastTouchX;
                    final float dy = touchY - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;
                    if(mPosX + width > displayWidth){
                        mPosX = displayWidth - width;
                        Log.d("onTouchEvent", "1");
                    }
                    if(mPosX < 0){
                        mPosX = 0;
                        Log.d("onTouchEvent", "2");
                    }

                    if(mPosY + height > displayHeight){
                        mPosY = displayHeight - height;
                        Log.d("onTouchEvent", "3");
                    }
                    if(mPosY < 0){
                        mPosY = 0;
                        Log.d("onTouchEvent", "4");
                    }

                    Log.d("mPosX mPosY", String.valueOf(mPosX) + ", " + String.valueOf(mPosY));
                    Log.d("width height", String.valueOf(width) + ", " + String.valueOf(height));
                    invalidate();
                }else if(mode == DROW){
                    Log.d("onTouchEvent Event", "ACTION_MOVE");
                    //drawPath.lineTo(touchX, touchY);
                    drawPath.lineTo(touchX - mPosX, touchY - mPosY);
                }else{
                    /*newDist = spacing(event);
                    Log.d("zoom", "newDist=" + newDist);
                    Log.d("zoom", "oldDist=" + oldDist);
                    if (newDist - oldDist > 20) { // zoom in
                        oldDist = newDist;
                    } else if(oldDist - newDist > 20) { // zoom out
                        oldDist = newDist;
                    }*/
                }
                mLastTouchX = touchX;
                mLastTouchY = touchY;
                //TODO: 좌표 저장해서 리스트 만들기, (x,y)
                break;
            case MotionEvent.ACTION_UP:
                if(mode == DRAG) {

                }else if(mode == DROW){
                    //drawPath.lineTo(touchX, touchY);
                    drawPath.lineTo(touchX - mPosX, touchY - mPosY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                }
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            /*case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                newDist = spacing(event);
                oldDist = spacing(event);
                break;*/
            default:
                return false;
        }
        invalidate();
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    public Bitmap getBitmap(){
        return canvasBitmap;
    }

    public void setBackgrountBitmap(Bitmap bitmap){
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
        width = canvasBitmap.getWidth();
        height = canvasBitmap.getHeight();
        if(displayHeight / displayWidth > height / width){ //가로가꽉참
            mPosY = (displayHeight - height) / 2;
        }else{
            mPosX = (displayWidth - width) / 2;
        }
    }

    public void setItemMode(boolean mode){
        isDrawMode = mode;
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
