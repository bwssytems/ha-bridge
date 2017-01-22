package com.bwssystems.HABridge.plugins.hue;

import com.bwssystems.HABridge.api.hue.DeviceResponse;


public class HueDevice {
	private DeviceResponse device;
	private String huedeviceid;
	private String hueaddress;
	private String huename;
	public DeviceResponse getDevice() {
		return device;
	}
	public void setDevice(DeviceResponse adevice) {
		this.device = adevice;
	}
	public String getHuedeviceid() {
		return huedeviceid;
	}
	public void setHuedeviceid(String huedeviceid) {
		this.huedeviceid = huedeviceid;
	}
	public String getHueaddress() {
		return hueaddress;
	}
	public void setHueaddress(String ahueaddress) {
		this.hueaddress = ahueaddress;
	}
	public String getHuename() {
		return huename;
	}
	public void setHuename(String ahuename) {
		this.huename = ahuename;
	}
}
