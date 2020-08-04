package com.little_wizard.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.little_wizard.myapplication.util.Coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyView extends View {
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private final int DROW = 3;

    private static int pointCount = 0;
    private final int pick = 10;
    List list;

    private int mode;
    private boolean isDrawMode;

    private Path drawPath, viewPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFFFF0000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private ScaleGestureDetector scaleDetector; // span, 중점 구하기 용으로만 사용
    private float mScaleFactor = 1.0f;

    private float width;
    private float height;
    private float originalWidth;
    private float originalHeight;
    private float mPosX;
    private float mPosY;
    private float mLastPosX;
    private float mLastPosY;

    private float magification = 1f;

    private float mLastTouchX;
    private float mLastTouchY;

    private float offsetX;
    private float offsetY;

    private float posX1, posX2, posY1, posY2;
    private float oldDist = 1f;
    private float newDist = 1f;
    private float scale = 1f;
    private float lastScale = 1f;

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
                Log.d("detector getFocusX", String.valueOf(detector.getFocusX()));
                Log.d("detector getFocusY", String.valueOf(detector.getFocusY()));
                //invalidate();
                return false;
            }
        });
        this.displayHeight = displayHeight;
        this.displayWidth = displayWidth;
        list = new ArrayList<Coordinates>();
    }

    private void setupDrawing(){

        drawPath = new Path();
        viewPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        //drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged( w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        Log.d("scale", String.valueOf(scale));
        Rect dst = new Rect((int)mPosX, (int)mPosY, (int)(mPosX + width), (int)(mPosY + height));
        canvas.drawBitmap(canvasBitmap, null, dst, canvasPaint);
        canvas.drawPath(viewPath, drawPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //float touchX = 1 / mScaleFactor * event.getX();
        //float touchY = 1 / mScaleFactor * event.getY();
        float absX =  1 / magification * (event.getX() - mPosX);
        float absY =  1 / magification * (event.getY() - mPosY);
        Log.d("onTouchEvent absolute", String.format("%f, %f", absX, absY));

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(mode == NONE) {
                    Log.d("onTouchEvent Event", "ACTION_DOWN");
                    if(isDrawMode){
                        drawPath.moveTo(absX, absY);
                        viewPath.moveTo(event.getX(), event.getY());

                        pointCount++;
                        if(pointCount % pick == 0){
                            list.add(new Coordinates(absX / 1000, absY / 1000));
                            pointCount = 0;
                            Log.d("getPointer", String.valueOf(absX) + "," + String.valueOf(absY));
                        }

                        mode = DROW;
                    }else{
                        posX1 = (int) event.getX();
                        posY1 = (int) event.getY();
                        offsetX=posX1-mPosX;
                        offsetY=posY1-mPosY;

                        Log.d("zoom", "mode=DRAG" );

                        mode = DRAG;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG) {
                    mPosX=posX2-offsetX;
                    mPosY=posY2-offsetY;
                    posX2 = (int) event.getX();
                    posY2 = (int) event.getY();
                    if(Math.abs(posX2-posX1)>20 || Math.abs(posY2-posY1)>20) {
                        posX1 = posX2;
                        posY1 = posY2;
                        Log.d("drag","mode=DRAG");
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

                }else if(mode == DROW){
                    Log.d("onTouchEvent Event", "ACTION_MOVE");
                    //drawPath.lineTo(touchX, touchY);
                    drawPath.lineTo(absX, absY);
                    viewPath.lineTo(event.getX(), event.getY());
                    pointCount++;
                    if(pointCount % pick == 0){
                        list.add(new Coordinates(absX / 1000, absY / 1000));
                        pointCount = 0;
                        Log.d("getPointer", String.valueOf(absX) + "," + String.valueOf(absY));
                    }
                }else{
                    magification = width / originalWidth;
                    float pastDist = newDist;
                    newDist = spacing(event);
                    if (newDist - oldDist > 20) {  // zoom in
                        if(magification > 3f){
                            newDist = pastDist;
                        }else{
                            scale = (float)Math.sqrt(((newDist-oldDist)*(newDist-oldDist))/(height*height + width * width));
                            mPosY=mPosY-(height*scale/2);
                            mPosX=mPosX-(width*scale/2);

                            height=height*(1+scale);
                            width=width*(1+scale);

                            oldDist = newDist;
                        }

                    } else if(oldDist - newDist > 20) {  // zoom out
                        if(magification <= 1f){
                            newDist = pastDist;
                        }else {
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
                //TODO: 좌표 저장해서 리스트 만들기, (x,y)
                break;
            case MotionEvent.ACTION_UP:
                if(mode == DRAG) {

                }else if(mode == DROW){
                    //drawPath.lineTo(touchX, touchY);
                    pointCount++;
                    if(pointCount % pick == 0){
                        list.add(new Coordinates(absX / 1000, absY / 1000));
                        pointCount = 0;
                        Log.d("getPointer", String.valueOf(absX) + "," + String.valueOf(absY));
                    }

                    drawPath.lineTo(absX, absY);
                    viewPath.lineTo(event.getX(), event.getY());
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    viewPath.reset();
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
        if(event.getPointerCount() > 1) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }else{
            return oldDist;
        }
    }

    public Bitmap getBitmap(){
        return canvasBitmap;
    }

    public void setBackgrountBitmap(Bitmap bitmap){
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
        originalWidth = width = canvasBitmap.getWidth();
        originalHeight = height = canvasBitmap.getHeight();
        if(displayHeight / displayWidth > height / width){ //가로가꽉참
            mPosY = (displayHeight - height) / 2;
        }else{
            mPosX = (displayWidth - width) / 2;
        }
    }

    public void setItemMode(boolean mode){
        isDrawMode = mode;
    }

    public List getList(){
        Log.d("getListFunc", list.toString());
        return list;
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
