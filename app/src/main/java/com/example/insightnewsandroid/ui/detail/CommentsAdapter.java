package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.databinding.ItemCommentBinding;
import com.example.insightnewsandroid.databinding.ItemSubCommentBinding;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> comments;
    private final Context context;
    private final int currentUserId;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onLikeClick(int commentId, boolean isSubComment);
        void onReplyClick(int commentId, String username);
        void onDeleteClick(int commentId, boolean isSubComment);
    }

    public CommentsAdapter(List<Comment> comments, Context context, int currentUserId) {
        this.comments = comments;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;
        private boolean isExpanded = false;

        ViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Comment comment) {
            binding.tvUsername.setText(comment.getUsername());
            binding.tvCommentContent.setText(comment.getComment());
            binding.tvTimestamp.setText(comment.getCreatedAt()); // [已修复] 恢复父评论时间戳
            binding.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
            binding.ivLike.setSelected(comment.isLike());

            Glide.with(context).load(comment.getUserImg()).placeholder(R.drawable.ic_default_avatar).into(binding.ivUserAvatar);

            binding.ivDeleteComment.setVisibility(comment.getUserId() == currentUserId ? View.VISIBLE : View.GONE);
            binding.ivDeleteComment.setOnClickListener(v -> {
                if(listener != null) listener.onDeleteClick(comment.getId(), false);
            });

            binding.ivLike.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClick(comment.getId(), false);
            });

            binding.ivReply.setOnClickListener(v -> {
                if (listener != null) listener.onReplyClick(comment.getId(), comment.getUsername());
            });

            updateSubCommentsUI(comment);
        }

        private void updateSubCommentsUI(final Comment comment) {
            List<Comment> children = comment.getChildren();
            boolean hasChildren = children != null && !children.isEmpty();

            if (!hasChildren) {
                binding.tvExpandReplies.setVisibility(View.GONE);
                binding.subCommentsContainer.setVisibility(View.GONE);
                return;
            }

            binding.tvExpandReplies.setVisibility(View.VISIBLE);
            binding.subCommentsContainer.removeAllViews();

            if (isExpanded) {
                for (Comment child : children) {
                    binding.subCommentsContainer.addView(createSubCommentView(child));
                }
                binding.subCommentsContainer.setVisibility(View.VISIBLE);
                binding.tvExpandReplies.setText("收起回复");
            } else {
                binding.subCommentsContainer.addView(createSubCommentView(children.get(0)));
                binding.subCommentsContainer.setVisibility(View.VISIBLE);
                if (children.size() > 1) {
                    binding.tvExpandReplies.setText(String.format("—— 查看另外 %d 条回复", children.size() - 1));
                } else {
                    binding.tvExpandReplies.setVisibility(View.GONE);
                }
            }

            binding.tvExpandReplies.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                updateSubCommentsUI(comment);
            });
        }

        private View createSubCommentView(final Comment subComment) {
            ItemSubCommentBinding subBinding = ItemSubCommentBinding.inflate(LayoutInflater.from(context));
            subBinding.tvUsername.setText(subComment.getUsername());
            subBinding.tvCommentContent.setText(subComment.getComment());
            subBinding.tvSubCommentTimestamp.setText(subComment.getCreatedAt()); // [已修复] 恢复子评论时间戳
            
            subBinding.tvSubCommentLikeCount.setText(String.valueOf(subComment.getLikeCount()));
            subBinding.ivSubCommentLike.setSelected(subComment.isLike());
            subBinding.ivDeleteSubComment.setVisibility(subComment.getUserId() == currentUserId ? View.VISIBLE : View.GONE);

            subBinding.ivSubCommentLike.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClick(subComment.getId(), true);
            });

            subBinding.ivDeleteSubComment.setOnClickListener(v -> {
                if(listener != null) listener.onDeleteClick(subComment.getId(), true);
            });

            Glide.with(context).load(subComment.getUserImg()).placeholder(R.drawable.ic_default_avatar).into(subBinding.ivUserAvatar);
            return subBinding.getRoot();
        }
    }
}
