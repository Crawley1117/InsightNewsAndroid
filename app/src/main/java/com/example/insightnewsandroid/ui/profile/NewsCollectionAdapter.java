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
        void onCollectClick(int position);
        void onLikeClick(int position);
        void onDislikeClick(int position);
        void onItemClick(int position);
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

        // 设置新闻标题
        holder.tvTitle.setText(newsItem.getTitle());

        // 设置新闻日期
        holder.tvDate.setText(newsItem.getDate());

        // 设置整个卡片的背景
        setupCardBackground(holder, newsItem);

        // 设置可信度标签文本
        setupCredibilityTag(holder, newsItem);

        // 设置收藏按钮状态
        if (newsItem.isCollected()) {
            holder.ivCollect.setImageResource(R.drawable.ic_collect_selected);
        } else {
            holder.ivCollect.setImageResource(R.drawable.ic_collect_unselected);
        }

        // 设置点赞/拉踩状态
        updateLikeDislikeUI(holder, newsItem);

        // 设置点击监听器
        holder.ivCollect.setOnClickListener(v -> {
            if (onActionListener != null) {
                onActionListener.onCollectClick(position);
            }
        });

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

        holder.itemView.setOnClickListener(v -> {
            if (onActionListener != null) {
                onActionListener.onItemClick(position);
            }
        });
    }

    private void setupCardBackground(ViewHolder holder, NewsItem newsItem) {
        String credibilityLevel = newsItem.getCredibilityLevel();

        if (credibilityLevel != null && !credibilityLevel.isEmpty()) {
            // 根据可信度等级设置整个卡片的背景
            switch (credibilityLevel) {
                case "高":
                    holder.itemView.setBackgroundResource(R.drawable.bg_highscore);
                    break;
                case "较高":
                    holder.itemView.setBackgroundResource(R.drawable.bg_sub_highscore);
                    break;
                case "中":
                    holder.itemView.setBackgroundResource(R.drawable.bg_medium);
                    break;
                case "较低":
                    holder.itemView.setBackgroundResource(R.drawable.bg_sub_lowscore);
                    break;
                case "低":
                    holder.itemView.setBackgroundResource(R.drawable.bg_lowscore);
                    break;
                default:
                    holder.itemView.setBackgroundResource(R.drawable.bg_medium);
                    break;
            }
        } else {
            // 如果没有可信度信息，使用默认背景
            holder.itemView.setBackgroundResource(R.drawable.bg_medium);
        }
    }

    private void setupCredibilityTag(ViewHolder holder, NewsItem newsItem) {
        String credibilityLevel = newsItem.getCredibilityLevel();
        String credibilityScore = newsItem.getCredibilityScore();

        if (credibilityLevel != null && !credibilityLevel.isEmpty()) {
            holder.tvCredibilityTag.setVisibility(View.VISIBLE);

            // 设置标签文本
            String tagText = "可信度" + credibilityLevel;
            if (credibilityScore != null && !credibilityScore.isEmpty()) {
                tagText = "可信度" + credibilityLevel + " (" + credibilityScore + ")";
            }
            holder.tvCredibilityTag.setText(tagText);
        } else {
            holder.tvCredibilityTag.setVisibility(View.GONE);
        }
    }

    private void updateLikeDislikeUI(ViewHolder holder, NewsItem newsItem) {
        if (newsItem.isLiked()) {
            holder.ivLike.setColorFilter(0xFFFF0000); // 红色
            holder.ivDislike.setColorFilter(0xFF888888); // 灰色
        } else if (newsItem.isDisliked()) {
            holder.ivLike.setColorFilter(0xFF888888); // 灰色
            holder.ivDislike.setColorFilter(0xFFFF0000); // 红色
        } else {
            holder.ivLike.setColorFilter(0xFF888888); // 灰色
            holder.ivDislike.setColorFilter(0xFF888888); // 灰色
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvCredibilityTag;
        ImageView ivCollect;
        ImageView ivLike;
        ImageView ivDislike;
        View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvDate = itemView.findViewById(R.id.tv_news_date);
            tvCredibilityTag = itemView.findViewById(R.id.tv_credibility_tag);
            ivCollect = itemView.findViewById(R.id.iv_collect);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivDislike = itemView.findViewById(R.id.iv_dislike);
        }
    }
}
