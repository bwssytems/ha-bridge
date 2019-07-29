package com.bwssystems.HABridge.plugins.exec;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;

public class CommandHome implements Home {
	private static final Logger log = LoggerFactory.getLogger(CommandHome.class);
	private BridgeSettings theSettings;
	private boolean closed;

	public CommandHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity, Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String theItem = anItem.getItem().getAsString().replaceAll("^\"|\"$", "");
		log.debug("Exec Request called with url: {} and exec Garden: {}", theItem, (theSettings.getBridgeSecurity().getExecGarden() == null ? "not given" : theSettings.getBridgeSecurity().getExecGarden()));
		String responseString = null;
		String intermediate;
		if (theItem.contains("exec://"))
			intermediate = theItem.substring(anItem.getItem().getAsString().indexOf("://") + 3);
		else
			intermediate = theItem;
		intermediate = BrightnessDecode.calculateReplaceIntensityValue(intermediate, intensity, targetBri, targetBriInc, false);
		if (colorData != null) {
			intermediate = ColorDecode.replaceColorData(intermediate, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);	
		}
		intermediate = DeviceDataDecode.replaceDeviceData(intermediate, device);
		intermediate = TimeDecode.replaceTimeValue(intermediate);
		String execGarden = theSettings.getBridgeSecurity().getExecGarden();
		if(execGarden != null && !execGarden.trim().isEmpty()) {
			intermediate = new File(execGarden.trim(), intermediate).getAbsolutePath();
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
						+ "/state\"}}]";
			}
		} else {
			log.warn("Could not execute request. Request is empty.");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
					+ "/state\"}}]";
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
	public void refresh() {
		// noop		
	}
	
	@Override
	public void closeHome() {
		log.debug("Closing Home.");
		if(closed) {
			log.debug("Home is already closed....");
			return;
		}
		
		closed = true;
	}

}
