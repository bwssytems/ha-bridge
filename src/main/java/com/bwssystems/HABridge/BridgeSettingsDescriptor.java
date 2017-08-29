package com.bwssystems.HABridge;

import java.util.List;
import java.util.Map;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.bwssystems.HABridge.api.hue.HueConstants;
import com.bwssystems.HABridge.api.hue.WhitelistEntry;

public class BridgeSettingsDescriptor {
	@SerializedName("upnpconfigaddress")
	@Expose
	private String upnpconfigaddress;
	@SerializedName("useupnpiface")
	@Expose
	private boolean useupnpiface;
	@SerializedName("serverport")
	@Expose
	private Integer serverport;
	@SerializedName("upnpresponseport")
	@Expose
	private Integer upnpresponseport;
	@SerializedName("upnpdevicedb")
	@Expose
	private String upnpdevicedb;
	@SerializedName("upnpgroupdb")
	@Expose
	private String upnpgroupdb;
	@SerializedName("veraaddress")
	@Expose
	private IpList veraaddress;
	@SerializedName("harmonyaddress")
	@Expose
	private IpList harmonyaddress;
	@SerializedName("buttonsleep")
	@Expose
	private Integer buttonsleep;
	@SerializedName("upnpstrict")
	@Expose
	private boolean upnpstrict;
	@SerializedName("traceupnp")
	@Expose
	private boolean traceupnp;
	@SerializedName("nestuser")
	@Expose
	private String nestuser;
	@SerializedName("nestpwd")
	@Expose
	private String nestpwd;
	@SerializedName("farenheit")
	@Expose
	private boolean farenheit;
	@SerializedName("configfile")
	@Expose
	private String configfile;
	@SerializedName("numberoflogmessages")
	@Expose
	private Integer numberoflogmessages;
	@SerializedName("hueaddress")
	@Expose
	private IpList hueaddress;
	@SerializedName("haladdress")
	@Expose
	private IpList haladdress;
	@SerializedName("haltoken")
	@Expose
	private String haltoken;
	@SerializedName("whitelist")
	@Expose
	private Map<String, WhitelistEntry> whitelist;
	@SerializedName("myechourl")
	@Expose
	private String myechourl;
	@SerializedName("webaddress")
	@Expose
	private String webaddress;
	@SerializedName("mqttaddress")
	@Expose
	private IpList mqttaddress;
	@SerializedName("hassaddress")
	@Expose
	private IpList hassaddress;
	@SerializedName("domoticzaddress")
	@Expose
	private IpList domoticzaddress;
	@SerializedName("somfyaddress")
	@Expose
	private IpList somfyaddress;
	@SerializedName("hubversion")
	@Expose
	private String hubversion;
	@SerializedName("securityData")
	@Expose
	private String securityData;

	
	private boolean settingsChanged;
	private boolean veraconfigured;
	private boolean harmonyconfigured;
	private boolean hueconfigured;
	private boolean nestconfigured;
	private boolean halconfigured;
	private boolean mqttconfigured;
	private boolean hassconfigured;
	private boolean domoticzconfigured;
	private boolean somfyconfigured;
	private boolean lifxconfigured;
	
	public BridgeSettingsDescriptor() {
		super();
		this.upnpstrict = true;
		this.useupnpiface = false;
		this.traceupnp = false;
		this.nestconfigured = false;
		this.veraconfigured = false;
		this.somfyconfigured = false;
		this.harmonyconfigured = false;
		this.hueconfigured = false;
		this.halconfigured = false;
		this.mqttconfigured = false;
		this.hassconfigured = false;
		this.domoticzconfigured = false;
		this.somfyconfigured = false;
		this.lifxconfigured = false;
		this.farenheit = true;
		this.securityData = null;
		this.settingsChanged = false;
		this.myechourl = "alexa.amazon.com/spa/index.html#cards";
		this.webaddress = "0.0.0.0";
		this.hubversion = HueConstants.HUB_VERSION;
	}
	public String getUpnpConfigAddress() {
		return upnpconfigaddress;
	}
	public void setUpnpConfigAddress(String upnpConfigAddress) {
		this.upnpconfigaddress = upnpConfigAddress;
	}
	public boolean isUseupnpiface() {
		return useupnpiface;
	}
	public void setUseupnpiface(boolean useupnpiface) {
		this.useupnpiface = useupnpiface;
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
	public String getUpnpGroupDb() {
		return upnpgroupdb;
	}
	public void setUpnpGroupDb(String upnpGroupDb) {
		this.upnpgroupdb = upnpGroupDb;
	}
	public IpList getVeraAddress() {
		return veraaddress;
	}
	public IpList getSomfyAddress() {
		return somfyaddress;
	}
	public void setVeraAddress(IpList veraAddress) {
		this.veraaddress = veraAddress;
	}
	public void setSomfyAddress(IpList somfyAddress) {
		this.somfyaddress = somfyAddress;
	}
	public IpList getHarmonyAddress() {
		return harmonyaddress;
	}
	public void setHarmonyAddress(IpList harmonyaddress) {
		this.harmonyaddress = harmonyaddress;
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
	public boolean isSomfyconfigured() {
		return somfyconfigured;
	}
	public void setVeraconfigured(boolean veraconfigured) {
		this.veraconfigured = veraconfigured;
	}
	public void setSomfyconfigured(boolean somfyconfigured) {
		this.somfyconfigured = somfyconfigured;
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
	protected void removeWhitelist() {
		whitelist = null;
	}
	public boolean isSettingsChanged() {
		return settingsChanged;
	}
	public void setSettingsChanged(boolean settingsChanged) {
		this.settingsChanged = settingsChanged;
	}
	public String getMyechourl() {
		return myechourl;
	}
	public void setMyechourl(String myechourl) {
		this.myechourl = myechourl;
	}
	public String getWebaddress() {
		return webaddress;
	}
	public void setWebaddress(String webaddress) {
		this.webaddress = webaddress;
	}
	public IpList getMqttaddress() {
		return mqttaddress;
	}
	public void setMqttaddress(IpList mqttaddress) {
		this.mqttaddress = mqttaddress;
	}
	public boolean isMqttconfigured() {
		return mqttconfigured;
	}
	public void setMqttconfigured(boolean mqttconfigured) {
		this.mqttconfigured = mqttconfigured;
	}
	public IpList getHassaddress() {
		return hassaddress;
	}
	public void setHassaddress(IpList hassaddress) {
		this.hassaddress = hassaddress;
	}
	public boolean isHassconfigured() {
		return hassconfigured;
	}
	public void setHassconfigured(boolean hassconfigured) {
		this.hassconfigured = hassconfigured;
	}
	public String getHubversion() {
		return hubversion;
	}
	public void setHubversion(String hubversion) {
		this.hubversion = hubversion;
	}
	public IpList getDomoticzaddress() {
		return domoticzaddress;
	}
	public void setDomoticzaddress(IpList domoticzaddress) {
		this.domoticzaddress = domoticzaddress;
	}
	public boolean isDomoticzconfigured() {
		return domoticzconfigured;
	}
	public void setDomoticzconfigured(boolean domoticzconfigured) {
		this.domoticzconfigured = domoticzconfigured;
	}
	public boolean isLifxconfigured() {
		return lifxconfigured;
	}
	public void setLifxconfigured(boolean lifxconfigured) {
		this.lifxconfigured = lifxconfigured;
	}
	public String getSecurityData() {
		return securityData;
	}
	public void setSecurityData(String securityData) {
		this.securityData = securityData;
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
		if(devicesList.get(0).getPassword() == null || devicesList.get(0).getPassword().trim().isEmpty()) {
			if(this.getHaltoken() == null || this.getHaltoken().equals(""))
				return false;
		}
		return true;
	}
	public Boolean isValidMQTT() {
		if(this.getMqttaddress() == null || this.getMqttaddress().getDevices().size() <= 0)
			return false;		
		List<NamedIP> devicesList = this.getMqttaddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidHass() {
		if(this.getHassaddress() == null || this.getHassaddress().getDevices().size() <= 0)
			return false;		
		List<NamedIP> devicesList = this.getHassaddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidDomoticz() {
		if(this.getDomoticzaddress() == null || this.getDomoticzaddress().getDevices().size() <= 0)
			return false;		
		List<NamedIP> devicesList = this.getDomoticzaddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidSomfy() {
		if(this.getSomfyAddress() == null || this.getSomfyAddress().getDevices().size() <= 0)
			return false;
		List<NamedIP> devicesList = this.getSomfyAddress().getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidLifx() {
		return this.isLifxconfigured();
	}
	public void updateHue(NamedIP aHue) {
		int indexHue = -1;
		for( int i = 0; i < hueaddress.getDevices().size(); i++) {
			if(hueaddress.getDevices().get(i).getName().equals(aHue.getName()))
				indexHue = i;
		}
		if(indexHue >= 0) {
			hueaddress.getDevices().set(indexHue, aHue);
			this.setSettingsChanged(true);
		}
	}
}
