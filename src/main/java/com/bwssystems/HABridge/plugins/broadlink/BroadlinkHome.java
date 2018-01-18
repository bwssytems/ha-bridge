package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Configuration;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.MP1Device;
import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.SP1Device;
import com.github.mob41.blapi.SP2Device;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;
import com.google.gson.Gson;

public class BroadlinkHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(BroadlinkHome.class);
    private static final String _a1 = "A1";
    private static final String _mp1 = "MP1";
    private static final String _sp1 = "SP1";
    private static final String _sp2 = "SP2";
    private static final String _rm2 = "RM2";
	private Map<String, BLDevice> broadlinkMap;
	private Boolean validBroadlink;
	private Gson aGsonHandler;
	private boolean closed;
	private Boolean isDevMode;
	
	public BroadlinkHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		broadlinkMap = null;
		aGsonHandler = null;
		BLDevice[] clients;

        isDevMode = Boolean.parseBoolean(System.getProperty("dev.mode", "false"));
		validBroadlink = bridgeSettings.getBridgeSettingsDescriptor().isValidBroadlink();

		log.info("Broadlink Home created." + (validBroadlink ? "" : " No Broadlinks configured.") + (isDevMode ? " DevMode is set." : ""));
		if(validBroadlink) {
			broadlinkMap = new HashMap<String, BLDevice>();
	    	try {
	    		log.info("Broadlink discover....");
				if(isDevMode) {
					clients = TestBLDevice.discoverDevices(InetAddress.getByName(bridgeSettings.getBridgeSettingsDescriptor().getUpnpConfigAddress()), Configuration.BROADLINK_DISCOVER_PORT, Configuration.BROADLINK_DISCONVER_TIMEOUT);
				}
				else
					clients = BLDevice.discoverDevices(InetAddress.getByName(bridgeSettings.getBridgeSettingsDescriptor().getUpnpConfigAddress()), Configuration.BROADLINK_DISCOVER_PORT, Configuration.BROADLINK_DISCONVER_TIMEOUT);
				if(clients.length <= 0) {
					log.warn("Did not discover any Broadlinks, try again with bridge reinitialization");
					broadlinkMap = null;
					validBroadlink = false;
					return this;
				}
	    		for(int i = 0; i < clients.length; i++) {
	    			if(clients[i].getDeviceType() != BLDevice.DEV_A1)
	    				broadlinkMap.put(clients[i].getHost() + "-" + String.format("%04x", clients[i].getDeviceType()), clients[i]);
	    		}
			} catch (IOException e) {
				log.warn("Could not discover Broadlinks, with IO Exception", e);
				broadlinkMap = null;
				validBroadlink = false;
				return this;
			}
	    }
		return this;
	}

	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for Broadlink");
		if(!validBroadlink)
			return null;
		BroadlinkEntry theResponse = null;
		Iterator<String> keys = broadlinkMap.keySet().iterator();
		List<BroadlinkEntry> deviceList = new ArrayList<BroadlinkEntry>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = toEntry(broadlinkMap.get(key));
			if(theResponse != null)
				deviceList.add(theResponse);
			else {
				log.warn("Cannot get BroadlinkDevice with name: " + key + ", skipping this Broadlink.");
				continue;
			}
		}
		return deviceList;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String theReturn = null;
    	boolean changeState = false;
    	String theStringData = null;
		log.debug("executing HUE api request to send message to BroadlinkDevice: " + anItem.getItem().toString());
		if(!validBroadlink) {
			log.warn("Should not get here, no Broadlinks configured");
			theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no LifxDevices configured\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";

		} else {
			BroadlinkEntry broadlinkCommand = null;
			if(anItem.getItem().isJsonObject())
				broadlinkCommand = aGsonHandler.fromJson(anItem.getItem(), BroadlinkEntry.class);
			else
				broadlinkCommand = aGsonHandler.fromJson(anItem.getItem().getAsString(), BroadlinkEntry.class);
			BLDevice theDevice = broadlinkMap.get(broadlinkCommand.getId());
			if (theDevice == null) {
				log.warn("Should not get here, no BroadlinkDevices available");
				theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Should not get here, no Broadlinks available\", \"parameter\": \"/lights/"
						+ lightId + "state\"}}]";
			} else {
					log.debug("calling BroadlinkDevice: " + broadlinkCommand.getName());
		            switch (theDevice.getDeviceType()) {
		            case BLDevice.DEV_A1:
		            	log.debug("Broadlink A1 device called and not supported. No Action, device name = " + device.getName() + ", id= " + broadlinkCommand.getId());
		                break;
		            case BLDevice.DEV_MP1:
		            	if(broadlinkCommand.getCommand().equals("on"))
		            		changeState = true;
		            	else
		            		changeState = false;
		                try {
							((MP1Device) theDevice).setState(Integer.parseInt(broadlinkCommand.getData()), changeState);
						} catch (NumberFormatException e1) {
							log.error("Call to " + _mp1 + " device failed with number format exception.", e1);
							theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"" + _mp1 + " number format error.\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						} catch (Exception e1) {
							log.error("Call to " + _mp1 + " device failed with exception.", e1);
							theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"" + _mp1 + " device call error.\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						};
		                break;
		            case BLDevice.DEV_SP2:
		            case BLDevice.DEV_SP2_HONEYWELL_ALT1:
		            case BLDevice.DEV_SP2_HONEYWELL_ALT2:
		            case BLDevice.DEV_SP2_HONEYWELL_ALT3:
		            case BLDevice.DEV_SP2_HONEYWELL_ALT4:
		            case BLDevice.DEV_SPMINI:
		            case BLDevice.DEV_SP3:
		            case BLDevice.DEV_SPMINI2:
		            case BLDevice.DEV_SPMINI_OEM_ALT1:
		            case BLDevice.DEV_SPMINI_OEM_ALT2:
		            case BLDevice.DEV_SPMINI_PLUS:
		            	if(broadlinkCommand.getCommand().equals("on"))
		            		changeState = true;
		            	else
		            		changeState = false;
		                try {
							((SP2Device) theDevice).setState(changeState);
						} catch (Exception e1) {
							log.error("Call to " + _sp2 + " device failed with exception.", e1);
							theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"" + _sp2 + " device call error.\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						}
		                break;
		            case BLDevice.DEV_SP1:
		            	if(broadlinkCommand.getCommand().equals("on"))
		            		changeState = true;
		            	else
		            		changeState = false;
		                try {
							((SP1Device) theDevice).setPower(changeState);
						} catch (Exception e) {
							log.error("Call to " + _sp1 + " device failed with exception.", e);
							theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"" + _sp1 + " device call error.\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						}
		                break;
		            case BLDevice.DEV_RM_2:
		            case BLDevice.DEV_RM_MINI:
		            case BLDevice.DEV_RM_PRO_PHICOMM:
		            case BLDevice.DEV_RM_2_HOME_PLUS:
		            case BLDevice.DEV_RM_2_2HOME_PLUS_GDT:
		            case BLDevice.DEV_RM_2_PRO_PLUS:
		            case BLDevice.DEV_RM_2_PRO_PLUS_2:
		            case BLDevice.DEV_RM_2_PRO_PLUS_2_BL:
		            case BLDevice.DEV_RM_MINI_SHATE:
		            	if(broadlinkCommand.getData() != null && !broadlinkCommand.getData().trim().isEmpty()) {
							if(targetBri != null || targetBriInc != null) {
								theStringData = BrightnessDecode.calculateReplaceIntensityValue(broadlinkCommand.getData().trim(), intensity, targetBri, targetBriInc, true);
							}
							if(colorData != null) {
								theStringData = ColorDecode.replaceColorData(theStringData, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), true);
							}
							theStringData = DeviceDataDecode.replaceDeviceData(theStringData, device);
							theStringData = TimeDecode.replaceTimeValue(theStringData);
			            	byte[] theData = DatatypeConverter.parseHexBinary(theStringData);
			            	SendDataCmdPayload thePayload = new SendDataCmdPayload(theData);
			                try {
								((RM2Device) theDevice).sendCmdPkt(Configuration.BROADLINK_DISCONVER_TIMEOUT, thePayload);
							} catch (IOException e) {
								log.error("Call to " + _rm2 + " device failed with exception.", e);
								theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
										+ "\",\"description\": \"" + _rm2 + " device call error.\", \"parameter\": \"/lights/"
										+ lightId + "state\"}}]";
							}
		            	}
		            	else {
							log.error("Call to " + _rm2 + " with no data, noop");
							theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"" + _rm2 + " could not call device without data.\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
		            	}
		                break;

		            }
			}
		}
		return theReturn;
	}

	private BroadlinkEntry toEntry(BLDevice broadlinkObject) {
		BroadlinkEntry anEntry = new BroadlinkEntry();
		anEntry.setId(broadlinkObject.getHost() + "-" + String.format("%04x", broadlinkObject.getDeviceType()));
		anEntry.setName(broadlinkObject.getDeviceDescription());
		anEntry.setType(convertType(broadlinkObject));
		return anEntry;
	}

	private String convertType(BLDevice aDevice) {
		String theType = null;
        switch (aDevice.getDeviceType()) {
        case BLDevice.DEV_A1:
        	theType = _a1;
            break;
        case BLDevice.DEV_MP1:
        	theType = _mp1;
            break;
        case BLDevice.DEV_SP2:
        case BLDevice.DEV_SP2_HONEYWELL_ALT1:
        case BLDevice.DEV_SP2_HONEYWELL_ALT2:
        case BLDevice.DEV_SP2_HONEYWELL_ALT3:
        case BLDevice.DEV_SP2_HONEYWELL_ALT4:
        case BLDevice.DEV_SPMINI:
        case BLDevice.DEV_SP3:
        case BLDevice.DEV_SPMINI2:
        case BLDevice.DEV_SPMINI_OEM_ALT1:
        case BLDevice.DEV_SPMINI_OEM_ALT2:
        case BLDevice.DEV_SPMINI_PLUS:
        	theType = _sp2;
            break;
        case BLDevice.DEV_SP1:
        	theType = _sp1;
            break;
        case BLDevice.DEV_RM_2:
        case BLDevice.DEV_RM_MINI:
        case BLDevice.DEV_RM_PRO_PHICOMM:
        case BLDevice.DEV_RM_2_HOME_PLUS:
        case BLDevice.DEV_RM_2_2HOME_PLUS_GDT:
        case BLDevice.DEV_RM_2_PRO_PLUS:
        case BLDevice.DEV_RM_2_PRO_PLUS_2:
        case BLDevice.DEV_RM_2_PRO_PLUS_2_BL:
        case BLDevice.DEV_RM_MINI_SHATE:
        	theType = _rm2;
            break;

        }
		return theType;
	}
	@Override
	public void closeHome() {
		if(!validBroadlink)
			return;
		log.debug("Closing Home.");
		if(closed) {
			log.debug("Home is already closed....");
			return;
		}
		closed = true;
	}
}
