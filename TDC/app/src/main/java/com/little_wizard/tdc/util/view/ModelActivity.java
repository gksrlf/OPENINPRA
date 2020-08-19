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
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontSaver;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.io.IOUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    @BindView(R.id.valueSlider)
    Slider valueSlider;
    @BindView(R.id.xAxisPlus)
    ImageButton xAxisPlus;
    @BindView(R.id.xAxisMinus)
    ImageButton xAxisMinus;
    @BindView(R.id.yAxisPlus)
    ImageButton yAxisPlus;
    @BindView(R.id.yAxisMinus)
    ImageButton yAxisMinus;
    @BindView(R.id.zAxisPlus)
    ImageButton zAxisPlus;
    @BindView(R.id.zAxisMinus)
    ImageButton zAxisMinus;
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

        ContentUtils.setThreadActivity(this);

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
                LoadDialog loadDialog = new LoadDialog(this, this);
                loadDialog.show();
                return true;
            }

            case R.id.action_save: {
                ContentUtils.setThreadActivity(this);
                WavefrontSaver saver = new WavefrontSaver(scene.getObjects(), FilenameUtils.getBaseName(paramUri.toString()));
                File file = saver.OutFileToVertexBuffer(getExternalCacheDir().getAbsolutePath() + "/Test.obj", SCALE_MAX);
                if (file != null) {
                    transfer.upload(R.string.s3_bucket_resize, file.getName(), file);
                    finish();
                } else
                    Toast.makeText(this, getString(R.string.saveFailed), Toast.LENGTH_SHORT).show();
                return true;
            }
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
        vibrate();
        axisMode = axisMode != AXIS_SCALE ? AXIS_SCALE : AXIS_DEFAULT;
        axisLayout.setVisibility(axisMode != AXIS_DEFAULT ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.rotate)
    public void onRotateClicked() {
        vibrate();
        axisMode = axisMode != AXIS_ROTATE ? AXIS_ROTATE : AXIS_DEFAULT;
        axisLayout.setVisibility(axisMode != AXIS_DEFAULT ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.transform)
    public void onTransformClicked() {
        vibrate();
        axisMode = axisMode != AXIS_TRANSFORM ? AXIS_TRANSFORM : AXIS_DEFAULT;
        axisLayout.setVisibility(axisMode != AXIS_DEFAULT ? View.VISIBLE : View.GONE);
    }

    private void axisLayoutInit() {
        axisVal = valueSlider.getValue();
        valueSlider.addOnChangeListener((slider, value, fromUser) -> axisVal = value);

        xAxisPlus.setOnClickListener(view -> {
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

        xAxisMinus.setOnClickListener(view -> {
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

        yAxisPlus.setOnClickListener(view -> {
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

        yAxisMinus.setOnClickListener(view -> {
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

        zAxisPlus.setOnClickListener(view -> {
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

        zAxisMinus.setOnClickListener(view -> {
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
            /*
            Object3DData replace3DData;
            replace3DData = Object3DBuilder.loadSelectedObject(new File(selectedObject.getId()));
            scene.getObjects().remove(selectedObject);
            replace3DData.setScale(selectedObject.getScale());
            replace3DData.setPosition(selectedObject.getPosition());
            replace3DData.setRotation(selectedObject.getRotation());
            scene.addObject(replace3DData);
            selectedObject = replace3DData;
             */

            Object3DData replace3DData = null;
            try {
                replace3DData = Object3DBuilder.loadSelectedObject(new File(selectedObject.getId()));
                InputStream open = ContentUtils.getInputStream(replace3DData.getTextureFile());
                replace3DData.setTextureData(IOUtils.read(open));
                scene.getObjects().remove(selectedObject);
                replace3DData.setScale(selectedObject.getScale());
                replace3DData.setPosition(selectedObject.getPosition());
                replace3DData.setRotation(selectedObject.getRotation());
                scene.addObject(replace3DData);
                selectedObject = replace3DData;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return replace3DData;
        }
        return null;
    }

    private void vibrate() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @Override
    public void onSelectedObjectChanged(Object3DData selectedObject) {
        vibrate();
        this.selectedObject = selectedObject;
        if (selectedObject == null) {
            axisMode = AXIS_DEFAULT;
            axisLayout.setVisibility(View.GONE);
            modifyLayout.setVisibility(View.GONE);
        } else modifyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(View view, List<RepoItem> list) {

    }

    @Override
    public void onItemLongClick(View view, List<RepoItem> list) {
    }

    @Override
    public void onSelectedItem(File file) {
        try {
            Object3DData obj = Object3DBuilder.loadSelectedObject(file);
            InputStream open = ContentUtils.getInputStream(obj.getTextureFile());
            obj.setTextureData(IOUtils.read(open));
            obj.setScale(new float[]{SCALE_MAX, SCALE_MAX, SCALE_MAX});
            obj.setPosition(new float[]{0.0f, 0.0f, 0.0f});
            obj.setRotation(new float[]{0.0f, 0.0f, 0.0f});
            scene.addObject(obj);
            Toast.makeText(this, file.getName() + " 생성 완료.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}