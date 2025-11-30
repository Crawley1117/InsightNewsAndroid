package com.example.insightnewsandroid.data.model;

import com.example.insightnewsandroid.db.SuspiciousSpan; // 导入 db 包中的 SuspiciousSpan
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 分析报告的响应模型
 * 此模型用于接收后端 "查看分析报告" 接口返回的完整报告
 */
public class AnalysisReport {

    @SerializedName("id")
    private String id;

    @SerializedName("original_content") // 原始内容
    private String originalContent;

    @SerializedName("analysis_result") // 整体分析结果，例如 "高", "中", "低"
    private String analysisResult;

    @SerializedName("score") // 可信度分数
    private int score;

    @SerializedName("detailed_report") // 详细报告文本
    private String detailedReport;

    @SerializedName("suspicious_spans") // 可疑片段列表
    private List<SuspiciousSpan> suspiciousSpans; // 使用 db 包中的 SuspiciousSpan

    @SerializedName("created_at") // 创建时间
    private String createdAt;

    // 构造函数
    public AnalysisReport() {}

    public AnalysisReport(String id, String originalContent, String analysisResult, int score, String detailedReport, List<SuspiciousSpan> suspiciousSpans, String createdAt) {
        this.id = id;
        this.originalContent = originalContent;
        this.analysisResult = analysisResult;
        this.score = score;
        this.detailedReport = detailedReport;
        this.suspiciousSpans = suspiciousSpans;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDetailedReport() {
        return detailedReport;
    }

    public void setDetailedReport(String detailedReport) {
        this.detailedReport = detailedReport;
    }

    public List<SuspiciousSpan> getSuspiciousSpans() {
        return suspiciousSpans;
    }

    public void setSuspiciousSpans(List<SuspiciousSpan> suspiciousSpans) {
        this.suspiciousSpans = suspiciousSpans;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}