package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;

public interface HueMulatorHandler {
	public String deviceHandler(CallItem anItem, MultiCommandUtil multiComand, String lightId, int iterationCount, DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc);
}
