package com.bwssystems.HABridge.plugins.hass;

public class HassDevice {
	private State deviceState;
	private String deviceName;
	private String domain;
	private Boolean secure;
	private String hassaddress;
	private String hassname;
	public State getDeviceState() {
		return deviceState;
	}
	public void setDeviceState(State deviceState) {
		this.deviceState = deviceState;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public Boolean getSecure() {
		return secure;
	}
	public void setSecure(Boolean secure) {
		this.secure = secure;
	}
	public String getHassaddress() {
		return hassaddress;
	}
	public void setHassaddress(String hassaddress) {
		this.hassaddress = hassaddress;
	}
	public String getHassname() {
		return hassname;
	}
	public void setHassname(String hassname) {
		this.hassname = hassname;
	}
}
