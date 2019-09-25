package com.bwssystems.HABridge.hue;

public class XYColorSpace {
    float[] xy;
    int brightness;

    public float[] getXy() {
        return xy;
    }

    public void setXy(float[] xy) {
        this.xy = xy;
    }

    public int getBrightness() {
        return brightness;
    }

    public float getBrightnessAdjusted() {
        return ((float) brightness / 254.0f) * 100f;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}