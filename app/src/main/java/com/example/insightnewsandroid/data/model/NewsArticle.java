package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;

public class NewsArticle {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("content")
    private String content;
    @SerializedName("author")
    private String author;
    @SerializedName("publish_date")
    private String publishDate;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("category")
    private String category;
    @SerializedName("source")
    private String source;
    @SerializedName("is_favorite")
    private boolean isFavorite; // [已新增] 话题是否被收藏

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
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
