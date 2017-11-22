package com.bwssystems.HABridge.api.hue;

import java.util.Map;

import com.bwssystems.HABridge.dao.GroupDescriptor;
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
    @SerializedName("state")
    private GroupState state;

	public DeviceState getAction() {
		return action;
	}
	public void setAction(DeviceState action) {
		this.action = action;
	}

	public GroupState getState() {
		return state;
	}
	public void setState(GroupState state) {
		this.state = state;
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
	
	public static GroupResponse createDefaultGroupResponse(Map<String, DeviceResponse> deviceList) {
		String[] theList = new String[deviceList.size()];
		Boolean all_on = true;
		Boolean any_on = false;
		int i = 0;
		for (Map.Entry<String, DeviceResponse> device : deviceList.entrySet()) {
			if (Integer.parseInt(device.getKey()) >= 10000) { // don't show fake lights for other groups
				continue;
			}
		    theList[i] = device.getKey();
		    Boolean is_on = device.getValue().getState().isOn();
		    if (is_on)
		    	any_on = true;
		    else
		    	all_on = false;
		    i++;
		}
		GroupResponse theResponse = new GroupResponse();
		theResponse.setAction(DeviceState.createDeviceState(true));
		theResponse.setState(new GroupState(all_on, any_on));
		theResponse.setName("Group 0");
		theResponse.setLights(theList);
		theResponse.setType("LightGroup");
		return theResponse;
	}

	public static GroupResponse createResponse(GroupDescriptor group, Map<String, DeviceResponse> lights){
        GroupResponse response = new GroupResponse();
        Boolean all_on = true;
		Boolean any_on = false;
		String[] groupLights = null; 
		if (lights == null) {
			all_on = false;
			groupLights = group.getLights();
		} else {
			for (DeviceResponse light : lights.values()) {
		    Boolean is_on = light.getState().isOn();
		    if (is_on)
		    	any_on = true;
		    else
		    	all_on = false;
			}	

			// group.getLights() is not filtered by requester, lights is
			// we want the filtered version but keep the order from group.getLights()
			groupLights = new String[lights.size()];
			int i = 0;
			for (String light : group.getLights()) {
				if (lights.keySet().contains(light)) {
					groupLights[i] = light;
					i++;
				}
			}
		}
		
        response.setState(new GroupState(all_on, any_on));
        response.setAction(group.getAction());
        response.setName(group.getName());
        response.setType(group.getGroupType());
        response.setLights(groupLights);
        response.setClass_name(group.getGroupClass());

        return response;
    }
}
