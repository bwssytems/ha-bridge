package com.bwssystems.HABridge.plugins.hal;

import com.bwssystems.HABridge.NamedIP;

public class HalDevice {
	private String haldevicetype;
	private String haldevicename;
	private NamedIP haladdress;
	private DeviceElements buttons;
	public String getHaldevicetype() {
		return haldevicetype;
	}
	public void setHaldevicetype(String haldevicetype) {
		this.haldevicetype = haldevicetype;
	}
	public String getHaldevicename() {
		return haldevicename;
	}
	public void setHaldevicename(String haldevicename) {
		this.haldevicename = haldevicename;
	}
	public NamedIP getHaladdress() {
		return haladdress;
	}
	public void setHaladdress(NamedIP haladdress) {
		this.haladdress = haladdress;
	}
	public DeviceElements getButtons() {
		return buttons;
	}
	public void setButtons(DeviceElements buttons) {
		this.buttons = buttons;
	}
}
