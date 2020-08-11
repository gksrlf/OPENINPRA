package com.little_wizard.tdc.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.classes.RepoItem;
import com.little_wizard.tdc.ui.camera.CameraActivity;
import com.little_wizard.tdc.ui.settings.SettingsActivity;
import com.little_wizard.tdc.util.NetworkStatus;
import com.little_wizard.tdc.util.S3Transfer;
import com.little_wizard.tdc.util.view.ModelActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements RepositoryAdapter.ItemClickListener, S3Transfer.TransferCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.camera)
    Button camera;
    @BindView(R.id.slidingPanel)
    SlidingUpPanelLayout slidingPanel;
    @BindView(R.id.slideGuide)
    ImageView slideGuide;
    @BindView(R.id.repositoryRecycler)
    RecyclerView repositoryRecycler;

    RepositoryAdapter repositoryAdapter;
    @BindView(R.id.networkLayout)
    LinearLayout networkLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    Context mContext;
    public static boolean darkMode;

    private String TAG = getClass().getSimpleName();

    NetworkStatus status;
    S3Transfer transfer;

    String[] extensions = {"mtl", "jpg", "obj"};

    boolean expanded = false;

    long backKeyPressedTime = 0;
    long TIME_INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = prefs.getBoolean("darkMode", false);
        if (darkMode) setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        camera.setBackgroundResource(darkMode ? R.drawable.btn_bg_white : R.drawable.btn_bg_black);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);

        mContext = this;

        status = new NetworkStatus(mContext);

        init();
    }

    @Override
    public void onBackPressed() {
        if (expanded) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + TIME_INTERVAL) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(mContext, getString(R.string.exit_confirm), Toast.LENGTH_SHORT).show();
            } else {
                moveTaskToBack(true);
                finishAndRemoveTask();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
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
                intent = new Intent(mContext, SettingsActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        return true;
    }

    @OnClick(R.id.camera)
    public void onViewClicked() {
        startActivity(new Intent(mContext, CameraActivity.class));
    }

    @Override
    public void onItemClick(View view, RepoItem item) {
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(getString(R.string.downloading));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);

        final int[] successCount = {0};

        for (String ext : extensions) {
            if (successCount[0] == -1) break;
            Completable.create(sub -> {
                File file = new File(getExternalCacheDir().getAbsolutePath()
                        + "/" + item.name + "." + ext);
                if (!file.exists()) {
                    if (!status.isConnected()) {
                        Toast.makeText(mContext, getString(R.string.network_not_connected), Toast.LENGTH_LONG).show();
                        return;
                    }
                    progressDialog.show();
                    transfer.download(R.string.s3_bucket_resize, file).setTransferListener(new TransferListener() {
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
        transfer = new S3Transfer(this);

        repositoryRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        repositoryAdapter = new RepositoryAdapter(mContext);
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
                        slideGuide.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_down));
                        expanded = true;
                        break;
                    case COLLAPSED:
                        slideGuide.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_up));
                        expanded = false;
                        break;
                }
                if (newState != SlidingUpPanelLayout.PanelState.EXPANDED) return;

                repositoryAdapter.clear();

                if (!status.isConnected()) {
                    networkLayout.setVisibility(View.VISIBLE);
                    return;
                } else networkLayout.setVisibility(View.GONE);

                Observable<RepoItem> observable = Observable.create(
                        emitter -> {
                            progressBar.setVisibility(View.VISIBLE);
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
                                    for (S3ObjectSummary summary : objects.getObjectSummaries()) {
                                        String key = summary.getKey();
                                        String baseName = FilenameUtils.getBaseName(key);
                                        String ext = FilenameUtils.getExtension(key);
                                        if (!fileList.contains(baseName) && ext.equals("jpg")) {
                                            fileList.add(baseName);
                                            emitter.onNext(new RepoItem(baseName
                                                    , Uri.parse("https://" + getString(R.string.s3_bucket_resize)
                                                    + ".s3.ap-northeast-2.amazonaws.com/" + baseName + ".jpg")));
                                        }
                                    }
                                    listObject.setMarker(objects.getNextMarker());
                                } while (objects.isTruncated());
                                emitter.onComplete();
                            }).start();
                        }
                );

                List<RepoItem> itemList = new ArrayList<>();
                Observer<RepoItem> observer = new Observer<RepoItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RepoItem item) {
                        itemList.add(item);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        runOnUiThread(() -> {
                            repositoryAdapter.setItemList(itemList);
                            progressBar.setVisibility(View.GONE);
                        });
                    }
                };
                observable.subscribe(observer);
            }
        });
    }

    private String fileToMD5(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String convertHashToString(byte[] md5Bytes) {
        StringBuilder returnVal = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            returnVal.append(Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1));
        }
        return returnVal.toString();
    }

    private void launchModelRendererActivity(String uri) {
        Log.i("Menu", "Launching renderer for '" + uri + "'");
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        intent.putExtra("URI", uri);
        intent.putExtra("MODIFY", false);
        startActivity(intent);
    }

    @Override
    public void onStateChanged(TransferState state) {

    }

    @Override
    public void onError(int id, Exception e) {

    }
}