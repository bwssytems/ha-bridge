package com.bwssystems.HABridge.plugins.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.util.UDPDatagramSender;

public class UDPHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(UDPHome.class);
    private UDPDatagramSender theUDPDatagramSender;
	private byte[] sendData;
    
	public UDPHome(BridgeSettingsDescriptor bridgeSettings, UDPDatagramSender aUDPDatagramSender) {
		super();
		theUDPDatagramSender = aUDPDatagramSender;
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc, DeviceDescriptor device, String body) {
		log.debug("executing HUE api request to UDP: " + anItem.getItem().toString());
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
			String intermediate = anItem.getItem().getAsString()
					.substring(anItem.getItem().getAsString().indexOf("://") + 3);
			String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
			String theUrlBody = intermediate.substring(intermediate.indexOf('/') + 1);
			String hostAddr = null;
			String port = null;
			InetAddress IPAddress = null;
			if (hostPortion.contains(":")) {
				hostAddr = hostPortion.substring(0, intermediate.indexOf(':'));
				port = hostPortion.substring(intermediate.indexOf(':') + 1);
			} else
				hostAddr = hostPortion;
			try {
				IPAddress = InetAddress.getByName(hostAddr);
			} catch (UnknownHostException e) {
				// noop
			}

			if (theUrlBody.startsWith("0x")) {
				theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody,
						state, theStateChanges, stateHasBri, stateHasBriInc, true);
				sendData = DatatypeConverter.parseHexBinary(theUrlBody.substring(2));
			} else {
				theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody,
						state, theStateChanges, stateHasBri, stateHasBriInc, false);
				sendData = theUrlBody.getBytes();
			}
			try {
				theUDPDatagramSender.sendUDPResponse(sendData, IPAddress,
						Integer.parseInt(port));
			} catch (NumberFormatException e) {
				// noop
			} catch (IOException e) {
				// noop
			}
		}
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getItems(String type) {
		// Not a resource
		return null;
	}

	@Override
	public void closeHome() {
		// TODO Auto-generated method stub
		
	}

}
