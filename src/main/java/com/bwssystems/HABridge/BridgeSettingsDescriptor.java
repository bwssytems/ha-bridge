package com.bwssystems.HABridge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import com.bwssystems.HABridge.api.hue.HueConstants;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.api.hue.WhitelistEntry;

public class BridgeSettingsDescriptor {
	private static final String DEPRACATED_INTERNAL_USER = "thehabridgeuser";
	private static final String TEST_USER_TYPE = "test_ha_bridge";
	private String upnpconfigaddress;
	private Integer serverport;
	private Integer upnpresponseport;
	private String upnpdevicedb;
	private IpList veraaddress;
	private IpList harmonyaddress;
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
	private boolean settingsChanged;
	private String myechourl;
	private String webaddress;
	private IpList mqttaddress;
	private boolean mqttconfigured;
	private IpList hassaddress;
	private boolean hassconfigured;
	private String hubversion;
	private IpList domoticzaddress;
	private boolean domoticzconfigured;
	private IpList somfyaddress;
	private boolean somfyconfigured;
	private boolean lifxconfigured;
	private String securityData;
	
	public BridgeSettingsDescriptor() {
		super();
		this.upnpstrict = true;
		this.traceupnp = false;
		this.nestconfigured = false;
		this.veraconfigured = false;
		this.somfyconfigured = false;
		this.harmonyconfigured = false;
		this.hueconfigured = false;
		this.halconfigured = false;
		this.mqttconfigured = false;
		this.hassconfigured = false;
		this.farenheit = true;
		this.whitelist = null;
		this.settingsChanged = false;
		this.myechourl = "echo.amazon.com/#cards";
		this.webaddress = "0.0.0.0";
		this.hubversion = HueConstants.HUB_VERSION;
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
	public void setWhitelist(Map<String, WhitelistEntry> whitelist) {
		this.whitelist = whitelist;
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
		if(this.getHaltoken() == null || this.getHaltoken().equals(""))
			return false;
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

	public HueError[] validateWhitelistUser(String aUser, String userDescription, boolean strict) {
		String validUser = null;
		boolean found = false;
		if (aUser != null && !aUser.equalsIgnoreCase("undefined") && !aUser.equalsIgnoreCase("null")
				&& !aUser.equalsIgnoreCase("")) {
			if (whitelist != null) {
				Set<String> theUserIds = whitelist.keySet();
				Iterator<String> userIterator = theUserIds.iterator();
				while (userIterator.hasNext()) {
					validUser = userIterator.next();
					if (validUser.equals(aUser))
						found = true;
				}
			}
		}

		if(!found && !strict) {
			newWhitelistUser(aUser, userDescription);
			
			found = true;
		}
		
		if (!found) {
			return HueErrorResponse.createResponse("1", "/api/" + aUser, "unauthorized user", null, null, null).getTheErrors();
		}

		Object anUser = whitelist.remove(DEPRACATED_INTERNAL_USER);
		if(anUser != null)
			setSettingsChanged(true);
		
		return null;
	}
	
	public void newWhitelistUser(String aUser, String userDescription) {
		if(aUser.equals(DEPRACATED_INTERNAL_USER))
			return;
		if (whitelist == null) {
			whitelist  = new HashMap<>();
		}
		if(userDescription == null)
			userDescription = "auto insert user";
		
		whitelist.put(aUser, WhitelistEntry.createEntry(userDescription));
		setSettingsChanged(true);
	}

	public String createWhitelistUser(String userDescription) {
		String aUser = getNewUserID();
		newWhitelistUser(aUser, userDescription);
		return aUser;
	}

	private String getNewUserID() {
		UUID uid = UUID.randomUUID();
		StringTokenizer st = new StringTokenizer(uid.toString(), "-");
		String newUser = "";
		while (st.hasMoreTokens()) {
			newUser = newUser + st.nextToken();
		}

		return newUser;
	}
	
	public void removeTestUsers() {
		if (whitelist != null) {
			Object anUser = whitelist.remove(DEPRACATED_INTERNAL_USER);
			if(anUser != null)
				setSettingsChanged(true);

		    Iterator<Entry<String, WhitelistEntry>> it = whitelist.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, WhitelistEntry> pair = it.next();
		        it.remove(); // avoids a ConcurrentModificationException
		        if(pair.getValue().getName().equals(TEST_USER_TYPE)) {
			        whitelist.remove(pair.getKey());
					setSettingsChanged(true);
		        }
		    }
		}
	}
}
