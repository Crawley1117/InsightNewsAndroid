package com.example.insightnewsandroid.ui.credibility;

public class ChatMessage {
    public enum Type { USER, AI }
    private Type type;
    private String text;
    private String imageUrl;
    private String fileName;
    private CredibilityResult result;

    public ChatMessage(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    // Getters & Setters
    public Type getType() { return type; }
    public String getText() { return text; }
    public String getImageUrl() { return imageUrl; }
    public String getFileName() { return fileName; }
    public CredibilityResult getResult() { return result; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setResult(CredibilityResult result) { this.result = result; }
}