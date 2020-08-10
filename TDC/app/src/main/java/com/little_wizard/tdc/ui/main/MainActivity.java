package com.little_wizard.tdc.ui.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
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
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.classes.RepoItem;
import com.little_wizard.tdc.ui.camera.CameraActivity;
import com.little_wizard.tdc.ui.settings.SettingsActivity;
import com.little_wizard.tdc.util.view.ModelActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements RepositoryAdapter.ItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    public static boolean darkMode;
    @BindView(R.id.picture)
    Button picture;
    @BindView(R.id.slidingPanel)
    SlidingUpPanelLayout slidingPanel;
    @BindView(R.id.slideGuide)
    TextView slideGuide;
    @BindView(R.id.repositoryRecycler)
    RecyclerView repositoryRecycler;

    RepositoryAdapter repositoryAdapter;

    private String TAG = getClass().getSimpleName();

    public static AmazonS3 s3;
    TransferUtility transferUtility;

    String[] extensions = {"mtl", "jpg", "obj"};

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
        if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
        init();
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

    private List<String> getObjectListFromFolder(String bucketName, String folderKey) {
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

    @Override
    public void onItemClick(View view, RepoItem item) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.downloading));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        final int[] successCount = {0};

        for (String ext : extensions) {
            if (successCount[0] == -1) break;
            Completable.create(sub -> {
                String name = item.name + "." + ext;
                File file = new File(getExternalCacheDir().getAbsolutePath()
                        + "/" + name);
                if (!file.exists()) {
                    progressDialog.show();
                    TransferObserver transferObserver = transferUtility.download(
                            getString(R.string.s3_bucket_resize),
                            file.getName(),
                            file
                    );
                    transferObserver.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            Log.d(TAG, state.toString());
                            if (state == TransferState.COMPLETED)
                                sub.onComplete();
                            if (state != TransferState.IN_PROGRESS)
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        }

                        @Override
                        public void onError(int id, Exception e) {
                            sub.onError(e);
                        }
                    });
                } else sub.onComplete();
            }).subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onComplete() {
                    successCount[0]++;
                    if (successCount[0] == extensions.length) {
                        launchModelRendererActivity("file://" + getExternalCacheDir()
                                .getAbsolutePath() + "/" + item.name + ".obj");
                    }
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    successCount[0] = -1;
                }
            });
        }
    }

    void init() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:13fe9921-fc1f-49ab-b998-c59c0a367efe",
                Regions.AP_NORTHEAST_2
        );
        s3 = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
        transferUtility = TransferUtility.builder().s3Client(MainActivity.s3).context(this).build();
        TransferNetworkLossHandler.getInstance(this);

        repositoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        repositoryAdapter = new RepositoryAdapter(MainActivity.this);
        repositoryRecycler.setAdapter(repositoryAdapter);
        repositoryAdapter.setClickListener(this);

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

                List<RepoItem> itemList = new ArrayList<>();
                Thread thread = new Thread(() -> {
                    AWSCredentials crd = new BasicAWSCredentials("AKIAUIINZG7SDUSSH4XX", "MrDjFVRQXRKb95nmx0K48+s1srLJEqtRpImeOctE");
                    AmazonS3 s3 = new AmazonS3Client(crd);
                    ListObjectsRequest listObject = new ListObjectsRequest();
                    listObject.setBucketName(getString(R.string.s3_bucket_resize));
                    listObject.setPrefix("");
                    ObjectListing objects = s3.listObjects(listObject);
                    List<String> fileList = new ArrayList<>();
                    do {
                        objects = s3.listObjects(listObject);
                        for (S3ObjectSummary summary : objects.getObjectSummaries()) {
                            String key = summary.getKey();
                            String baseName = FilenameUtils.getBaseName(key);
                            String ext = FilenameUtils.getExtension(key);
                            if (!fileList.contains(baseName) && ext.equals("jpg")) {
                                fileList.add(baseName);
                                itemList.add(new RepoItem(baseName
                                        , Uri.parse("https://" + getString(R.string.s3_bucket_resize)
                                        + ".s3.ap-northeast-2.amazonaws.com/" + baseName + ".jpg")));
                            }
                        }
                        listObject.setMarker(objects.getNextMarker());
                    } while (objects.isTruncated());
                    Log.d(TAG, fileList.toString());
                });
                thread.start();
                try {
                    thread.join();
                    repositoryAdapter.setItemList(itemList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void launchModelRendererActivity(String uri) {
        Log.i("Menu", "Launching renderer for '" + uri + "'");
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        intent.putExtra("URI", uri);
        intent.putExtra("MODIFY", false);
        startActivity(intent);
    }
}