package com.bwssystems.HABridge.plugins.hal;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class DeviceElements {
	@SerializedName(value="DeviceElements", alternate={"SceneElements", "GroupElements", "HVACElements", "MacroElements", "IrElements", "IrButtons"})
	private List<DeviceName> DeviceElements;

	public List<DeviceName> getDeviceElements() {
		return DeviceElements;
	}

	public void setDeviceElements(List<DeviceName> deviceElements) {
		DeviceElements = deviceElements;
	}
}
