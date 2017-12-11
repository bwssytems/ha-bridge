package com.bwssystems.HABridge.plugins.homewizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;

/**
 * Control HomeWizard devices over HomeWizard Cloud
 * 
 * @author Björn Rennfanz (bjoern@fam-rennfanz.de)
 *
 */
public class HomeWizardHome implements Home {

	private static final Logger log = LoggerFactory.getLogger(HomeWizardHome.class);
	
	private Map<String, HomeWizzardSmartPlugInfo> plugGateways;
	private Boolean validHomeWizard;
	private boolean closed;
	
	public HomeWizardHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}
	
	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {

		String responseString = null;
		if (!validHomeWizard) {
			
			log.warn("Should not get here, no HomeWizard smart plug available");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no HomeWizard smart plug available\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";
		} else {
			
			if (anItem.getType() != null && anItem.getType().trim().equalsIgnoreCase(DeviceMapTypes.HOMEWIZARD_DEVICE[DeviceMapTypes.typeIndex])) {

				log.debug("Executing HUE api request to change activity to HomeWizard smart plug: " + anItem.getItem().toString());
				String jsonToPost = anItem.getItem().toString();
				
				HomeWizzardSmartPlugInfo homeWizzardHandler = getHomeWizzardHandler(device.getTargetDevice());
				if(homeWizzardHandler == null) {
					log.warn("Should not get here, no HomeWizard smart plug configured");
					responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
							+ "\",\"description\": \"Should not get here, no HomeWizard smart plug configured\", \"parameter\": \"/lights/"
							+ lightId + "state\"}}]";
				} else {
					try {
						homeWizzardHandler.execApply(jsonToPost);
					} catch (Exception e) {
						
						log.warn("Error posting request to HomeWizard smart plug");
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Error posting request to HomeWizard smart plug\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
					}
				}
			}
		}
		
		return responseString;
	}

	public HomeWizzardSmartPlugInfo getHomeWizzardHandler(String plugName) {
		return  plugGateways.get(plugName);
	}
	
	public List<HomeWizardSmartPlugDevice> getDevices() {
		
		log.debug("consolidating devices for plug gateways");
		Iterator<String> keys = plugGateways.keySet().iterator();
		ArrayList<HomeWizardSmartPlugDevice> deviceList = new ArrayList<>();
		
		while(keys.hasNext())
		{
			String key = keys.next();
			for(HomeWizardSmartPlugDevice device : plugGateways.get(key).getDevices())
				deviceList.add(device);
		}
		
		return deviceList;
	}
	
	@Override
	public Object getItems(String type) {
		
		if (validHomeWizard)
		{
			if (type.equalsIgnoreCase(DeviceMapTypes.HOMEWIZARD_DEVICE[DeviceMapTypes.typeIndex]))
			{
				return getDevices();
			}
		}

		return null;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		
		validHomeWizard = bridgeSettings.getBridgeSettingsDescriptor().isValidHomeWizard();
		log.info("HomeWizard Home created. " + (validHomeWizard ? "" : "No HomeWizard gateways configured."));
		
		if (validHomeWizard)
		{
			plugGateways = new HashMap<>();
			Iterator<NamedIP> gatewaysList = bridgeSettings.getBridgeSettingsDescriptor().getHomeWizardAddress().getDevices().iterator();
			
			while(gatewaysList.hasNext()) {
				
				NamedIP gateway = gatewaysList.next();
				plugGateways.put(gateway.getName(), new HomeWizzardSmartPlugInfo(gateway, gateway.getName()));
			}
		}
		
		return this;
	}

	@Override
	public void closeHome() {
		
		log.debug("Closing Home.");
		if(closed) {
			
			log.debug("Home is already closed....");
			return;
		}
		
		plugGateways = null;
		closed = true;
	}
}
