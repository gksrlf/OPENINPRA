package com.little_wizard.tdc.util.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.slider.Slider;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.classes.RepoItem;
import com.little_wizard.tdc.ui.main.MainActivity;
import com.little_wizard.tdc.ui.main.RepositoryAdapter;
import com.little_wizard.tdc.util.S3Transfer;
import com.little_wizard.tdc.util.demo.ModelSurfaceView;
import com.little_wizard.tdc.util.demo.SceneLoader;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.Object3DBuilder;
import org.andresoviedo.android_3d_model_engine.services.Object3DUnpacker;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontSaver;
import org.andresoviedo.util.android.ContentUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This activity represents the container for our 3D viewer.
 *
 * @author andresoviedo
 */
public class ModelActivity extends AppCompatActivity implements SceneLoader.Callback, RepositoryAdapter.ItemClickListener, LoadDialog.Callback {

    private static final int REQUEST_CODE_LOAD_TEXTURE = 1000;
    @BindView(R.id.layout)
    ConstraintLayout layout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.scale)
    ImageButton scale;
    @BindView(R.id.transform)
    ImageButton transform;
    @BindView(R.id.modifyLayout)
    LinearLayout modifyLayout;
    @BindView(R.id.rotate)
    ImageButton rotate;
    @BindView(R.id.slider)
    Slider slider;
    @BindView(R.id.xAxisZoomIn)
    ImageButton xAxisZoomIn;
    @BindView(R.id.xAxisZoomOut)
    ImageButton xAxisZoomOut;
    @BindView(R.id.yAxisZoomIn)
    ImageButton yAxisZoomIn;
    @BindView(R.id.yAxisZoomOut)
    ImageButton yAxisZoomOut;
    @BindView(R.id.zAxisZoomIn)
    ImageButton zAxisZoomIn;
    @BindView(R.id.zAxisZoomOut)
    ImageButton zAxisZoomOut;
    @BindView(R.id.axisLayout)
    ConstraintLayout axisLayout;

    private Uri paramUri;

    private float[] backgroundColor = new float[]{0f, 0f, 0f, 1.0f};

    private ModelSurfaceView gLView;

    private SceneLoader scene;

    private int axisMode = -1;
    final int AXIS_SCALE = 1;
    final int AXIS_ROTATE = 2;
    final int AXIS_TRANSFORM = 3;
    final int AXIS_DEFAULT = -1;

    final float SCALE_MIN = 0.5f;
    final float SCALE_MAX = 12.0f;

    private Object3DData selectedObject;

    private float[] RED_COLOR = new float[]{1f, 0f, 0f, 1f};
    private float[] GREEN_COLOR = new float[]{0f, 1f, 0f, 1f};

    private float axisVal;

    S3Transfer transfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.darkMode) setTheme(R.style.Theme_MaterialComponents_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        ButterKnife.bind(this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getString("URI") != null) {
                this.paramUri = Uri.parse(b.getString("URI"));
            }
        }
        Log.i("Renderer", "Params: uri '" + paramUri + "'");

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(FilenameUtils.getBaseName(paramUri.toString()));
        }

        transfer = new S3Transfer(this);

        scene = new SceneLoader(this);
        scene.setCallback(this);
        scene.init();
        scene.setDrawAxis(true);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        try {
            gLView = new ModelSurfaceView(this);
            gLView.setId(View.generateViewId());
            layout.addView(gLView);

            ConstraintSet set = new ConstraintSet();
            set.clone(layout);
            set.constrainWidth(gLView.getId(), ConstraintSet.MATCH_CONSTRAINT);
            set.constrainHeight(gLView.getId(), ConstraintSet.MATCH_CONSTRAINT);
            set.connect(gLView.getId(), ConstraintSet.TOP, toolbar.getId(), ConstraintSet.BOTTOM);
            set.connect(gLView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(gLView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(gLView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.applyTo(layout);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading OpenGL view:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        ContentUtils.printTouchCapabilities(getPackageManager());

        axisLayoutInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_model, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_load: {
                LoadDialog loadDialog = new LoadDialog(this, scene.getObjects(), this);
                loadDialog.show();
                return true;
            }

            case R.id.action_save: {
                if (selectedObject != null) {
                    ContentUtils.setThreadActivity(this);
                    WavefrontSaver saver = new WavefrontSaver(replace3DData(), FilenameUtils.getBaseName(paramUri.toString()));
                    File file = saver.OutFileToVertexBuffer(getExternalCacheDir().getAbsolutePath() + "/TestCube.obj", SCALE_MAX);
                    transfer.upload(R.string.s3_bucket_resize, file.getName(), file);
                    finish();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Uri getParamUri() {
        return paramUri;
    }

    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public SceneLoader getScene() {
        return scene;
    }

    public ModelSurfaceView getGLView() {
        return gLView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_LOAD_TEXTURE) {// The URI of the selected file
            final Uri uri = data.getData();
            if (uri != null) {
                Log.i("ModelActivity", "Loading texture '" + uri + "'");
                try {
                    ContentUtils.setThreadActivity(this);
                    scene.loadTexture(null, uri);
                } catch (IOException ex) {
                    Log.e("ModelActivity", "Error loading texture: " + ex.getMessage(), ex);
                    Toast.makeText(this, "Error loading texture '" + uri + "'. " + ex
                            .getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    ContentUtils.setThreadActivity(null);
                }
            }
        }
    }

    @OnClick(R.id.scale)
    public void onScaleClicked() {
        if (selectedObject.getIsClicked()) destroyPointCube();
        axisMode = axisMode != AXIS_SCALE ? AXIS_SCALE : AXIS_DEFAULT;
        axisLayout.setVisibility(axisMode != AXIS_DEFAULT ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.rotate)
    public void onRotateClicked() {
        if (selectedObject.getIsClicked()) destroyPointCube();
        axisMode = axisMode != AXIS_ROTATE ? AXIS_ROTATE : AXIS_DEFAULT;
        axisLayout.setVisibility(axisMode != AXIS_DEFAULT ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.transform)
    public void onTransformClicked() {
        if (selectedObject.getIsClicked()) destroyPointCube();
        axisMode = axisMode != AXIS_TRANSFORM ? AXIS_TRANSFORM : AXIS_DEFAULT;
        axisLayout.setVisibility(axisMode != AXIS_DEFAULT ? View.VISIBLE : View.GONE);
    }

    private void createPointCube(Object3DData obj) {
        List<float[]> vertexArrayList;

        ContentUtils.setThreadActivity(this);
        ContentUtils.provideAssets(this);

        if (!obj.getIsClicked()) {
            Object3DUnpacker unPacker = new Object3DUnpacker(obj);

            unPacker.unpackingArrayBuffer();
            unPacker.unpackingBuffer();
            vertexArrayList = unPacker.getVertexArrayList();

            for (int i = 0; i < vertexArrayList.size(); i++) {
                Object3DData objPoint = Object3DBuilder.loadSelectedObjectPoints(this, "Point.obj", selectedObject);
                float val1 = vertexArrayList.get(i)[0] * 19.5f + obj.getPositionX() * obj.getScaleX();
                float val2 = vertexArrayList.get(i)[1] * 19.5f + obj.getPositionY() * obj.getScaleY();
                float val3 = vertexArrayList.get(i)[2] * 19.5f + obj.getPositionZ() * obj.getScaleZ();
                objPoint.setPosition(new float[]{
                        (float) ((val1 * (obj.getScaleX() * 100 / SCALE_MAX) / 100) * 2.5),
                        (float) ((val2 * (obj.getScaleY() * 100 / SCALE_MAX) / 100) * 2.5),
                        (float) ((val3 * (obj.getScaleZ() * 100 / SCALE_MAX) / 100) * 2.5)
                });
                objPoint.setRotation(obj.getRotation());
                objPoint.setScale(new float[]{0.25f, 0.25f, 0.25f});
                objPoint.setColor(RED_COLOR);
                scene.addObject(objPoint);
            }

            ContentUtils.setThreadActivity(null);
            ContentUtils.clearDocumentsProvided();

            obj.setIsClicked(true);
        }
    }

    private void destroyPointCube() {
        List<Object3DData> objects = scene.getObjects();
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getId().equals("Point.obj")) {
                objects.remove(i--);
            }
        }
    }

    private void axisLayoutInit() {
        axisVal = slider.getValue();
        slider.addOnChangeListener((slider, value, fromUser) -> axisVal = value);

        xAxisZoomIn.setOnClickListener(view -> {
            switch (axisMode) {
                case AXIS_SCALE: {
                    float[] scale = selectedObject.getScale();
                    if (scale[0] >= SCALE_MAX || scale[0] + axisVal >= SCALE_MAX) {
                        selectedObject.setScale(new float[]{SCALE_MAX, scale[1], scale[2]});
                        return;
                    }
                    selectedObject.setScale(new float[]{scale[0] + axisVal, scale[1], scale[2]});
                    break;
                }

                case AXIS_ROTATE: {
                    float[] rot = selectedObject.getRotation();
                    selectedObject.setRotation(new float[]{rot[0] + axisVal, rot[1], rot[2]});
                    break;
                }

                case AXIS_TRANSFORM: {
                    float[] pos = selectedObject.getPosition();
                    selectedObject.setPosition(new float[]{pos[0] + axisVal, pos[1], pos[2]});
                    break;
                }
            }
            replace3DData();
        });

        xAxisZoomOut.setOnClickListener(view -> {
            switch (axisMode) {
                case AXIS_SCALE: {
                    float[] scale = selectedObject.getScale();
                    if (scale[0] <= SCALE_MIN || scale[0] - axisVal <= SCALE_MIN) {
                        selectedObject.setScale(new float[]{SCALE_MIN, scale[1], scale[2]});
                        return;
                    }
                    selectedObject.setScale(new float[]{scale[0] - axisVal, scale[1], scale[2]});
                    break;
                }

                case AXIS_ROTATE: {
                    float[] rot = selectedObject.getRotation();
                    selectedObject.setRotation(new float[]{rot[0] - axisVal, rot[1], rot[2]});
                    break;
                }

                case AXIS_TRANSFORM: {
                    float[] pos = selectedObject.getPosition();
                    selectedObject.setPosition(new float[]{pos[0] - axisVal, pos[1], pos[2]});
                    break;
                }
            }
            replace3DData();
        });

        yAxisZoomIn.setOnClickListener(view -> {
            switch (axisMode) {
                case AXIS_SCALE: {
                    float[] scale = selectedObject.getScale();
                    if (scale[1] >= SCALE_MAX || scale[1] + axisVal >= SCALE_MAX) {
                        selectedObject.setScale(new float[]{scale[0], SCALE_MAX, scale[2]});
                        return;
                    }
                    selectedObject.setScale(new float[]{scale[0], scale[1] + axisVal, scale[2]});
                    break;
                }

                case AXIS_ROTATE: {
                    float[] rot = selectedObject.getRotation();
                    selectedObject.setRotation(new float[]{rot[0], rot[1] + axisVal, rot[2]});
                    break;
                }

                case AXIS_TRANSFORM: {
                    float[] pos = selectedObject.getPosition();
                    selectedObject.setPosition(new float[]{pos[0], pos[1] + axisVal, pos[2]});
                    break;
                }
            }
            replace3DData();
        });

        yAxisZoomOut.setOnClickListener(view -> {
            switch (axisMode) {
                case AXIS_SCALE: {
                    float[] scale = selectedObject.getScale();
                    if (scale[1] <= SCALE_MIN || scale[1] - axisVal <= SCALE_MIN) {
                        selectedObject.setScale(new float[]{scale[0], SCALE_MIN, scale[2]});
                        return;
                    }
                    selectedObject.setScale(new float[]{scale[0], scale[1] - axisVal, scale[2]});
                    break;
                }

                case AXIS_ROTATE: {
                    float[] rot = selectedObject.getRotation();
                    selectedObject.setRotation(new float[]{rot[0], rot[1] - axisVal, rot[2]});
                    break;
                }

                case AXIS_TRANSFORM: {
                    float[] pos = selectedObject.getPosition();
                    selectedObject.setPosition(new float[]{pos[0], pos[1] - axisVal, pos[2]});
                    break;
                }
            }
            replace3DData();
        });

        zAxisZoomIn.setOnClickListener(view -> {
            switch (axisMode) {
                case AXIS_SCALE: {
                    float[] scale = selectedObject.getScale();
                    if (scale[2] >= SCALE_MAX || scale[2] + axisVal >= SCALE_MAX) {
                        selectedObject.setScale(new float[]{scale[0], scale[1], SCALE_MAX});
                        return;
                    }
                    selectedObject.setScale(new float[]{scale[0], scale[1], scale[2] + axisVal});
                    break;
                }

                case AXIS_ROTATE: {
                    float[] rot = selectedObject.getRotation();
                    selectedObject.setRotation(new float[]{rot[0], rot[1], rot[2] + axisVal});
                    break;
                }

                case AXIS_TRANSFORM: {
                    float[] pos = selectedObject.getPosition();
                    selectedObject.setPosition(new float[]{pos[0], pos[1], pos[2] + axisVal});
                    break;
                }
            }
            replace3DData();
        });

        zAxisZoomOut.setOnClickListener(view -> {
            switch (axisMode) {
                case AXIS_SCALE: {
                    float[] scale = selectedObject.getScale();
                    if (scale[2] <= SCALE_MIN || scale[2] - axisVal <= SCALE_MIN) {
                        selectedObject.setScale(new float[]{scale[0], scale[1], SCALE_MIN});
                        return;
                    }
                    selectedObject.setScale(new float[]{scale[0], scale[1], scale[2] - axisVal});
                    break;
                }

                case AXIS_ROTATE: {
                    float[] rot = selectedObject.getRotation();
                    selectedObject.setRotation(new float[]{rot[0], rot[1], rot[2] - axisVal});
                    break;
                }

                case AXIS_TRANSFORM: {
                    float[] pos = selectedObject.getPosition();
                    selectedObject.setPosition(new float[]{pos[0], pos[1], pos[2] - axisVal});
                    break;
                }
            }
            replace3DData();
        });
    }

    private Object3DData replace3DData() {
        if (selectedObject != null) {
            Object3DData replace3DData;
            replace3DData = Object3DBuilder.loadSelectedObject(new File(selectedObject.getId()));
            scene.getObjects().remove(selectedObject);
            replace3DData.setScale(selectedObject.getScale());
            replace3DData.setPosition(selectedObject.getPosition());
            replace3DData.setRotation(selectedObject.getRotation());
            scene.addObject(replace3DData);
            selectedObject = replace3DData;
            return replace3DData;
        }
        return null;
    }

    @Override
    public void onSelectedObjectChanged(Object3DData selectedObject) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        this.selectedObject = selectedObject;
        if (selectedObject == null) {
            destroyPointCube();
            axisMode = AXIS_DEFAULT;
            axisLayout.setVisibility(View.GONE);
            modifyLayout.setVisibility(View.GONE);
        } else {
            float[] pos = selectedObject.getPosition();
            Log.d("POS", Arrays.toString(pos));
            Log.d("selectedObject", selectedObject.getId());
            modifyLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(View view, List<RepoItem> list) {

    }

    @Override
    public void onItemLongClick(View view, List<RepoItem> list) {
    }

    @Override
    public void onSelectedItem(File file) {
        scene.addObject(Object3DBuilder.loadSelectedObject(file));
        Toast.makeText(this, file.getName() + " 생성 완료.", Toast.LENGTH_SHORT).show();
    }
}