package com.bwssystems.HABridge.api.hue;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;

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

	public static HuePublicConfig createConfig(String name, String ipaddress) {
		HuePublicConfig aConfig = new HuePublicConfig();
		aConfig.setMac(HuePublicConfig.getMacAddress(ipaddress));
		aConfig.setApiversion("1.14.0");
		aConfig.setSwversion("01033989");
		aConfig.setName(name);
		aConfig.setBridgeid(HuePublicConfig.getBridgeIdFromMac(aConfig.getMac(), ipaddress));
		aConfig.setModelid("BSB002");
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

	protected static String getBridgeIdFromMac(String macAddr, String ipAddr)
	{
    	StringTokenizer st = new StringTokenizer(macAddr, ":");
    	String bridgeId = "";
//    	String port = null;
    	while(st.hasMoreTokens()) {
    		bridgeId = bridgeId + st.nextToken();
    	}
//    	if(ipAddr.contains(":")) {
//    		port = ipAddr.substring(ipAddr.indexOf(":"));
//        	BigInteger bigInt = BigInteger.valueOf(Integer.getInteger(port).intValue());
//        	byte[] theBytes = bigInt.toByteArray();
//        	bridgeId = bridgeId + DatatypeConverter.printHexBinary(theBytes);
//    	}
//    	else
//    		bridgeId = bridgeId + "0800";
		return bridgeId;
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
