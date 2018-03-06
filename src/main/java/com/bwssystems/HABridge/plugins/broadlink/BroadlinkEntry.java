package com.bwssystems.HABridge.plugins.broadlink;

public class BroadlinkEntry {
	private String name;
	private String id;
	private String ipAddr;
	private String macAddr;
	private String command;
	private String data;
	private String type;
	private String baseType;
	private String desc;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public String getIpAddr() {
		return ipAddr;
	}
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	public String getMacAddr() {
		return macAddr;
	}
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBaseType() {
		return baseType;
	}
	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public boolean hasIpAndMac() {
		boolean deviceOk = true;
		if(ipAddr == null || ipAddr.trim().isEmpty())
			deviceOk = false;
		else if(macAddr == null || macAddr.trim().isEmpty())
			deviceOk = false;
		else if(type == null || type.trim().isEmpty())
			deviceOk = false;
		return deviceOk;
	}
}
