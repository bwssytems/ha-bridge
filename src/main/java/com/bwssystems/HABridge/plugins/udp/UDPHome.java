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
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
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
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, DeviceDescriptor device, String body) {
		log.debug("executing HUE api request to UDP: " + anItem.getItem().getAsString());
		String intermediate = anItem.getItem().getAsString().substring(anItem.getItem().getAsString().indexOf("://") + 3);
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
			log.warn("Udp Call, unknown host, continuing...");
			return null;
		}

		theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody, intensity, targetBri, targetBriInc, true);
		theUrlBody = TimeDecode.replaceTimeValue(theUrlBody);
		if (theUrlBody.startsWith("0x")) {
			sendData = DatatypeConverter.parseHexBinary(theUrlBody.substring(2));
		} else {
			sendData = theUrlBody.getBytes();
		}
		try {
			theUDPDatagramSender.sendUDPResponse(sendData, IPAddress, Integer.parseInt(port));
		} catch (NumberFormatException e) {
			log.warn("Udp Call, Number format exception on port, continuing...");
		} catch (IOException e) {
			log.warn("IO exception on udp call, continuing...");
		}
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		log.info("UDP Home created.");
		return this;
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
