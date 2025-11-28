package com.example.insightnewsandroid.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.databinding.ActivitySearchResultsBinding;
import com.example.insightnewsandroid.ui.explore.ExploreTopicAdapter;

public class SearchResultsActivity extends AppCompatActivity {

    public static final String EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD";

    private ActivitySearchResultsBinding binding;
    private SearchViewModel viewModel;
    private ExploreTopicAdapter adapter;
    private AuthRepository authRepository;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        authRepository = new AuthRepository(this);
        token = authRepository.getAuthToken();

        String keyword = getIntent().getStringExtra(EXTRA_SEARCH_KEYWORD);
        if (keyword == null || keyword.isEmpty()) {
            Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.etSearch.setText(keyword);
        initRecyclerView();
        initObservers();
        setupSearchListener();

        viewModel.performSearch(token, keyword);
    }

    private void initRecyclerView() {
        adapter = new ExploreTopicAdapter();
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(adapter);
        // Add item click listener if needed
    }

    private void initObservers() {
        viewModel.getSearchResults().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                if (apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                    adapter.submitList(apiResponse.getData());
                    binding.tvNoResults.setVisibility(View.GONE);
                    binding.rvSearchResults.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoResults.setVisibility(View.VISIBLE);
                    binding.rvSearchResults.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "搜索失败", Toast.LENGTH_SHORT).show();
                binding.tvNoResults.setVisibility(View.VISIBLE);
                binding.rvSearchResults.setVisibility(View.GONE);
            }
        });
    }

    private void setupSearchListener() {
        binding.ivBack.setOnClickListener(v -> finish());

        binding.tvSearch.setOnClickListener(v -> performNewSearch());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performNewSearch();
                return true;
            }
            return false;
        });
    }

    // [已修复] 不再跳转，而是在当前页面刷新数据
    private void performNewSearch() {
        String newKeyword = binding.etSearch.getText().toString().trim();
        if (!newKeyword.isEmpty()) {
            viewModel.performSearch(token, newKeyword);
        }
    }
}
