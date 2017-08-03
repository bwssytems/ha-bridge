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

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TCPHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(TCPHome.class);
	private byte[] sendData;
	private Map<String, Socket> theSockets;
	private Gson aGsonHandler;
    

	public TCPHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		Socket dataSendSocket = null;
		log.debug("executing HUE api request to TCP: " + anItem.getItem().getAsString());
		String theUrl = anItem.getItem().getAsString();

		if(theUrl != null && !theUrl.isEmpty () && theUrl.contains("tcp://")) {
			if(!theUrl.startsWith("{\"tcpDevice\""))
				theUrl = "{\"tcpDevice\":\"" + theUrl + "\"}";
			TcpDevice theDevice = aGsonHandler.fromJson(theUrl, TcpDevice.class);
			String intermediate = theDevice.getTcpDevice().substring(theDevice.getTcpDevice().indexOf("://") + 3);
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
					return aGsonHandler.toJson(HueErrorResponse.createResponse("901", null, "Cannot connect, Unknown Host", null, "/lights/" + device.getId(), null).getTheErrors());
				}
		
				try {
					dataSendSocket = new Socket(IPAddress, Integer.parseInt(port));
					if(theDevice.isPersistent())
						theSockets.put(hostPortion, dataSendSocket);
				} catch (Exception e) {
					return aGsonHandler.toJson(HueErrorResponse.createResponse("901", null, "Cannot connect, Socket Creation issue", null, "/lights/" + device.getId(), null).getTheErrors());
				}
			}
	
			theUrlBody = TimeDecode.replaceTimeValue(theUrlBody);
			if (theUrlBody.startsWith("0x")) {
				theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody, intensity, targetBri, targetBriInc, true);
				if (colorData != null) {
					theUrlBody = ColorDecode.replaceColorData(theUrlBody, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), true);	
				}
				theUrlBody = DeviceDataDecode.replaceDeviceData(theUrlBody, device);
				sendData = DatatypeConverter.parseHexBinary(theUrlBody.substring(2));
			} else {
				theUrlBody = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody, intensity, targetBri, targetBriInc, false);
				if (colorData != null) {
					theUrlBody = ColorDecode.replaceColorData(theUrlBody, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);	
				}
				theUrlBody = DeviceDataDecode.replaceDeviceData(theUrlBody, device);
				theUrlBody = StringEscapeUtils.unescapeJava(theUrlBody);
				sendData = theUrlBody.getBytes();
			}

			try {
					DataOutputStream outToClient = new DataOutputStream(dataSendSocket.getOutputStream());
					outToClient.write(sendData);
					outToClient.flush();
			} catch (IOException e) {
				log.warn("Could not send data to TCP socket <<<" + e.getMessage() + ">>>, closing socket: " + theUrl);
				try {
					dataSendSocket.close();
				} catch (IOException e1) {
					// noop
				}
				dataSendSocket = null;
				if(theDevice.isPersistent())
					theSockets.remove(hostPortion);
				return aGsonHandler.toJson(HueErrorResponse.createResponse("901", null, "Cannot send data", null, "/lights/" + device.getId(), null).getTheErrors());
			}

			if(!theDevice.isPersistent()) {
				try {
					if(dataSendSocket != null)
						dataSendSocket.close();
				} catch (IOException e1) {
					// noop
				}
				dataSendSocket = null;
			}
		} else
			log.warn("Tcp Call to be presented as tcp://<ip_address>:<port>/payload, format of request unknown: " + theUrl);
		return null;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		log.info("TCP Home created.");
		theSockets = new HashMap<String, Socket>();
		aGsonHandler = new GsonBuilder().create();
		return this;
	}

	@Override
	public Object getItems(String type) {
		// Not a resource
		return null;
	}

	@Override
	public void closeHome() {
		log.debug("Shutting down TCP sockets.");
		if(theSockets != null && !theSockets.isEmpty()) {
			Iterator<String> keys = theSockets.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				try {
					theSockets.get(key).close();
				} catch (IOException e) {
					// noop
				}
			}
		}
	}

}
