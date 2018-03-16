package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class DeviceProperties {
    @SerializedName("value")
    private String value;

    @SerializedName("saveLogs")
    private String saveLogs;

    @SerializedName("userDescription")
    private String userDescription;
    public String getValue() {
        return value;
    }
	public String getSaveLogs() {
		return saveLogs;
	}
	public String getUserDescription() {
		return userDescription;
	}
}
