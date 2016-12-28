package com.bwssystems.HABridge.plugins.NestBridge;

public class NestInstruction {
	private String name;
	private Boolean away;
	private String control;
	private String temp;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getAway() {
		return away;
	}
	public void setAway(Boolean away) {
		this.away = away;
	}
	public String getControl() {
		return control;
	}
	public void setControl(String control) {
		this.control = control;
	}
	public String getTemp() {
		return temp;
	}
	public void setTemp(String temp) {
		this.temp = temp;
	}
}
