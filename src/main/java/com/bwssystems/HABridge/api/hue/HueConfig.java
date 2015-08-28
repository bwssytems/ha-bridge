package com.bwssystems.HABridge.api.hue;

import java.util.HashMap;
import java.util.Map;

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
	private Map<String, WhitelistEntry> whitelist;

	public static HueConfig createConfig(String name, String ipaddress, String devicetype, String userid) {
		HueConfig aConfig = new HueConfig();
		aConfig.setApiversion("1.4.0");
		aConfig.setPortalservices(false);
		aConfig.setGateway("192.168.1.1");
		aConfig.setMac("00:00:88:00:bb:ee");
		aConfig.setSwversion("01005215");
		aConfig.setLinkbutton(false);
		aConfig.setIpaddress(ipaddress);
		aConfig.setProxyport(0);
		aConfig.setSwupdate(Swupdate.createSwupdate());
		aConfig.setNetmask("255.255.255.0");
		aConfig.setName(name);
		aConfig.setDhcp(true);
		aConfig.setUtc("2014-07-17T09:27:35");
		aConfig.setProxyaddress("0.0.0.0");
		aConfig.setLocaltime("2014-07-17T11:27:35");
		aConfig.setTimezone("America/Chicago");
		aConfig.setZigbeechannel("6");
		Map<String, WhitelistEntry> awhitelist = new HashMap<>();
		awhitelist.put(userid, WhitelistEntry.createEntry(devicetype));
		aConfig.setWhitelist(awhitelist);
		

		return aConfig;
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
}
