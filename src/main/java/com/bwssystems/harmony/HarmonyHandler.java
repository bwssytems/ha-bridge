package com.bwssystems.harmony;

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

    public HarmonyHandler(HarmonyClient theClient, Boolean noopCallsSetting) {
		super();
		noopCalls = noopCallsSetting;
		harmonyClient = theClient;
	}

    public List<Activity> getActivities() {
		log.debug("Harmony api activities list requested.");
		return harmonyClient.getConfig().getActivities();
    }

	public List<Device> getDevices() {
		log.debug("Harmony api device list requested.");
		return harmonyClient.getConfig().getDevices();
	}

	public HarmonyConfig getConfig() {
		log.debug("Harmony api config requested.");
		return harmonyClient.getConfig();
	}

	public Activity getCurrentActivity() {
		log.debug("Harmony api current sctivity requested.");
		return harmonyClient.getCurrentActivity();
	}

	public Boolean startActivity(String anActivity) {
		log.debug("Harmony api start activity requested for: " + anActivity + " noop mode: " + noopCalls);
		if (anActivity != null && !anActivity.isEmpty()) {
			try {
				if (!noopCalls)
					harmonyClient.startActivity(Integer.parseInt(anActivity));
			} catch (IllegalArgumentException e) {
				try {
					if (!noopCalls)
						harmonyClient.startActivityByName(anActivity);
				} catch (IllegalArgumentException ei) {
					log.error("Error in finding activity: " + anActivity);
					return false;
				}
			}
		} else {
			log.error("Error in finding activity: " + anActivity);
			return false;
		}

		return true;
	}

	public Boolean pressButton(String aDevice, String aDeviceButton) {
		log.debug("Harmony api press a button requested for device: " + aDevice + " and a for button: " + aDeviceButton + " noop mode: " + noopCalls);
		if (aDeviceButton != null && !aDeviceButton.isEmpty()) {
			try {
				if (!noopCalls)
					harmonyClient.pressButton(Integer.parseInt(aDevice), aDeviceButton);
			} catch (IllegalArgumentException e) {
				try {
					if (!noopCalls)
						harmonyClient.pressButton(aDevice, aDeviceButton);
				} catch (IllegalArgumentException ei) {
					log.error("Error in finding device: " + aDevice +" and a button: " + aDeviceButton);
					return false;
				}
			}
		} else {
			log.error("Error in finding device: " + aDevice +" and a button: " + aDeviceButton);
			return false;
		}

		return true;
	}
}
