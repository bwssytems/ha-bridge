package com.bwssystems.HABridge.plugins.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;

public class TCPHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(TCPHome.class);
	private byte[] sendData;
	private Map<String, Socket> theSockets;
    

	public TCPHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, DeviceDescriptor device, String body) {
		Socket dataSendSocket = null;
		log.debug("executing HUE api request to TCP: " + anItem.getItem().getAsString());
		String theUrl = anItem.getItem().getAsString();
		if(theUrl != null && !theUrl.isEmpty () && theUrl.startsWith("tcp://")) {
			String intermediate = theUrl.substring(theUrl.indexOf("://") + 3);
			String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
			String theUrlBody = intermediate.substring(intermediate.indexOf('/') + 1);
			String hostAddr = null;
			String port = null;
			InetAddress IPAddress = null;
			dataSendSocket = theSockets.get(hostPortion);
			if(dataSendSocket == null) {
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
		
				try {
					dataSendSocket = new Socket(IPAddress, Integer.parseInt(port));
					theSockets.put(hostPortion, dataSendSocket);
				} catch (Exception e) {
					// noop
				}
			}
	
			theUrlBody = TimeDecode.replaceTimeValue(theUrlBody);
			if (theUrlBody.startsWith("0x")) {
				theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody, intensity, targetBri, targetBriInc, true);
				sendData = DatatypeConverter.parseHexBinary(theUrlBody.substring(2));
			} else {
				theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody, intensity, targetBri, targetBriInc, false);
				theUrlBody = StringEscapeUtils.unescapeJava(theUrlBody);
				sendData = theUrlBody.getBytes();
			}
			try {
				DataOutputStream outToClient = new DataOutputStream(dataSendSocket.getOutputStream());
				outToClient.write(sendData);
				outToClient.flush();
			} catch (Exception e) {
				// noop
			}
		} else
			log.warn("Tcp Call to be presented as tcp://<ip_address>:<port>/payload, format of request unknown: " + theUrl);
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		log.info("TCP Home created.");
		theSockets = new HashMap<String, Socket>();
		return this;
	}

	@Override
	public Object getItems(String type) {
		// Not a resource
		return null;
	}

	@Override
	public void closeHome() {
		Iterator<?> anIterator = theSockets.entrySet().iterator();
		while(anIterator.hasNext()) {
			Socket aSocket = (Socket) anIterator.next();
			try {
				aSocket.close();
			} catch (IOException e) {
				// noop
			}
		}
	}

}
