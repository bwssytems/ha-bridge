package com.bwssystems.HABridge.plugins.hal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.bwssystems.HABridge.util.TextStringFormatter;
import com.google.gson.Gson;

public class HalInfo {
    private static final Logger log = LoggerFactory.getLogger(HalInfo.class);
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
    private NamedIP halAddress;

    public HalInfo(NamedIP addressName, String aGivenToken) {
		super();
        httpClient = new HTTPHandler();
        halAddress = addressName;
        if(halAddress.getPassword() == null || halAddress.getPassword().trim().isEmpty())
        	halAddress.setPassword(aGivenToken);
	}

	public List<HalDevice> getLights() {
    	return getHalDevices(DEVICE_REQUEST + LIGHT_REQUEST + TOKEN_REQUEST, LIGHT_REQUEST);
    }

	public List<HalDevice> getAppliances() {
    	return getHalDevices(DEVICE_REQUEST + APPL_REQUEST + TOKEN_REQUEST, APPL_REQUEST);
    }

	public List<HalDevice> getTheatre() {
    	return getHalDevices(DEVICE_REQUEST + THEATRE_REQUEST + TOKEN_REQUEST, THEATRE_REQUEST);
    }

	public List<HalDevice> getCustom() {
    	return getHalDevices(DEVICE_REQUEST + CUSTOM_REQUEST + TOKEN_REQUEST, CUSTOM_REQUEST);
    }

	public List<HalDevice> getHVAC() {
    	return getHalDevices(HVAC_REQUEST + TOKEN_REQUEST, HVAC_TYPE);
    }

	public List<HalDevice> getGroups() {
    	return getHalDevices(GROUP_REQUEST + TOKEN_REQUEST, GROUP_TYPE);
    }

	public List<HalDevice> getMacros() {
    	return getHalDevices(MACRO_REQUEST + TOKEN_REQUEST, MACRO_TYPE);
    }

	public List<HalDevice> getScenes() {
    	return getHalDevices(SCENE_REQUEST + TOKEN_REQUEST, SCENE_TYPE);
    }

	public List<HalDevice> getButtons() {
		 List<HalDevice> irDataDevices = getHalDevices(IRDATA_REQUEST + TOKEN_REQUEST, IRDATA_TYPE);
		 
		 return getDeviceButtons(irDataDevices);
    }

	public List<HalDevice> getHome(String theDeviceName) {
		List<HalDevice> deviceList = null;
    	deviceList = new ArrayList<HalDevice>();
		HalDevice aNewHalDevice = new HalDevice();
		aNewHalDevice.setHaldevicetype(HOME_TYPE);
		aNewHalDevice.setHaldevicename(theDeviceName);
		deviceList.add(aNewHalDevice);
    	return deviceList;
    }

	private List<HalDevice> getHalDevices(String apiType, String deviceType) {
		DeviceElements theHalApiResponse = null;
		List<HalDevice> deviceList = null;

		String theUrl = null;
    	String theData;
    	if(halAddress.getSecure()!= null && halAddress.getSecure())
    		theUrl = "https://";
    	else
    		theUrl = "http://";
   		theUrl = theUrl + halAddress.getIp() + apiType + halAddress.getPassword();
   		theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
    	if(theData != null) {
    		log.debug("GET " + deviceType + " HalApiResponse - data: " + theData);
	    	theHalApiResponse = new Gson().fromJson(theData, DeviceElements.class);
	    	if(theHalApiResponse == null || theHalApiResponse.getDeviceElements() == null) {
	    		StatusDescription theStatus = new Gson().fromJson(theData, StatusDescription.class);
	    		if(theStatus.getStatus() == null) {
	    			log.warn("Cannot get an devices for type " + deviceType + " for hal " + halAddress.getName() + " as response is not parsable.");
	    		}
	    		else {
	    			log.warn("Cannot get an devices for type " + deviceType + " for hal " + halAddress.getName() + ". Status: " + theStatus.getStatus() + ", with description: " + theStatus.getDescription());
	    		}
	        	return deviceList;
	    	}
	    	deviceList = new ArrayList<HalDevice>();
	    	
	    	Iterator<DeviceName> theDeviceNames = theHalApiResponse.getDeviceElements().iterator();
	    	while(theDeviceNames.hasNext()) {
	    		DeviceName theDevice = theDeviceNames.next();
				HalDevice aNewHalDevice = new HalDevice();
				aNewHalDevice.setHaldevicetype(deviceType);
				aNewHalDevice.setHaldevicename(theDevice.getDeviceName());
				NamedIP theaddress = new NamedIP();
				theaddress.setIp(halAddress.getIp());
				theaddress.setName(halAddress.getName());
				aNewHalDevice.setHaladdress(theaddress);
				deviceList.add(aNewHalDevice);
	    		
	    	}
    	}
    	else {
    		log.warn("Get Hal device types " + deviceType + " for " + halAddress.getName() + " - returned null, no data.");
    	}
    	return deviceList;
    }

	private List<HalDevice> getDeviceButtons(List<HalDevice> theIrDevices) {
		DeviceElements theHalApiResponse = null;
		List<HalDevice> deviceList = null;

		String theUrl = null;
		String theData;
		if(theIrDevices == null)
			return null;
		Iterator<HalDevice> theHalDevices = theIrDevices.iterator();
		deviceList = new ArrayList<HalDevice>();
		while (theHalDevices.hasNext()) {
			HalDevice theHalDevice = theHalDevices.next();
	    	if(halAddress.getSecure()!= null && halAddress.getSecure())
	    		theUrl = "https://";
	    	else
	    		theUrl = "http://";
			theUrl = theUrl + halAddress.getIp() + IRBUTTON_REQUEST + TextStringFormatter.forQuerySpaceUrl(theHalDevice.getHaldevicename()) + TOKEN_REQUEST + halAddress.getPassword();
			theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
			if (theData != null) {
				log.debug("GET IrData for IR Device " + theHalDevice.getHaldevicename() + " HalApiResponse - data: " + theData);
				try {
					theHalApiResponse = new Gson().fromJson(theData, DeviceElements.class);
				} catch (Exception e) {
					theHalApiResponse = null;
				}
				if (theHalApiResponse == null || theHalApiResponse.getDeviceElements() == null) {
					StatusDescription theStatus = new Gson().fromJson(theData, StatusDescription.class);
					if (theStatus.getStatus() == null) {
						log.warn("Cannot get buttons for IR Device " + theHalDevice.getHaldevicename() + " for hal "
								+ halAddress.getName() + " as response is not parsable.");
					} else {
						log.warn("Cannot get buttons for IR Device " + theHalDevice.getHaldevicename() + " for hal "
								+ halAddress.getName() + ". Status: " + theStatus.getStatus() + ", with description: "
								+ theStatus.getDescription());
					}
					return deviceList;
				}
				theHalDevice.setButtons(theHalApiResponse);
				deviceList.add(theHalDevice);

			} else {
				log.warn("Get Hal buttons for IR Device " + theHalDevice.getHaldevicename() + " for "
						+ halAddress.getName() + " - returned null, no data.");
			}
		}
		return deviceList;
	}

	public String deviceCommand(String theUrl) {
		String theData = null;
		theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
		return theData;
	}
	
	public NamedIP getHalAddress() {
		return halAddress;
	}

	public void setHalAddress(NamedIP halAddress) {
		this.halAddress = halAddress;
	}

	public void closeInfo() {
		if(httpClient != null)
			httpClient.closeHandler();
		httpClient = null;
		halAddress = null;
	}
}
