package com.bwssystems.HABridge;

import java.util.List;
import java.util.Map;

import com.bwssystems.HABridge.api.hue.WhitelistEntry;

public class BridgeSettingsDescriptor {
	private String upnpconfigaddress;
	private Integer serverport;
	private Integer upnpresponseport;
	private String upnpdevicedb;
	private IpList veraaddress;
	private IpList harmonyaddress;
	private String harmonyuser;
	private String harmonypwd;
	private Integer buttonsleep;
	private boolean upnpstrict;
	private boolean traceupnp;
	private String nestuser;
	private String nestpwd;
	private boolean veraconfigured;
	private boolean harmonyconfigured;
	private boolean nestconfigured;
	private boolean farenheit;
	private String configfile;
	private Integer numberoflogmessages;
	private IpList hueaddress;
	private boolean hueconfigured;
	private IpList haladdress;
	private String haltoken;
	private boolean halconfigured;
	private Map<String, WhitelistEntry> whitelist;
	
	public BridgeSettingsDescriptor() {
		super();
		this.upnpstrict = true;
		this.traceupnp = false;
		this.nestconfigured = false;
		this.veraconfigured = false;
		this.harmonyconfigured = false;
		this.hueconfigured = false;
		this.farenheit = true;
	}
	public String getUpnpConfigAddress() {
		return upnpconfigaddress;
	}
	public void setUpnpConfigAddress(String upnpConfigAddress) {
		this.upnpconfigaddress = upnpConfigAddress;
	}
	public Integer  getServerPort() {
		return serverport;
	}
	public void setServerPort(Integer serverPort) {
		this.serverport = serverPort;
	}
	public void setServerPort(String serverPort) {
		this.serverport = Integer.valueOf(serverPort);
	}
	public Integer getUpnpResponsePort() {
		return upnpresponseport;
	}
	public void setUpnpResponsePort(Integer upnpResponsePort) {
		this.upnpresponseport = upnpResponsePort;
	}
	public void setUpnpResponsePort(String upnpResponsePort) {
		this.upnpresponseport = Integer.valueOf(upnpResponsePort);
	}
	public String getUpnpDeviceDb() {
		return upnpdevicedb;
	}
	public void setUpnpDeviceDb(String upnpDeviceDb) {
		this.upnpdevicedb = upnpDeviceDb;
	}
	public IpList getVeraAddress() {
		return veraaddress;
	}
	public void setVeraAddress(IpList veraAddress) {
		this.veraaddress = veraAddress;
	}
	public IpList getHarmonyAddress() {
		return harmonyaddress;
	}
	public void setHarmonyAddress(IpList harmonyaddress) {
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
	public String getNestuser() {
		return nestuser;
	}
	public void setNestuser(String nestuser) {
		this.nestuser = nestuser;
	}
	public String getNestpwd() {
		return nestpwd;
	}
	public void setNestpwd(String nestpwd) {
		this.nestpwd = nestpwd;
	}
	public boolean isVeraconfigured() {
		return veraconfigured;
	}
	public void setVeraconfigured(boolean veraconfigured) {
		this.veraconfigured = veraconfigured;
	}
	public boolean isHarmonyconfigured() {
		return harmonyconfigured;
	}
	public void setHarmonyconfigured(boolean harmonyconfigured) {
		this.harmonyconfigured = harmonyconfigured;
	}
	public boolean isNestConfigured() {
		return nestconfigured;
	}
	public void setNestConfigured(boolean isNestConfigured) {
		this.nestconfigured = isNestConfigured;
	}
	public Integer getButtonsleep() {
		return buttonsleep;
	}
	public void setButtonsleep(Integer buttonsleep) {
		this.buttonsleep = buttonsleep;
	}
	public String getConfigfile() {
		return configfile;
	}
	public void setConfigfile(String configfile) {
		this.configfile = configfile;
	}
	public Integer getNumberoflogmessages() {
		return numberoflogmessages;
	}
	public void setNumberoflogmessages(Integer numberoflogmessages) {
		this.numberoflogmessages = numberoflogmessages;
	}
	public boolean isFarenheit() {
		return farenheit;
	}
	public void setFarenheit(boolean farenheit) {
		this.farenheit = farenheit;
	}
	public IpList getHueaddress() {
		return hueaddress;
	}
	public void setHueaddress(IpList hueaddress) {
		this.hueaddress = hueaddress;
	}
	public boolean isHueconfigured() {
		return hueconfigured;
	}
	public void setHueconfigured(boolean hueconfigured) {
		this.hueconfigured = hueconfigured;
	}
	public IpList getHaladdress() {
		return haladdress;
	}
	public void setHaladdress(IpList haladdress) {
		this.haladdress = haladdress;
	}
	public String getHaltoken() {
		return haltoken;
	}
	public void setHaltoken(String haltoken) {
		this.haltoken = haltoken;
	}
	public boolean isHalconfigured() {
		return halconfigured;
	}
	public void setHalconfigured(boolean halconfigured) {
		this.halconfigured = halconfigured;
	}
	public Map<String, WhitelistEntry> getWhitelist() {
		return whitelist;
	}
	public void setWhitelist(Map<String, WhitelistEntry> whitelist) {
		this.whitelist = whitelist;
	}
	public Boolean isValidVera() {
		if(this.getVeraAddress() == null || this.getVeraAddress().getDevices().size() <= 0)
			return false;
		List<NamedIP> devicesList = this.getVeraAddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidHarmony() {
		if(this.getHarmonyAddress() == null || this.getHarmonyAddress().getDevices().size() <= 0)
			return false;		
		List<NamedIP> devicesList = this.getHarmonyAddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		if(this.getHarmonyPwd() == null || this.getHarmonyPwd().equals(""))
			return false;
		if(this.getHarmonyUser() == null || this.getHarmonyUser().equals(""))
			return false;
		return true;
	}
	public Boolean isValidNest() {
		if(this.getNestpwd() == null || this.getNestpwd().equals(""))
			return false;
		if(this.getNestuser() == null || this.getNestuser().equals(""))
			return false;
		return true;
	}
	public Boolean isValidHue() {
		if(this.getHueaddress() == null || this.getHueaddress().getDevices().size() <= 0)
			return false;
		List<NamedIP> devicesList = this.getHueaddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidHal() {
		if(this.getHaladdress() == null || this.getHaladdress().getDevices().size() <= 0)
			return false;		
		List<NamedIP> devicesList = this.getHaladdress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		if(this.getHaltoken() == null || this.getHaltoken().equals(""))
			return false;
		return true;
	}
}
