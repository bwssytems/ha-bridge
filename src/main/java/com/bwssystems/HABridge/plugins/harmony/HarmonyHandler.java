package com.bwssystems.HABridge.plugins.harmony;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;
import net.whistlingfish.harmony.config.HarmonyConfig;

public class HarmonyHandler {
    private static final Logger log = LoggerFactory.getLogger(HarmonyHandler.class);
    private HarmonyClient harmonyClient;
    private Boolean noopCalls;
    private Boolean devMode;
    private DevModeResponse devResponse;

    public HarmonyHandler(HarmonyClient theClient, Boolean noopCallsSetting, DevModeResponse devResponseSetting) {
		super();
		noopCalls = noopCallsSetting;
		devMode = Boolean.TRUE;
		devResponse = null;
		if(devResponseSetting == null)
			devMode = Boolean.FALSE;
		else
			devResponse = devResponseSetting;
		harmonyClient = theClient;
	}

    public List<Activity> getActivities() {
		log.debug("Harmony api activities list requested.");
        if(devMode)
        	return devResponse.getActivities();

       	return harmonyClient.getConfig().getActivities();
    }

	public List<Device> getDevices() {
		log.debug("Harmony api device list requested.");
        if(devMode)
        	return devResponse.getDevices();

       	return harmonyClient.getConfig().getDevices();
	}

	public HarmonyConfig getConfig() {
		log.debug("Harmony api config requested.");
        if(devMode)
        	return devResponse.getConfig();

		return harmonyClient.getConfig();
	}

	public Activity getCurrentActivity() {
		log.debug("Harmony api current sctivity requested.");
        if(devMode)
        	return devResponse.getCurrentActivity();
        
		return harmonyClient.getCurrentActivity();
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
				}
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
				}
			}
		} else {
			log.error("Error in finding device: " + aDeviceButton.getDevice() +" and a button: " + aDeviceButton.getButton() + " for a hub: " + aDeviceButton.getHub());
			return false;
		}

		return true;
	}

	public void shutdown() {
		log.debug("Harmony api shutdown requested.");
        if(devMode)
        	return;
        
		harmonyClient.disconnect();
		harmonyClient = null;
	}

}
