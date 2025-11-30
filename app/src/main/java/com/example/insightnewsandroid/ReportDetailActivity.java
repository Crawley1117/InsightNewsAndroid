// ReportDetailActivity.java
package com.example.insightnewsandroid;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.insightnewsandroid.databinding.ActivityReportDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReportDetailBinding binding = ActivityReportDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 从 Intent 获取数据
        String title = getIntent().getStringExtra("title");
        long date = getIntent().getLongExtra("date", 0L);
        String credibility = getIntent().getStringExtra("credibility");
        String report = getIntent().getStringExtra("report");

        // 格式化日期
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(date));

        // 设置 UI
        binding.newsTitle.setText(title);
        binding.detectionDate.setText(dateStr);
        binding.credibilityLevel.setText(credibility);
        binding.fullReport.setText(report);

        // 设置可信度标签背景
        int backgroundRes;
        switch (credibility) {
            case "高":
                backgroundRes = R.drawable.bg_credibility_high;
                break;
            case "较高":
                backgroundRes = R.drawable.bg_credibility_medium;
                break;
            case "低":
                backgroundRes = R.drawable.bg_credibility_low;
                break;
            default:
                backgroundRes = R.drawable.bg_credibility_medium;
        }
        binding.credibilityLevel.setBackgroundResource(backgroundRes);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}