package com.bwssystems.HABridge;

public class BridgeSettings {
	private String upnpconfigaddress;
	private String serverport;
	private String upnpresponseport;
	private String upnpdevicedb;
	private String veraaddress;
	private String harmonyaddress;
	private String harmonyuser;
	private String harmonypwd;
	private boolean upnpstrict;
	private boolean traceupnp;
	private boolean vtwocompatibility;
	
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

	public String getHarmonyAddress() {
		return harmonyaddress;
	}
	public void setHarmonyAddress(String harmonyaddress) {
		this.harmonyaddress = harmonyaddress;
	}
	public String getHarmonyUser() {
		return harmonyuser;
	}
	public void setHarmonyUser(String harmonyuser) {
		this.harmonyuser = harmonyuser;
	}
	public String getHarmonyPwd() {
		return harmonypwd;
	}
	public void setHarmonyPwd(String harmonypwd) {
		this.harmonypwd = harmonypwd;
	}
	public boolean isUpnpStrict() {
		return upnpstrict;
	}
	public void setUpnpStrict(boolean upnpStrict) {
		this.upnpstrict = upnpStrict;
	}
	public boolean isTraceupnp() {
		return traceupnp;
	}
	public void setTraceupnp(boolean traceupnp) {
		this.traceupnp = traceupnp;
	}
	
	public boolean isVtwocompatibility() {
		return vtwocompatibility;
	}
	public void setVtwocompatibility(boolean vtwocompatibility) {
		this.vtwocompatibility = vtwocompatibility;
	}
	
	public Boolean isValidVera() {
		if(this.veraaddress.contains(Configuration.DEFAULT_VERA_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidHarmony() {
		if(this.harmonyaddress.contains(Configuration.DEFAULT_HARMONY_ADDRESS))
			return false;
		if(this.harmonypwd == null || this.harmonypwd == "")
			return false;
		if(this.harmonyuser == null || this.harmonyuser == "")
			return false;
		return true;
	}
}
