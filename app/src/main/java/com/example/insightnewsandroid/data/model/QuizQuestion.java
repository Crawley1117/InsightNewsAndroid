package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;

public class QuizQuestion {

    @SerializedName("question")
    private String question;

    @SerializedName("answer")
    private String answer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
