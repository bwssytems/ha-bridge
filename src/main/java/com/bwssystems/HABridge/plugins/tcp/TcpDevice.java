package com.bwssystems.HABridge.plugins.tcp;

public class TcpDevice {
	private String tcpDevice;
	private boolean persistent;
	public String getTcpDevice() {
		return tcpDevice;
	}
	public void setTcpDevice(String tcpDevice) {
		this.tcpDevice = tcpDevice;
	}
	public boolean isPersistent() {
		return persistent;
	}
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
}
