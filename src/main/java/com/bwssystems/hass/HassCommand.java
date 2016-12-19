package com.bwssystems.hass;

public class HassCommand {
	private String entityId;
	private String hassName;
	private String state;
	private Integer bri;
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getHassName() {
		return hassName;
	}
	public void setHassName(String hassName) {
		this.hassName = hassName;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Integer getBri() {
		return bri;
	}
	public void setBri(Integer bri) {
		this.bri = bri;
	}
	
}
