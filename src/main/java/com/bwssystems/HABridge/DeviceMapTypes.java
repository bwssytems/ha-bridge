package com.bwssystems.HABridge;

import java.util.ArrayList;

public class DeviceMapTypes {

	public final static String[] CUSTOM_DEVICE = { "custom", "Custom"};
	public final static String[] VERA_DEVICE = { "veraDevice", "Vera Device"};
	public final static String[] VERA_SCENE = { "veraScene", "Vera Scene"};
	public final static String[] HARMONY_ACTIVITY = { "harmonyActivity", "Harmony Activity"};
	public final static String[] HARMONY_BUTTON = { "harmonyButton", "Harmony Button"};
	public final static String[] NEST_HOMEAWAY = { "nestHomeAway", "Nest Home Status"};
	public final static String[] NEST_THERMO_SET = { "nestThermoSet", "Nest Thermostat"};
	public final static String[] HUE_DEVICE = { "hueDevice", "Hue Device"};
	public final static String[] HAL_DEVICE = { "halDevice", "HAL Device"};
	public final static String[] HAL_BUTTON = { "halButton", "HAL Button"};
	public final static String[] HAL_HOME = { "halHome", "HAL Home Status"};
	public final static String[] HAL_THERMO_SET = { "halThermoSet", "HAL Thermostat"};
	public final static String[] MQTT_MESSAGE = { "mqttMessage", "MQTT Message"};
	public final static String[] EXEC_DEVICE_COMPAT = { "exec", "Execute Script/Program"};
	public final static String[] CMD_DEVICE = { "cmdDevice", "Execute Command/Script/Program"};
	public final static String[] HASS_DEVICE = { "hassDevice", "HomeAssistant Device"};
	public final static String[] TCP_DEVICE = { "tcpDevice", "TCP Device"};
	public final static String[] TCP_DEVICE_COMPAT = { "TCP", "TCP Device"};
	public final static String[] UDP_DEVICE = { "udpDevice", "UDP Device"};
	public final static String[] UDP_DEVICE_COMPAT = { "UDP", "UDP Device"};
	public final static String[] HTTP_DEVICE = { "httpDevice", "HTTP Device"};
	public final static String[] DOMOTICZ_DEVICE = { "domoticzDevice", "Domoticz Device"};
	public final static String[] SOMFY_DEVICE = { "somfyDevice", "Somfy Device"};
	public final static String[] LIFX_DEVICE = { "lifxDevice", "LIFX Device"};

	public final static int typeIndex = 0;
	public final static int displayIndex = 1;

	ArrayList<String[]> deviceMapTypes;
	
	public DeviceMapTypes() {
		super();
		deviceMapTypes = new ArrayList<String[]>();
		deviceMapTypes.add(CMD_DEVICE);
		deviceMapTypes.add(DOMOTICZ_DEVICE);
		deviceMapTypes.add(HAL_DEVICE);
		deviceMapTypes.add(HAL_HOME);
		deviceMapTypes.add(HAL_THERMO_SET);
		deviceMapTypes.add(HAL_BUTTON);
		deviceMapTypes.add(HARMONY_ACTIVITY);
		deviceMapTypes.add(HARMONY_BUTTON);
		deviceMapTypes.add(HASS_DEVICE);
		deviceMapTypes.add(HTTP_DEVICE);
		deviceMapTypes.add(HUE_DEVICE);
		deviceMapTypes.add(LIFX_DEVICE);
		deviceMapTypes.add(MQTT_MESSAGE);
		deviceMapTypes.add(NEST_HOMEAWAY);
		deviceMapTypes.add(NEST_THERMO_SET);
		deviceMapTypes.add(SOMFY_DEVICE);
		deviceMapTypes.add(TCP_DEVICE);
		deviceMapTypes.add(UDP_DEVICE);
		deviceMapTypes.add(VERA_DEVICE);
		deviceMapTypes.add(VERA_SCENE);
		deviceMapTypes.add(SOMFY_DEVICE);
	}
	public static int getTypeIndex() {
		return typeIndex;
	}
	public static int getDisplayIndex() {
		return displayIndex;
	}
	public ArrayList<String[]> getDeviceMapTypes() {
		return deviceMapTypes;
	}
	
	public Boolean validateType(String type) {
		if(type == null || type.trim().isEmpty())
			return false;
		for(String[] mapType : deviceMapTypes) {
			if(type.trim().contentEquals(mapType[typeIndex]))
				return true;
		}
		if(type.trim().contentEquals(EXEC_DEVICE_COMPAT[typeIndex]))
			return true;
		if(type.trim().contentEquals(TCP_DEVICE_COMPAT[typeIndex]))
			return true;
		if(type.trim().contentEquals(UDP_DEVICE_COMPAT[typeIndex]))
			return true;
		return false;
	}
}