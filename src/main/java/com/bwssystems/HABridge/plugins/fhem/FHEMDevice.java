package com.bwssystems.HABridge.plugins.fhem;

public class FHEMDevice {

	private String address;
	private String name;
	private Result item;
	private String csrfToken;
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
	public Result getItem() {
		return item;
	}
	public void setItem(Result item) {
		this.item = item;
	}
	public String getCsrfToken(){ return csrfToken; }
	public void setCsrfToken(String csrfToken){ this.csrfToken = csrfToken; }
}
