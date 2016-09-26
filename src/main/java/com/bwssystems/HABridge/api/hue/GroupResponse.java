package com.bwssystems.HABridge.api.hue;

import java.util.List;

import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.dao.DeviceRepository;

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
	
	 public static GroupResponse createGroupResponse(List<DeviceDescriptor> deviceList) {
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
		 return theResponse;
	 }
}
