package com.example.insightnewsandroid.ui.detail;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
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
    private int keyboardHeight = 0;

    public static TopicCommentsFragment newInstance(int topicId, String token) {
        TopicCommentsFragment fragment = new TopicCommentsFragment();
        Bundle args = new Bundle();
        args.putInt("TOPIC_ID", topicId);
        args.putString("TOKEN", token);
        fragment.setArguments(args);
        return fragment;
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
                binding.etComment.setHint("回复 " + username);
                binding.etComment.requestFocus();
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String commentContent = binding.etComment.getText().toString().trim();
            if (!commentContent.isEmpty()) {
                viewModel.addComment(topicId, commentContent);
            } else {
                Toast.makeText(getContext(), "评论内容不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadComments();
        });

        binding.etComment.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnSend.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        Log.d("TopicCommentsFragment", "initView完成, topicId=" + topicId);
    }

    /**
     * 动态检测键盘高度 - 每个人的键盘高度可能不同
     */
    private void setupKeyboardListener() {
        if (getActivity() != null) {
            final View rootView = getActivity().getWindow().getDecorView();

            keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                private final Rect rect = new Rect();
                private boolean lastKeyboardShowing = false;
                private int lastKeyboardHeight = 0;

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
                    if (keyboardNowShowing != lastKeyboardShowing ||
                            (keyboardNowShowing && heightDifference != lastKeyboardHeight)) {

                        lastKeyboardShowing = keyboardNowShowing;
                        lastKeyboardHeight = heightDifference;

                        if (keyboardNowShowing) {
                            keyboardHeight = heightDifference;
                            onKeyboardShown(keyboardHeight);
                            Log.d("TopicCommentsFragment", "键盘弹出，动态高度: " + keyboardHeight + "px, 屏幕高度: " + screenHeight + "px");
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
     * 键盘显示时的处理 - 动态适配不同键盘高度
     */
    private void onKeyboardShown(int keyboardHeight) {
        Log.d("TopicCommentsFragment", "检测到键盘高度: " + keyboardHeight + "px");

        // 计算输入区域需要上移的距离（键盘高度 + 状态栏高度补偿）
        int statusBarHeight = getStatusBarHeight();
        int translateY = -keyboardHeight + statusBarHeight;

        Log.d("TopicCommentsFragment", "状态栏高度: " + statusBarHeight + "px, 实际上移距离: " + translateY + "px");

        // 使用平移动画将输入区域上移到键盘上方
        binding.llCommentInput.animate()
                .translationY(translateY)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 调整 RecyclerView 的底部边距，确保内容不被键盘遮挡
        // 底部边距 = 键盘高度 + 输入区域高度 + 额外间距
        int inputAreaHeight = binding.llCommentInput.getHeight();
        int bottomMargin = keyboardHeight + inputAreaHeight + dpToPx(8);

        ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) binding.recyclerViewComments.getLayoutParams();
        recyclerParams.bottomMargin = bottomMargin;
        binding.recyclerViewComments.setLayoutParams(recyclerParams);
        binding.recyclerViewComments.requestLayout();

        Log.d("TopicCommentsFragment", "输入区域高度: " + inputAreaHeight + "px, RecyclerView底部边距: " + bottomMargin + "px");

        // 延迟滚动到底部，确保布局更新完成
        new Handler().postDelayed(() -> {
            scrollToBottomSmooth();
        }, 200);
    }

    /**
     * 键盘隐藏时的处理
     */
    private void onKeyboardHidden() {
        Log.d("TopicCommentsFragment", "键盘隐藏，恢复输入区域位置");

        // 使用平移动画将输入区域移回原位
        binding.llCommentInput.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 恢复 RecyclerView 的底部边距
        ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) binding.recyclerViewComments.getLayoutParams();
        recyclerParams.bottomMargin = dpToPx(8);
        binding.recyclerViewComments.setLayoutParams(recyclerParams);
        binding.recyclerViewComments.requestLayout();

        // 清除焦点
        binding.etComment.clearFocus();
        binding.etComment.setHint("期待您的评论...");
    }

    /**
     * 获取状态栏高度
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            Log.e("TopicCommentsFragment", "获取状态栏高度失败: " + e.getMessage());
            // 默认状态栏高度
            statusBarHeight = dpToPx(24);
        }
        return statusBarHeight;
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
     * dp转px - 确保此方法只定义一次
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
                String newComment = binding.etComment.getText().toString().trim();
                if (!newComment.isEmpty()) {
                    addNewCommentToUI(newComment);
                    binding.etComment.setText("");
                    binding.etComment.setHint("期待您的评论");
                    binding.btnSend.setVisibility(View.GONE);
                }
                loadComments();
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

    private void updateCommentsTime() {
        if (viewModel != null && commentsAdapter != null) {
            viewModel.updateCommentsRelativeTime(topicId);
            List<Comment> updatedComments = viewModel.getCommentsForTopic(topicId);
            commentsAdapter.updateComments(updatedComments);
        }
    }

    private void addNewCommentToUI(String commentContent) {
        Comment newComment = new Comment();
        newComment.setId((int) System.currentTimeMillis());
        newComment.setUsername(getCurrentUsername());
        newComment.setUserImg(getCurrentUserAvatar());
        newComment.setComment(commentContent);
        newComment.setTimestamp(System.currentTimeMillis());
        newComment.setCreatedAt("刚刚");
        newComment.setLikeCount(0);
        newComment.setLike(false);
        newComment.setMyComment(true);
        newComment.setChildren(new ArrayList<>());

        List<Comment> currentComments = commentsAdapter.getComments();
        if (currentComments == null) {
            currentComments = new ArrayList<>();
        }

        currentComments.add(0, newComment);
        commentsAdapter.updateComments(currentComments);

        binding.tvPlaceholder.setVisibility(View.GONE);
        binding.recyclerViewComments.setVisibility(View.VISIBLE);

        scrollToBottomSmooth();

        Toast.makeText(getContext(), "评论已发布", Toast.LENGTH_SHORT).show();
    }

    private void loadComments() {
        if (getContext() == null) return;

        List<Comment> comments = viewModel.getCommentsForTopic(topicId);
        Log.d("TopicCommentsFragment", "加载评论，数量: " + comments.size());

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

    private String getCurrentUsername() {
        if (getContext() != null) {
            UserProfile profile = UserProfileManager.INSTANCE.getCurrentProfile(getContext());
            String username = profile.getUsername();
            return username;
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

    public void refreshComments() {
        loadComments();
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