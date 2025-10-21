package com.example.insightnewsandroid.ui.credibility;

public class CredibilityResult {
    private final int score;
    private final String reason;
    public CredibilityResult(int score, String reason) {
        this.score = score;
        this.reason = reason;
    }
    public int getScore() { return score; }
    public String getReason() { return reason; }
}