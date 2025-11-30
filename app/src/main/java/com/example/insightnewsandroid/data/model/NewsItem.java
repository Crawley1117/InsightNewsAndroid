package com.example.insightnewsandroid.data.model;

public class NewsItem {
    private String id = "";
    private String title = "";
    private String date = "";
    private String imageUrl = "";
    private int viewCount = 0;
    private int likeCount = 0;
    private int commentCount = 0;
    private boolean isLiked = false;
    private boolean isDisliked = false;
    private int topicId = 0;
    private String topicTitle = "";
    private String topicCategory = "";
    private String content = "";
    private boolean isCollected = false;
    private String credibilityLevel; // 可信度等级：高、中、低
    private String credibilityScore; // 可信度分数

    // 构造函数
    public NewsItem() {}

    public NewsItem(String id, String title, String date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public boolean isDisliked() { return isDisliked; }
    public void setDisliked(boolean disliked) { isDisliked = disliked; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getTopicTitle() { return topicTitle; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }

    public String getTopicCategory() { return topicCategory; }
    public void setTopicCategory(String topicCategory) { this.topicCategory = topicCategory; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCollected() { return isCollected; }
    public void setCollected(boolean collected) { isCollected = collected; }
    public String getCredibilityLevel() {
        return credibilityLevel;
    }

    public void setCredibilityLevel(String credibilityLevel) {
        this.credibilityLevel = credibilityLevel;
    }

    public String getCredibilityScore() {
        return credibilityScore;
    }

    public void setCredibilityScore(String credibilityScore) {
        this.credibilityScore = credibilityScore;
    }
}
