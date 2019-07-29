package com.bwssystems.HABridge.plugins.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.bwssystems.HABridge.plugins.fibaro.FibaroTestData;
import com.bwssystems.HABridge.plugins.vera.VeraTestData;
import com.google.gson.Gson;

public class HTTPHome implements Home {
	private static final Logger log = LoggerFactory.getLogger(HTTPHome.class);
	private static HTTPHandler anHttpHandler = null;
	private Boolean isDevMode;
	private boolean closed;

	public HTTPHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}
	
	public static HTTPHandler getHandler() {
		if(anHttpHandler == null)
			anHttpHandler = new HTTPHandler();
		return anHttpHandler;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String responseString = null;
		
		String theUrl = anItem.getItem().getAsString().replaceAll("^\"|\"$", "");
		if(theUrl != null && !theUrl.isEmpty () && (theUrl.startsWith("http://") || theUrl.startsWith("https://"))) {
			//Backwards Compatibility Items
			if(anItem.getHttpVerb() == null || anItem.getHttpVerb().isEmpty())
			{
				if(device.getHttpVerb() != null && !device.getHttpVerb().isEmpty())
					anItem.setHttpVerb(device.getHttpVerb());
			}
	
			if(anItem.getHttpHeaders() == null || anItem.getHttpHeaders().isEmpty()) {
				if(device.getHeaders() != null && !device.getHeaders().isEmpty() )
					anItem.setHttpHeaders(device.getHeaders());
			}
	
			log.debug("executing HUE api request to Http "
					+ (anItem.getHttpVerb() == null ? "GET" : anItem.getHttpVerb()) + ": "
					+ anItem.getItem().getAsString());

			String anUrl = BrightnessDecode.calculateReplaceIntensityValue(theUrl,
					intensity, targetBri, targetBriInc, false);
			if (colorData != null) {
				anUrl = ColorDecode.replaceColorData(anUrl, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);	
			}
			anUrl = DeviceDataDecode.replaceDeviceData(anUrl, device);
			anUrl = TimeDecode.replaceTimeValue(anUrl);

			String aBody = null;
			if(anItem.getHttpBody()!= null && !anItem.getHttpBody().isEmpty()) {
				aBody = BrightnessDecode.calculateReplaceIntensityValue(anItem.getHttpBody(),
						intensity, targetBri, targetBriInc, false);
				if (colorData != null) {
					aBody = ColorDecode.replaceColorData(aBody, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);	
				}
				aBody = DeviceDataDecode.replaceDeviceData(aBody, device);
				aBody = TimeDecode.replaceTimeValue(aBody);
			}
			// make call
			String httpReply = anHttpHandler.doHttpRequest(anUrl, anItem.getHttpVerb(), anItem.getContentType(), aBody,	new Gson().fromJson(anItem.getHttpHeaders(), NameValue[].class));
			if (httpReply == null) {
				log.warn("Error on calling url to change device state: " + anUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "/state", null, null).getTheErrors(), HueError[].class);
			}
			
			if(isDevMode)
				log.info("Dev Mode response dump <<<" + httpReply +">>>");
		} else {
			log.warn("HTTP Call to be presented as http(s)://<ip_address>(:<port>)/payload, format of request unknown: " + theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"Error on calling url to change device state", "/lights/"
					+ lightId + "/state", null, null).getTheErrors(), HueError[].class);
		}

		return responseString;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		isDevMode = Boolean.parseBoolean(System.getProperty("dev.mode", "false"));
		log.info("HTTP Home created." + (isDevMode ? " DevMode is set." : ""));
		if(isDevMode) {
			anHttpHandler = new HttpTestHandler();
			((HttpTestHandler)anHttpHandler).setTheData("id=sdata", VeraTestData.SDataTestData);
			((HttpTestHandler)anHttpHandler).setTheData("action=SetLoadLevelTarget", VeraTestData.SetLoadLevelTargetData);
			((HttpTestHandler)anHttpHandler).setTheData("action=SetTarget", VeraTestData.SetTargetData);
			((HttpTestHandler)anHttpHandler).setTheData("action=RunScene", VeraTestData.RunSceneData);
			((HttpTestHandler)anHttpHandler).setTheData("/api/callAction", FibaroTestData.CallActionTestData);
			((HttpTestHandler)anHttpHandler).setTheData("/api/sceneControl", FibaroTestData.SceneControlTestData);
			((HttpTestHandler)anHttpHandler).setTheData(null, "generic treply - no match");
			
		}
		if(anHttpHandler == null)
			anHttpHandler = new HTTPHandler();
		return this;
	}

	@Override
	public Object getItems(String type) {
		// Not a resource
		return null;
	}

	@Override
	public void closeHome() {
		log.debug("Closing Home.");
		if(closed) {
			log.debug("Home is already closed....");
			return;
		}
		if(anHttpHandler != null)
			anHttpHandler.closeHandler();
		anHttpHandler = null;
		closed = true;
	}
	
	@Override
	public void refresh() {
		// noop		
	}
}
