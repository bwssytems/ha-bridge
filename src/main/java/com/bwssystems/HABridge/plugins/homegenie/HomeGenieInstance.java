package com.bwssystems.HABridge.plugins.homegenie;

import java.util.ArrayList;
import java.util.List;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeGenieInstance {
    private static final Logger log = LoggerFactory.getLogger(HomeGenieInstance.class);
    private NamedIP homegenieIP;
    private NameValue[] headers;

    public HomeGenieInstance(NamedIP theNamedIp, HTTPHandler httpClient) {
        homegenieIP = theNamedIp;
        headers = null;
        // gatewayLogin(httpClient);
    }

    public Boolean callCommand(String deviceId, String moduleType, HomeGenieCommandDetail commandData, HTTPHandler httpClient) {
        log.debug("calling HomeGenie: {}:{}{}{}", homegenieIP.getIp(), homegenieIP.getPort(), moduleType, commandData.getCommand());
        String aUrl = null;

        headers = getAuthHeader();

        aUrl = homegenieIP.getHttpPreamble() + "/api/" + moduleType + "/" + deviceId + "/" +  commandData.getCommand();

        String theLevel = commandData.getLevel();
        if(commandData.getCommand().contains("Level")) {
            if(theLevel != null && theLevel.length() > 0)
                aUrl = aUrl + "/" + theLevel;
            else
                aUrl = aUrl + "100";
        }

        String theData = httpClient.doHttpRequest(aUrl, HttpPut.METHOD_NAME, "application/json", null, headers);
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

        headers = getAuthHeader();

        theUrl = theUrl + homegenieIP.getHttpPreamble() + "/api/HomeAutomation.HomeGenie/Config/Modules.List";
   
        theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
        if (theData != null) {
            log.debug("GET HomeGenie Devices - data: " + theData);
            try {
                hgModules = new Gson().fromJson(theData, Module[].class);
                if (hgModules != null && hgModules.length > 0) {
                    deviceList = new ArrayList<Module>();
                    for (int i = 0; i < hgModules.length; i++) {
                        if(hgModules[i].isSwitch() || hgModules[i].isDimmer())
                            deviceList.add(hgModules[i]);
                    }
                }
            } catch (Exception e) {
                log.warn("Cannot get an devices for Homegenie {} Gson Parse Error.", homegenieIP.getName());
            }
        }
        return deviceList;
    }

    private NameValue[] getAuthHeader() {
 /*       if (headers == null) {
            headers = new NameValue[3];
            headers[0] = new NameValue();
            headers[0].setName("Authorization");
            headers[0].setValue("Bearer " + moziotToken.getJwt());
            headers[1] = new NameValue();
            headers[1].setName("Content-Type");
            headers[1].setValue("application/json");
            headers[2] = new NameValue();
            headers[2].setName("Accept");
            headers[2].setValue("application/json");
        }
*/
        return headers;
    }

    private void gatewayLogin(HTTPHandler httpClient) {
/*        String aUrl = null;

        if (homegenieIP.getSecure() != null && homegenieIP.getSecure())
            aUrl = "https://";
        else
            aUrl = "http://";

        headers = new NameValue[2];
        headers[0] = new NameValue();
        headers[0].setName("Content-Type");
        headers[0].setValue("application/json");
        headers[1] = new NameValue();
        headers[1].setName("Accept");
        headers[1].setValue("application/json");
        aUrl = aUrl + homegenieIP.getIp() + ":" + homegenieIP.getPort() + "/login";
        log.debug("gateway login URL: {}", aUrl);
        String commandData = "{\"email\": \"" + homegenieIP.getUsername() + "\", \"password\":\"" + homegenieIP.getPassword()
                + "\"}";
        log.debug("The login body: {}", commandData);
        String theData = httpClient.doHttpRequest(aUrl, HttpPost.METHOD_NAME, "application/json", commandData, headers);
        if (theData != null) {
            log.debug("GET Mozilla login - data: {}", theData);
            try {
                moziotToken = new Gson().fromJson(theData, JWT.class);
            } catch (Exception e) {
                log.warn("Cannot get login for HomeGenie {} Gson Parse Error.", homegenieIP.getName());
            }
        } else {
            log.warn("Could not login {} error: <<<{}>>>", homegenieIP.getName(), theData);
        }

        headers = null;
*/
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