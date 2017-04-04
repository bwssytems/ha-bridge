package com.bwssystems.HABridge.plugins.exec;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;

public class CommandHome implements Home {
	private static final Logger log = LoggerFactory.getLogger(CommandHome.class);
	private BridgeSettings theSettings;

	public CommandHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int itensity, Integer targetBri, Integer targetBriInc, DeviceDescriptor device, String body) {
		log.debug("Exec Request called with url: " +  anItem.getItem().getAsString() + " and exec Garden: "  + (theSettings.getBridgeSecurity().getExecGarden() == null ? "not given" : theSettings.getBridgeSecurity().getExecGarden()));
		String responseString = null;
		String intermediate;
		if (anItem.getItem().getAsString().contains("exec://"))
			intermediate = anItem.getItem().getAsString().substring(anItem.getItem().getAsString().indexOf("://") + 3);
		else
			intermediate = anItem.getItem().getAsString();
		intermediate = BrightnessDecode.calculateReplaceIntensityValue(intermediate, itensity, targetBri, targetBriInc, false);
		intermediate = DeviceDataDecode.replaceDeviceData(intermediate, device);
		intermediate = TimeDecode.replaceTimeValue(intermediate);
		String execGarden = theSettings.getBridgeSecurity().getExecGarden();
		if(execGarden != null && !execGarden.trim().isEmpty()) {
			if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
				intermediate = execGarden + "\\" + intermediate;
			else
				intermediate = execGarden + "/" + intermediate;
		}

		String anError = doExecRequest(intermediate, lightId);
		if (anError != null) {
			responseString = anError;
		}
		return responseString;
	}

	private String doExecRequest(String anItem, String lightId) {
		log.debug("Executing request: " + anItem);
		String responseString = null;
		if (anItem != null && !anItem.equalsIgnoreCase("")) {
			try {
				Process p = Runtime.getRuntime().exec(anItem);
				log.debug("Process running: " + p.isAlive());
			} catch (IOException e) {
				log.warn("Could not execute request: " + anItem + " with message: " + e.getMessage());
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
	public Home createHome(BridgeSettings bridgeSettings) {
		log.info("Command Home for system program execution created.");
		this.theSettings = bridgeSettings; 
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
