package com.bwssystems.HABridge.plugins.harmony;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;
import net.whistlingfish.harmony.config.HarmonyConfig;
import com.bwssystems.HABridge.NamedIP;

public class HarmonyHandler {
    private static final Logger log = LoggerFactory.getLogger(HarmonyHandler.class);
    private HarmonyClient harmonyClient;
    private Boolean noopCalls;
    private Boolean devMode;
    private DevModeResponse devResponse;
    private NamedIP myNameAndIP;

    public HarmonyHandler(HarmonyClient theClient, Boolean noopCallsSetting, DevModeResponse devResponseSetting, NamedIP aNameAndIP) {
		super();
		noopCalls = noopCallsSetting;
		devMode = Boolean.TRUE;
		devResponse = null;
		if(devResponseSetting == null)
			devMode = Boolean.FALSE;
		else
			devResponse = devResponseSetting;
		harmonyClient = theClient;
		myNameAndIP = aNameAndIP;
	}

    public List<Activity> getActivities() {
		log.debug("Harmony api activities list requested.");
        if(devMode)
        	return devResponse.getActivities();

		List<Activity> listOfActivities = null;
		
		try {
			listOfActivities = harmonyClient.getConfig().getActivities();
		} catch (RuntimeException e) {
			handleExceptionError(e);
		}

       	return listOfActivities;
    }

	public List<Device> getDevices() {
		log.debug("Harmony api device list requested.");
        if(devMode)
        	return devResponse.getDevices();

		List<Device> listOfDevices = null;

		try {
			listOfDevices = harmonyClient.getConfig().getDevices();
		} catch (RuntimeException e) {
			handleExceptionError(e);
		}
       	return listOfDevices;
	}

	public HarmonyConfig getConfig() {
		log.debug("Harmony api config requested.");
        if(devMode)
        	return devResponse.getConfig();

		HarmonyConfig aConfig = null;
		try {
			aConfig = harmonyClient.getConfig();
		} catch (RuntimeException e) {
			handleExceptionError(e);
		}
		return aConfig;
	}

	public Activity getCurrentActivity() {
		log.debug("Harmony api current sctivity requested.");
        if(devMode)
        	return devResponse.getCurrentActivity();
	 
		Activity anActivity = null;
		try {
			anActivity = harmonyClient.getCurrentActivity();
		} catch (RuntimeException e) {
			handleExceptionError(e);
		}
		return anActivity;
	}

	public Boolean startActivity(RunActivity anActivity) {
		log.debug("Harmony api start activity requested for: " + anActivity.getName() + " for a hub: " + anActivity.getHub() + " noop mode: " + noopCalls);
		if (anActivity.isValid()) {
			try {
				if (noopCalls || devMode) {
            		if(devMode)
            		{
            			if(anActivity != null)
            				devResponse.setCurrentActivity(devResponse.getConfig().getActivityByName(anActivity.getName()));
            		}

					log.info("noop mode: Harmony api start activity requested for: " + anActivity.getName() + " for a hub: " + anActivity.getHub());
				}
				else
					harmonyClient.startActivity(Integer.parseInt(anActivity.getName()));
			} catch (IllegalArgumentException e) {
				try {
					if (!noopCalls)
						harmonyClient.startActivityByName(anActivity.getName());
				} catch (IllegalArgumentException ei) {
					log.error("Error in finding activity: " + anActivity.getName() + " for a hub: " + anActivity.getHub());
					return false;
				} catch (RuntimeException e1) {
					handleExceptionError(e1);
				}
			} catch (RuntimeException e1) {
				handleExceptionError(e1);
			}
		} else {
			log.error("Error in finding activity: " + anActivity.getName() + " for a hub: " + anActivity.getHub());
			return false;
		}

		return true;
	}

	public Boolean pressButton(ButtonPress aDeviceButton) {
		log.debug("Harmony api press a button requested for device: " + aDeviceButton.getDevice() + " and a for button: " + aDeviceButton.getButton() + " with pressTime of: " + aDeviceButton.getPressTime() + " for a hub: " + aDeviceButton.getHub() + " noop mode: " + noopCalls);
		if (aDeviceButton.isValid()) {
			try {
				if (noopCalls || devMode) {
					log.info("noop mode: Harmony api press a button requested for device: " + aDeviceButton.getDevice() + " and a for button: " + aDeviceButton.getButton() +
							" with a pressTime of: " + aDeviceButton.getPressTime() + " for a hub: " + aDeviceButton.getHub());
				}
            	else {
            		if(aDeviceButton.getPressTime() != null && aDeviceButton.getPressTime() > 0)
            			harmonyClient.pressButton(Integer.parseInt(aDeviceButton.getDevice()), aDeviceButton.getButton(), aDeviceButton.getPressTime());
            		else
            			harmonyClient.pressButton(Integer.parseInt(aDeviceButton.getDevice()), aDeviceButton.getButton());
            	}
			} catch (IllegalArgumentException e) {
				try {
					if (!noopCalls)
						harmonyClient.pressButton(aDeviceButton.getDevice(), aDeviceButton.getButton());
				} catch (IllegalArgumentException ei) {
					log.error("Error in finding device: " + aDeviceButton.getDevice() +" and a button: " + aDeviceButton.getButton() + " for a hub: " + aDeviceButton.getHub());
					return false;
				} catch (RuntimeException e1) {
					handleExceptionError(e1);
				}
			} catch (RuntimeException e1) {
				handleExceptionError(e1);
			}
		} else {
			log.error("Error in finding device: " + aDeviceButton.getDevice() +" and a button: " + aDeviceButton.getButton() + " for a hub: " + aDeviceButton.getHub());
			return false;
		}

		return true;
	}

	void handleExceptionError(Exception e) {
		if(e.getMessage().equalsIgnoreCase("Failed communicating with Harmony Hub")) {
			log.warn("Issue in communcicating with Harmony Hub, retrying connect....");
			try {
				harmonyClient.disconnect();
			} catch(Exception e1) {
				log.warn("Hub disconnect failed, continuing....");
			}
			harmonyClient.connect(myNameAndIP.getIp());
		}
	}

	public void shutdown() {
		log.debug("Harmony api shutdown requested.");
        if(devMode)
        	return;
        
		harmonyClient.disconnect();
		harmonyClient = null;
	}

}
