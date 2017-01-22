package com.bwssystems.HABridge.plugins.harmony;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;
import net.whistlingfish.harmony.config.HarmonyConfig;

public class DevModeResponse {
    final Logger log = LoggerFactory.getLogger(DevModeResponse.class);

	private final static String powerOff = "PowerOff";
	private HarmonyConfig harmonyConfig;
	private Activity currentActivity;

	public DevModeResponse() {
		super();
		harmonyConfig = HarmonyConfig.parse(dataReader("/config.data"));
		this.currentActivity = harmonyConfig.getActivityByName(powerOff);
	}
	
	public Activity getCurrentActivity() {
		return currentActivity;
	}
	
	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}
	
	public List<Activity> getActivities() {
		return harmonyConfig.getActivities();
	}
	
	public List<Device> getDevices() {
		return harmonyConfig.getDevices();
	}

	public HarmonyConfig getConfig() {
		return harmonyConfig;
	}

	private String dataReader(String filePath) {

		String content = null;
		try {
			InputStream input = getClass().getResourceAsStream(filePath);
			OutputStream out = new ByteArrayOutputStream();
			int read;
			byte[] bytes = new byte[1024];

			while ((read = input.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			content = out.toString();
		} catch (IOException e) {
			log.error("Error reading the file: " + filePath + " message: " + e.getMessage(), e);
		}
		return content;
	}

}
