package com.bwssystems.HABridge.api.hue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class HueConfig
{
	private Boolean portalservices;
	private String gateway;
	private String mac;
	private String swversion;
	private String apiversion;
	private Boolean linkbutton;
	private String ipaddress;
	private Integer proxyport;
	private Swupdate swupdate;
	private String netmask;
	private String name;
	private Boolean dhcp;
	private String UTC;
	private String proxyaddress;
	private String localtime;
	private String timezone;
	private String zigbeechannel;
	private String modelid;
	private String bridgeid;
	private Boolean factorynew;
	private String replacesbridgeid;
	private Map<String, WhitelistEntry> whitelist;

	public static HueConfig createConfig(String name, String ipaddress, Map<String, WhitelistEntry> awhitelist, String emulateHubVersion, boolean isLinkButtonPressed) {
		HueConfig aConfig = new HueConfig();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		aConfig.setMac(HueConfig.getMacAddress(ipaddress));
		aConfig.setApiversion(HueConstants.API_VERSION);
		aConfig.setPortalservices(false);
		aConfig.setGateway(ipaddress);
		aConfig.setSwversion(emulateHubVersion);
		aConfig.setLinkbutton(isLinkButtonPressed);
		aConfig.setIpaddress(ipaddress);
		aConfig.setProxyport(0);
		aConfig.setSwupdate(Swupdate.createSwupdate());
		aConfig.setNetmask("255.255.255.0");
		aConfig.setName(name);
		aConfig.setDhcp(true);
		aConfig.setUtc(dateFormatGmt.format(new Date()));
		aConfig.setProxyaddress("none");
		aConfig.setLocaltime(dateFormat.format(new Date()));
		aConfig.setTimezone(TimeZone.getDefault().getID());
		aConfig.setZigbeechannel("6");
		aConfig.setBridgeid(HuePublicConfig.createConfig(name, ipaddress, emulateHubVersion).getHueBridgeIdFromMac());
		aConfig.setModelid(HueConstants.MODEL_ID);
		aConfig.setFactorynew(false);
		aConfig.setReplacesbridgeid(null);
		aConfig.setWhitelist(awhitelist);

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
	public Boolean getPortalservices() {
		return portalservices;
	}

	public void setPortalservices(Boolean portalservices) {
		this.portalservices = portalservices;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
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

	public Boolean getLinkbutton() {
		return linkbutton;
	}

	public void setLinkbutton(Boolean linkbutton) {
		this.linkbutton = linkbutton;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public Integer getProxyport() {
		return proxyport;
	}

	public void setProxyport(Integer proxyport) {
		this.proxyport = proxyport;
	}

	public Swupdate getSwupdate() {
		return swupdate;
	}

	public void setSwupdate(Swupdate swupdate) {
		this.swupdate = swupdate;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getDhcp() {
		return dhcp;
	}

	public void setDhcp(Boolean dhcp) {
		this.dhcp = dhcp;
	}

	public String getUtc() {
		return UTC;
	}

	public void setUtc(String utc) {
		this.UTC = utc;
	}

	public String getProxyaddress() {
		return proxyaddress;
	}

	public void setProxyaddress(String proxyaddress) {
		this.proxyaddress = proxyaddress;
	}

	public Map<String, WhitelistEntry> getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(Map<String, WhitelistEntry> whitelist) {
		this.whitelist = whitelist;
	}

	public String getApiversion() {
		return apiversion;
	}

	public void setApiversion(String apiversion) {
		this.apiversion = apiversion;
	}

	public String getLocaltime() {
		return localtime;
	}

	public void setLocaltime(String localtime) {
		this.localtime = localtime;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getZigbeechannel() {
		return zigbeechannel;
	}

	public void setZigbeechannel(String zigbeechannel) {
		this.zigbeechannel = zigbeechannel;
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
