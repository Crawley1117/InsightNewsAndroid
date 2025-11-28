package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.content.DialogInterface;
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

public class TopicCommentsFragment extends Fragment {

    private FragmentTopicCommentsBinding binding;
    private DetailViewModel viewModel;
    private CommentsAdapter commentsAdapter;
    private String topicId;
    private String token;
    private int currentlyLoadingParentId = -1;

    public interface OnCommentActionListener {
        void onShowCommentInput(String hint, int commentId);
    }

    private OnCommentActionListener commentActionListener;

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
        if (context instanceof OnCommentActionListener) {
            commentActionListener = (OnCommentActionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCommentActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        commentActionListener = null;
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
        commentsAdapter = new CommentsAdapter(new ArrayList<>(), requireContext());
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(commentsAdapter);

        commentsAdapter.setOnCommentActionListener(new CommentsAdapter.OnCommentActionListener() {
            @Override
            public void onLikeClick(int commentId) {
                if (token != null && !token.isEmpty()) {
                    viewModel.toggleCommentLike(token, commentId);
                } else {
                    Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReplyClick(int commentId, String username) {
                if (commentActionListener != null) commentActionListener.onShowCommentInput("回复 " + username, commentId);
            }

            @Override
            public void onLoadRepliesClick(int parentCommentId) {
                currentlyLoadingParentId = parentCommentId;
                viewModel.fetchCommentReplies(token, parentCommentId);
            }

            @Override
            public void onDeleteClick(int commentId) {
                showDeleteConfirmationDialog(commentId);
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadComments);
    }

    private void initObserver() {
        viewModel.getComments().observe(getViewLifecycleOwner(), apiResponse -> {
            binding.swipeRefresh.setRefreshing(false);
            if (apiResponse != null && apiResponse.getCode() == 200) {
                List<Comment> parentComments = apiResponse.getData();
                binding.tvPlaceholder.setVisibility(parentComments == null || parentComments.isEmpty() ? View.VISIBLE : View.GONE);
                commentsAdapter.updateComments(parentComments != null ? parentComments : new ArrayList<>());
            } else {
                Toast.makeText(getContext(), "加载评论失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCommentReplies().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200 && currentlyLoadingParentId != -1) {
                List<Comment> replies = apiResponse.getData();
                List<Comment> parentComments = commentsAdapter.getComments();
                for (int i = 0; i < parentComments.size(); i++) {
                    if (parentComments.get(i).getId() == currentlyLoadingParentId) {
                        parentComments.get(i).setChildren(replies);
                        commentsAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            } else {
                Toast.makeText(getContext(), "加载回复失败", Toast.LENGTH_SHORT).show();
            }
            currentlyLoadingParentId = -1; 
        });

        viewModel.getToggleLikeResult().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                loadComments();
            }
        });

        // [已新增] 观察删除评论的结果
        viewModel.getDeleteCommentResult().observe(getViewLifecycleOwner(), apiResponse -> {
            if(apiResponse != null && apiResponse.getCode() == 200){
                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                // 列表刷新已在ViewModel中处理
            }
        });
    }

    private void showDeleteConfirmationDialog(int commentId) {
        new AlertDialog.Builder(getContext())
                .setTitle("删除评论")
                .setMessage("您确定要删除这条评论吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    if (token != null && !token.isEmpty()) {
                        viewModel.deleteComment(token, topicId, commentId);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public void loadComments() {
        if (getContext() == null || topicId == null) return;
        binding.swipeRefresh.setRefreshing(true);
        viewModel.fetchComments(token, topicId, 1, 10); 
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
