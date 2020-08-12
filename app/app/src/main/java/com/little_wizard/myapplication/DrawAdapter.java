package com.little_wizard.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.little_wizard.myapplication.util.ObjectBuffer;

import java.util.ArrayList;
import java.util.List;

public class DrawAdapter extends RecyclerView.Adapter<DrawAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    private List<ObjectBuffer.Element> elementList = new ArrayList<>();

    private ItemClickListener mClickListener;

    DrawAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.draw_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Glide.with(mContext).load(elementList.get(pos).bitmap).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return elementList.size();
    }

    public void setElementList(List<ObjectBuffer.Element> elementList) {
        this.elementList = elementList;
        notifyDataSetChanged();
    }

    public void clear() {
        elementList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    void setClickListener(ItemClickListener listener) {
        this.mClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int pos);
    }
}