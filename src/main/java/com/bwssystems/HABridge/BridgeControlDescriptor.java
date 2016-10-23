package com.bwssystems.HABridge;

public class BridgeControlDescriptor {
	private boolean reinit;
	private boolean stop;

	public BridgeControlDescriptor() {
		super();
		this.reinit = false;
		this.stop = false;
	}

	public boolean isReinit() {
		return reinit;
	}
	public void setReinit(boolean reinit) {
		this.reinit = reinit;
	}
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
}
