package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsArticle {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("publish_date")
    private String publishDate;

    @SerializedName("category")
    private String category;

    @SerializedName("source")
    private String source;

    @SerializedName("attentionNum")
    private Integer attentionNum;

    @SerializedName("news")
    private List<News> news;

    @SerializedName("description")
    private String content;

    @SerializedName("topicCover")
    private String imageUrl;

    @SerializedName("favorited")
    private boolean favorite;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getAttentionNum() {
        return attentionNum != null ? attentionNum : 0;
    }

    public void setAttentionNum(Integer attentionNum) {
        this.attentionNum = attentionNum;
    }

    public List<News> getNews() { return news; }
    public void setNews(List<News> news) { this.news = news; }

    // 收藏状态相关方法
    public boolean isFavorited() {
        return favorite || isFavorite; // 如果两个字段都检查
    }

    public void setFavorited(boolean favorited) {
        this.favorite = favorited;
        this.isFavorite = favorited;
    }

    public boolean isFavorite() {
        return isFavorite || favorite; // 兼容两种字段
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        this.favorite = favorite;
    }
}