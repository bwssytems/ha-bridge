package com.bwssystems.HABridge;

public class BridgeSettings {
	private String upnpconfigaddress;
	private String serverport;
	private String upnpresponseport;
	private String upnpdevicedb;
	private String veraaddress;
	private boolean upnpStrict;
	
	public String getUpnpConfigAddress() {
		return upnpconfigaddress;
	}
	public void setUpnpConfigAddress(String upnpConfigAddress) {
		this.upnpconfigaddress = upnpConfigAddress;
	}
	public String getServerPort() {
		return serverport;
	}
	public void setServerPort(String serverPort) {
		this.serverport = serverPort;
	}
	public String getUpnpResponsePort() {
		return upnpresponseport;
	}
	public void setUpnpResponsePort(String upnpResponsePort) {
		this.upnpresponseport = upnpResponsePort;
	}
	public String getUpnpDeviceDb() {
		return upnpdevicedb;
	}
	public void setUpnpDeviceDb(String upnpDeviceDb) {
		this.upnpdevicedb = upnpDeviceDb;
	}
	public String getVeraAddress() {
		return veraaddress;
	}
	public void setVeraAddress(String veraAddress) {
		this.veraaddress = veraAddress;
	}

	public boolean isUpnpStrict() {
		return upnpStrict;
	}
	public void setUpnpStrict(boolean upnpStrict) {
		this.upnpStrict = upnpStrict;
	}
}
