package com.bwssystems.hue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.http.HTTPHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HueHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HueHome.class);
	private Map<String, HueInfo> hues;
	private String theHUERegisteredUser;
	private Boolean validHue;
	private Gson aGsonHandler;
	private HTTPHandler anHttpHandler;
	
	public HueHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for hues");
		Iterator<String> keys = hues.keySet().iterator();
		ArrayList<HueDevice> deviceList = new ArrayList<HueDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			HueApiResponse theResponse = hues.get(key).getHueApiResponse();
			if(theResponse != null) {
				Map<String, DeviceResponse> theDevices = theResponse.getLights();
				if(theDevices != null) {
					Iterator<String> deviceKeys = theDevices.keySet().iterator();
					while(deviceKeys.hasNext()) {
						String theDeviceKey = deviceKeys.next();
						HueDevice aNewHueDevice = new HueDevice();
						aNewHueDevice.setDevice(theDevices.get(theDeviceKey));
						aNewHueDevice.setHuedeviceid(theDeviceKey);
						aNewHueDevice.setHueaddress(hues.get(key).getHueAddress().getIp());
						aNewHueDevice.setHuename(key);
						deviceList.add(aNewHueDevice);
					}
				}
				else {
					deviceList = null;
					break;
				}
			}
			else
				log.warn("Cannot get lights for Hue with name: " + key);
		}
		return deviceList;
	}

	public String getTheHUERegisteredUser() {
		return theHUERegisteredUser;
	}

	public void setTheHUERegisteredUser(String theHUERegisteredUser) {
		this.theHUERegisteredUser = theHUERegisteredUser;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc, DeviceDescriptor device, String body) {
		String responseString = null;
		String hueUser;
		HueDeviceIdentifier deviceId = aGsonHandler.fromJson(anItem.getItem(), HueDeviceIdentifier.class);
		if (getTheHUERegisteredUser() == null) {
			hueUser = HueUtil.registerWithHue(anHttpHandler, deviceId.getIpAddress(), device.getName(),
					getTheHUERegisteredUser());
			if (hueUser == null) {
				return responseString;
			}
			setTheHUERegisteredUser(hueUser);
		}

		// make call
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
			responseString = anHttpHandler.doHttpRequest(
					"http://" + deviceId.getIpAddress() + "/api/" + getTheHUERegisteredUser()
							+ "/lights/" + deviceId.getDeviceId() + "/state",
					HttpPut.METHOD_NAME, "application/json", body, null);
			if (responseString.contains("[{\"error\":"))
					x = aMultiUtil.getSetCount();
		}
		if (responseString == null) {
			log.warn("Error on calling Hue passthru to change device state: " + device.getName());
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Error on calling HUE to change device state\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";
		} else if (responseString.contains("[{\"error\":")) {
			if(responseString.contains("unauthorized user")) {
				setTheHUERegisteredUser(null);
				hueUser = HueUtil.registerWithHue(anHttpHandler, deviceId.getIpAddress(), device.getName(),
						getTheHUERegisteredUser());
				if (hueUser == null) {
					return responseString;
				}
				setTheHUERegisteredUser(hueUser);
			}
			else
				log.warn("Error occurred when calling Hue Passthru: " + responseString);
		}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		validHue = bridgeSettings.isValidHue();
		if(!validHue) {
			log.debug("No Hue Configuration");
		} else {
			hues = new HashMap<String, HueInfo>();
			Iterator<NamedIP> theList = bridgeSettings.getHueaddress().getDevices().iterator();
			while(theList.hasNext()) {
				NamedIP aHue = theList.next();
	      		hues.put(aHue.getName(), new HueInfo(aHue, this));
			}
			theHUERegisteredUser = null;
			aGsonHandler =
					new GsonBuilder()
			//	.registerTypeAdapter(CallItem.class, new CallItemDeserializer())
			.create();
		}
		return this;
	}

	@Override
	public void closeHome() {
		anHttpHandler.closeHandler();
	}
}
