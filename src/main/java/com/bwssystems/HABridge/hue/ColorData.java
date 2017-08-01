package com.bwssystems.HABridge.hue;

import java.util.List;

public class ColorData {
	public enum ColorMode { XY, CT, HS}

	private ColorMode mode;
	private Object data;
	
	public ColorData(ColorMode mode, Object value) {
		this.mode = mode;
		this.data = value;
	}
	
	public Object getData() {
		return data;
	}

	public ColorMode getColorMode() {
		return mode;
	}

}