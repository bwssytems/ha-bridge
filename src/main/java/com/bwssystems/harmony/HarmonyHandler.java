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

	public Boolean startActivity(RunActivity anActivity) {
		log.debug("Harmony api start activity requested for: " + anActivity.getName() + " noop mode: " + noopCalls);
		if (anActivity.isValid()) {
			try {
				if (!noopCalls)
					harmonyClient.startActivity(Integer.parseInt(anActivity.getName()));
				else
					log.info("noop mode: Harmony api start activity requested for: " + anActivity.getName());
			} catch (IllegalArgumentException e) {
				try {
					if (!noopCalls)
						harmonyClient.startActivityByName(anActivity.getName());
				} catch (IllegalArgumentException ei) {
					log.error("Error in finding activity: " + anActivity.getName());
					return false;
				}
			}
		} else {
			log.error("Error in finding activity: " + anActivity.getName());
			return false;
		}

		return true;
	}

	public Boolean pressButton(ButtonPress aDeviceButton) {
		log.debug("Harmony api press a button requested for device: " + aDeviceButton.getDevice() + " and a for button: " + aDeviceButton.getButton() + " noop mode: " + noopCalls);
		if (aDeviceButton.isValid()) {
			try {
				if (!noopCalls)
					harmonyClient.pressButton(Integer.parseInt(aDeviceButton.getDevice()), aDeviceButton.getButton());
				else
					log.info("noop mode: Harmony api press a button requested for device: " + aDeviceButton.getDevice() + " and a for button: " + aDeviceButton.getButton());
			} catch (IllegalArgumentException e) {
				try {
					if (!noopCalls)
						harmonyClient.pressButton(aDeviceButton.getDevice(), aDeviceButton.getButton());
				} catch (IllegalArgumentException ei) {
					log.error("Error in finding device: " + aDeviceButton.getDevice() +" and a button: " + aDeviceButton.getButton());
					return false;
				}
			}
		} else {
			log.error("Error in finding device: " + aDeviceButton.getDevice() +" and a button: " + aDeviceButton.getButton());
			return false;
		}

		return true;
	}
}
