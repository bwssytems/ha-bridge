package com.bwssystems.HABridge.plugins.moziot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MozIotCommand {
    @SerializedName("url")
    @Expose
	private String url;
    @SerializedName("command")
    @Expose
	private MozIotCommandDetail command;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public MozIotCommandDetail getCommand() {
		return command;
	}
	public void setCommand(MozIotCommandDetail command) {
		this.command = command;
	}

}
