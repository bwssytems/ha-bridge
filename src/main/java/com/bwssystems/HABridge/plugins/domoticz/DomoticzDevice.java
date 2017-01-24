package com.bwssystems.HABridge.plugins.domoticz;

public class DomoticzDevice {
	private String devicetype;
	private String devicename;
	private String domoticzaddress;
	private String domoticzname;
	public String getDevicetype() {
		return devicetype;
	}
	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype;
	}
	public String getDevicename() {
		return devicename;
	}
	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}
	public String getDomoticzaddress() {
		return domoticzaddress;
	}
	public void setDomoticzaddress(String domoticzaddress) {
		this.domoticzaddress = domoticzaddress;
	}
	public String getDomoticzname() {
		return domoticzname;
	}
	public void setDomoticzname(String domoticzname) {
		this.domoticzname = domoticzname;
	}
}