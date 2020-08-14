package com.little_wizard.tdc.ui.main;

import android.content.Context;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.classes.RepoItem;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    private Map<String, List<RepoItem>> itemMap = new HashMap<>();

    private ItemClickListener mClickListener;

    RepositoryAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.repository_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        String key = (String) itemMap.keySet().toArray()[pos];
        RepoItem item = null;
        for (RepoItem tmp : Objects.requireNonNull(itemMap.get(key))) {
            if (FilenameUtils.getExtension(tmp.getName()).equals("jpg")) {
                item = tmp;
                break;
            }
        }
        if (item == null) {
            Toast.makeText(mContext, R.string.jpg_not_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        String path = "https://" + mContext.getString(R.string.s3_bucket_resize)
                + ".s3.ap-northeast-2.amazonaws.com/" + item.getName();
        Glide.with(mContext).load(path).centerCrop().into(holder.imageView);
        holder.name.setText(key);
    }

    @Override
    public int getItemCount() {
        return itemMap.size();
    }

    public void setItemList(List<RepoItem> itemList) {
        for (RepoItem item : itemList) {
            String baseName = FilenameUtils.getBaseName(item.getName());
            List<RepoItem> list = itemMapIndexOf(itemMap, baseName);
            list.add(item);
            itemMap.put(baseName, list);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        itemMap.clear();
        notifyDataSetChanged();
    }

    private List<RepoItem> itemMapIndexOf(Map<String, List<RepoItem>> map, String target) {
        for (String key : map.keySet()) {
            if (key.equals(target)) {
                return map.get(key);
            }
        }
        return new ArrayList<>();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView imageView;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            name = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                String key = (String) itemMap.keySet().toArray()[getAdapterPosition()];
                mClickListener.onItemClick(view, itemMap.get(key));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) {
                Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                String key = (String) itemMap.keySet().toArray()[getAdapterPosition()];
                mClickListener.onItemLongClick(view, itemMap.get(key));
            }
            return false;
        }
    }

    void setClickListener(ItemClickListener listener) {
        this.mClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, List<RepoItem> list);

        void onItemLongClick(View view, List<RepoItem> list);
    }
}