package com.little_wizard.tdc.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.ui.camera.CameraActivity;
import com.little_wizard.tdc.ui.settings.SettingsActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static boolean darkMode;
    @BindView(R.id.picture)
    Button picture;
    @BindView(R.id.slidingPanel)
    SlidingUpPanelLayout slidingPanel;
    @BindView(R.id.slideGuide)
    TextView slideGuide;

    private String TAG = getClass().getSimpleName();

    public static AmazonS3 s3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = prefs.getBoolean("darkMode", false);
        if (darkMode) setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        picture.setBackgroundResource(darkMode ? R.drawable.btn_bg_white : R.drawable.btn_bg_black);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:13fe9921-fc1f-49ab-b998-c59c0a367efe",
                Regions.AP_NORTHEAST_2
        );
        s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));

        slidingPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case EXPANDED:
                        slideGuide.setText(getString(R.string.slide_down));
                        break;
                    case COLLAPSED:
                        slideGuide.setText(getString(R.string.slide_up));
                        break;
                }
                if (newState != SlidingUpPanelLayout.PanelState.EXPANDED) return;
                new Thread(() -> {
                    AWSCredentials crd = new BasicAWSCredentials("AKIAUIINZG7SDUSSH4XX", "MrDjFVRQXRKb95nmx0K48+s1srLJEqtRpImeOctE");
                    AmazonS3 s3 = new AmazonS3Client(crd);
                    ListObjectsRequest listObject = new ListObjectsRequest();
                    listObject.setBucketName(getString(R.string.s3_bucket_resize));
                    listObject.setPrefix("");
                    ObjectListing objects = s3.listObjects(listObject);
                    List<String> fileList = new ArrayList<>();
                    do {
                        objects = s3.listObjects(listObject);
                        for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                            String baseName = FilenameUtils.getBaseName(objectSummary.getKey());
                            if (!fileList.contains(baseName)) fileList.add(baseName);
                        }
                        listObject.setMarker(objects.getNextMarker());
                        Log.d(TAG, fileList.toString());
                    } while (objects.isTruncated());
                }).start();
                //TODO: recyclerView 만들고, 해당 아이템을 선택하면 파일들을 다운로드해서 뷰어에 띄우기.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        return true;
    }

    @OnClick(R.id.picture)
    public void onViewClicked() {
        startActivity(new Intent(this, CameraActivity.class));
    }

    public List<String> getObjectListFromFolder(String bucketName, String folderKey) {
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest()
                        .withBucketName(bucketName)
                        .withPrefix(folderKey + "/");
        List<String> keys = new ArrayList<>();
        ObjectListing objects = s3.listObjects(listObjectsRequest);
        for (; ; ) {
            List<S3ObjectSummary> summaries = objects.getObjectSummaries();
            if (summaries.size() < 1) {
                break;
            }
            summaries.forEach(s -> keys.add(s.getKey()));
            objects = s3.listNextBatchOfObjects(objects);
        }
        return keys;
    }
}