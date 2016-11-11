package com.bwssystems.harmony;

public class ButtonPress {
	private String device;
	private String button;
	private Integer delay;
	private Integer count;
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
	public Integer getDelay() {
		return delay;
	}
	public void setDelay(Integer delay) {
		this.delay = delay;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Boolean isValid() {
		if (device != null && !device.isEmpty()){
			if (button != null && !button.isEmpty())
				return true;
		}
		return false;
	}
}
