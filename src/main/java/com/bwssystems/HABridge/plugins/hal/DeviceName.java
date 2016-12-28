package com.bwssystems.HABridge.plugins.hal;

import com.google.gson.annotations.SerializedName;

public class DeviceName {
	@SerializedName(value="DeviceName", alternate={"SceneName", "GroupName", "HVACName", "MacroName", "IrName", "IrButton"})
	private String DeviceName;

	public String getDeviceName() {
		return DeviceName;
	}

	public void setDeviceName(String deviceName) {
		DeviceName = deviceName;
	}

}
