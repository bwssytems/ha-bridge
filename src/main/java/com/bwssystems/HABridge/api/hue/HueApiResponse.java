package com.bwssystems.HABridge.api.hue;

import java.util.HashMap;
import java.util.Map;

import com.bwssystems.HABridge.api.hue.DeviceResponse;

/**
 * Created by arm on 4/14/15.
 */
public class HueApiResponse {
    private Map<String, DeviceResponse> lights;
    private Map<String, String> scenes;
    private Map<String, String> groups;
    private Map<String, String> schedules;
    private Map<String, String> sensors;
    private Map<String, String> rules;
    private HueConfig config;

    public HueApiResponse(String name, String ipaddress, String devicetype, String userid) {
		super();
		this.setConfig(HueConfig.createConfig(name, ipaddress, devicetype, userid));
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

	public Map<String, String> getScenes() {
		return scenes;
	}

	public void setScenes(Map<String, String> scenes) {
		this.scenes = scenes;
	}

	public Map<String, String> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, String> groups) {
		this.groups = groups;
	}

	public Map<String, String> getSchedules() {
		return schedules;
	}

	public void setSchedules(Map<String, String> schedules) {
		this.schedules = schedules;
	}

	public Map<String, String> getSensors() {
		return sensors;
	}

	public void setSensors(Map<String, String> sensors) {
		this.sensors = sensors;
	}

	public Map<String, String> getRules() {
		return rules;
	}

	public void setRules(Map<String, String> rules) {
		this.rules = rules;
	}

	public HueConfig getConfig() {
		return config;
	}

	public void setConfig(HueConfig config) {
		this.config = config;
	}    
}
