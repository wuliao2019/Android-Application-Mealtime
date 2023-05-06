package com.cqu.mealtime.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cqu.mealtime.Comment;
import com.cqu.mealtime.R;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final List<Comment> comments;
    List<String> stalls;
    List<String> canteens;
    private final Context context;

    public CommentsAdapter(Context context, List<Comment> comments, List<String> stalls, List<String> canteens) {
        this.comments = comments;
        this.context = context;
        this.canteens = canteens;
        this.stalls = stalls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_card, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentTitle;
        TextView commentRemark;
        TextView commentStall;
        TextView commentTime;
        TextView commentUser;

        public ViewHolder(View view) {
            super(view);
            commentTitle = view.findViewById(R.id.comment_title);
            commentRemark = view.findViewById(R.id.comment_remark);
            commentStall = view.findViewById(R.id.comment_stall);
            commentTime = view.findViewById(R.id.comment_time);
            commentUser = view.findViewById(R.id.user_name);
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commentTitle.setText(comment.getTitle());
        holder.commentRemark.setText(comment.getRemark());
        holder.commentTime.setText(comment.getTime());
        holder.commentUser.setText("来自："+comment.getUsername());
        holder.commentStall.setVisibility(View.VISIBLE);
        if (comment.getCan_id() == 0 && comment.getStall_id() == 0)
            holder.commentStall.setVisibility(View.GONE);
        else if (comment.getStall_id() == 0)
            holder.commentStall.setText(canteens.get(comment.getCan_id()));
        else
            holder.commentStall.setText(canteens.get(comment.getCan_id()) + " · " + stalls.get(comment.getStall_id()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}
