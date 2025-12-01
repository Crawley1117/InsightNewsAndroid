// ReportDetailActivity.java
package com.example.insightnewsandroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.model.AnalysisReport;
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.databinding.ActivityReportDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailActivity extends AppCompatActivity {

    private ActivityReportDetailBinding binding; // 添加 binding 成员变量
    private AuthRepository authRepo; // 用于获取 Token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportDetailBinding.inflate(getLayoutInflater()); // 使用 binding
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        authRepo = new AuthRepository(this); // 初始化 AuthRepository

        // 从 Intent 获取数据 (现在主要是 reportId)
        String reportId = getIntent().getStringExtra("reportId"); // 假设传入的是 reportId
        String fallbackTitle = getIntent().getStringExtra("title"); // 作为备选
        String fallbackCredibility = getIntent().getStringExtra("credibility"); // 作为备选
        String fallbackReport = getIntent().getStringExtra("report"); // 作为备选

        if (reportId != null) {
            // 从网络获取报告详情
            loadReportFromNetwork(reportId);
        } else {
            // 如果没有 reportId，则使用 Intent 中的备选数据（本地数据）
            Log.w("ReportDetailActivity", "No reportId provided, falling back to local/intent data.");
            loadLocalReport(fallbackTitle, fallbackCredibility, fallbackReport);
        }
    }

    // --- 新增：从网络加载报告详情 ---
    private void loadReportFromNetwork(String reportId) {
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            loadLocalReport("未登录", "未知", "无法获取报告，请先登录。"); // 显示错误信息
            return;
        }

        Call<BaseResponse<AnalysisReport>> call = ApiManager.getAuthService().getAnalysisReport("Bearer " + token, reportId);
        call.enqueue(new Callback<BaseResponse<AnalysisReport>>() {
            @Override
            public void onResponse(Call<BaseResponse<AnalysisReport>> call, Response<BaseResponse<AnalysisReport>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<AnalysisReport> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        AnalysisReport report = baseResponse.getData();
                        if (report != null) {
                            // 设置 UI
                            binding.newsTitle.setText(report.getOriginalContent() != null ? report.getOriginalContent() : "未知内容");
                            // binding.detectionDate.setText(...); // 从 report.getCreatedAt() 解析
                            binding.credibilityLevel.setText(report.getAnalysisResult() != null ? report.getAnalysisResult() : "未知");
                            binding.fullReport.setText(report.getDetailedReport() != null ? report.getDetailedReport() : "暂无详细报告");

                            // 设置可信度标签背景
                            setCreditBackground(report.getAnalysisResult() != null ? report.getAnalysisResult() : "未知");
                        } else {
                            Log.e("ReportDetailActivity", "AnalysisReport data is null");
                            loadLocalReport("数据错误", "未知", "服务器返回数据格式错误。"); // 显示错误信息
                        }
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e("ReportDetailActivity", "获取报告失败: " + msg);
                        loadLocalReport("获取失败", "未知", "获取报告失败: " + msg); // 显示错误信息
                    }
                } else {
                    Log.e("ReportDetailActivity", "获取报告失败. HTTP code: " + response.code());
                    loadLocalReport("网络错误", "未知", "获取报告失败: " + response.code()); // 显示错误信息
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AnalysisReport>> call, Throwable t) {
                Log.e("ReportDetailActivity", "获取报告失败", t);
                loadLocalReport("网络错误", "未知", "网络错误: " + t.getMessage()); // 显示错误信息
            }
        });
    }
    // --- 新增结束 ---

    // --- 新增：设置可信度背景的方法 ---
    private void setCreditBackground(String credibility) {
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
    // --- 新增结束 ---

    // --- 新增：加载本地/Intent数据的方法 ---
    private void loadLocalReport(String title, String credibility, String report) {
        binding.newsTitle.setText(title);
        // binding.detectionDate.setText(...); // 如果有日期
        binding.credibilityLevel.setText(credibility);
        binding.fullReport.setText(report);

        // 设置可信度标签背景
        setCreditBackground(credibility);
    }
    // --- 新增结束 ---

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}