package com.bwssystems.HABridge;

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
	public final static String[] EXEC_DEVICE = { "execDevice", "Execute Script/Program"};
	public final static String[] HASS_DEVICE = { "hassDevice", "HomeAssistant Device"};

	public final static int typeIndex = 0;
	public final static int displayIndex = 1;

	public String[] customDevice;
	public String[] veraDevice;
	public String[] veraScene;
	public String[] harmonyActivity;
	public String[] harmonyButton;
	public String[] nestHomeAway;
	public String[] nestThermoSet;
	public String[] hueDevice;
	public String[] halDevice;
	public String[] halButton;
	public String[] halHome;
	public String[] halThermoSet;
	public String[] mqttMessage;
	public String[] execDevice;
	public String[] hassDevice;

	public int typeindex;
	public int displayindex;
	
	
	public DeviceMapTypes() {
		super();
		this.setCustomDevice(CUSTOM_DEVICE);
		this.setDisplayindex(displayIndex);
		this.setExecDevice(EXEC_DEVICE);
		this.setHalButton(HAL_BUTTON);
		this.setHalDevice(HAL_DEVICE);
		this.setHalHome(HAL_HOME);
		this.setHalThermoSet(HAL_THERMO_SET);
		this.setHarmonyActivity(HARMONY_ACTIVITY);
		this.setHarmonyButton(HARMONY_BUTTON);
		this.setHueDevice(HUE_DEVICE);
		this.setMqttMessage(MQTT_MESSAGE);
		this.setNestHomeAway(NEST_HOMEAWAY);
		this.setNestThermoSet(NEST_THERMO_SET);
		this.setTypeindex(typeIndex);
		this.setVeraDevice(VERA_DEVICE);
		this.setVeraScene(VERA_SCENE);
		this.setHassDevice(HASS_DEVICE);
	}
	public String[] getCustomDevice() {
		return customDevice;
	}
	public void setCustomDevice(String[] customDevice) {
		this.customDevice = customDevice;
	}
	public String[] getVeraDevice() {
		return veraDevice;
	}
	public void setVeraDevice(String[] veraDevice) {
		this.veraDevice = veraDevice;
	}
	public String[] getVeraScene() {
		return veraScene;
	}
	public void setVeraScene(String[] veraScene) {
		this.veraScene = veraScene;
	}
	public String[] getHarmonyActivity() {
		return harmonyActivity;
	}
	public void setHarmonyActivity(String[] harmonyActivity) {
		this.harmonyActivity = harmonyActivity;
	}
	public String[] getHarmonyButton() {
		return harmonyButton;
	}
	public void setHarmonyButton(String[] harmonyButton) {
		this.harmonyButton = harmonyButton;
	}
	public String[] getNestHomeAway() {
		return nestHomeAway;
	}
	public void setNestHomeAway(String[] nestHomeAway) {
		this.nestHomeAway = nestHomeAway;
	}
	public String[] getNestThermoSet() {
		return nestThermoSet;
	}
	public void setNestThermoSet(String[] nestThermoSet) {
		this.nestThermoSet = nestThermoSet;
	}
	public String[] getHueDevice() {
		return hueDevice;
	}
	public void setHueDevice(String[] hueDevice) {
		this.hueDevice = hueDevice;
	}
	public String[] getHalDevice() {
		return halDevice;
	}
	public void setHalDevice(String[] halDevice) {
		this.halDevice = halDevice;
	}
	public String[] getHalButton() {
		return halButton;
	}
	public void setHalButton(String[] halButton) {
		this.halButton = halButton;
	}
	public String[] getHalHome() {
		return halHome;
	}
	public void setHalHome(String[] halHome) {
		this.halHome = halHome;
	}
	public String[] getHalThermoSet() {
		return halThermoSet;
	}
	public void setHalThermoSet(String[] halThermoSet) {
		this.halThermoSet = halThermoSet;
	}
	public String[] getMqttMessage() {
		return mqttMessage;
	}
	public void setMqttMessage(String[] mqttMessage) {
		this.mqttMessage = mqttMessage;
	}
	public String[] getExecDevice() {
		return execDevice;
	}
	public void setExecDevice(String[] execDevice) {
		this.execDevice = execDevice;
	}
	public String[] getHassDevice() {
		return hassDevice;
	}
	public void setHassDevice(String[] hassDevice) {
		this.hassDevice = hassDevice;
	}
	public int getTypeindex() {
		return typeindex;
	}
	public void setTypeindex(int typeindex) {
		this.typeindex = typeindex;
	}
	public int getDisplayindex() {
		return displayindex;
	}
	public void setDisplayindex(int displayindex) {
		this.displayindex = displayindex;
	}

}