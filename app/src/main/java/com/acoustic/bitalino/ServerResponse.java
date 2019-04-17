package com.acoustic.bitalino;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class ServerResponse<T> {
    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private T data;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
