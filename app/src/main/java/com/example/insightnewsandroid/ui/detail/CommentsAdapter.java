package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.Comment;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private String token;
    private OnCommentActionListener listener;
    private Context context;
    private int topicId;

    public interface OnCommentActionListener {
        void onLikeClick(int topicId, int commentId, boolean newLikeStatus, int newLikeCount);
        void onReplyClick(int commentId, String username);
    }

    public List<Comment> getComments() {
        return comments;
    }

    public CommentsAdapter(List<Comment> comments, String token, Context context, int topicId) {
        this.comments = comments;
        this.token = token;
        this.context = context;
        this.topicId = topicId;
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
        Log.d("CommentsAdapter", "监听器设置: " + (listener != null ? "成功" : "失败"));
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
        Log.d("CommentsAdapter", "评论列表更新，数量: " + (newComments != null ? newComments.size() : 0));
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvUsername;
        private TextView tvTime;
        private TextView tvContent;
        private TextView tvLikeCount;
        private ImageView ivLike;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            ivLike = itemView.findViewById(R.id.iv_like);
        }

        public void bind(Comment comment) {
            Log.d("CommentsAdapter", "绑定评论: id=" + comment.getId() + ", like=" + comment.isLike() + ", likeCount=" + comment.getLikeCount());

            // 加载头像
            String userImg = comment.getUserImg();
            if (userImg != null && !userImg.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(userImg)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            tvUsername.setText(comment.getUsername());
            tvTime.setText(comment.getCreatedAt());
            tvContent.setText(comment.getComment());
            tvLikeCount.setText(String.valueOf(comment.getLikeCount()));

            // 设置点赞状态
            if (comment.isLike()) {
                ivLike.setImageResource(R.drawable.ic_like_selected);
                ivLike.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                ivLike.setImageResource(R.drawable.ic_like_unselected);
                ivLike.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.gray));
            }

            // 点赞点击事件 - 修复版本
            ivLike.setOnClickListener(v -> {
                Log.d("CommentsAdapter", "点赞按钮被点击, commentId=" + comment.getId());

                // 计算新的点赞状态和数量
                boolean newLikeStatus = !comment.isLike();
                int newLikeCount = newLikeStatus ? comment.getLikeCount() + 1 : comment.getLikeCount() - 1;

                Log.d("CommentsAdapter", "新状态: " + newLikeStatus + ", 新点赞数: " + newLikeCount);

                // 立即更新comment对象
                comment.setLike(newLikeStatus);
                comment.setLikeCount(newLikeCount);

                // 立即更新UI（即时反馈）
                tvLikeCount.setText(String.valueOf(newLikeCount));
                if (newLikeStatus) {
                    ivLike.setImageResource(R.drawable.ic_like_selected);
                    ivLike.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red));
                } else {
                    ivLike.setImageResource(R.drawable.ic_like_unselected);
                    ivLike.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.gray));
                }

                // 通知ViewModel保存数据
                if (listener != null) {
                    listener.onLikeClick(topicId, comment.getId(), newLikeStatus, newLikeCount);
                    Log.d("CommentsAdapter", "已通知ViewModel更新点赞状态");
                } else {
                    Log.e("CommentsAdapter", "listener为null，无法通知点赞事件");
                }
            });

            // 整个item点击事件（用于回复）
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClick(comment.getId(), comment.getUsername());
                }
            });
        }
    }
}