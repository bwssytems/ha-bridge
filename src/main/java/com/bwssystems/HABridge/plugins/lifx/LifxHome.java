package com.bwssystems.HABridge.plugins.lifx;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.github.besherman.lifx.LFXClient;
import com.github.besherman.lifx.LFXGroup;
import com.github.besherman.lifx.LFXGroupCollection;
import com.github.besherman.lifx.LFXGroupCollectionListener;
import com.github.besherman.lifx.LFXLight;
import com.github.besherman.lifx.LFXLightCollection;
import com.github.besherman.lifx.LFXLightCollectionListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LifxHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(LifxHome.class);
    private static final float DIM_DIVISOR = (float)254.00;
	private Map<String, LifxDevice> lifxMap;
	private LFXClient client;        
	private Boolean validLifx;
	private Gson aGsonHandler;
	
	public LifxHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		lifxMap = null;
		aGsonHandler = null;
		validLifx = bridgeSettings.getBridgeSettingsDescriptor().isValidLifx();
		log.info("LifxDevice Home created." + (validLifx ? "" : " No LifxDevices configured."));
		if(validLifx) {
	    	try {
	    		log.info("Open Lifx client....");
	    		InetAddress configuredAddress = InetAddress.getByName(bridgeSettings.getBridgeSettingsDescriptor().getUpnpConfigAddress());
	    		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(configuredAddress);
	    		InetAddress bcastInetAddr = null;
	            if (networkInterface != null) {
	                for (InterfaceAddress ifaceAddr : networkInterface.getInterfaceAddresses()) {
	                    InetAddress addr = ifaceAddr.getAddress();
	                    if (addr instanceof Inet4Address) {
	                    	bcastInetAddr = ifaceAddr.getBroadcast();
	                    	break;
	                    }
	                }
	            }
	            if(bcastInetAddr != null) {
		    		lifxMap = new HashMap<String, LifxDevice>();
		    		log.info("Opening LFX Client with broadcast address: " + bcastInetAddr.getHostAddress());
		    		client = new LFXClient(bcastInetAddr.getHostAddress());
		    		client.getLights().addLightCollectionListener(new MyLightListener(lifxMap));
		    		client.getGroups().addGroupCollectionListener(new MyGroupListener(lifxMap));
					client.open(false);
					aGsonHandler =
							new GsonBuilder()
							.create();
	            } else {
					log.warn("Could not open LIFX, no bcast addr available, check your upnp config address.");
					client = null;
					validLifx = false;
					return this;
	            }
			} catch (IOException e) {
				log.warn("Could not open LIFX, with IO Exception", e);
				client = null;
				validLifx = false;
				return this;
			} catch (InterruptedException e) {
				log.warn("Could not open LIFX, with Interruprted Exception", e);
				client = null;
				validLifx = false;
				return this;
			}
        }
		return this;
	}

	public LifxDevice getLifxDevice(String aName) {
		if(!validLifx)
			return null;
		LifxDevice aLifxDevice = null;
		if(aName == null || aName.equals("")) {
			log.debug("Cannot get LifxDevice for name as it is empty.");
		}
		else {
			aLifxDevice = lifxMap.get(aName);
			log.debug("Retrieved a LifxDevice for name: " + aName);
		}
		return aLifxDevice;
	}
	
	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for lifx");
		if(!validLifx)
			return null;
		LifxEntry theResponse = null;
		Iterator<String> keys = lifxMap.keySet().iterator();
		List<LifxEntry> deviceList = new ArrayList<LifxEntry>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = lifxMap.get(key).toEntry();
			if(theResponse != null)
				deviceList.add(theResponse);
			else {
				log.warn("Cannot get LifxDevice with name: " + key + ", skipping this Lifx.");
				continue;
			}
		}
		return deviceList;
	}

	@SuppressWarnings("unused")
	private Boolean addLifxLights(LFXLightCollection theDeviceList) {
		if(!validLifx)
			return false;
		Iterator<LFXLight> devices = theDeviceList.iterator();;
		while(devices.hasNext()) {
			LFXLight theDevice = devices.next();
			LifxDevice aNewLifxDevice = new LifxDevice(theDevice, LifxDevice.LIGHT_TYPE);
			lifxMap.put(aNewLifxDevice.toEntry().getName(), aNewLifxDevice);
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private Boolean addLifxGroups(LFXGroupCollection theDeviceList) {
		if(!validLifx)
			return false;
		Iterator<LFXGroup> devices = theDeviceList.iterator();;
		while(devices.hasNext()) {
			LFXGroup theDevice = devices.next();
			LifxDevice aNewLifxDevice = new LifxDevice(theDevice, LifxDevice.GROUP_TYPE);
			lifxMap.put(aNewLifxDevice.toEntry().getName(), aNewLifxDevice);
		}
		return true;
	}
	
	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String theReturn = null;
		float aBriValue;
		float theValue;
		log.debug("executing HUE api request to send message to LifxDevice: " + anItem.getItem().toString());
		if(!validLifx) {
			log.warn("Should not get here, no LifxDevice clients configured");
			theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no LifxDevices configured\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";

		} else {
			LifxEntry lifxCommand = null;
			if(anItem.getItem().isJsonObject())
				lifxCommand = aGsonHandler.fromJson(anItem.getItem(), LifxEntry.class);
			else
				lifxCommand = aGsonHandler.fromJson(anItem.getItem().getAsString(), LifxEntry.class);
			LifxDevice theDevice = getLifxDevice(lifxCommand.getName());
			if (theDevice == null) {
				log.warn("Should not get here, no LifxDevices available");
				theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Should not get here, no Lifx clients available\", \"parameter\": \"/lights/"
						+ lightId + "state\"}}]";
			} else {
					log.debug("calling LifxDevice: " + lifxCommand.getName());
					if(theDevice.getType().equals(LifxDevice.LIGHT_TYPE)) {
						LFXLight theLight = (LFXLight)theDevice.getLifxObject();
						if(body.contains("true"))
							theLight.setPower(true);
						if(body.contains("false"))
							theLight.setPower(false);
						if(targetBri != null || targetBriInc != null) {
							aBriValue = (float)BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc);
							theValue = aBriValue/DIM_DIVISOR;
							if(theValue > (float)1.0)
								theValue = (float)0.99;
							theLight.setBrightness(theValue);
						}
					} else if (theDevice.getType().equals(LifxDevice.GROUP_TYPE)) {
						LFXGroup theGroup = (LFXGroup)theDevice.getLifxObject();
						if(body.contains("true"))
							theGroup.setPower(true);
						if(body.contains("false"))
							theGroup.setPower(false);
					}
			}
		}
		return theReturn;
	}

	@Override
	public void closeHome() {
		if(!validLifx)
			return;
		client.close();
	}
    private static class MyLightListener implements LFXLightCollectionListener {
        private static final Logger log = LoggerFactory.getLogger(MyLightListener.class);
        private Map<String, LifxDevice> aLifxMap;
    	public MyLightListener(Map<String, LifxDevice> theMap) {
    		aLifxMap = theMap;
    	}
        @Override
        public void lightAdded(LFXLight light) {
            log.debug("Light added, label: " + light.getLabel() + " and id: " + light.getID());
			LifxDevice aNewLifxDevice = new LifxDevice(light, LifxDevice.LIGHT_TYPE);
			aLifxMap.put(aNewLifxDevice.toEntry().getName(), aNewLifxDevice);
        }

        @Override
        public void lightRemoved(LFXLight light) {
        	log.debug("Light removed, label: " + light.getLabel() + " and id: " + light.getID());
            aLifxMap.remove(light.getLabel());
        }

        
    }
    private static class MyGroupListener implements LFXGroupCollectionListener {
        private static final Logger log = LoggerFactory.getLogger(MyLightListener.class);
        private Map<String, LifxDevice> aLifxMap;
    	public MyGroupListener(Map<String, LifxDevice> theMap) {
    		aLifxMap = theMap;
    	}

        @Override
        public void groupAdded(LFXGroup group) {
        	log.debug("Group: " + group.getLabel() + " added: " + group.size());
			LifxDevice aNewLifxDevice = new LifxDevice(group, LifxDevice.GROUP_TYPE);
			aLifxMap.put(aNewLifxDevice.toEntry().getName(), aNewLifxDevice);
        }

        @Override
        public void groupRemoved(LFXGroup group) {
        	log.debug("Group: " + group.getLabel() + " removed");
            aLifxMap.remove(group.getLabel());
        }
        
    }
}
