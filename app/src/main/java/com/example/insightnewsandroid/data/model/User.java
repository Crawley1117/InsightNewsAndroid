package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private int id;

    @SerializedName(value = "name", alternate = {"username"})
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName(value = "avatar", alternate = {"avatarUrl"}) 
    private String avatarUrl;

    @SerializedName("gender")
    private String gender;

    @SerializedName("profile")
    private String profile; // 简介

    // [已新增] 补充缺失的字段
    @SerializedName("region")
    private String region;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
