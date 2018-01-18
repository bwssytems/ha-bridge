package com.bwssystems.HABridge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.devicemanagmeent.ResourceHandler;
import com.bwssystems.HABridge.plugins.NestBridge.NestHome;
import com.bwssystems.HABridge.plugins.broadlink.BroadlinkHome;
import com.bwssystems.HABridge.plugins.domoticz.DomoticzHome;
import com.bwssystems.HABridge.plugins.exec.CommandHome;
import com.bwssystems.HABridge.plugins.fhem.FHEMHome;
import com.bwssystems.HABridge.plugins.hal.HalHome;
import com.bwssystems.HABridge.plugins.harmony.HarmonyHome;
import com.bwssystems.HABridge.plugins.hass.HassHome;
import com.bwssystems.HABridge.plugins.homewizard.HomeWizardHome;
import com.bwssystems.HABridge.plugins.http.HTTPHome;
import com.bwssystems.HABridge.plugins.hue.HueHome;
import com.bwssystems.HABridge.plugins.lifx.LifxHome;
import com.bwssystems.HABridge.plugins.mqtt.MQTTHome;
import com.bwssystems.HABridge.plugins.openhab.OpenHABHome;
import com.bwssystems.HABridge.plugins.somfy.SomfyHome;
import com.bwssystems.HABridge.plugins.tcp.TCPHome;
import com.bwssystems.HABridge.plugins.udp.UDPHome;
import com.bwssystems.HABridge.plugins.vera.VeraHome;
import com.bwssystems.HABridge.plugins.fibaro.FibaroHome;
import com.bwssystems.HABridge.util.UDPDatagramSender;

public class HomeManager {
	private static final Logger log = LoggerFactory.getLogger(HomeManager.class);
	Map<String, Home> homeList;
	Map<String, Home> resourceList;
	
	public HomeManager() {
		homeList = new HashMap<String, Home>();
		resourceList = new HashMap<String, Home>();
	}

	// factory method
	public void buildHomes(BridgeSettings bridgeSettings, UDPDatagramSender aUdpDatagramSender) {
		Home aHome = null;
        //setup the harmony connection if available
		aHome = new HarmonyHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.HARMONY_ACTIVITY[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HARMONY_ACTIVITY[DeviceMapTypes.typeIndex], aHome);
		resourceList.put(DeviceMapTypes.HARMONY_BUTTON[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HARMONY_BUTTON[DeviceMapTypes.typeIndex], aHome);
        //setup the nest connection if available
		aHome = new NestHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.NEST_HOMEAWAY[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.NEST_HOMEAWAY[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.NEST_THERMO_SET[DeviceMapTypes.typeIndex], aHome);
        //setup the hue passtrhu configuration if available
		aHome = new HueHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex], aHome);
        //setup the hal configuration if available
		aHome = new HalHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.HAL_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HAL_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HAL_BUTTON[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HAL_HOME[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HAL_THERMO_SET[DeviceMapTypes.typeIndex], aHome);
        //setup the mqtt handlers if available
		aHome = new MQTTHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.MQTT_MESSAGE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.MQTT_MESSAGE[DeviceMapTypes.typeIndex], aHome);
        //setup the HomeAssistant configuration if available
		aHome = new HassHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.HASS_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HASS_DEVICE[DeviceMapTypes.typeIndex], aHome);
		// Setup the HomeWizard configuration if available
		aHome = new HomeWizardHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.HOMEWIZARD_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.HOMEWIZARD_DEVICE[DeviceMapTypes.typeIndex], aHome);		
		//setup the command execution Home
		aHome = new CommandHome(bridgeSettings);
		homeList.put(DeviceMapTypes.EXEC_DEVICE_COMPAT[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.CMD_DEVICE[DeviceMapTypes.typeIndex], aHome);
		//setup the http handler Home
		aHome = new HTTPHome(bridgeSettings);
		homeList.put(DeviceMapTypes.HTTP_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.CUSTOM_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.VERA_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.VERA_SCENE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.FIBARO_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.FIBARO_SCENE[DeviceMapTypes.typeIndex], aHome);
		//setup the tcp handler Home
		aHome = new TCPHome(bridgeSettings);
		homeList.put(DeviceMapTypes.TCP_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.TCP_DEVICE_COMPAT[DeviceMapTypes.typeIndex], aHome);
		//setup the udp handler Home
		aHome = new UDPHome(bridgeSettings, aUdpDatagramSender);
		homeList.put(DeviceMapTypes.UDP_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.UDP_DEVICE_COMPAT[DeviceMapTypes.typeIndex], aHome);
		// Setup Vera Home if available
		aHome = new VeraHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.VERA_DEVICE[DeviceMapTypes.typeIndex], aHome);
		resourceList.put(DeviceMapTypes.VERA_SCENE[DeviceMapTypes.typeIndex], aHome);
		// Setup Fibaro Home if available
		aHome = new FibaroHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.FIBARO_DEVICE[DeviceMapTypes.typeIndex], aHome);
		resourceList.put(DeviceMapTypes.FIBARO_SCENE[DeviceMapTypes.typeIndex], aHome);
        //setup the Domoticz configuration if available
		aHome = new DomoticzHome(bridgeSettings);
		homeList.put(DeviceMapTypes.DOMOTICZ_DEVICE[DeviceMapTypes.typeIndex], aHome);
		resourceList.put(DeviceMapTypes.DOMOTICZ_DEVICE[DeviceMapTypes.typeIndex], aHome);
		//setup the Somfy configuration if availableOPENHAB
		aHome = new SomfyHome(bridgeSettings);
		homeList.put(DeviceMapTypes.SOMFY_DEVICE[DeviceMapTypes.typeIndex], aHome);
		resourceList.put(DeviceMapTypes.SOMFY_DEVICE[DeviceMapTypes.typeIndex], aHome);
        //setup the Lifx configuration if available
		aHome = new LifxHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.LIFX_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.LIFX_DEVICE[DeviceMapTypes.typeIndex], aHome);
        //setup the OpenHAB configuration if available
		aHome = new OpenHABHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.OPENHAB_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.OPENHAB_DEVICE[DeviceMapTypes.typeIndex], aHome);
        //setup the OpenHAB configuration if available
		aHome = new FHEMHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.FHEM_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.FHEM_DEVICE[DeviceMapTypes.typeIndex], aHome);
        //setup the Broadlink configuration if available
		aHome = new BroadlinkHome(bridgeSettings);
		resourceList.put(DeviceMapTypes.BROADLINK_DEVICE[DeviceMapTypes.typeIndex], aHome);
		homeList.put(DeviceMapTypes.BROADLINK_DEVICE[DeviceMapTypes.typeIndex], aHome);
	}
	
	public Home findHome(String type) {
		return homeList.get(type);
	}
	public ResourceHandler findResource(String type) {
		return resourceList.get(type);
	}

	public void closeHomes() {
		log.info("Manager close homes called....");
		Collection<Home> theHomes = homeList.values();
		for(Home aHome : theHomes) {
			log.info("Closing home: " + aHome.getClass().getCanonicalName());
			aHome.closeHome();
		}
		homeList.clear();
		homeList = null;
	}
}
