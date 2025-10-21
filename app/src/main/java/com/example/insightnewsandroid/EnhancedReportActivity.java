// EnhancedReportActivity.java
package com.example.insightnewsandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.insightnewsandroid.db.SuspiciousSpan;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class EnhancedReportActivity extends AppCompatActivity {

    private TextView newsContent;
    private List<SuspiciousSpan> spans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_report);

        // 设置标题
        TextView titleView = findViewById(R.id.newsTitle);
        String title = getIntent().getStringExtra("title");
        if (title != null) {
            titleView.setText(title);
        }

        // 获取新闻全文和可疑片段
        String fullText = getIntent().getStringExtra("fullText");
        String spansJson = getIntent().getStringExtra("suspiciousSpans");

        newsContent = findViewById(R.id.newsContent);
        if (fullText != null) {
            newsContent.setText(fullText);
        }

        // 解析可疑片段
        if (spansJson != null && !spansJson.isEmpty()) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<SuspiciousSpan>>() {}.getType();
            spans = gson.fromJson(spansJson, listType);
            if (spans != null) {
                applyHighlights(fullText);
            }
        }

        // 悬浮按钮：AI 助手
        findViewById(R.id.fabAiHelper).setOnClickListener(v -> {
            // 跳转到检测界面（CredibilityFragment 需包装在 Activity 中）
            // 临时跳转到 MainActivity 并导航到 CredibilityFragment
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // 底部按钮
        findViewById(R.id.btnExport).setOnClickListener(v -> showExportDialog());
        findViewById(R.id.btnBookmark).setOnClickListener(v -> toggleBookmark());
        findViewById(R.id.btnRate).setOnClickListener(v -> Toast.makeText(this, "评价功能开发中", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnTranslate).setOnClickListener(v -> Toast.makeText(this, "翻译功能开发中", Toast.LENGTH_SHORT).show());
    }

    private void applyHighlights(String fullText) {
        SpannableString spannable = new SpannableString(fullText);
        for (SuspiciousSpan span : spans) {
            if (span.start >= 0 && span.end <= fullText.length() && span.start < span.end) {
                // 添加下划线
                spannable.setSpan(new UnderlineSpan(), span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // 添加点击事件
                spannable.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        showAnalysisPopup(span);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(ContextCompat.getColor(EnhancedReportActivity.this, android.R.color.holo_red_dark));
                        ds.setUnderlineText(true);
                    }
                }, span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        newsContent.setText(spannable);
        newsContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showAnalysisPopup(SuspiciousSpan span) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("片段分析")
                .setMessage("可信度: " + span.credibilityScore + "%\n\n" +
                        "分析: " + span.analysis + "\n\n" +
                        "依据: " + span.evidence)
                .setPositiveButton("关闭", null)
                .show();
    }

    private void showExportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("导出报告")
                .setItems(new CharSequence[]{"保存为图片", "保存为 PDF"}, (d, which) -> {
                    if (which == 0) {
                        exportAsImage();
                    } else {
                        exportAsPdf();
                    }
                })
                .show();
    }

    private void exportAsImage() {
        Toast.makeText(this, "图片导出功能开发中", Toast.LENGTH_SHORT).show();
        // TODO: 实现截图逻辑（需处理 ScrollView 内容完整捕获）
    }

    private void exportAsPdf() {
        Toast.makeText(this, "PDF导出功能开发中", Toast.LENGTH_SHORT).show();
        // TODO: 实现 PDF 生成逻辑
    }

    private void toggleBookmark() {
        Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
        // TODO: 实现收藏逻辑（更新数据库字段）
    }
}