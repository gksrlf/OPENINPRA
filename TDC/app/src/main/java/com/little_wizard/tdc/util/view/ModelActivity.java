package com.little_wizard.tdc.util.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.slider.RangeSlider;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.ui.main.MainActivity;
import com.little_wizard.tdc.util.demo.ModelSurfaceView;
import com.little_wizard.tdc.util.demo.SceneLoader;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.Object3DBuilder;
import org.andresoviedo.android_3d_model_engine.services.Object3DUnpacker;
import org.andresoviedo.util.android.ContentUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
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
public class ModelActivity extends AppCompatActivity implements SceneLoader.Callback {

    private static final int REQUEST_CODE_LOAD_TEXTURE = 1000;
    @BindView(R.id.layout)
    ConstraintLayout layout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.select)
    ImageButton select;
    @BindView(R.id.scale)
    ImageButton scale;
    @BindView(R.id.transform)
    ImageButton transform;
    @BindView(R.id.modifyLayout)
    LinearLayout modifyLayout;
    @BindView(R.id.axisSliderLayout)
    LinearLayout axisSliderLayout;
    @BindView(R.id.xAxisSlider)
    RangeSlider xAxisSlider;
    @BindView(R.id.yAxisSlider)
    RangeSlider yAxisSlider;
    @BindView(R.id.zAxisSlider)
    RangeSlider zAxisSlider;
    @BindView(R.id.scaleSlider)
    RangeSlider scaleSlider;

    private Uri paramUri;

    private float[] backgroundColor = new float[]{0f, 0f, 0f, 1.0f};

    private ModelSurfaceView gLView;

    private SceneLoader scene;

    private boolean scaleSliderVisible = false;
    private boolean axisSliderVisible = false;
    private Object3DData selectedObject;

    private float[] RED_COLOR = new float[]{1f, 0f, 0f, 1f};
    private float[] GREEN_COLOR = new float[]{0f, 1f, 0f, 1f};

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

        scene = new SceneLoader(this);
        scene.setCallback(this);
        scene.init();

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

        sliderInit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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

    @OnClick(R.id.select)
    public void onSelectClicked() {
        if (selectedObject != null) {
            if (!selectedObject.getId().equals("models/Point.obj")) {
                if (selectedObject.getIsClicked()) {
                    selectedObject.setIsClicked(false);
                    destroyPointCube();
                } else createPointCube();
            }
        }
    }

    @OnClick(R.id.scale)
    public void onScaleClicked() {
        if (selectedObject.getIsClicked()) destroyPointCube();
        if (axisSliderVisible) {
            axisSliderVisible = false;
            axisSliderLayout.setVisibility(View.GONE);
        }
        scaleSliderVisible = !scaleSliderVisible;
        scaleSlider.setVisibility(scaleSliderVisible ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.transform)
    public void onTransformClicked() {
        if (selectedObject.getIsClicked()) destroyPointCube();
        if (scaleSliderVisible) {
            scaleSliderVisible = false;
            scaleSlider.setVisibility(View.GONE);
        }
        axisSliderVisible = !axisSliderVisible;
        axisSliderLayout.setVisibility(axisSliderVisible ? View.VISIBLE : View.GONE);
    }

    private void createPointCube() {
        List<float[]> vertexArrayList;

        ContentUtils.setThreadActivity(this);
        ContentUtils.provideAssets(this);

        if (!selectedObject.getIsClicked()) {
            Object3DUnpacker unPacker = new Object3DUnpacker(selectedObject);

            unPacker.unpackingArrayBuffer();
            unPacker.unpackingBuffer();
            vertexArrayList = unPacker.getVertexArrayList();

            for (int i = 0; i < vertexArrayList.size(); i++) {
                Object3DData objPoint = Object3DBuilder.loadSelectedObjectPoints(this, "models/Point.obj", selectedObject);
                objPoint.setPosition(new float[]{
                        vertexArrayList.get(i)[0] * 16.5f + selectedObject.getPositionX(),
                        vertexArrayList.get(i)[1] * 16.5f + selectedObject.getPositionY(),
                        vertexArrayList.get(i)[2] * 16.5f + selectedObject.getPositionZ()});
                objPoint.setScale(new float[]{0.3f, 0.3f, 0.3f});
                objPoint.setColor(RED_COLOR);
                scene.addObject(objPoint);
            }

            ContentUtils.setThreadActivity(null);
            ContentUtils.clearDocumentsProvided();

            selectedObject.setIsClicked(true);
        }
    }

    private void destroyPointCube() {
        List<Object3DData> objects = scene.getObjects();
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getId().equals("models/Point.obj")) {
                objects.remove(i--);
            }
        }
    }

    private void sliderInit() {
        scaleSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        scaleSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (selectedObject != null) {
                float[] scale = selectedObject.getScale();
                Log.d("Scale", Arrays.toString(scale));
                if (scale[0] <= 0.5f && value <= 0) return;
                if (scale[0] >= 10.0f && value >= 0) return;
                selectedObject.setScale(new float[]{scale[0] + value, scale[1] + value, scale[2] + value});
            }
        });

        xAxisSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        xAxisSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (selectedObject != null) {
                float[] pos = selectedObject.getPosition();
                selectedObject.setPosition(new float[]{pos[0], pos[1], pos[2] + value});
            }
        });

        yAxisSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        yAxisSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (selectedObject != null) {
                float[] pos = selectedObject.getPosition();
                selectedObject.setPosition(new float[]{pos[0] + value, pos[1], pos[2]});
            }
        });

        zAxisSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        zAxisSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (selectedObject != null) {
                float[] pos = selectedObject.getPosition();
                selectedObject.setPosition(new float[]{pos[0], pos[1] + value, pos[2]});
            }
        });
    }

    @Override
    public void onSelectedObjectChanged(Object3DData selectedObject) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        this.selectedObject = selectedObject;
        if (selectedObject == null) {
            destroyPointCube();
            axisSliderVisible = false;
            scaleSliderVisible = false;
            axisSliderLayout.setVisibility(View.GONE);
            scaleSlider.setVisibility(View.GONE);
            modifyLayout.setVisibility(View.GONE);
        } else modifyLayout.setVisibility(View.VISIBLE);
    }
}