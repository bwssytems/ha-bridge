package com.bwssystems.exec;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;

public class CommandHome implements Home {
	private static final Logger log = LoggerFactory.getLogger(CommandHome.class);

	public CommandHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc) {
		log.debug("Exec Request called with url: " +  anItem.getItem().toString());
		String responseString = null;
		String intermediate;
		if (anItem.getItem().toString().contains("exec://"))
			intermediate = anItem.getItem().toString().substring(anItem.getItem().toString().indexOf("://") + 3);
		else
			intermediate = anItem.getItem().toString();
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
			String anError = doExecRequest(intermediate,
					BrightnessDecode.calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), lightId);
			if (anError != null) {
				responseString = anError;
				x = aMultiUtil.getSetCount();
			}
		}
		return responseString;
	}

	private String doExecRequest(String anItem, int intensity, String lightId) {
		log.debug("Executing request: " + anItem);
		String responseString = null;
		if (anItem != null && !anItem.equalsIgnoreCase("")) {
			try {
				Process p = Runtime.getRuntime().exec(BrightnessDecode.replaceIntensityValue(anItem, intensity, false));
				log.debug("Process running: " + p.isAlive());
			} catch (IOException e) {
				log.warn("Could not execute request: " + anItem, e);
				responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
						+ "state\"}}]";
			}
		} else {
			log.warn("Could not execute request. Request is empty.");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
					+ "state\"}}]";
		}

		return responseString;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		// noop
		return this;
	}

	@Override
	public Object getItems(String type) {
		// noop
		return null;
	}

	@Override
	public void closeHome() {
		// noop
		
	}

}
