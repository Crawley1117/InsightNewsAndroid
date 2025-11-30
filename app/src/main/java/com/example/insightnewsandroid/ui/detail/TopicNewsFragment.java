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
import com.example.insightnewsandroid.EnhancedReportActivity;

import java.util.ArrayList;

public class TopicNewsFragment extends Fragment {

    private FragmentTopicNewsBinding binding;
    private DetailTopicViewModel viewModel;
    private NewsAdapter newsAdapter;

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
        initView();
        initObserver();
    }

    private void initView() {
        newsAdapter = new NewsAdapter(new ArrayList<>());
        binding.recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNews.setAdapter(newsAdapter);

        newsAdapter.setOnNewsClickListener(newsItem -> {
            Log.d("TopicNewsFragment", "新闻被点击: " + newsItem.getTitle());

            Intent intent = new Intent(getActivity(), EnhancedReportActivity.class);
            intent.putExtra("title", newsItem.getTitle());

            if (newsItem.getContent() != null && !newsItem.getContent().isEmpty()) {
                intent.putExtra("fullText", newsItem.getContent());
            } else {
                String placeholderContent = newsItem.getTitle() + "\n\n" +
                        "这是关于 \"" + newsItem.getTitle() + "\" 的详细新闻报道内容。\n\n" +
                        "在这里可以看到新闻的完整分析报告，包括可信度评估、关键信息提取和相关背景分析。";
                intent.putExtra("fullText", placeholderContent);
            }

            if (newsItem.getImageUrl() != null && !newsItem.getImageUrl().isEmpty()) {
                intent.putExtra("imageUrl", newsItem.getImageUrl());
            }

            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            // The refresh is triggered by the parent Activity's ViewModel
            // So we just show the indicator, the update will come from the observer
            if (viewModel != null) {
                 int topicId = getArguments() != null ? getArguments().getInt("TOPIC_ID") : -1;
                 String token = getArguments() != null ? getArguments().getString("TOKEN") : "";
                 if(topicId != -1) {
                     viewModel.fetchTopicDetails(token, topicId);
                 }
            }
        });
    }

    private void initObserver() {
        viewModel = new ViewModelProvider(requireActivity()).get(DetailTopicViewModel.class);

        // [已修复] Fragment只负责观察和显示数据
        viewModel.getRelatedNews().observe(getViewLifecycleOwner(), newsList -> {
            binding.swipeRefresh.setRefreshing(false);
            if (newsList != null && !newsList.isEmpty()) {
                newsAdapter.updateNews(newsList);
                binding.tvPlaceholder.setVisibility(View.GONE);
                binding.recyclerViewNews.setVisibility(View.VISIBLE);
            } else {
                binding.tvPlaceholder.setVisibility(View.VISIBLE);
                binding.recyclerViewNews.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
