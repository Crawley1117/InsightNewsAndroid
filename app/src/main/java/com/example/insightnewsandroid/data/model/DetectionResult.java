// 文件: app/src/main/java/com/example/insightnewsandroid/data/model/DetectionResult.java
package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DetectionResult {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("credibility")
    private Integer credibility; // 信任度分数

    @SerializedName("publishDate")
    private String publishDate;

    @SerializedName("creationTime")
    private String creationTime;

    @SerializedName("isDislike")
    private Integer isDislike;

    @SerializedName("likeCount")
    private Integer likeCount;

    @SerializedName("favoriteCount")
    private Integer favoriteCount;

    @SerializedName("evidenceChain")
    private List<EvidenceChainItem> evidenceChain;

    @SerializedName("collect")
    private Boolean collect;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getCredibility() { return credibility; }
    public void setCredibility(Integer credibility) { this.credibility = credibility; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getCreationTime() { return creationTime; }
    public void setCreationTime(String creationTime) { this.creationTime = creationTime; }

    public Integer getIsDislike() { return isDislike; }
    public void setIsDislike(Integer isDislike) { this.isDislike = isDislike; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public List<EvidenceChainItem> getEvidenceChain() { return evidenceChain; }
    public void setEvidenceChain(List<EvidenceChainItem> evidenceChain) { this.evidenceChain = evidenceChain; }

    public Boolean getCollect() { return collect; }
    public void setCollect(Boolean collect) { this.collect = collect; }

    // 内部类: EvidenceChainItem
    public static class EvidenceChainItem {
        @SerializedName("quote")
        private String quote;

        @SerializedName("reason")
        private String reason;

        @SerializedName("score")
        private Double score;

        // Getters and Setters
        public String getQuote() { return quote; }
        public void setQuote(String quote) { this.quote = quote; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
}