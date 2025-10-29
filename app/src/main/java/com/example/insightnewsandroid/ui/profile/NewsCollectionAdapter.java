package com.example.insightnewsandroid.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.NewsItem;

import java.util.List;

public class NewsCollectionAdapter extends RecyclerView.Adapter<NewsCollectionAdapter.ViewHolder> {

    private List<NewsItem> newsList;
    private OnActionListener onActionListener;

    public interface OnActionListener {
        void onLikeClick(int position);
        void onDislikeClick(int position);
    }

    public NewsCollectionAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.onActionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);

        holder.tvTitle.setText(newsItem.getTitle());
        holder.tvDate.setText(newsItem.getDate());

        // 三个状态的处理：
        if (newsItem.isLiked()) {
            // 点赞状态 - 红色
            holder.ivLike.setColorFilter(0xFFFF0000);
            holder.ivDislike.setColorFilter(0xFF888888); // 拉踩按钮变灰
        } else if (newsItem.isDisliked()) {
            // 拉踩状态 - 红色
            holder.ivLike.setColorFilter(0xFF888888); // 点赞按钮变灰
            holder.ivDislike.setColorFilter(0xFFFF0000);
        } else {
            // 中立状态 - 两个按钮都灰色
            holder.ivLike.setColorFilter(0xFF888888);
            holder.ivDislike.setColorFilter(0xFF888888);
        }

        // 设置点击监听器
        holder.ivLike.setOnClickListener(v -> {
            if (onActionListener != null) {
                onActionListener.onLikeClick(position);
            }
        });

        holder.ivDislike.setOnClickListener(v -> {
            if (onActionListener != null) {
                onActionListener.onDislikeClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        ImageView ivLike;
        ImageView ivDislike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvDate = itemView.findViewById(R.id.tv_news_date);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivDislike = itemView.findViewById(R.id.iv_dislike);
        }
    }
}