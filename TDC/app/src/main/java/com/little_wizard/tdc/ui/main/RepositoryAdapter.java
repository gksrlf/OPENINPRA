package com.little_wizard.tdc.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.little_wizard.tdc.R;
import com.little_wizard.tdc.classes.RepoItem;

import java.util.ArrayList;
import java.util.List;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    private List<RepoItem> itemList = new ArrayList<>();

    private ViewHolder.ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;

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
        RepoItem item = itemList.get(pos);
        Glide.with(mContext).load(item.jpgPath).centerCrop().into(holder.imageView);
        holder.name.setText(item.name);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setItemList(List<RepoItem> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
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
                mClickListener.onItemClick(view, categoryList.get(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, categoryList.get(getAdapterPosition()));
            }
            return false;
        }

        void setClickListener(ItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }

        public interface ItemClickListener {
            void onItemClick(View view, int position);
        }

        public interface ItemLongClickListener {
            void onItemLongClick(View view, int position);
        }
    }
}