package com.bwssystems.HABridge.plugins.moziot;

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

public class MozIotInstance {
    private static final Logger log = LoggerFactory.getLogger(MozIotInstance.class);
    private JWT moziotToken;
    private NamedIP mozIotIP;
    private NameValue[] headers;

    public MozIotInstance(NamedIP theNamedIp, HTTPHandler httpClient) {
        mozIotIP = theNamedIp;
        headers = null;
        gatewayLogin(httpClient);
    }

    public Boolean callCommand(String deviceId, String aCommand, MozIotCommandDetail commandData, HTTPHandler httpClient) {
        log.debug("calling Mozilla IOT: {}:{}{}{}", mozIotIP.getIp(), mozIotIP.getPort(), aCommand, commandData.getBody());
        String aUrl = null;

        if (mozIotIP.getSecure() != null && mozIotIP.getSecure())
            aUrl = "https://";
        else
            aUrl = "http://";
        headers = getAuthHeader();

        aUrl = aUrl + mozIotIP.getIp() + ":" + mozIotIP.getPort() + "/things/" + deviceId + "/" +  aCommand;
        String theData = httpClient.doHttpRequest(aUrl, HttpPut.METHOD_NAME, "application/json", commandData.getBody(), headers);
        log.debug("call Command return is: <<<{}>>>", theData);
        if (theData.contains("error") || theData.contains("ERROR") || theData.contains("Error"))
            return false;
        return true;
    }

    public List<MozillaThing> getDevices(HTTPHandler httpClient) {
        log.debug("calling Mozilla IOT: " + mozIotIP.getIp() + ":" + mozIotIP.getPort());
        List<MozillaThing> deviceList = null;
        MozillaThing[] theThings;
        String theUrl = null;
        String theData;

        if (mozIotIP.getSecure() != null && mozIotIP.getSecure())
            theUrl = "https://";
        else
            theUrl = "http://";
        headers = getAuthHeader();

        theUrl = theUrl + mozIotIP.getIp() + ":" + mozIotIP.getPort() + "/things";
        theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
        if (theData != null) {
            log.debug("GET Mozilla IOT Devices - data: " + theData);
            try {
                theThings = new Gson().fromJson(theData, MozillaThing[].class);
                if (theThings != null && theThings.length > 0) {
                    deviceList = new ArrayList<MozillaThing>();
                    for (int i = 0; i < theThings.length; i++) {
                        deviceList.add(theThings[i]);
                    }
                }
            } catch (Exception e) {
                log.warn("Cannot get an devices for Mozilla IOT {} Gson Parse Error.", mozIotIP.getName());
            }
        }
        return deviceList;
    }

    private NameValue[] getAuthHeader() {
        if (headers == null) {
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
        return headers;
    }

    private void gatewayLogin(HTTPHandler httpClient) {
        String aUrl = null;

        if (mozIotIP.getSecure() != null && mozIotIP.getSecure())
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
        aUrl = aUrl + mozIotIP.getIp() + ":" + mozIotIP.getPort() + "/login";
        log.debug("gateway login URL: {}", aUrl);
        String commandData = "{\"email\": \"" + mozIotIP.getUsername() + "\", \"password\":\"" + mozIotIP.getPassword()
                + "\"}";
        log.debug("The login body: {}", commandData);
        String theData = httpClient.doHttpRequest(aUrl, HttpPost.METHOD_NAME, "application/json", commandData, headers);
        if (theData != null) {
            log.debug("GET Mozilla login - data: {}", theData);
            try {
                moziotToken = new Gson().fromJson(theData, JWT.class);
            } catch (Exception e) {
                log.warn("Cannot get login for Mozilla IOT {} Gson Parse Error.", mozIotIP.getName());
            }
        } else {
            log.warn("Could not login {} error: <<<{}>>>", mozIotIP.getName(), theData);
        }

        headers = null;
    }

    public NamedIP getMozIotIP() {
        return mozIotIP;
    }

    public void setMozIotIP(NamedIP mozIotIP) {
        this.mozIotIP = mozIotIP;
    }

    protected void closeClient() {
    }

    public JWT getMoziotToken() {
        return moziotToken;
    }

    public void setMoziotToken(JWT moziotToken) {
        this.moziotToken = moziotToken;
    }
}