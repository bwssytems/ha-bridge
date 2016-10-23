package com.bwssystems.harmony;

public class ButtonPress {
	private String device;
	private String button;
	private int pressHoldTime=200;

	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getButton() {
		return button;
	}
	public void setButton(String button) {
		this.button = button;
	}
	public Boolean isValid() {
		if (device != null && !device.isEmpty()){
			if (button != null && !button.isEmpty())
				return true;
		}
		return false;
	}

	public int getPressHoldTime() {
		return pressHoldTime;
	}

	public void setPressHoldTime(int pressHoldTime) {
		this.pressHoldTime = pressHoldTime;
	}
}
