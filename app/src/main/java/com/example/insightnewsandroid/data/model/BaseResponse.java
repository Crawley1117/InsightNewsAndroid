package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class BaseResponse<T> { // 泛型化，以便 data 字段可以是不同类型
    @SerializedName("code")
    private int code;

    @SerializedName("msg") // 使用后端实际返回的字段名
    private String msg;

    @SerializedName("data")
    private T data; // 使用泛型 T

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 判断是否成功，根据后端返回 code == 200 为成功
    public boolean isSuccess() {
        return code == 200;
    }
}