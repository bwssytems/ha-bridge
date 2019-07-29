package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.math.BigInteger;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.BridgeSettingsDescriptor;
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
import com.bwssystems.HABridge.util.HexLibrary;
import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.MP1Device;
import com.github.mob41.blapi.SP1Device;
import com.github.mob41.blapi.SP2Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.mac.MacFormatException;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;
import com.google.gson.Gson;

public class BroadlinkHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(BroadlinkHome.class);
	private Map<String, BLDevice> broadlinkMap;
	private Boolean validBroadlink;
	private boolean closed;
	private Boolean isDevMode;
	private BridgeSettingsDescriptor bridgeSettingsDesc;
	
	public BroadlinkHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		broadlinkMap = null;
		bridgeSettingsDesc = bridgeSettings.getBridgeSettingsDescriptor();
		validBroadlink = bridgeSettings.getBridgeSettingsDescriptor().isValidBroadlink();
		isDevMode = Boolean.parseBoolean(System.getProperty("dev.mode", "false"));
		if (isDevMode)
			validBroadlink = true;

		if(validBroadlink)
			broadlinkDiscover();

		log.info("Broadlink Home created." + (validBroadlink ? "" : " No Broadlinks configured.") + (isDevMode ? " DevMode is set." : ""));
		return this;
	}

	@Override
	public Object getItems(String type) {
		List<BroadlinkEntry> deviceList = new ArrayList<BroadlinkEntry>();
		if(!validBroadlink || broadlinkMap == null)
			return deviceList;
		BroadlinkEntry theResponse = null;
		log.debug("consolidating devices for Broadlink");
		Iterator<String> keys = broadlinkMap.keySet().iterator();
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
	public void refresh() {
		if(validBroadlink)
			broadlinkDiscover();
		
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
					+ lightId + "/state\"}}]";

		} else {
			BroadlinkEntry broadlinkCommand = null;
			if(anItem.getItem().isJsonObject())
				broadlinkCommand = new Gson().fromJson(anItem.getItem(), BroadlinkEntry.class);
			else
				broadlinkCommand = new Gson().fromJson(anItem.getItem().getAsString().replaceAll("^\"|\"$", ""), BroadlinkEntry.class);
			BLDevice theDevice = null;
			if(broadlinkMap != null && !broadlinkMap.isEmpty())
				theDevice = broadlinkMap.get(broadlinkCommand.getId());

			if (theDevice == null) {
				if(broadlinkCommand.hasIpAndMac()) {
					byte[] intBytes = HexLibrary.decodeHexString(broadlinkCommand.getType());
					BigInteger theBig = new BigInteger(intBytes);
					int theType =  theBig.intValue();
					try {
		            	theDevice = BLDevice.createInstance((short)theType, broadlinkCommand.getIpAddr(), new Mac(broadlinkCommand.getMacAddr()));
					} catch (MacFormatException e) {
						log.warn("Could not initialize BroadlinkDevice device due to Mac (" + broadlinkCommand.getId() + ") format exception: " + e.getMessage());
						theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Could not initialize BroadlinkDevice device due to Mac format exception\", \"parameter\": \"/lights/"
								+ lightId + "/state\"}}]";
					} catch (IOException e) {
						log.warn("Could not initialize BroadlinkDevice device due to IP Address (" + broadlinkCommand.getId() + ") exception: " + e.getMessage());
						theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Could not initialize BroadlinkDevice device due to IP Address exception\", \"parameter\": \"/lights/"
								+ lightId + "/state\"}}]";
					} catch (Exception e) {
						log.warn("Could not initialize BroadlinkDevice device due to (" + broadlinkCommand.getId() + ") exception: " + e.getMessage());
						theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Could not initialize BroadlinkDevice device due to exception\", \"parameter\": \"/lights/"
								+ lightId + "/state\"}}]";
					}

	            	if(broadlinkMap == null)
						broadlinkMap = new HashMap<String, BLDevice>();
					
					if (theDevice != null) {
						String newId = theDevice.getHost() + "-" + String.format("%04x", theDevice.getDeviceType());
						if (broadlinkMap.get(newId) == null) {
							broadlinkMap.put(newId, theDevice);
						}
					}
				}
			}
			if (theDevice == null) {
				log.warn("Should not get here, no BroadlinkDevice not available");
				theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Should not get here, no Broadlinks available\", \"parameter\": \"/lights/"
						+ lightId + "/state\"}}]";
			} else {
					log.debug("calling BroadlinkDevice: " + broadlinkCommand.getName());
					try {
	                	if(!isDevMode) {
	                		if(!theDevice.auth()) {
								log.error("Call to " + broadlinkCommand.getId() + " device authorization failed.");
								theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
										+ "\",\"description\": \"" + broadlinkCommand.getId() + " device auth error.\", \"parameter\": \"/lights/"
										+ lightId + "/state\"}}]";
							}	
	                	}
			            switch (theDevice.getDeviceType()) {
			            case BLDevice.DEV_A1:
			            	log.debug("Broadlink A1 device called and not supported. No Action, device name = " + device.getName() + ", id= " + broadlinkCommand.getId());
			                break;
			            case BLDevice.DEV_MP1:
			            	if(broadlinkCommand.getCommand().equals("on"))
			            		changeState = true;
			            	else
			            		changeState = false;
							((MP1Device) theDevice).setState(Integer.parseInt(broadlinkCommand.getData()), changeState);
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
							((SP2Device) theDevice).setState(changeState);
			                break;
			            case BLDevice.DEV_SP1:
			            	if(broadlinkCommand.getCommand().equals("on"))
			            		changeState = true;
			            	else
			            		changeState = false;
							((SP1Device) theDevice).setPower(changeState);
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
			            		theStringData = broadlinkCommand.getData().trim();
								if(targetBri != null || targetBriInc != null) {
									theStringData = BrightnessDecode.calculateReplaceIntensityValue(theStringData, intensity, targetBri, targetBriInc, true);
								}
								if(colorData != null) {
									theStringData = ColorDecode.replaceColorData(theStringData, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), true);
								}
								theStringData = DeviceDataDecode.replaceDeviceData(theStringData, device);
								theStringData = TimeDecode.replaceTimeValue(theStringData);
				            	byte[] theData = HexLibrary.decodeHexString(theStringData);
				            	SendDataCmdPayload thePayload = new SendDataCmdPayload(theData);
				                		
				                DatagramPacket thePacket = theDevice.sendCmdPkt(Configuration.BROADLINK_DISCONVER_TIMEOUT, thePayload);
				                String returnData = null;
				                if(thePacket != null)
				                	returnData = HexLibrary.encodeHexString(thePacket.getData());
				                else
				                	returnData = "No Data - null";
				                log.debug("RM2 Device data return: <<<" + returnData + ">>>");
			            	}
			            	else {
								log.error("Call to " + broadlinkCommand.getId() + " with no data, noop");
								theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
										+ "\",\"description\": \"" + broadlinkCommand.getId() + " could not call device without data.\", \"parameter\": \"/lights/"
										+ lightId + "/state\"}}]";
			            	}
			                break;
	
			            }
					} catch (Exception e) {
						log.error("Call to " + broadlinkCommand.getId() + " device failed with exception: " + e.getMessage(), e);
						theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"" + broadlinkCommand.getId() + " device call error.\", \"parameter\": \"/lights/"
								+ lightId + "/state\"}}]";
					}
		            
			}
		}
		
		return theReturn;
	}

	private BroadlinkEntry toEntry(BLDevice broadlinkObject) {
		short baseType = 0;
		switch (broadlinkObject.getDeviceType()) {
        case BLDevice.DEV_MP1:
        	baseType = BLDevice.DEV_MP1;
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
        	baseType = BLDevice.DEV_SP2;
            break;
        case BLDevice.DEV_SP1:
        	baseType = BLDevice.DEV_SP1;
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
        	baseType = BLDevice.DEV_RM_2;
            break;
		}
		BroadlinkEntry anEntry = new BroadlinkEntry();
		anEntry.setId(broadlinkObject.getHost() + "-" + String.format("%04x", broadlinkObject.getDeviceType()));
		anEntry.setName(broadlinkObject.getDeviceDescription());
		anEntry.setType(String.format("%04x", broadlinkObject.getDeviceType()));
		anEntry.setBaseType(String.format("%04x", baseType));
		anEntry.setDesc(BLDevice.getDescOfType(broadlinkObject.getDeviceType()));
		anEntry.setIpAddr(broadlinkObject.getHost());
		anEntry.setMacAddr(broadlinkObject.getMac().getMacString());
		return anEntry;
	}

	public BLDevice[] broadlinkDiscover () {
		BLDevice[] clients = null;
		int aDiscoverPort = Configuration.BROADLINK_DISCOVER_PORT;
		broadlinkMap = new HashMap<String, BLDevice>();
		while(aDiscoverPort > 0) {
	    	try {
	    		log.info("Broadlink discover....");
				if(isDevMode) {
					clients = TestBLDevice.discoverDevices(InetAddress.getByName(bridgeSettingsDesc.getUpnpConfigAddress()), aDiscoverPort, Configuration.BROADLINK_DISCONVER_TIMEOUT);
				}
				else
					clients = BLDevice.discoverDevices(InetAddress.getByName(bridgeSettingsDesc.getUpnpConfigAddress()), aDiscoverPort, Configuration.BROADLINK_DISCONVER_TIMEOUT);
	    		for(int i = 0; i < clients.length; i++) {
	    			if(clients[i].getDeviceType() != BLDevice.DEV_A1 && broadlinkMap.get(clients[i].getHost() + "-" + String.format("%04x", clients[i].getDeviceType())) == null) {
	    				broadlinkMap.put(clients[i].getHost() + "-" + String.format("%04x", clients[i].getDeviceType()), clients[i]);
	    				log.debug("Adding Device to Map - host: " + clients[i].getHost() + ", device Type: " + clients[i].getDeviceDescription() + ", mac: " + (clients[i].getMac() == null ? "no Mac in client" : clients[i].getMac().getMacString()));
	    			} else {
	    				log.debug("Ignoring Device (already in the list or an A1 Device) - host: " + clients[i].getHost() + ", device Type: " + clients[i].getDeviceDescription() + ", mac: " + (clients[i].getMac() == null ? "no Mac in client" : clients[i].getMac().getMacString()));
	    			}
	    		}
				aDiscoverPort = 0;
			} catch (BindException e) {
				log.warn("Could not discover Broadlinks, Port in use, increasing by 11");
				aDiscoverPort += 11;
				if(aDiscoverPort > Configuration.BROADLINK_DISCOVER_PORT + 110)
					aDiscoverPort = 0;
			} catch (IOException e) {
				log.warn("Could not discover Broadlinks, with IO Exception", e);
				broadlinkMap = null;
				aDiscoverPort = 0;
			}
		}
		if(clients == null || clients.length <= 0) {
			log.warn("Did not discover any Broadlinks.");
			broadlinkMap = null;
		} else {
			log.info("Broadlink discover found " + clients.length + " clients.");
		}
		return clients;
	}
	
	@Override
	public void closeHome() {
		if(!validBroadlink)
			return;
		log.debug("Closing Home.");
		if(broadlinkMap != null) {
			broadlinkMap.clear();
			broadlinkMap = null;
		}
		if(closed) {
			log.debug("Home is already closed....");
			return;
		}
		closed = true;
	}
}
