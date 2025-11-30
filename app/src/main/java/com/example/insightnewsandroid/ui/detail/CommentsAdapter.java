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
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onLikeClick(int commentId);
        void onReplyClick(int commentId, String username);
        void onLoadRepliesClick(int parentCommentId);
        void onDeleteClick(int commentId);
    }

    public CommentsAdapter(List<Comment> comments, Context context) {
        this.comments = comments;
        this.context = context;
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

        ViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Comment comment) {
            binding.tvUsername.setText(comment.getUsername());
            binding.tvCommentContent.setText(comment.getComment());
            binding.tvTimestamp.setText(comment.getCreatedAt());
            binding.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
            binding.ivLike.setSelected(comment.isLike());

            Glide.with(context).load(comment.getUserImg()).placeholder(R.drawable.ic_default_avatar).into(binding.ivUserAvatar);

            // [已修改] 根据 myComment 字段显示/隐藏删除按钮
            binding.ivDeleteComment.setVisibility(comment.isMyComment() ? View.VISIBLE : View.GONE);
            binding.ivDeleteComment.setOnClickListener(v -> {
                if(listener != null) listener.onDeleteClick(comment.getId());
            });

            binding.ivLike.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClick(comment.getId());
            });

            binding.ivReply.setOnClickListener(v -> {
                if (listener != null) listener.onReplyClick(comment.getId(), comment.getUsername());
            });

            updateSubCommentsUI(comment);
        }

        private void updateSubCommentsUI(Comment comment) {
            binding.subCommentsContainer.removeAllViews();
            
            List<Comment> children = comment.getChildren();
            boolean hasLoadedChildren = children != null && !children.isEmpty();

            if (hasLoadedChildren) {
                for (Comment child : children) {
                    binding.subCommentsContainer.addView(createSubCommentView(child));
                }
                binding.tvExpandReplies.setText("收起回复");
            } else {
                binding.tvExpandReplies.setText("查看回复");
            }
            
            binding.tvExpandReplies.setVisibility(View.VISIBLE); 

            binding.tvExpandReplies.setOnClickListener(v -> {
                if (listener != null) {
                    if (hasLoadedChildren) {
                        comment.setChildren(null);
                        updateSubCommentsUI(comment);
                    } else {
                        listener.onLoadRepliesClick(comment.getId());
                    }
                }
            });
        }

        private View createSubCommentView(Comment subComment) {
            ItemSubCommentBinding subBinding = ItemSubCommentBinding.inflate(LayoutInflater.from(context));
            subBinding.tvUsername.setText(subComment.getUsername());
            subBinding.tvCommentContent.setText(subComment.getComment());
            Glide.with(context).load(subComment.getUserImg()).placeholder(R.drawable.ic_default_avatar).into(subBinding.ivUserAvatar);
            return subBinding.getRoot();
        }
    }
}
