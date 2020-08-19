package com.little_wizard.tdc.util.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.little_wizard.tdc.R;
import com.little_wizard.tdc.classes.RepoItem;
import com.little_wizard.tdc.ui.main.MainActivity;
import com.little_wizard.tdc.ui.main.RepositoryAdapter;

import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.Object3DBuilder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

public class LoadDialog extends Dialog implements RepositoryAdapter.ItemClickListener {

    interface Callback {
        void onSelectedItem(File file);
    }

    private Callback callback;

    TextView title;
    RecyclerView recycler;
    Button cancel;
    LinearLayout emptyLayout;

    RepositoryAdapter adapter;
    List<Object3DData> dataList;

    Context context;

    public LoadDialog(@NonNull Context context, List<Object3DData> dataList, Callback callback) {
        super(context);
        this.dataList = dataList;
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.model_dialog);
        context = this.getContext();

        title = findViewById(R.id.title);
        cancel = findViewById(R.id.cancel);
        recycler = findViewById(R.id.recycler);
        emptyLayout = findViewById(R.id.emptyLayout);

        cancel.setOnClickListener(view -> dismiss());

        recycler.setLayoutManager(new LinearLayoutManager(context));
        adapter = new RepositoryAdapter(context);
        recycler.setAdapter(adapter);
        adapter.setClickListener(this);

        File f = new File(context.getExternalCacheDir().getAbsolutePath() + "/");
        File[] files = f.listFiles(pathName -> {
            for (Object3DData data : dataList) {
                if (data.getId().equals(pathName.getPath())) {
                    return false;
                }
            }
            return FilenameUtils.getExtension(pathName.getName()).equals("obj");
        });
        List<RepoItem> items = new ArrayList<>();
        assert files != null;
        for (File file : files)
            items.add(new RepoItem(file.getName(), MainActivity.fileToMD5(file)));
        if (!items.isEmpty()) adapter.setItemList(items);
        else emptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(View view, List<RepoItem> list) {
        try {
            if (callback != null) {
                callback.onSelectedItem(new File(context.getExternalCacheDir().getAbsolutePath() + "/" + list.get(0).getName()));
                dismiss();
            } else Toast.makeText(context, "Callback == null", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, List<RepoItem> list) {

    }
}