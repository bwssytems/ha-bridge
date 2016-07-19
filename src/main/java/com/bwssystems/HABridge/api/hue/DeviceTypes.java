package com.bwssystems.HABridge.api.hue;

public class DeviceTypes {
	private Boolean bridge;
	private String[] lights;
	public Boolean getBridge() {
		return bridge;
	}
	public void setBridge(Boolean bridge) {
		this.bridge = bridge;
	}
	public String[] getLights() {
		return lights;
	}
	public void setLights(String[] lights) {
		this.lights = lights;
	}
}
