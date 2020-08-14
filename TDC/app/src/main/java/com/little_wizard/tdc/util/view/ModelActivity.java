package com.little_wizard.tdc.util.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import org.andresoviedo.util.android.ContentUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.Arrays;

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
    }

    @OnClick(R.id.scale)
    public void onScaleClicked() {
        if (axisSliderVisible) {
            axisSliderVisible = false;
            axisSliderLayout.setVisibility(View.GONE);
        }
        scaleSliderVisible = !scaleSliderVisible;
        scaleSlider.setVisibility(scaleSliderVisible ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.transform)
    public void onTransformClicked() {
        if (scaleSliderVisible) {
            scaleSliderVisible = false;
            scaleSlider.setVisibility(View.GONE);
        }
        axisSliderVisible = !axisSliderVisible;
        axisSliderLayout.setVisibility(axisSliderVisible ? View.VISIBLE : View.GONE);
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
        this.selectedObject = selectedObject;
        if (selectedObject == null) {
            axisSliderVisible = false;
            scaleSliderVisible = false;
            axisSliderLayout.setVisibility(View.GONE);
            scaleSlider.setVisibility(View.GONE);
            modifyLayout.setVisibility(View.GONE);
        } else modifyLayout.setVisibility(View.VISIBLE);
    }
}