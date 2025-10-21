// DetectionRecordEntity.java
package com.example.insightnewsandroid.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore; // 👈 新增

@Entity(tableName = "detection_records")
public class DetectionRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public long detectionDate;
    public String credibilityLevel;
    public String fullReport;

    // 无参构造函数（Room 必须）
    public DetectionRecordEntity() {}

    // 有参构造函数 —— 加上 @Ignore
    @Ignore
    public DetectionRecordEntity(String title, long detectionDate, String credibilityLevel, String fullReport) {
        this.title = title;
        this.detectionDate = detectionDate;
        this.credibilityLevel = credibilityLevel;
        this.fullReport = fullReport;
    }
}