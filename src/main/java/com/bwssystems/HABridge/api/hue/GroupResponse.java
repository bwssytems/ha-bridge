package com.bwssystems.HABridge.api.hue;

import java.util.List;

import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.google.gson.annotations.SerializedName;

public class GroupResponse {
    @SerializedName("action")
	private DeviceState action;
    @SerializedName("lights")
	private String[] lights;
    @SerializedName("name")
	private String name;
    @SerializedName("type")
	private String type;
    @SerializedName("class")
    String class_name;

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
	
	 public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClass_name() {
		return class_name;
	}
	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}
	public static GroupResponse createDefaultGroupResponse(List<DeviceDescriptor> deviceList) {
	        String[] theList = new String[deviceList.size()];
	        int i = 0;
	        for (DeviceDescriptor device : deviceList) {
             theList[i] = device.getId();
             i++;
	        }
		 GroupResponse theResponse = new GroupResponse();
		 theResponse.setAction(DeviceState.createDeviceState());
		 theResponse.setName("Lightset 0");
		 theResponse.setLights(theList);
		 theResponse.setType("LightGroup");
		 return theResponse;
	 }
	public static GroupResponse createOtherGroupResponse(List<DeviceDescriptor> deviceList) {
        String[] theList = new String[deviceList.size()];
        int i = 0;
        for (DeviceDescriptor device : deviceList) {
         theList[i] = device.getId();
         i++;
        }
	 GroupResponse theResponse = new GroupResponse();
	 theResponse.setAction(DeviceState.createDeviceState());
	 theResponse.setName("AGroup");
	 theResponse.setLights(theList);
	 theResponse.setType("Room");
	 theResponse.setClass_name("Other");

	 return theResponse;
 }
}
