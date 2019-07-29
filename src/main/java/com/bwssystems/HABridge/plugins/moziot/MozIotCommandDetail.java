package com.bwssystems.HABridge.plugins.moziot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MozIotCommandDetail {
    @SerializedName("on")
    @Expose
	private boolean on;
    @SerializedName("level")
    @Expose
	private String level;
    @SerializedName("color")
    @Expose
	private String color;

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBody() {
        String theBody = "";

        if(level != null && level != "") {
            theBody = "{\"level\":" + level + "}";
        }
        else if(color != null && color != "") {
            theBody = "{\"color\":\"" + color + "\"}";
        } else {
            theBody = "{\"on\":" + on + "}";
        }
        return theBody;
    }
}