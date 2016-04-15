package com.bwssystems.hue;

import com.bwssystems.HABridge.api.hue.DeviceResponse;


public class HueDevice {
	private DeviceResponse device;
	private String hubaddress;
	private String hubname;
	public DeviceResponse getDevice() {
		return device;
	}
	public void setDevice(DeviceResponse device) {
		this.device = device;
	}
	public String getHubaddress() {
		return hubaddress;
	}
	public void setHubaddress(String hubaddress) {
		this.hubaddress = hubaddress;
	}
	public String getHubname() {
		return hubname;
	}
	public void setHubname(String hubname) {
		this.hubname = hubname;
	}
}
