package com.example.insightnewsandroid.ui.search;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.databinding.ActivitySearchBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private AuthRepository authRepository;
    private String token;

    private List<String> fullHistoryList = new ArrayList<>();
    private boolean isHistoryExpanded = false;
    private static final int COLLAPSED_HISTORY_SIZE = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        authRepository = new AuthRepository(this);
        token = authRepository.getAuthToken();

        initViews();
        setupObservers();

        binding.historyContainer.setVisibility(View.VISIBLE);
        binding.hotSearchContainer.setVisibility(View.VISIBLE);

        if (token != null && !token.isEmpty()) {
            viewModel.loadSearchHistory(token);
            // [已修正] 传递token参数
            viewModel.fetchHotSearchTerms(token);
        } else {
            // 如果未登录，显示提示
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.tvSearch.setOnClickListener(v -> performSearch());
        binding.ivDeleteHistory.setOnClickListener(v -> showClearHistoryDialog());
        
        binding.ivExpandHistory.setOnClickListener(v -> {
            isHistoryExpanded = !isHistoryExpanded;
            updateHistoryChips();
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void setupObservers() {
        viewModel.getSearchHistory().observe(this, apiResponse -> {
            if (token == null || token.isEmpty()) {
                binding.historyContainer.setVisibility(View.GONE);
                return;
            }

            if (apiResponse != null && apiResponse.getCode() == 200) {
                fullHistoryList = apiResponse.getData();
                updateHistoryChips(); 
            } else {
                Toast.makeText(this, "获取历史记录失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getHotSearchTerms().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                List<String> hotTerms = apiResponse.getData();
                if (hotTerms != null && !hotTerms.isEmpty()) {
                    binding.rvHotSearch.setVisibility(View.VISIBLE);
                    binding.rvHotSearch.setLayoutManager(new LinearLayoutManager(this));
                    HotSearchAdapter adapter = new HotSearchAdapter(this, hotTerms, term -> {
                        binding.etSearch.setText(term);
                        performSearch();
                    });
                    binding.rvHotSearch.setAdapter(adapter);
                } else {
                    binding.rvHotSearch.setVisibility(View.GONE);
                }
            } else {
                binding.rvHotSearch.setVisibility(View.GONE);
                Toast.makeText(this, "获取热搜词失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getClearHistoryResult().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                Toast.makeText(this, "历史记录已清空", Toast.LENGTH_SHORT).show();
                fullHistoryList.clear();
                updateHistoryChips();
            }
        });
    }

    private void updateHistoryChips() {
        boolean hasHistory = fullHistoryList != null && !fullHistoryList.isEmpty();
        binding.ivDeleteHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);

        if (!hasHistory) {
            binding.chipGroupHistory.setVisibility(View.GONE);
            binding.ivExpandHistory.setVisibility(View.GONE);
            return;
        }

        binding.chipGroupHistory.setVisibility(View.VISIBLE);
        binding.chipGroupHistory.removeAllViews();

        List<String> listToShow = fullHistoryList;
        if (fullHistoryList.size() > COLLAPSED_HISTORY_SIZE && !isHistoryExpanded) {
            listToShow = fullHistoryList.subList(0, COLLAPSED_HISTORY_SIZE);
            binding.ivExpandHistory.setVisibility(View.VISIBLE);
            binding.ivExpandHistory.setImageResource(R.drawable.ic_arrow_down);
        } else if (fullHistoryList.size() > COLLAPSED_HISTORY_SIZE && isHistoryExpanded) {
            binding.ivExpandHistory.setVisibility(View.VISIBLE);
            binding.ivExpandHistory.setImageResource(R.drawable.ic_arrow_up);
        } else {
            binding.ivExpandHistory.setVisibility(View.GONE);
        }

        for (String term : listToShow) {
            Chip chip = new Chip(this);
            chip.setText(term);
            chip.setOnClickListener(v -> {
                binding.etSearch.setText(term);
                performSearch();
            });
            binding.chipGroupHistory.addView(chip);
        }
    }

    private void performSearch() {
        String searchTerm = binding.etSearch.getText().toString().trim();
        if (!searchTerm.isEmpty()) {
            Toast.makeText(this, "添加历史记录功能，等待后端接口", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SearchResultsActivity.class);
            intent.putExtra(SearchResultsActivity.EXTRA_SEARCH_KEYWORD, searchTerm);
            startActivity(intent);
        }
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空历史记录")
                .setMessage("您确定要清空所有搜索历史吗？此操作无法撤销。")
                .setPositiveButton("清空", (dialog, which) -> {
                    if (token != null && !token.isEmpty()) {
                        viewModel.clearSearchHistory(token);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
