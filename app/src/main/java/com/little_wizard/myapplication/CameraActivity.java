package com.little_wizard.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.ui.main.MainActivity;
import com.little_wizard.tdc.util.permission.PermissionHelper;
*/
import java.io.File;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

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

    //TransferUtility transferUtility;
    @BindView(R.id.album)
    Button album;
    @BindView(R.id.capture)
    ImageButton capture;

    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if (MainActivity.darkMode) setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        //album.setBackgroundResource(MainActivity.darkMode
         //       ? R.drawable.btn_bg_white : R.drawable.btn_bg_black);

        /*CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:13fe9921-fc1f-49ab-b998-c59c0a367efe", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
        transferUtility = TransferUtility.builder().s3Client(s3).context(this).build();
        TransferNetworkLossHandler.getInstance(this);*/

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initSurfaceView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            assert data != null;
            //upload(getRealPathFromURI(data.getData()));
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

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            new SaveImageTask().execute(bitmap);
        }
    };

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
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
        public void onError(CameraDevice camera, int error) {
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
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
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

    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
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

    public static final String insertImage(ContentResolver cr,
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
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
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

    /*private void upload(String path) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progressDialog));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
        progressDialog.show();

        File file = new File(path);
        TransferObserver transferObserver = transferUtility.upload(
                "imagebucket20200724",
                file.getName(),
                file
        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state.toString());
                if (state == TransferState.COMPLETED) {
                    Toast.makeText(CameraActivity.this, "전송 완료.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (state == TransferState.COMPLETED || state == TransferState.FAILED) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float v = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percent = (int) v;
                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percent + "%");
            }

            @Override
            public void onError(int id, Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                Toast.makeText(CameraActivity.this, "전송 실패.", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

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

    private class SaveImageTask extends AsyncTask<Bitmap, Void, String> {
        @Override
        protected String doInBackground(Bitmap... data) {
            Bitmap bitmap = null;
            try {
                bitmap = getRotatedBitmap(data[0], 90);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return insertImage(getContentResolver(), bitmap
                    , "" + System.currentTimeMillis(), "");
        }

        @Override
        protected void onPostExecute(String path) {
           // upload(getRealPathFromURI(Uri.parse(path)));
        }
    }
}