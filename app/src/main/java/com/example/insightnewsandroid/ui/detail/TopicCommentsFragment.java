package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.databinding.FragmentTopicCommentsBinding;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class TopicCommentsFragment extends Fragment {

    private FragmentTopicCommentsBinding binding;
    private DetailTopicViewModel viewModel;
    private CommentsAdapter commentsAdapter;

    private int topicId;
    private String token;

    private Handler timeUpdateHandler = new Handler();
    private Runnable timeUpdateRunnable;
    private static final long TIME_UPDATE_INTERVAL = 60000;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private boolean isKeyboardShowing = false;

    // 定义回调接口 - 修改：添加 commentId 参数
    public interface OnCommentActionListener {
        void onShowCommentInput(String hint, int commentId);
    }

    private OnCommentActionListener commentActionListener;

    public static TopicCommentsFragment newInstance(int topicId, String token) {
        TopicCommentsFragment fragment = new TopicCommentsFragment();
        Bundle args = new Bundle();
        args.putInt("TOPIC_ID", topicId);
        args.putString("TOKEN", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 检查Activity是否实现了回调接口
        if (context instanceof OnCommentActionListener) {
            commentActionListener = (OnCommentActionListener) context;
        } else {
            Log.w("TopicCommentsFragment", "Activity没有实现OnCommentActionListener接口");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        commentActionListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTopicCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            topicId = getArguments().getInt("TOPIC_ID");
            token = getArguments().getString("TOKEN");
        }

        initView();
        initObserver();
        loadComments();
        startTimeUpdater();

        setupKeyboardListener();
    }

    private void initView() {
        commentsAdapter = new CommentsAdapter(new ArrayList<>(), token, requireContext(), topicId);
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(commentsAdapter);

        commentsAdapter.setOnCommentActionListener(new CommentsAdapter.OnCommentActionListener() {
            @Override
            public void onLikeClick(int topicId, int commentId, boolean newLikeStatus, int newLikeCount) {
                Log.d("TopicCommentsFragment", "onLikeClick: topicId=" + topicId + ", commentId=" + commentId + ", newLikeStatus=" + newLikeStatus);
                viewModel.toggleCommentLike(topicId, commentId, newLikeStatus, newLikeCount);
            }

            @Override
            public void onReplyClick(int commentId, String username) {
                // 使用接口回调通知Activity，并传递 commentId
                if (commentActionListener != null) {
                    commentActionListener.onShowCommentInput("回复 " + username, commentId);
                } else {
                    // 备用方案：显示提示信息
                    showCommentInputFallback("回复 " + username);
                }
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadComments();
        });

        Log.d("TopicCommentsFragment", "initView完成, topicId=" + topicId);
    }

    /**
     * 备用方案：显示提示信息
     */
    private void showCommentInputFallback(String hint) {
        // 由于现在使用对话框方式，备用方案只需要显示提示
        Toast.makeText(getContext(), hint, Toast.LENGTH_SHORT).show();
        Log.d("TopicCommentsFragment", "备用方案：显示提示: " + hint);
    }

    /**
     * 动态检测键盘高度
     */
    private void setupKeyboardListener() {
        if (getActivity() != null) {
            final View rootView = getActivity().getWindow().getDecorView();

            keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                private final Rect rect = new Rect();
                private boolean lastKeyboardShowing = false;

                @Override
                public void onGlobalLayout() {
                    // 获取窗口可见区域
                    rootView.getWindowVisibleDisplayFrame(rect);

                    int screenHeight = rootView.getHeight();
                    int visibleHeight = rect.bottom - rect.top;
                    int heightDifference = screenHeight - visibleHeight;

                    // 键盘显示的条件：高度差大于屏幕高度的15%
                    boolean keyboardNowShowing = heightDifference > screenHeight * 0.15;

                    // 只有状态变化时才处理
                    if (keyboardNowShowing != lastKeyboardShowing) {
                        lastKeyboardShowing = keyboardNowShowing;

                        if (keyboardNowShowing) {
                            onKeyboardShown(heightDifference);
                            Log.d("TopicCommentsFragment", "键盘弹出，高度: " + heightDifference + "px");
                        } else {
                            onKeyboardHidden();
                            Log.d("TopicCommentsFragment", "键盘收起");
                        }
                    }
                }
            };

            rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }

    /**
     * 键盘显示时的处理 - 调整评论列表底部边距
     */
    private void onKeyboardShown(int keyboardHeight) {
        Log.d("TopicCommentsFragment", "键盘弹出，调整评论列表底部边距: " + keyboardHeight + "px");

        // 计算输入区域的高度（大约100dp）
        int inputAreaHeight = dpToPx(100);

        // 设置评论列表的底部边距 = 键盘高度 + 输入区域高度
        ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) binding.recyclerViewComments.getLayoutParams();
        recyclerParams.bottomMargin = keyboardHeight + inputAreaHeight;
        binding.recyclerViewComments.setLayoutParams(recyclerParams);
        binding.recyclerViewComments.requestLayout();

        Log.d("TopicCommentsFragment", "设置底部边距: " + recyclerParams.bottomMargin + "px");

        // 延迟滚动到底部，确保能看到最新评论
        new Handler().postDelayed(() -> {
            scrollToBottomSmooth();
        }, 200);
    }

    /**
     * 键盘隐藏时的处理 - 恢复评论列表布局
     */
    private void onKeyboardHidden() {
        Log.d("TopicCommentsFragment", "键盘隐藏，恢复评论列表布局");

        // 恢复评论列表的底部边距
        ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) binding.recyclerViewComments.getLayoutParams();
        recyclerParams.bottomMargin = dpToPx(8);
        binding.recyclerViewComments.setLayoutParams(recyclerParams);
        binding.recyclerViewComments.requestLayout();
    }

    /**
     * 当Activity的输入框获得焦点时调用
     */
    public void onCommentInputFocused() {
        // 确保评论列表可见并滚动到底部
        binding.recyclerViewComments.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            scrollToBottomSmooth();
        }, 300);
    }

    /**
     * 平滑滚动到底部
     */
    private void scrollToBottomSmooth() {
        if (commentsAdapter != null && commentsAdapter.getItemCount() > 0) {
            int lastPosition = commentsAdapter.getItemCount() - 1;
            binding.recyclerViewComments.smoothScrollToPosition(lastPosition);
            Log.d("TopicCommentsFragment", "平滑滚动到底部，位置: " + lastPosition);
        }
    }

    /**
     * 立即滚动到底部
     */
    private void scrollToBottom() {
        if (commentsAdapter != null && commentsAdapter.getItemCount() > 0) {
            int lastPosition = commentsAdapter.getItemCount() - 1;
            binding.recyclerViewComments.scrollToPosition(lastPosition);
            Log.d("TopicCommentsFragment", "立即滚动到底部，位置: " + lastPosition);
        }
    }

    /**
     * dp转px
     */
    private int dpToPx(int dp) {
        if (getContext() == null) return dp;
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void initObserver() {
        viewModel = new ViewModelProvider(requireActivity()).get(DetailTopicViewModel.class);

        if (getContext() != null) {
            viewModel.setAppContext(getContext());
        }

        viewModel.getCommentSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                loadComments();
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        viewModel.getLikeSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Log.d("TopicCommentsFragment", "点赞成功，刷新评论列表");
                loadComments();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void startTimeUpdater() {
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateCommentsTime();
                timeUpdateHandler.postDelayed(this, TIME_UPDATE_INTERVAL);
            }
        };
        timeUpdateHandler.postDelayed(timeUpdateRunnable, TIME_UPDATE_INTERVAL);
    }

    /**
     * 更新评论时间显示
     */
    private void updateCommentsTime() {
        if (viewModel != null && commentsAdapter != null) {
            Log.d("TopicCommentsFragment", "开始更新评论时间显示");
            viewModel.updateCommentsRelativeTime(topicId);
            List<Comment> updatedComments = viewModel.getCommentsForTopic(topicId);

            if (updatedComments != null && !updatedComments.isEmpty()) {
                commentsAdapter.updateComments(updatedComments);
                Log.d("TopicCommentsFragment", "评论时间显示已更新，数量: " + updatedComments.size());
            }
        }
    }

    private void loadComments() {
        if (getContext() == null) return;

        List<Comment> comments = viewModel.getCommentsForTopic(topicId);
        Log.d("TopicCommentsFragment", "加载评论，数量: " + comments.size());

        comments = updateCommentsUserInfo(comments);
        commentsAdapter.updateComments(comments);

        if (comments.isEmpty()) {
            binding.tvPlaceholder.setVisibility(View.VISIBLE);
            binding.recyclerViewComments.setVisibility(View.GONE);
        } else {
            binding.tvPlaceholder.setVisibility(View.GONE);
            binding.recyclerViewComments.setVisibility(View.VISIBLE);
        }

        binding.swipeRefresh.setRefreshing(false);
    }

    /**
     * 更新评论中的用户信息
     */
    private List<Comment> updateCommentsUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return comments;
        }

        int currentUserId = getCurrentUserId();
        String currentUsername = getCurrentUsername();
        String currentUserAvatar = getCurrentUserAvatar();

        for (Comment comment : comments) {
            if (comment.getUserId() == currentUserId) {
                comment.setUsername(currentUsername);
                comment.setUserImg(currentUserAvatar);
            }

            if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
                updateCommentsUserInfo(comment.getChildren());
            }
        }

        return comments;
    }

    /**
     * 获取当前用户ID
     */
    private int getCurrentUserId() {
        if (getContext() != null) {
            UserProfile profile = UserProfileManager.INSTANCE.getCurrentProfile(getContext());
            return profile.getUserId();
        }
        return 1;
    }

    private String getCurrentUsername() {
        if (getContext() != null) {
            UserProfile profile = UserProfileManager.INSTANCE.getCurrentProfile(getContext());
            String username = profile.getUsername();
            return username != null ? username : "新用户";
        }
        return "新用户";
    }

    private String getCurrentUserAvatar() {
        if (getContext() != null) {
            UserProfile profile = UserProfileManager.INSTANCE.getCurrentProfile(getContext());
            String avatar = profile.getAvatarUri();
            return avatar != null ? avatar : "";
        }
        return "";
    }

    /**
     * 刷新评论列表
     */
    public void refreshComments() {
        loadComments();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadComments();
        Log.d("TopicCommentsFragment", "Fragment恢复，重新加载评论");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (timeUpdateHandler != null && timeUpdateRunnable != null) {
            timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
        }

        if (getActivity() != null && keyboardLayoutListener != null) {
            View rootView = getActivity().getWindow().getDecorView();
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }

        binding = null;
    }
}
