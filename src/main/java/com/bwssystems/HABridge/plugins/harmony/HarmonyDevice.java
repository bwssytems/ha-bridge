package com.bwssystems.HABridge.plugins.harmony;

import java.io.UnsupportedEncodingException;

import net.whistlingfish.harmony.config.Device;

public class HarmonyDevice {
	private Device device;
	private String hub;
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		byte ptext[];
		String theLabel = device.getLabel();
		try {
			ptext = theLabel.getBytes("ISO-8859-1");
			device.setLabel(new String(ptext, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			device.setLabel(theLabel);
		} 
		this.device = device;
	}
	public String getHub() {
		return hub;
	}
	public void setHub(String hub) {
		this.hub = hub;
	}
}
