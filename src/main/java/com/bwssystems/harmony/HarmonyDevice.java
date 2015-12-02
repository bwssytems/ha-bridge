package com.bwssystems.harmony;

import net.whistlingfish.harmony.config.Device;

public class HarmonyDevice {
	private Device device;
	private String hub;
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public String getHub() {
		return hub;
	}
	public void setHub(String hub) {
		this.hub = hub;
	}
}
