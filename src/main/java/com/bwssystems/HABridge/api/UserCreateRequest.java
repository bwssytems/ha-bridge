package com.bwssystems.HABridge.api;

public class UserCreateRequest {
    private String devicetype;
	private String username;

    public String getDevicetype() {
		return devicetype;
	}
	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
