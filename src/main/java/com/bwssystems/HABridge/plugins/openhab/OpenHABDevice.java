package com.bwssystems.HABridge.plugins.openhab;

public class OpenHABDevice {
	
	private String address;
	private String name;
	private OpenHABItem item;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public OpenHABItem getItem() {
		return item;
	}
	public void setItem(OpenHABItem item) {
		this.item = item;
	}

}
