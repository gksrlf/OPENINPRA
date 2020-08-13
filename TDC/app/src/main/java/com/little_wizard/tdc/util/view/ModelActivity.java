package com.little_wizard.tdc.util.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.little_wizard.tdc.util.scene_loader.SceneLoader;

import org.andresoviedo.util.android.ContentUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This activity represents the container for our 3D viewer.
 *
 * @author andresoviedo
 */
public class ModelActivity extends AppCompatActivity {

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
    @BindView(R.id.reshapeLayout)
    ConstraintLayout reshapeLayout;
    @BindView(R.id.xAxisSlider)
    RangeSlider xAxisSlider;
    @BindView(R.id.yAxisSlider)
    RangeSlider yAxisSlider;
    @BindView(R.id.zAxisSlider)
    RangeSlider zAxisSlider;

    private Uri paramUri;

    private float[] backgroundColor = new float[]{0f, 0f, 0f, 1.0f};

    private ModelSurfaceView gLView;

    private SceneLoader scene;

    private boolean sliderVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.darkMode) setTheme(R.style.Theme_MaterialComponents_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getString("URI") != null) {
                this.paramUri = Uri.parse(b.getString("URI"));
            }
        }
        Log.i("Renderer", "Params: uri '" + paramUri + "'");

        // Create our 3D sceneario
        if (paramUri == null) {
            //scene = new ExampleSceneLoader(this);
        } else {
            scene = new SceneLoader(this);
        }
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

        axisSliderInit();
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
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_LOAD_TEXTURE:
                // The URI of the selected file
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
    }

    @OnClick(R.id.transform)
    public void onTransformClicked() {
        sliderVisible = !sliderVisible;
        axisSliderLayout.setVisibility(sliderVisible ? View.VISIBLE : View.GONE);
    }

    private void axisSliderInit() {
        xAxisSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        xAxisSlider.addOnChangeListener((slider, value, fromUser) ->
                Log.d("Slider", String.valueOf(value)));

        yAxisSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        yAxisSlider.addOnChangeListener((slider, value, fromUser) ->
                Log.d("Slider", String.valueOf(value)));

        zAxisSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                slider.setValues(0.0f, 0.0f);
            }
        });
        zAxisSlider.addOnChangeListener((slider, value, fromUser) ->
                Log.d("Slider", String.valueOf(value)));
    }
}
