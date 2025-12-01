package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.databinding.FragmentTopicCommentsBinding;
import java.util.ArrayList;
import java.util.List;

public class TopicCommentsFragment extends Fragment implements CommentsAdapter.OnCommentActionListener {

    private FragmentTopicCommentsBinding binding;
    private DetailViewModel viewModel;
    private CommentsAdapter commentsAdapter;
    private String topicId;
    private String token;
    private final int CURRENT_USER_ID = 1; // 从登录信息中获取

    public interface OnCommentInputListener {
        void onShowCommentInput(String hint, int commentId);
    }

    private OnCommentInputListener commentInputListener;

    public static TopicCommentsFragment newInstance(int topicId, String token) {
        TopicCommentsFragment fragment = new TopicCommentsFragment();
        Bundle args = new Bundle();
        args.putString("TOPIC_ID", String.valueOf(topicId));
        args.putString("TOKEN", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCommentInputListener) {
            commentInputListener = (OnCommentInputListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCommentInputListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        commentInputListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTopicCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            topicId = getArguments().getString("TOPIC_ID");
            token = getArguments().getString("TOKEN");
        }

        viewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);

        initView();
        initObserver();
        loadComments();
    }

    private void initView() {
        commentsAdapter = new CommentsAdapter(new ArrayList<>(), requireContext(), CURRENT_USER_ID);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.commentsRecyclerView.setAdapter(commentsAdapter);
        commentsAdapter.setOnCommentActionListener(this);
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadComments);
    }

    private void initObserver() {
        viewModel.getComments().observe(getViewLifecycleOwner(), apiResponse -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            if (apiResponse != null && apiResponse.getCode() == 200) {
                List<Comment> parentComments = apiResponse.getData();
                binding.placeholderText.setVisibility(parentComments == null || parentComments.isEmpty() ? View.VISIBLE : View.GONE);
                commentsAdapter.updateComments(parentComments != null ? parentComments : new ArrayList<>());
            } else {
                Toast.makeText(getContext(), "加载评论失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getToggleLikeResult().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                loadComments(); // 重新加载以反映变化
            }
        });

        viewModel.getDeleteCommentResult().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                loadComments();
            }
        });
    }

    @Override
    public void onLikeClick(int commentId, boolean isSubComment) {
        viewModel.toggleCommentLike(token, commentId);
    }

    @Override
    public void onReplyClick(int commentId, String username) {
        if (commentInputListener != null) commentInputListener.onShowCommentInput("回复 " + username, commentId);
    }

    @Override
    public void onDeleteClick(final int commentId, final boolean isSubComment) {
        new AlertDialog.Builder(getContext())
                .setTitle("确认删除")
                .setMessage("您确定要删除这条评论吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    viewModel.deleteComment(token, topicId, commentId);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public void loadComments() {
        if (getContext() == null || topicId == null) return;
        binding.swipeRefreshLayout.setRefreshing(true);
        viewModel.fetchComments(token, topicId, 1, 10);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}