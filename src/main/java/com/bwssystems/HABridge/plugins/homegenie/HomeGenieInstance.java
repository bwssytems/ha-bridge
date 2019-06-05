package com.bwssystems.HABridge.plugins.homegenie;

import java.util.ArrayList;
import java.util.List;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeGenieInstance {
    private static final Logger log = LoggerFactory.getLogger(HomeGenieInstance.class);
    private NamedIP homegenieIP;

    public HomeGenieInstance(NamedIP theNamedIp, HTTPHandler httpClient) {
        homegenieIP = theNamedIp;
    }

    public Boolean callCommand(String deviceId, String moduleType, HomeGenieCommandDetail commandData,
            HTTPHandler httpClient) {
        log.debug("calling HomeGenie: {}:{}{}{}", homegenieIP.getIp(), homegenieIP.getPort(), moduleType,
                commandData.getCommand());
        String aUrl = null;

        aUrl = homegenieIP.getHttpPreamble() + "/api/" + moduleType + "/" + deviceId + "/" + commandData.getCommand();

        String theLevel = commandData.getLevel();
        if (commandData.getCommand().contains("Level")) {
            if (theLevel != null && theLevel.length() > 0)
                aUrl = aUrl + "/" + theLevel;
            else
                aUrl = aUrl + "100";
        }

        String theData = httpClient.doHttpRequest(aUrl, HttpPut.METHOD_NAME, "application/json", null, httpClient.addBasicAuthHeader(null, homegenieIP));
        log.debug("call Command return is: <<<{}>>>", theData);
        if (!theData.contains("OK"))
            return false;
        return true;
    }

    public List<Module> getDevices(HTTPHandler httpClient) {
        log.debug("calling HomeGenie: " + homegenieIP.getIp() + ":" + homegenieIP.getPort());
        List<Module> deviceList = null;
        Module[] hgModules;
        String theUrl = null;
        String theData;

        theUrl = homegenieIP.getHttpPreamble() + "/api/HomeAutomation.HomeGenie/Config/Modules.List";

        theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, httpClient.addBasicAuthHeader(null, homegenieIP));
        if (theData != null) {
            log.debug("GET HomeGenie Devices - data: " + theData);
            try {
                hgModules = new Gson().fromJson(theData, Module[].class);
                if (hgModules != null && hgModules.length > 0) {
                    deviceList = new ArrayList<Module>();
                    for (int i = 0; i < hgModules.length; i++) {
                        if (hgModules[i].isSwitch() || hgModules[i].isDimmer())
                            deviceList.add(hgModules[i]);
                    }
                }
            } catch (Exception e) {
                log.warn("Cannot get an devices for Homegenie {} Gson Parse Error.", homegenieIP.getName());
            }
        }
        return deviceList;
    }

    public NamedIP getHomegenieIP() {
        return homegenieIP;
    }

    public void setHomegenieIP(NamedIP homegenieIP) {
        this.homegenieIP = homegenieIP;
    }

    protected void closeClient() {
    }
}