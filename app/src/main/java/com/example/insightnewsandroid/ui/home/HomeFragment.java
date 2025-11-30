// HomeFragment.java
package com.example.insightnewsandroid.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.insightnewsandroid.HistoryActivity;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.databinding.FragmentHomeBinding;
import com.example.insightnewsandroid.ui.credibility.CredibilityFragment;

public class HomeFragment extends Fragment {

        private FragmentHomeBinding binding;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
                binding = FragmentHomeBinding.inflate(inflater, container, false);
                return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);

                // 开始新检测 → 跳转到 CredibilityFragment
                binding.btnNewDetection.setOnClickListener(v -> {
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.nav_host_fragment, new CredibilityFragment())
                                .addToBackStack(null)
                                .commit();
                });

                // 查看历史 → 跳转到 HistoryActivity
                binding.btnViewHistory.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), HistoryActivity.class);
                        startActivity(intent);
                });
        }

        @Override
        public void onDestroyView() {
                super.onDestroyView();
                binding = null;
        }
}