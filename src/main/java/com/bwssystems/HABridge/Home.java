package com.bwssystems.HABridge;

import com.bwssystems.HABridge.hue.HueMulatorHandler;

public interface Home extends HueMulatorHandler {
	public Home createHome(BridgeSettingsDescriptor bridgeSettings);
}
