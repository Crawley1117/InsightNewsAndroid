// DetectionRecordEntity.java
package com.example.insightnewsandroid.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List; // ✅ 关键：导入 List

@Entity(tableName = "detection_records")
public class DetectionRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public long detectionDate;
    public String fullText;              // 新闻全文
    public String fullReport;            // AI 分析报告
    public String credibilityLevel;
    public String suspiciousSpansJson;   // 可疑片段 JSON

    public DetectionRecordEntity() {}

    @Ignore
    public DetectionRecordEntity(String title, long detectionDate, String fullText,
                                 String fullReport, String credibilityLevel,
                                 List<SuspiciousSpan> spans) {
        this.title = title;
        this.detectionDate = detectionDate;
        this.fullText = fullText;
        this.fullReport = fullReport;
        this.credibilityLevel = credibilityLevel;
        this.suspiciousSpansJson = new Gson().toJson(spans);
    }
}