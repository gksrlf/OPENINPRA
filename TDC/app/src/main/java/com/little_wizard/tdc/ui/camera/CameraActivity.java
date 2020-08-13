package com.little_wizard.tdc.ui.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.ui.draw.DrawActivity;
import com.little_wizard.tdc.ui.draw.EditActivity;
import com.little_wizard.tdc.ui.main.MainActivity;
import com.little_wizard.tdc.util.NetworkStatus;
import com.little_wizard.tdc.util.S3Transfer;
import com.little_wizard.tdc.util.permission.PermissionHelper;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity implements S3Transfer.TransferCallback {

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    SurfaceHolder mSurfaceHolder;
    SensorManager mSensorManager;
    CameraDevice mCameraDevice;
    int dsiWidth, dsiHeight;
    Handler mHandler;
    ImageReader mImageReader;
    CaptureRequest.Builder mPreviewBuilder;
    CameraCaptureSession mSession;

    private final int PERMISSIONS_REQUEST = 1001;
    private final int PICK_IMAGE = 1111;

    @BindView(R.id.album)
    Button album;
    @BindView(R.id.capture)
    ImageButton capture;

    NetworkStatus status;
    S3Transfer transfer;

    ProgressDialog progressDialog;

    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.darkMode) setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        album.setBackgroundResource(MainActivity.darkMode
                ? R.drawable.btn_bg_white : R.drawable.btn_bg_black);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        status = new NetworkStatus(this);

        transfer = new S3Transfer(this);
        transfer.setCallback(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initSurfaceView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean success = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    success = false;
                    break;
                }
            }
            if (success) {
                initCameraAndPreview();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            assert data != null;
            //upload(getRealPathFromURI(data.getData()));
            startEdit(data.getData());
        }
    }

    private void initSurfaceView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dsiHeight = displayMetrics.heightPixels;
        dsiWidth = displayMetrics.widthPixels;

        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                String[] permissions = {Manifest.permission.CAMERA
                        , Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSIONS_REQUEST);
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }
        });
    }

    private void initCameraAndPreview() {
        HandlerThread handlerThread = new HandlerThread("CAMERA2");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        Handler mainHandler = new Handler(getMainLooper());
        try {
            String cameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;
            CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            Size largestPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[0];
            Log.i(TAG, "LargestSize: " + largestPreviewSize.getWidth() + " " + largestPreviewSize.getHeight());

            //setAspectRatioTextureView(largestPreviewSize.getHeight(), largestPreviewSize.getWidth());

            mImageReader = ImageReader.newInstance(largestPreviewSize.getWidth(), largestPreviewSize.getHeight(), ImageFormat.JPEG,/*maxImages*/7);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler);
            if (!PermissionHelper.checkPermission(this, Manifest.permission.CAMERA)) {
                return;
            }
            cameraManager.openCamera(cameraID, deviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = reader -> runOnUiThread(() -> {
        Image image = reader.acquireNextImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        try {
            bitmap = getRotatedBitmap(bitmap, 90);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //String path = getRealPathFromURI(Uri.parse(insertImage(getContentResolver(), bitmap
                //, "" + System.currentTimeMillis(), "")));
        //TODO: 추상주소로 바꿔줌
        String uri = insertImage(getContentResolver(), bitmap, "" + System.currentTimeMillis(), "");
        //upload(path);
        startEdit(Uri.parse(uri));
    });

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            try {
                takePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Toast.makeText(CameraActivity.this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    private void takePreview() throws CameraAccessException {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(mSurfaceHolder.getSurface());
        mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), mSessionPreviewStateCallback, mHandler);
    }

    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mSession = session;
            try {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(CameraActivity.this, "카메라 구성 실패", Toast.LENGTH_SHORT).show();
        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            mSession = session;
            unlockFocus();
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            mSession = session;
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    public void takePicture() {
        try {
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);//用来设置拍照请求的request
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mSession.capture(mCaptureRequest, mSessionCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) {
        if (bitmap == null) return null;
        if (degrees == 0) return bitmap;

        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback,
                    mHandler);
            // After this, the camera will go back to the normal state of preview.
            mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback,
                    mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public static String insertImage(ContentResolver cr,
                                     Bitmap source,
                                     String title,
                                     String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                assert url != null;
                try (OutputStream imageOut = cr.openOutputStream(url)) {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                }
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    //TODO: DrawActivity에 upload함수 추가
    private void upload(String path) {
        if (!status.isConnected()) {
            Toast.makeText(this, R.string.network_not_connected, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setMessage(R.string.name_setting);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.save, (dialogInterface, i) -> {
            File file = new File(path);
            String text = editText.getText().toString() + ".jpg";
            transfer.upload(R.string.s3_bucket, FilenameUtils.getBaseName(text)
                    .isEmpty() ? file.getName() : text, file);
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
        });
        builder.show();
    }

    @OnClick(R.id.album)
    public void onAlbumClicked() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @OnClick(R.id.capture)
    public void onCaptureClicked() {
        takePicture();
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

    //TODO: 이미지 모드 선택 후 사진 Uri 넘김
    private void startEdit(Uri uri){
        final String[] menu = {"Symmetry", "Asymmetry"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Photo type");
        alertDialogBuilder.setItems(menu, (dialogInterface, i) -> {
            Intent intent;
            switch (i) {
                case 0:
                    intent = new Intent(getApplicationContext(), EditActivity.class);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setData(uri);
                    startActivity(intent);
                    break;
                case 1:
                    intent = new Intent(getApplicationContext(), DrawActivity.class);
                    intent.putExtra("mode", "asymmetry");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setData(uri);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}