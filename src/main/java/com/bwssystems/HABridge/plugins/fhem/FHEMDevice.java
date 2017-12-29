package com.bwssystems.HABridge.plugins.fhem;

public class FHEMDevice {
	
	private String address;
	private String name;
	private FHEMItem item;
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
	public FHEMItem getItem() {
		return item;
	}
	public void setItem(FHEMItem item) {
		this.item = item;
	}

}
