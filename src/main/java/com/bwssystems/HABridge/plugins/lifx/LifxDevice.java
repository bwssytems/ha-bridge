package com.bwssystems.HABridge.plugins.lifx;

import com.github.besherman.lifx.LFXGroup;
import com.github.besherman.lifx.LFXLight;

public class LifxDevice {
	private Object lifxObject;
	private String type;
	public final static String LIGHT_TYPE = "Light";
	public final static String GROUP_TYPE = "Group";
	
	public LifxDevice(Object lifxObject, String type) {
		super();
		this.lifxObject = lifxObject;
		this.type = type;
	}

	public LifxEntry toEntry() {
		LifxEntry anEntry = null;
		if(type.equals(LIGHT_TYPE)) {
			anEntry = new LifxEntry();
			anEntry.setId(((LFXLight)lifxObject).getID());
			anEntry.setName(((LFXLight)lifxObject).getLabel());
			anEntry.setType(LIGHT_TYPE);
		}
		if(type.equals(GROUP_TYPE)) {
			anEntry = new LifxEntry();
			anEntry.setId("na");
			anEntry.setName(((LFXGroup)lifxObject).getLabel());
			anEntry.setType(GROUP_TYPE);
		}
		return anEntry;
	}
	public Object getLifxObject() {
		return lifxObject;
	}

	public void setLifxObject(Object lifxObject) {
		this.lifxObject = lifxObject;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}