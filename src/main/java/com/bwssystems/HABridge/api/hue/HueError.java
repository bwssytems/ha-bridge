package com.bwssystems.HABridge.api.hue;

public class HueError {
	
	private HueErrorDetails error;

	public HueError(HueErrorDetails error) {
		super();
		this.error = error;
	}

	public HueErrorDetails getError() {
		return error;
	}

	public void setError(HueErrorDetails error) {
		this.error = error;
	}
}
