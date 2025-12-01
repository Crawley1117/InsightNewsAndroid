package com.example.insightnewsandroid.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.NewsArticle;

public class ExploreTopicAdapter extends ListAdapter<NewsArticle, ExploreTopicAdapter.TopicViewHolder> {

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(NewsArticle newsArticle);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ExploreTopicAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic_collection, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        NewsArticle newsArticle = getItem(position);
        holder.bind(newsArticle);
    }

    class TopicViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivThumb;
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvTopicTag;
        private final TextView tvFollowCount;

        TopicViewHolder(View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTopicTag = itemView.findViewById(R.id.tv_topic_tag);
            tvFollowCount = itemView.findViewById(R.id.tv_follow_count);
        }

        void bind(NewsArticle newsArticle) {
            tvTitle.setText(newsArticle.getTitle());

            // 设置内容（如果内容为空，显示默认文本）
            if (newsArticle.getContent() != null && !newsArticle.getContent().isEmpty()) {
                tvContent.setText(newsArticle.getContent());
            } else {
                tvContent.setText("暂无描述");
            }

            // 设置分类标签
            if (newsArticle.getCategory() != null && !newsArticle.getCategory().isEmpty()) {
                tvTopicTag.setText(newsArticle.getCategory());
            } else {
                tvTopicTag.setText("未分类");
            }

            // 设置关注人数 - 使用 getAttentionNum() 方法
            int attentionNum = newsArticle.getAttentionNum();
            tvFollowCount.setText(attentionNum + "人关注");

            // 加载图片
            if (newsArticle.getImageUrl() != null && !newsArticle.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(newsArticle.getImageUrl())
                        .placeholder(R.drawable.topic_background)
                        .into(ivThumb);
            } else {
                ivThumb.setImageResource(R.drawable.topic_background);
            }

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && newsArticle != null) {
                    onItemClickListener.onItemClick(newsArticle);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<NewsArticle> DIFF_CALLBACK = new DiffUtil.ItemCallback<NewsArticle>() {
        @Override
        public boolean areItemsTheSame(@NonNull NewsArticle oldItem, @NonNull NewsArticle newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NewsArticle oldItem, @NonNull NewsArticle newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getContent().equals(newItem.getContent());
        }
    };
}