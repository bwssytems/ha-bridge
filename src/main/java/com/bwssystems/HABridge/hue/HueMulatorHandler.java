package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.ColorData;

public interface HueMulatorHandler {
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity, Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body);
}
