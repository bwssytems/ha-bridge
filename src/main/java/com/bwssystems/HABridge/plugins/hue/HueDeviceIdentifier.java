package com.bwssystems.HABridge.plugins.hue;

public class HueDeviceIdentifier {
	private String hueName;
	private String ipAddress;
	private String deviceId;
	public String getHueName() {
		return hueName;
	}
	public void setHueName(String hueName) {
		this.hueName = hueName;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
}
