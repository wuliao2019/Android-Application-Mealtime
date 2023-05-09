package com.cqu.mealtime.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cqu.mealtime.Canteen;
import com.cqu.mealtime.R;
import com.cqu.mealtime.util.UtilKt;

import java.util.List;

class CanteenAdapter extends RecyclerView.Adapter<CanteenAdapter.Vh> {
    private final Context context;
    public List<Canteen> canteensList;

    public CanteenAdapter(Context context, List<Canteen> canteensList) {
        this.context = context;
        this.canteensList = canteensList;
    }

    @NonNull
    @Override
    public CanteenAdapter.Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(LayoutInflater.from(context).inflate(R.layout.canteen_card, parent, false));
    }

    @Override
    public void onBindViewHolder(CanteenAdapter.Vh holder, final int position) {
        holder.itemName.setText(canteensList.get(position).getName());
        holder.itemTime.setText("营业时间：\n" + canteensList.get(position).getTime());
        holder.itemNum.setText(String.valueOf(canteensList.get(position).getFlow()));
        holder.itemState.setText(canteensList.get(position).getState());
        holder.itemState.setTextColor(canteensList.get(position).getColor());
        holder.itemNum.setTextColor(canteensList.get(position).getColor());

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onClick(position, v));
        }
    }

    public interface OnItemClickListener {
        void onClick(int position, View v);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return canteensList.size();
    }

    static class Vh extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final TextView itemState;
        private final TextView itemNum;
        private final TextView itemTime;

        public Vh(View itemView) {
            super(itemView);
            UtilKt.addClickScale(itemView, 0.9f, 150);
            itemName = itemView.findViewById(R.id.canteen_name);
            itemState = itemView.findViewById(R.id.canteen_state);
            itemNum = itemView.findViewById(R.id.canteen_num);
            itemTime = itemView.findViewById(R.id.canteen_time);
        }
    }
}