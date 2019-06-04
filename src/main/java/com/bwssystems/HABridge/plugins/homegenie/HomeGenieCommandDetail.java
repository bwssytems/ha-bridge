package com.bwssystems.HABridge.plugins.homegenie;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HomeGenieCommandDetail {
    @SerializedName("command")
    @Expose
	private String command;
    @SerializedName("level")
    @Expose
	private String level;
    @SerializedName("color")
    @Expose
	private String color;

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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}