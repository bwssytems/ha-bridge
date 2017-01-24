package com.bwssystems.HABridge.plugins.domoticz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.bwssystems.HABridge.util.TextStringFormatter;
import com.google.gson.Gson;

public class DomoticzHandler {
    private static final Logger log = LoggerFactory.getLogger(DomoticzHandler.class);
    private static final String DEVICE_REQUEST = "/DeviceData!DeviceCmd=GetNames!DeviceType=";
    private static final String HVAC_REQUEST = "/HVACData!HVACCmd=GetNames";
    private static final String GROUP_REQUEST = "/GroupData!GroupCmd=GetNames";
    private static final String MACRO_REQUEST = "/MacroData!MacroCmd=GetNames";
    private static final String SCENE_REQUEST = "/SceneData!SceneCmd=GetNames";
    private static final String IRDATA_REQUEST = "/IrData!IRCmd=GetNames";
    private static final String IRBUTTON_REQUEST = "/IrData!IRCmd=GetButtons!IrDevice=";
    private static final String TOKEN_REQUEST = "?Token=";
    private static final String LIGHT_REQUEST = "Light";
    private static final String APPL_REQUEST = "Appl";
    // private static final String VIDEO_REQUEST = "Video";
    private static final String THEATRE_REQUEST = "Theatre";
    private static final String CUSTOM_REQUEST = "Custom";
    private static final String HVAC_TYPE = "HVAC";
    private static final String HOME_TYPE = "Home";
    private static final String GROUP_TYPE = "Group";
    private static final String MACRO_TYPE = "Macro";
    private static final String SCENE_TYPE = "Scene";
    private static final String IRDATA_TYPE = "IrData";
    private HTTPHandler httpClient;
    private NamedIP domoticzAddress;
	private String theToken;

    public DomoticzHandler(NamedIP addressName, String aGivenToken) {
		super();
        httpClient = new HTTPHandler();
        domoticzAddress = addressName;
        theToken = aGivenToken;
	}

	public List<DomoticzDevice> getLights() {
    	return getDomoticzDevices(DEVICE_REQUEST + LIGHT_REQUEST + TOKEN_REQUEST, LIGHT_REQUEST);
    }

	public List<DomoticzDevice> getAppliances() {
    	return getDomoticzDevices(DEVICE_REQUEST + APPL_REQUEST + TOKEN_REQUEST, APPL_REQUEST);
    }

	public List<DomoticzDevice> getTheatre() {
    	return getDomoticzDevices(DEVICE_REQUEST + THEATRE_REQUEST + TOKEN_REQUEST, THEATRE_REQUEST);
    }

	public List<DomoticzDevice> getCustom() {
    	return getDomoticzDevices(DEVICE_REQUEST + CUSTOM_REQUEST + TOKEN_REQUEST, CUSTOM_REQUEST);
    }

	public List<DomoticzDevice> getHVAC() {
    	return getDomoticzDevices(HVAC_REQUEST + TOKEN_REQUEST, HVAC_TYPE);
    }

	public List<DomoticzDevice> getGroups() {
    	return getDomoticzDevices(GROUP_REQUEST + TOKEN_REQUEST, GROUP_TYPE);
    }

	public List<DomoticzDevice> getMacros() {
    	return getDomoticzDevices(MACRO_REQUEST + TOKEN_REQUEST, MACRO_TYPE);
    }

	public List<DomoticzDevice> getScenes() {
    	return getDomoticzDevices(SCENE_REQUEST + TOKEN_REQUEST, SCENE_TYPE);
    }

	public List<DomoticzDevice> getButtons() {
		 List<DomoticzDevice> irDataDevices = getDomoticzDevices(IRDATA_REQUEST + TOKEN_REQUEST, IRDATA_TYPE);
		 
		 return getDeviceButtons(irDataDevices);
    }

	public List<DomoticzDevice> getHome(String theDeviceName) {
		List<DomoticzDevice> deviceList = null;
    	deviceList = new ArrayList<DomoticzDevice>();
		DomoticzDevice aNewDomoticzDevice = new DomoticzDevice();
		aNewDomoticzDevice.setDomoticzdevicetype(HOME_TYPE);
		aNewDomoticzDevice.setDomoticzdevicename(theDeviceName);
		deviceList.add(aNewDomoticzDevice);
    	return deviceList;
    }

	private List<DomoticzDevice> getDomoticzDevices(String apiType, String deviceType) {
		Devices theDomoticzApiResponse = null;
		List<DomoticzDevice> deviceList = null;

		String theUrl = null;
    	String theData;
   		theUrl = "http://" + domoticzAddress.getIp() + apiType + theToken;
   		theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
    	if(theData != null) {
    		log.debug("GET " + deviceType + " DomoticzApiResponse - data: " + theData);
	    	theDomoticzApiResponse = new Gson().fromJson(theData, Devices.class);
	    	if(theDomoticzApiResponse.getResult() == null) {
    			log.warn("Cannot get an devices for type " + deviceType + " for Domoticz " + domoticzAddress.getName() + " as response is not parsable.");
	        	return deviceList;
	    	}
	    	deviceList = new ArrayList<DomoticzDevice>();
	    	
	    	Iterator<DeviceResult> theDeviceNames = theDomoticzApiResponse.getResult().iterator();
	    	while(theDeviceNames.hasNext()) {
	    		DeviceResult theDevice = theDeviceNames.next();
				DomoticzDevice aNewDomoticzDevice = new DomoticzDevice();
				aNewDomoticzDevice.setDomoticzdevicetype(deviceType);
//				aNewDomoticzDevice.setDomoticzdevicename(theDevice.getDeviceName());
				deviceList.add(aNewDomoticzDevice);
	    		
	    	}
    	}
    	else {
    		log.warn("Get Domoticz device types " + deviceType + " for " + domoticzAddress.getName() + " - returned null, no data.");
    	}
    	return deviceList;
    }

	private List<DomoticzDevice> getDeviceButtons(List<DomoticzDevice> theIrDevices) {
		Devices theDomoticzApiResponse = null;
		List<DomoticzDevice> deviceList = null;

		String theUrl = null;
		String theData;
		if(theIrDevices == null)
			return null;
		Iterator<DomoticzDevice> theDomoticzDevices = theIrDevices.iterator();
		deviceList = new ArrayList<DomoticzDevice>();
		while (theDomoticzDevices.hasNext()) {
			DomoticzDevice theDomoticzDevice = theDomoticzDevices.next();
			theUrl = "http://" + domoticzAddress.getIp() + IRBUTTON_REQUEST + TextStringFormatter.forQuerySpaceUrl(theDomoticzDevice.getDomoticzdevicename()) + TOKEN_REQUEST + theToken;
			theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
			if (theData != null) {
				log.debug("GET IrData for IR Device " + theDomoticzDevice.getDomoticzdevicename() + " DomoticzApiResponse - data: " + theData);
				theDomoticzApiResponse = new Gson().fromJson(theData, Devices.class);
				if (theDomoticzApiResponse.getResult() == null) {
					log.warn("Cannot get buttons for IR Device " + theDomoticzDevice.getDomoticzdevicename() + " for Domoticz "
								+ domoticzAddress.getName() + " as response is not parsable.");
					return deviceList;
				}
//				theDomoticzDevice.setButtons(theDomoticzApiResponse);
				deviceList.add(theDomoticzDevice);

			} else {
				log.warn("Get Domoticz buttons for IR Device " + theDomoticzDevice.getDomoticzdevicename() + " for "
						+ domoticzAddress.getName() + " - returned null, no data.");
			}
		}
		return deviceList;
	}

	public NamedIP getDomoticzAddress() {
		return domoticzAddress;
	}

	public void setDomoticzAddress(NamedIP DomoticzAddress) {
		this.domoticzAddress = DomoticzAddress;
	}

}
