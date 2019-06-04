package com.bwssystems.HABridge.plugins.homegenie;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HomeGenieCommand {
    @SerializedName("moduleType")
    @Expose
	private String moduleType;
    @SerializedName("deviceId")
    @Expose
	private String deviceId;
    @SerializedName("command")
    @Expose
	private HomeGenieCommandDetail command;

	public String getModuleType() {
		return moduleType;
	}

	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public HomeGenieCommandDetail getCommand() {
		return command;
	}

	public void setCommand(HomeGenieCommandDetail command) {
		this.command = command;
	}
}
