package com.example.insightnewsandroid.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.databinding.FragmentTopicNewsBinding;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.EnhancedReportActivity; // 添加导入

import java.util.ArrayList;
import java.util.List;

public class TopicNewsFragment extends Fragment {

    private FragmentTopicNewsBinding binding;
    private DetailTopicViewModel viewModel;
    private NewsAdapter newsAdapter;

    private int topicId;

    public static TopicNewsFragment newInstance(int topicId, String token) {
        TopicNewsFragment fragment = new TopicNewsFragment();
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
        binding = FragmentTopicNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            topicId = getArguments().getInt("TOPIC_ID");
        }

        initView();
        initObserver();
        loadNews();
    }

    private void initView() {
        newsAdapter = new NewsAdapter(new ArrayList<>());
        binding.recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNews.setAdapter(newsAdapter);

        // 设置新闻点击监听 - 跳转到详细报告分析页
        newsAdapter.setOnNewsClickListener(newsItem -> {
            Log.d("TopicNewsFragment", "新闻被点击: " + newsItem.getTitle());

            // 跳转到详细报告分析页
            Intent intent = new Intent(getActivity(), EnhancedReportActivity.class);

            // 传递新闻数据
            intent.putExtra("title", newsItem.getTitle());

            // 传递新闻内容
            if (newsItem.getContent() != null && !newsItem.getContent().isEmpty()) {
                intent.putExtra("fullText", newsItem.getContent());
            } else {
                // 如果没有内容，使用标题和一些占位文本
                String placeholderContent = newsItem.getTitle() + "\n\n" +
                        "这是关于 \"" + newsItem.getTitle() + "\" 的详细新闻报道内容。\n\n" +
                        "在这里可以看到新闻的完整分析报告，包括可信度评估、关键信息提取和相关背景分析。";
                intent.putExtra("fullText", placeholderContent);
            }

            // 如果有图片URL，也传递过去（可选）
            if (newsItem.getImageUrl() != null && !newsItem.getImageUrl().isEmpty()) {
                intent.putExtra("imageUrl", newsItem.getImageUrl());
            }

            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadNews);
    }

    private void initObserver() {
        viewModel = new ViewModelProvider(requireActivity()).get(DetailTopicViewModel.class);

        viewModel.getRelatedNews().observe(getViewLifecycleOwner(), newsList -> {
            if (newsList != null && !newsList.isEmpty()) {
                newsAdapter.updateNews(newsList);
                binding.tvPlaceholder.setVisibility(View.GONE);
                binding.recyclerViewNews.setVisibility(View.VISIBLE);
            } else {
                binding.tvPlaceholder.setVisibility(View.VISIBLE);
                binding.recyclerViewNews.setVisibility(View.GONE);
            }
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void loadNews() {
        if (getContext() == null) return;
        viewModel.loadRelatedNewsForTopic(topicId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
