package com.example.insightnewsandroid.db;

import com.google.gson.annotations.SerializedName;

public class SuspiciousSpan {
    @SerializedName("text")
    public String text;

    @SerializedName("start")
    public int start;

    @SerializedName("end")
    public int end;

    @SerializedName("credibility_score") // 映射后端的 credibility_score 字段
    public int credibilityScore;

    @SerializedName("evidence")
    public String evidence;

    @SerializedName("analysis")
    public String analysis;
}