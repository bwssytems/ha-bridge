package com.bwssystems.hass;

public class HassDevice {
	private State deviceState;
	private String hassaddress;
	private String hassname;
	public State getDeviceState() {
		return deviceState;
	}
	public void setDeviceState(State deviceState) {
		this.deviceState = deviceState;
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
