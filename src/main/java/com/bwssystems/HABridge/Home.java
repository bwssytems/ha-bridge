package com.bwssystems.HABridge;

import com.bwssystems.HABridge.devicemanagmeent.ResourceHandler;
import com.bwssystems.HABridge.hue.HueMulatorHandler;

public interface Home extends HueMulatorHandler, ResourceHandler {
	public Home createHome(BridgeSettings bridgeSettings);
	public void closeHome();
}
