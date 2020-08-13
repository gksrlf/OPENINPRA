package com.little_wizard.tdc.ui.draw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.little_wizard.tdc.R;
import com.little_wizard.tdc.util.draw.ObjectBuffer;

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
        holder.imageButton.setOnClickListener(view -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(pos);
            }
        });
        holder.imageView.setImageBitmap((elementList.get(pos).getBitmap()));
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

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageButton imageButton;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            imageButton = itemView.findViewById(R.id.row_button);
        }
    }

    void setClickListener(ItemClickListener listener) {
        this.mClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(int pos);
    }
}