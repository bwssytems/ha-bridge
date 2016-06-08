package com.bwssystems.HABridge.api.hue;

public class HueErrorDetails {
	private String type;
	private String address;
	private String description;
	private String method_name;
	private String resource_name;
	private String value;
	public HueErrorDetails(String type, String address, String description, String method_name, String resource_name,
			String value) {
		super();
		this.type = type;
		this.address = address;
		this.description = description;
		this.method_name = method_name;
		this.resource_name = resource_name;
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMethod_name() {
		return method_name;
	}
	public void setMethod_name(String method_name) {
		this.method_name = method_name;
	}
	public String getResource_name() {
		return resource_name;
	}
	public void setResource_name(String resource_name) {
		this.resource_name = resource_name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
