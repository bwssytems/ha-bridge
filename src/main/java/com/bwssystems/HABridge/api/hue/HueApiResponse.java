package com.bwssystems.HABridge.api.hue;

import java.util.HashMap;
import java.util.Map;

import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.google.gson.JsonObject;

/**
 * Created by arm on 4/14/15.
 */
public class HueApiResponse {
    private Map<String, DeviceResponse> lights;
    private Map<String, JsonObject> scenes;
    private Map<String, GroupResponse> groups;
    private Map<String, JsonObject> schedules;
    private Map<String, JsonObject> sensors;
    private Map<String, JsonObject> rules;
    private HueConfig config;

    public HueApiResponse(String name, String ipaddress, Map<String, WhitelistEntry> awhitelist, String emulateHubVersion, boolean isLinkButtonPressed) {
		super();
		this.setConfig(HueConfig.createConfig(name, ipaddress, awhitelist, emulateHubVersion, isLinkButtonPressed));
		this.setRules(new HashMap<>());
		this.setSensors(new HashMap<>());
		this.setSchedules(new HashMap<>());
		this.setGroups(new HashMap<>());
		this.setScenes(new HashMap<>());
	}

	public Map<String, DeviceResponse> getLights() {
        return lights;
    }

    public void setLights(Map<String, DeviceResponse> lights) {
        this.lights = lights;
    }

	public Map<String, JsonObject> getScenes() {
		return scenes;
	}

	public void setScenes(Map<String, JsonObject> scenes) {
		this.scenes = scenes;
	}

	public Map<String, GroupResponse> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, GroupResponse> groups) {
		this.groups = groups;
	}

	public Map<String, JsonObject> getSchedules() {
		return schedules;
	}

	public void setSchedules(Map<String, JsonObject> schedules) {
		this.schedules = schedules;
	}

	public Map<String, JsonObject> getSensors() {
		return sensors;
	}

	public void setSensors(Map<String, JsonObject> sensors) {
		this.sensors = sensors;
	}

	public Map<String, JsonObject> getRules() {
		return rules;
	}

	public void setRules(Map<String, JsonObject> rules) {
		this.rules = rules;
	}

	public HueConfig getConfig() {
		return config;
	}

	public void setConfig(HueConfig config) {
		this.config = config;
	}    
}
