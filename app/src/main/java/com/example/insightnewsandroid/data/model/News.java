package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;

public class News {
    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("credibility")
    private int credibility;

    @SerializedName("publishDate")
    private String publishDate;

    @SerializedName("creationTime")
    private String creationTime;

    @SerializedName("collect")
    private boolean collect;

    @SerializedName("evidenceChain")
    private String evidenceChain;

    // ✅ 添加后端可能返回的其他字段
    @SerializedName("isDislike")
    private Boolean isDislike;

    @SerializedName("likeCount")
    private Integer likeCount;

    @SerializedName("favoriteCount")
    private Integer favoriteCount;

    private String content;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCredibility() { return credibility; }
    public void setCredibility(int credibility) { this.credibility = credibility; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getCreationTime() { return creationTime; }
    public void setCreationTime(String creationTime) { this.creationTime = creationTime; }

    public boolean isCollect() { return collect; }
    public void setCollect(boolean collect) { this.collect = collect; }

    public String getEvidenceChain() { return evidenceChain; }
    public void setEvidenceChain(String evidenceChain) { this.evidenceChain = evidenceChain; }

    public Boolean getIsDislike() { return isDislike; }
    public void setIsDislike(Boolean isDislike) { this.isDislike = isDislike; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    // ✅ getContent() 方法 - 根据后端实际数据结构调整
    public String getContent() {
        // 如果 content 字段已经被设置，返回它
        if (content != null && !content.isEmpty()) {
            return content;
        }
        // 如果有 evidenceChain，使用它作为内容
        else if (evidenceChain != null && !evidenceChain.isEmpty()) {
            // 如果 evidenceChain 是 JSON 格式，可以简单处理
            return "可信度分析报告：" + evidenceChain;
        }
        // 最后返回标题
        else {
            return title;
        }
    }

    public void setContent(String content) {
        this.content = content;
    }
}