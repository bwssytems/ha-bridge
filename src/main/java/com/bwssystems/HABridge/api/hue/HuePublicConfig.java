package com.bwssystems.HABridge.api.hue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;


public class HuePublicConfig
{
	private String name;
	private String apiversion;
	private String swversion;
	private String mac;
	private String bridgeid;
	private String replacesbridgeid;
	private Boolean factorynew;
	private String modelid;

	public static HuePublicConfig createConfig(String name, String ipaddress, String emulateHubVersion) {
		HuePublicConfig aConfig = new HuePublicConfig();
		aConfig.setMac(HuePublicConfig.getMacAddress(ipaddress));
		aConfig.setApiversion(HueConstants.API_VERSION);
		aConfig.setSwversion(emulateHubVersion);
		aConfig.setName(name);
		aConfig.setBridgeid(aConfig.getHueBridgeIdFromMac());
		aConfig.setModelid(HueConstants.MODEL_ID);
		aConfig.setFactorynew(false);
		aConfig.setReplacesbridgeid(null);

		return aConfig;
	}

	private static String getMacAddress(String addr)
	{
		InetAddress ip;
		StringBuilder sb = new StringBuilder();
		try {
				
			ip = InetAddress.getByName(addr);
			
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
				
			byte[] mac = network.getHardwareAddress();
				
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));		
			}
				
		} catch (UnknownHostException e) {
			
			sb.append("00:00:88:00:bb:ee");
			
		} catch (SocketException e){
				
			sb.append("00:00:88:00:bb:ee");
				
		} catch (Exception e){
			
			sb.append("00:00:88:00:bb:ee");
			
		}
		    
		return sb.toString();
	}

	public String getSNUUIDFromMac()
	{
    	StringTokenizer st = new StringTokenizer(this.getMac(), ":");
    	String bridgeUUID = "";
    	while(st.hasMoreTokens()) {
    		bridgeUUID = bridgeUUID + st.nextToken();
    	}
    	bridgeUUID = bridgeUUID.toLowerCase();
		return bridgeUUID.toLowerCase();
	}

	protected String getHueBridgeIdFromMac()
	{
		String cleanMac = this.getSNUUIDFromMac();
    	String bridgeId = cleanMac.substring(0, 6) + "FFFE" + cleanMac.substring(6);
		return bridgeId.toUpperCase();
	}
	
	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getSwversion() {
		return swversion;
	}

	public void setSwversion(String swversion) {
		this.swversion = swversion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getApiversion() {
		return apiversion;
	}

	public void setApiversion(String apiversion) {
		this.apiversion = apiversion;
	}


	public String getModelid() {
		return modelid;
	}

	public void setModelid(String modelid) {
		this.modelid = modelid;
	}

	public String getBridgeid() {
		return bridgeid;
	}

	public void setBridgeid(String bridgeid) {
		this.bridgeid = bridgeid;
	}

	public Boolean getFactorynew() {
		return factorynew;
	}

	public void setFactorynew(Boolean factorynew) {
		this.factorynew = factorynew;
	}

	public String getReplacesbridgeid() {
		return replacesbridgeid;
	}

	public void setReplacesbridgeid(String replacesbridgeid) {
		this.replacesbridgeid = replacesbridgeid;
	}
}
