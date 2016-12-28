package com.bwssystems.HABridge.plugins.NestBridge;

import java.io.UnsupportedEncodingException;

public class NestItem {
	private String name;
	private String id;
	private String type;
	private String location;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		byte ptext[];
		String theLabel = new String(name);
		try {
			ptext = theLabel.getBytes("ISO-8859-1");
			this.name = new String(ptext, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			this.name = theLabel;
		}
	}
	public String getId() {
		return id;
	}
	public void setId(String anid) {
		id = anid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
