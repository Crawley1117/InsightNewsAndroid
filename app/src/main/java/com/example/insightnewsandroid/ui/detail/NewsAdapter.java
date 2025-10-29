package com.example.insightnewsandroid.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // 添加这行导入
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;
    private OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }

    public void setOnNewsClickListener(OnNewsClickListener listener) {
        this.listener = listener;
    }

    public NewsAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    public void updateNews(List<NewsItem> newNews) {
        this.newsList = newNews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem news = newsList.get(position);
        holder.bind(news);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsClick(news);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivNews;
        private TextView tvTitle;
        private TextView tvTime;
        private TextView tvViews;
        private TextView tvLikes;
        private TextView tvComments;
        private TextView tvTopicTag;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNews = itemView.findViewById(R.id.iv_news);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvViews = itemView.findViewById(R.id.tv_views);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvComments = itemView.findViewById(R.id.tv_comments);
            tvTopicTag = itemView.findViewById(R.id.tv_topic_tag);
        }

        public void bind(NewsItem news) {
            tvTitle.setText(news.getTitle());
            tvTime.setText(news.getDate());
            tvViews.setText(news.getViewCount() + "浏览");
            tvLikes.setText(news.getLikeCount() + "赞");
            tvComments.setText(news.getCommentCount() + "评论");

            // 直接显示话题分类（政治、经济等）
            if (news.getTopicCategory() != null && !news.getTopicCategory().isEmpty()) {
                tvTopicTag.setVisibility(View.VISIBLE);
                tvTopicTag.setText(news.getTopicCategory());
            } else {
                tvTopicTag.setVisibility(View.GONE);
            }

            // 加载新闻图片（如果有）
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(news.getImageUrl())
                        .placeholder(R.color.gray_light)
                        .error(R.color.gray_light)
                        .into(ivNews);
            } else {
                ivNews.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.gray_light));
            }
        }
    }
}