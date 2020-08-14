package com.little_wizard.tdc.util.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.little_wizard.tdc.R;
import com.little_wizard.tdc.ui.main.MainActivity;
import com.little_wizard.tdc.util.demo.ModelSurfaceView;
import com.little_wizard.tdc.util.demo.SceneLoader;

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
    private static final int FULLSCREEN_DELAY = 10000;
    @BindView(R.id.layout)
    ConstraintLayout layout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.modifyTools)
    LinearLayout modifyTools;
    @BindView(R.id.zoomIn)
    ImageButton zoomIn;
    @BindView(R.id.zoomOut)
    ImageButton zoomOut;
    @BindView(R.id.panTool)
    ImageButton panTool;

    /**
     * Type of model if file name has no extension (provided though content provider)
     */
    private int paramType;
    /**
     * The file to load. Passed as input parameter
     */
    private Uri paramUri;
    /**
     * Enter into Android Immersive mode so the renderer is full screen or not
     */
    private boolean immersiveMode = true;
    /**
     * Background GL clear color. Default is light gray
     */
    private float[] backgroundColor = new float[]{0f, 0f, 0f, 1.0f};

    private ModelSurfaceView gLView;

    private SceneLoader scene;

    private Handler handler;

    boolean modify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.darkMode) setTheme(R.style.Theme_AppCompat_NoActionBar);
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
            if (b.getBoolean("MODIFY")) {
                modify = true;
                modifyTools.setVisibility(View.VISIBLE);
            }
        }
        Log.i("Renderer", "Params: uri '" + paramUri + "'");

        handler = new Handler(getMainLooper());

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(modify ? R.menu.menu_save : R.menu.menu_modify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_modify: {
                Intent intent = getIntent();
                intent.putExtra("MODIFY", true);
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            case R.id.action_save: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public Uri getParamUri() {
        return paramUri;
    }

    public int getParamType() {
        return paramType;
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

    @OnClick(R.id.zoomIn)
    public void onZoomInClicked() {
        //TODO: 확대 기능 구현.
    }

    @OnClick(R.id.zoomOut)
    public void onZoomOutClicked() {
        //TODO: 축소 기능 구현.
    }

    @OnClick(R.id.panTool)
    public void onPanToolClicked() {
        //TODO: 이동 기능 구현.
    }
}
