// ui/explore/ExploreTopicAdapter.java
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
import com.example.insightnewsandroid.databinding.ItemExploreTopicBinding;
import com.example.insightnewsandroid.ui.explore.model.ExploreTopic;

public class ExploreTopicAdapter extends ListAdapter<ExploreTopic, ExploreTopicAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ExploreTopic topic);
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
        ExploreTopic topic = getItem(position);
        holder.bind(topic);
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

        public void bind(ExploreTopic topic) {
            binding.tvTitle.setText(topic.getTitle());
            binding.tvContent.setText(topic.getContent());
            binding.tvCategory.setText(topic.getTopic());
            binding.tvFollows.setText(topic.getFollows() + "人关注");
            binding.tvHashTag.setText("#");

            // 正确的图片加载方式 - 避免方法重载冲突
            String imageUrl = topic.getThumbPhotoURL();

            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
                // 有有效图片URL时加载图片
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .into(binding.ivThumb);
            } else {
                // 没有图片时清除图片，显示灰色背景
                binding.ivThumb.setImageDrawable(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<ExploreTopic> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ExploreTopic>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExploreTopic oldItem, @NonNull ExploreTopic newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExploreTopic oldItem, @NonNull ExploreTopic newItem) {
                    return oldItem.equals(newItem);
                }
            };
}