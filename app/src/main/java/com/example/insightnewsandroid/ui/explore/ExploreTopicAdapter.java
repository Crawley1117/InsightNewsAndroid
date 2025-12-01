package com.example.insightnewsandroid.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.NewsArticle; // 导入新的数据模型
import com.example.insightnewsandroid.databinding.ItemExploreTopicBinding;

// 修改：整个Adapter现在使用NewsArticle模型
public class ExploreTopicAdapter extends ListAdapter<NewsArticle, ExploreTopicAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;

    // 修改：接口使用NewsArticle
    public interface OnItemClickListener {
        void onItemClick(NewsArticle article);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ExploreTopicAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExploreTopicBinding binding = ItemExploreTopicBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsArticle article = getItem(position);
        holder.bind(article);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemExploreTopicBinding binding;

        public ViewHolder(ItemExploreTopicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(getItem(position));
                }
            });
        }

        // 修改：bind方法现在接收NewsArticle对象
        public void bind(NewsArticle article) {
            binding.tvTitle.setText(article.getTitle());
            binding.tvContent.setText(article.getContent());

            // 隐藏在新模型中不存在的字段对应的UI控件
            binding.tvCategory.setVisibility(View.GONE);
            binding.tvFollows.setVisibility(View.GONE);
            binding.tvHashTag.setVisibility(View.GONE);

            String imageUrl = article.getImageUrl();

            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        // [已修改] 使用更合适的灰色背景作为占位图
                        .placeholder(R.drawable.button_background)
                        // [已修改] 加载错误时也使用相同的灰色背景
                        .error(R.drawable.button_background)
                        .into(binding.ivThumb);
            } else {
                // [已修改] 如果没有图片，也直接设置灰色背景
                binding.ivThumb.setImageResource(R.drawable.button_background);
            }
        }
    }

    // 修改：DiffUtil现在比较NewsArticle对象
    private static final DiffUtil.ItemCallback<NewsArticle> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NewsArticle>() {
                @Override
                public boolean areItemsTheSame(@NonNull NewsArticle oldItem, @NonNull NewsArticle newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull NewsArticle oldItem, @NonNull NewsArticle newItem) {
                    // 安全的内容比较，处理可能为 null 的字段
                    return java.util.Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && java.util.Objects.equals(oldItem.getContent(), newItem.getContent());
                }
            };
}