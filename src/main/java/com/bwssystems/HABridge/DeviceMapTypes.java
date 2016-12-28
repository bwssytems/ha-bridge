package com.bwssystems.HABridge;

import java.util.ArrayList;

public class DeviceMapTypes {

	public final static String[] CUSTOM_DEVICE = { "custom", "Custom", "none"};
	public final static String[] VERA_DEVICE = { "veraDevice", "Vera Device", "vera"};
	public final static String[] VERA_SCENE = { "veraScene", "Vera Scene", "vera"};
	public final static String[] HARMONY_ACTIVITY = { "harmonyActivity", "Harmony Activity", "harmony"};
	public final static String[] HARMONY_BUTTON = { "harmonyButton", "Harmony Button", "harmony"};
	public final static String[] NEST_HOMEAWAY = { "nestHomeAway", "Nest Home Status", "nest"};
	public final static String[] NEST_THERMO_SET = { "nestThermoSet", "Nest Thermostat", "nest"};
	public final static String[] HUE_DEVICE = { "hueDevice", "Hue Device", "hue"};
	public final static String[] HAL_DEVICE = { "halDevice", "HAL Device", "hal"};
	public final static String[] HAL_BUTTON = { "halButton", "HAL Button", "hal"};
	public final static String[] HAL_HOME = { "halHome", "HAL Home Status", "hal"};
	public final static String[] HAL_THERMO_SET = { "halThermoSet", "HAL Thermostat", "hal"};
	public final static String[] MQTT_MESSAGE = { "mqttMessage", "MQTT Message", "mqtt"};
	public final static String[] EXEC_DEVICE = { "exec", "Execute Script/Program", "command"};
	public final static String[] CMD_DEVICE = { "cmdDevice", "Execute Command/Script/Program", "command"};
	public final static String[] HASS_DEVICE = { "hassDevice", "HomeAssistant Device", "hass"};
	public final static String[] TCP_DEVICE = { "tcpDevice", "TCP Device", "none"};
	public final static String[] TCP_DEVICE_COMPAT = { "TCP", "TCP Device", "none"};
	public final static String[] UDP_DEVICE = { "udpDevice", "UDP Device", "none"};
	public final static String[] UDP_DEVICE_COMPAT = { "UDP", "UDP Device", "none"};
	public final static String[] HTTP_DEVICE = { "httpDevice", "HTTP Device", "none"};
	public final static String[] DEFAULT_DEVICE = { "udpDevice", "Default Device", "none"};

	public final static int typeIndex = 0;
	public final static int displayIndex = 1;
	public final static int resourceIndex = 1;

	ArrayList<String[]> deviceMapTypes;
	
	public DeviceMapTypes() {
		super();
		deviceMapTypes = new ArrayList<String[]>();
		deviceMapTypes.add(CMD_DEVICE);
		deviceMapTypes.add(DEFAULT_DEVICE);
		deviceMapTypes.add(HAL_DEVICE);
		deviceMapTypes.add(HAL_HOME);
		deviceMapTypes.add(HAL_THERMO_SET);
		deviceMapTypes.add(HAL_BUTTON);
		deviceMapTypes.add(HASS_DEVICE);
		deviceMapTypes.add(HTTP_DEVICE);
		deviceMapTypes.add(HUE_DEVICE);
		deviceMapTypes.add(MQTT_MESSAGE);
		deviceMapTypes.add(NEST_HOMEAWAY);
		deviceMapTypes.add(NEST_THERMO_SET);
		deviceMapTypes.add(TCP_DEVICE);
		deviceMapTypes.add(UDP_DEVICE);
		deviceMapTypes.add(VERA_DEVICE);
		deviceMapTypes.add(VERA_SCENE);
		deviceMapTypes.add(HARMONY_ACTIVITY);
		deviceMapTypes.add(HARMONY_BUTTON);
	}
	public static int getTypeIndex() {
		return typeIndex;
	}
	public static int getDisplayIndex() {
		return displayIndex;
	}
	public static int getResourceindex() {
		return resourceIndex;
	}
	public ArrayList<String[]> getDeviceMapTypes() {
		return deviceMapTypes;
	}
}