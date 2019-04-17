package com.acoustic.bitalino;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BitalinoDeviceInfo {
    @SerializedName("frequency")
    @Expose
    private int frequency;

    public BitalinoDeviceInfo(int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
