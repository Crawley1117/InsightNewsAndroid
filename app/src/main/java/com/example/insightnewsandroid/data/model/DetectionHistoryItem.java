package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * 检测历史记录项的响应模型
 * 此模型用于接收后端 "查看检测历史" 接口返回的单条记录
 */
public class DetectionHistoryItem {

    @SerializedName("id")
    private String id;

    @SerializedName("type") // 例如 "text", "image"
    private String type;

    @SerializedName("content_preview") // 内容预览
    private String contentPreview;

    @SerializedName("result") // 检测结果，例如 "高", "中", "低"
    private String result;

    @SerializedName("created_at") // 创建时间
    private String createdAt;

    // 构造函数
    public DetectionHistoryItem() {}

    public DetectionHistoryItem(String id, String type, String contentPreview, String result, String createdAt) {
        this.id = id;
        this.type = type;
        this.contentPreview = contentPreview;
        this.result = result;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}