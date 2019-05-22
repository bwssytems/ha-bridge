package com.bwssystems.HABridge.hue;

public class HueSatBri {
    int hue;
    int sat;
    int bri;

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public int getBri() {
        return bri;
    }

    public void setBri(int bri) {
        this.bri = bri;
    }

    public String toString() {
        String formatString = new String();

        formatString = "Hue: " + Integer.toString(hue) + ", Sat: " + Integer.toString(sat) + ", Bri: " + Integer.toString(bri);
        return formatString;
    }
}