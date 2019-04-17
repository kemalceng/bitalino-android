package com.acoustic.bitalino;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class CreateRecordResponse {
    @SerializedName("id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
