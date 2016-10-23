package com.bwssystems.HABridge.api.hue;

public class GroupResponse {
	private DeviceState action;
	private String[] lights;
	private String name;
	public DeviceState getAction() {
		return action;
	}
	public void setAction(DeviceState action) {
		this.action = action;
	}
	public String[] getLights() {
		return lights;
	}
	public void setLights(String[] lights) {
		this.lights = lights;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	 public static GroupResponse createGroupResponse(String[] theLights) {
		 GroupResponse theResponse = new GroupResponse();
		 theResponse.setAction(DeviceState.createDeviceState());
		 theResponse.setName("Lightset 0");
		 theResponse.setLights(theLights);
		 return theResponse;
	 }
}
