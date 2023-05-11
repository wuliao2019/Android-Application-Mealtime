package com.cqu.mealtime.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cqu.mealtime.R;
import com.cqu.mealtime.Stall;
import com.cqu.mealtime.util.UtilKt;

import java.util.List;

class StallAdapter extends RecyclerView.Adapter<StallAdapter.Vh> {
    private final Context context;
    public List<Stall> stallsList;

    public StallAdapter(Context context, List<Stall> stallsList) {
        this.context = context;
        this.stallsList = stallsList;
    }

    @NonNull
    @Override
    public StallAdapter.Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(LayoutInflater.from(context).inflate(R.layout.stall_card, parent, false));
    }

    @Override
    public void onBindViewHolder(StallAdapter.Vh holder, final int position) {
        holder.itemName.setText(stallsList.get(position).getName());
        holder.itemId.setText("#" + stallsList.get(position).getId());
        holder.itemType.setText(DashboardData.types.get(stallsList.get(position).getType()));
        holder.itemTypeBack.setCardBackgroundColor(DashboardData.backColors.get(stallsList.get(position).getType() - 1));
        holder.itemLocation.setText(DashboardData.canteens.get(stallsList.get(position).getLocation1()) + "·" + DashboardData.loc.get(stallsList.get(position).getLocation2()));
        holder.itemState.setText(stallsList.get(position).getState());
        holder.itemState.setTextColor(stallsList.get(position).getColor());
        holder.itemTime.setText("预计 " + stallsList.get(position).getWaitTime() + " min");
        if (stallsList.get(position).getName().length() > 7)
            holder.itemName.setTextSize(14);
        else
            holder.itemName.setTextSize(16);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onClick(position, v));
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemClickListener.onLongClick(position, v);
                return true;
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(int position, View v);

        void onLongClick(int position, View v);
    }

    private StallAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(StallAdapter.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return stallsList.size();
    }

    static class Vh extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final TextView itemId;
        private final TextView itemLocation;
        private final TextView itemType;
        private final TextView itemState;
        private final CardView itemTypeBack;
        private final TextView itemTime;

        public Vh(View itemView) {
            super(itemView);
            UtilKt.addClickScale(itemView, 0.9f, 150);
            itemName = itemView.findViewById(R.id.card_name);
            itemId = itemView.findViewById(R.id.card_id);
            itemLocation = itemView.findViewById(R.id.card_location);
            itemType = itemView.findViewById(R.id.card_type);
            itemTypeBack = itemView.findViewById(R.id.card_type_back);
            itemState = itemView.findViewById(R.id.card_state);
            itemTime = itemView.findViewById(R.id.card_time);
        }
    }
}