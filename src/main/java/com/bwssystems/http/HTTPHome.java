package com.bwssystems.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.google.gson.Gson;

public class HTTPHome implements Home {
	private static final Logger log = LoggerFactory.getLogger(HTTPHome.class);
	private HTTPHandler anHttpHandler;

	public HTTPHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc, DeviceDescriptor device, String body) {
		String responseString = null;
		log.debug("executing HUE api request to Http "
				+ (anItem.getHttpVerb() == null ? "GET" : anItem.getHttpVerb()) + ": "
				+ anItem.getItem().getAsString());

		for (int x = 0; x < aMultiUtil.getSetCount(); x++) {
			if (x > 0 || iterationCount > 0) {
				try {
					Thread.sleep(aMultiUtil.getTheDelay());
				} catch (InterruptedException e) {
					// ignore
				}
			}
			if (anItem.getDelay() != null && anItem.getDelay() > 0)
				aMultiUtil.setTheDelay(anItem.getDelay());
			else
				aMultiUtil.setTheDelay(aMultiUtil.getDelayDefault());
			String anUrl = BrightnessDecode.calculateReplaceIntensityValue(anItem.getItem().getAsString(),
					state, theStateChanges, stateHasBri, stateHasBriInc, false);
			String aBody;
			aBody = BrightnessDecode.calculateReplaceIntensityValue(anItem.getHttpBody(),
					state, theStateChanges, stateHasBri, stateHasBriInc,
					false);
			// make call
			if (anHttpHandler.doHttpRequest(anUrl, anItem.getHttpVerb(), anItem.getContentType(), aBody,
					new Gson().fromJson(anItem.getHttpHeaders(), NameValue[].class)) == null) {
				log.warn("Error on calling url to change device state: " + anUrl);
				responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Error on calling url to change device state\", \"parameter\": \"/lights/"
						+ lightId + "state\"}}]";
				x = aMultiUtil.getSetCount();
			}
		}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		anHttpHandler = new HTTPHandler();
		return this;
	}

	@Override
	public Object getItems(String type) {
		// Not a resource
		return null;
	}

	@Override
	public void closeHome() {
		anHttpHandler.closeHandler();
	}

}
