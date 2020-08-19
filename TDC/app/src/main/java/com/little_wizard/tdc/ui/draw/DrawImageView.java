package com.little_wizard.tdc.ui.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.little_wizard.tdc.R;
import com.little_wizard.tdc.util.draw.Coordinates;
import com.little_wizard.tdc.util.draw.DrawQueue;

import java.util.ArrayList;
import java.util.List;

public class DrawImageView extends androidx.appcompat.widget.AppCompatImageView {
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private final int DROW = 3;

    public static final int ASYMMETRY = 1;
    public static final int SYMMETRY = 2;

    private static int pointCount = 0;
    private final int pick = 3;
    private final int unitPixel = 3;

    private DrawQueue drawQueue;

    private int mode;
    private boolean isDrawMode;

    private Path drawPath, viewPath;
    private Paint drawPaint, viewPaint, canvasPaint;
    private int paintColor = 0xFFFF0000;

    private Canvas drawCanvas;
    private Bitmap canvasBitmap, originalBitmap;
    private final int paintWidth = 5;

    private float width;
    private float height;

    private float oldDist = 1f;
    private float scale = 1f;

    private int displayHeight;
    private int displayWidth;
    private float startX, startY;

    Menu activityMenu;
    private boolean confirmation;
    private int photo_mode;
    private float line;
    private float originalLine;

    ArrayList<Coordinates> listX = new ArrayList<>();
    ArrayList<Coordinates> listY = new ArrayList<>();
    Coordinates start, end;

    // matrix

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;
    float matrixValue[] = new float[9];
    float savedMatrixValue[] = new float[9];

    PointF startPoint = new PointF();
    PointF mid = new PointF();
    float nowDist = 0;

    private static final float MIN_ZOOM = 0.7f;
    private static final float MAX_ZOOM = 3.0f;

    public DrawImageView(Context context, int displayHeight, int displayWidth, int mode) {
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
        viewPaint.setStrokeWidth(paintWidth * getRealScale());
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
        canvas.drawBitmap(canvasBitmap, matrix, null);
        canvas.drawPath(viewPath, viewPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        Rect bounds = getDrawable().getBounds();

        width = bounds.right - bounds.left;
        height = bounds.bottom - bounds.top;
        matrix.getValues(matrixValue);

        float absX = 1 / getRealScale() * (event.getX() - matrixValue[Matrix.MTRANS_X]);
        float absY = 1 / getRealScale() * (event.getY() - matrixValue[Matrix.MTRANS_Y]);

        Bitmap previousBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Coordinates lastPoint = drawQueue.getLastPoint();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mode == NONE) {
                    Log.d("onTouchEvent Event", "ACTION_DOWN");
                    if (!isDraggable(event)) break;
                    if (isDrawMode && !confirmation) { // 그리기 모드 && 확정 상태가 아닐 때
                        if (!isInPicture(event)) break;
                        // 첫번째 터치 아닐 때
                        if (lastPoint != null) {
                            drawPath.moveTo(lastPoint.getX(), lastPoint.getY());
                            viewPath.moveTo(lastPoint.getX() * getRealScale() + matrixValue[Matrix.MTRANS_X], lastPoint.getY() * getRealScale() + matrixValue[Matrix.MTRANS_Y]);
                        } else { // 첫번째 터치
                            startX = absX;
                            startY = absY;
                            if (photo_mode == SYMMETRY) {
                                drawPath.moveTo(originalLine, absY);
                                drawPath.lineTo(absX, absY);
                                viewPath.moveTo(originalLine * getRealScale() + matrixValue[Matrix.MTRANS_X], event.getY());
                                viewPath.lineTo(event.getX(), event.getY());
                            } else {
                                drawPath.moveTo(absX, absY);
                                viewPath.moveTo(event.getX(), event.getY());
                            }
                        }
                        addAbsList(absX, absY);
                        start = new Coordinates((int) absX, (int) absY);
                        mode = DROW;
                    } else {
                        savedMatrix.set(matrix);
                        startPoint.set(event.getX(), event.getY());
                        mode = DRAG;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 1f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    if (isDraggable(event)) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                    }
                } else if (mode == DROW && !confirmation) {
                    if (isInPicture(event)) {
                        viewPath.lineTo(event.getX(), event.getY());

                        addAbsList(absX, absY);
                        pointCount++;
                        if (pointCount % pick == 0) {
                            drawPath.lineTo(absX, absY);
                            pointCount = 0;
                        }
                    } else { //draw상태에서 사진 범위 넘어갔을 때
                        mode = NONE;
                        addAbsList(absX, absY);
                        drawPath.lineTo(absX, absY);
                        viewPath.lineTo(event.getX(), event.getY());
                        drawCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                        viewPath.reset();

                        end = new Coordinates((int) absX, (int) absY);
                        drawQueue.push(listX, listY);
                        drawQueue.push(previousBitmap, start, end);

                        listX.clear();
                        listY.clear();
                        if (drawQueue.isClear()) {
                            setClearMenu();
                        } else {
                            setUnClearMenu();
                        }
                    }
                } else if(mode == ZOOM){
                    drawPath.reset();
                    viewPath.reset();
                    savedMatrix.getValues(savedMatrixValue);
                    matrix.getValues(matrixValue);

                    nowDist = spacing(event);

                    if (nowDist > 10f) {
                        matrix.set(savedMatrix);
                        scale = nowDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }

                    line = originalLine * getRealScale();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mode == DROW && !confirmation) {
                    addAbsList(absX, absY);
                    drawPath.lineTo(absX, absY);
                    viewPath.lineTo(event.getX(), event.getY());
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    viewPath.reset();

                    end = new Coordinates((int) absX, (int) absY);
                    drawQueue.push(listX, listY);
                    drawQueue.push(previousBitmap, start, end);

                    listX.clear();
                    listY.clear();
                    if (drawQueue.isClear()) {
                        setClearMenu();
                    } else {
                        setUnClearMenu();
                    }
                }
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            default:
                return false;
        }
        limitZoom(matrix);
        limitDrag(matrix);

        setImageMatrix(matrix);
        matrix.getValues(matrixValue);
        invalidate();
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

    public Bitmap getBitmap() {
        return canvasBitmap;
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);

        drawQueue.push(bitmap, null, null);
        drawQueue.push(null, null);
        line = originalLine;
        scale = 1f;

        bitmapDrawable = new BitmapDrawable(this.bitmap);
        setImageDrawable(bitmapDrawable);
        setScaleType(ScaleType.MATRIX);
        invalidate();
    }

    public float getRealScale(){
        matrix.getValues(matrixValue);
        float scaleX = matrixValue[Matrix.MSCALE_X];
        float skewY = matrixValue[Matrix.MSKEW_Y];
        return (float) Math.sqrt(scaleX * scaleX + skewY * skewY);
    }

    private void limitZoom(Matrix m) {
        float[] values = new float[9];
        m.getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];

        if(scaleX > MAX_ZOOM) {
            scaleX = MAX_ZOOM;
        } else if(scaleX < MIN_ZOOM) {
            scaleX = MIN_ZOOM;
        }

        if(scaleY > MAX_ZOOM) {
            scaleY = MAX_ZOOM;
        } else if(scaleY < MIN_ZOOM) {
            scaleY = MIN_ZOOM;
        }

        values[Matrix.MSCALE_X] = scaleX;
        values[Matrix.MSCALE_Y] = scaleY;
        m.setValues(values);
    }


    private void limitDrag(Matrix m) {
        float[] values = new float[9];
        m.getValues(values);
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];

        Rect bounds = getDrawable().getBounds();
        int viewWidth = getResources().getDisplayMetrics().widthPixels;
        int viewHeight = getResources().getDisplayMetrics().heightPixels;

        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;

        float minX = (-width + 20) * scaleX;
        float minY = (-height + 20) * scaleY;

        if(transX > (viewWidth - 20)) {
            transX = viewWidth - 20;
        } else if(transX < minX) {
            transX = minX;
        }

        if(transY > (viewHeight - 80)) {
            transY = viewHeight - 80;
        } else if(transY < minY) {
            transY = minY;
        }

        values[Matrix.MTRANS_X] = transX;
        values[Matrix.MTRANS_Y] = transY;
        m.setValues(values);
    }

    public void setItemMode(boolean mode) {
        isDrawMode = mode;
    }

    public List getListX() {
        return drawQueue.getResultX();
    }

    public List getListY() {
        return drawQueue.getResultY();
    }

    public ArrayList<Coordinates> getSymmetryResult() {
        return drawQueue.getResultY();
    }

    public ArrayList<Coordinates> getPairX() {
        return drawQueue.getPairX();
    }

    public ArrayList<Coordinates> getPairY() {
        return drawQueue.getPairY();
    }

    public void undo() {
        Bitmap previousBitmap = drawQueue.getPreviousBitmap().copy(Bitmap.Config.ARGB_8888, true);
        drawQueue.undo();

        drawCanvas.save();
        drawCanvas.drawBitmap(previousBitmap, 0, 0, null);
        drawCanvas.restore();
        invalidate();
        setConfirmation(false);
        if (drawQueue.isClear()) {
            activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
        }
    }

    public void clear() {
        drawQueue.clear();
        setConfirmation(false);
        activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
        setBackgroundBitmap(originalBitmap);

        float rate = (float)getWidth() / (float)getHeight();
        float bitmapRate = (float)originalBitmap.getWidth() / (float)originalBitmap.getHeight();
        if(rate > bitmapRate){
            matrixValue[Matrix.MTRANS_X] = (getWidth() - originalBitmap.getWidth()) / 2f;
            matrixValue[Matrix.MTRANS_Y] = 0;
        }else{
            matrixValue[Matrix.MTRANS_X] = 0;
            matrixValue[Matrix.MTRANS_Y] = (getHeight() - originalBitmap.getHeight()) / 2f;
        }
        matrixValue[Matrix.MSCALE_X] = matrixValue[Matrix.MSCALE_Y] = 1f;
        matrix.setValues(matrixValue);

        setImageMatrix(matrix);
        invalidate();
    }

    protected boolean isInPicture(MotionEvent e) {
        float mPosX = matrixValue[Matrix.MTRANS_X];
        float mPosY = matrixValue[Matrix.MTRANS_Y];
        float width = originalBitmap.getWidth() * getRealScale();
        float height = originalBitmap.getHeight() * getRealScale();

        //대칭 모드에서 축의 오른쪽인지 아닌지 확인
        if (photo_mode == SYMMETRY) {
            return (mPosX <= e.getX() && e.getX() <= mPosX + line && mPosY <= e.getY() && e.getY() <= mPosY + height) ? true : false;
        } else {
            return (mPosX <= e.getX() && e.getX() <= mPosX + width && mPosY <= e.getY() && e.getY() <= mPosY + height) ? true : false;
        }
    }

    protected boolean isDraggable(MotionEvent e) {
        float mPosX = matrixValue[Matrix.MTRANS_X];
        float mPosY = matrixValue[Matrix.MTRANS_Y];
        float width = originalBitmap.getWidth() * getRealScale();
        float height = originalBitmap.getHeight() * getRealScale();
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
            // 마지막 점까지 선 잇기
            drawPath.moveTo(lastPoint.getX(), lastPoint.getY());

            Coordinates start = lastPoint;
            Coordinates end;
            if (photo_mode == SYMMETRY) {
                point.add(new Coordinates(originalLine, lastPoint.getY()));
                end = new Coordinates(originalLine, lastPoint.getY());
                drawPath.lineTo(originalLine, lastPoint.getY());
            } else {
                point.add(new Coordinates(startX, startY));
                end = new Coordinates(startX, startY);
                drawPath.lineTo(startX, startY);
            }
            drawCanvas.drawPath(drawPath, drawPaint);
            drawQueue.push(previousBitmap, start, end);

            ArrayList<Coordinates> lastPointX = new ArrayList<>();
            lastPointX.add(drawQueue.getLastPointX());
            ArrayList<Coordinates> lastPointY = new ArrayList<>();
            lastPointY.add(drawQueue.getLastPointY());
            drawQueue.push(lastPointX, lastPointY);

            drawPath.reset();
            invalidate();
        } else {
            setUnClearMenu();
        }
    }

    public void setClearMenu() {
        activityMenu.findItem(R.id.draw_confirmation).setEnabled(false);
        activityMenu.findItem(R.id.draw_confirmation).setVisible(false);
        activityMenu.findItem(R.id.draw_add).setEnabled(true);
        activityMenu.findItem(R.id.draw_add).setVisible(true);
        activityMenu.findItem(R.id.draw_save).setEnabled(true);
        activityMenu.findItem(R.id.draw_save).setVisible(true);
    }

    public void setUnClearMenu() {
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

    public Bitmap getCroppedImage() {
        return Bitmap.createBitmap(originalBitmap, drawQueue.getStartX(), drawQueue.getStartY(), drawQueue.getWidth(), drawQueue.getHeight());
    }

    public float getLine() {
        return originalLine;
    }

    public void addAbsList(float absX, float absY) {
        listX.add(new Coordinates((int) absX / unitPixel * unitPixel, (int) absY));
        listY.add(new Coordinates((int) absX, (int) absY / unitPixel * unitPixel));
    }
}