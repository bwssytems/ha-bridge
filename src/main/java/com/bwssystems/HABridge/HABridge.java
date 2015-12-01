package com.bwssystems.HABridge;

import static spark.Spark.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.devicemanagmeent.*;
import com.bwssystems.HABridge.hue.HueMulator;
import com.bwssystems.HABridge.upnp.UpnpListener;
import com.bwssystems.HABridge.upnp.UpnpSettingsResource;
import com.bwssystems.harmony.HarmonyHome;
import com.google.gson.Gson;

public class HABridge {
	
	/*
	 * This program is based on the work of armzilla from this github repository:
	 * https://github.com/armzilla/amazon-echo-ha-bridge
	 * 
	 * This is the main entry point to start the amazon echo bridge.
	 * 
	 * This program is using sparkjava rest server to build all the http calls. 
	 * Sparkjava is a microframework that uses Jetty webserver module to host 
	 * its' calls. This is a very compact system than using the spring frameworks
	 * that was previously used.
	 * 
	 * There is a custom upnp listener that is started to handle discovery.
	 * 
	 * 
	 */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(HABridge.class);
        DeviceResource theResources;
        HarmonyHome harmonyHome;
        HueMulator theHueMulator;
        UpnpSettingsResource theSettingResponder;
        UpnpListener theUpnpListener;
        InetAddress address;
        String addressString;
        BridgeSettings bridgeSettings;
        Version theVersion;
        
        theVersion = new Version();

        log.info("HA Bridge (v" + theVersion.getVersion() + ") starting setup....");
        //get ip address for upnp requests
        try {
			address = InetAddress.getLocalHost();
			addressString = address.getHostAddress();
		} catch (UnknownHostException e) {
	        log.error("Cannot get ip address of this host, Exiting with message: " + e.getMessage(), e);
	        return;
		}
        
        bridgeSettings = new BridgeSettings();
        bridgeSettings.setServerPort(System.getProperty("server.port", Configuration.DFAULT_WEB_PORT));
        bridgeSettings.setUpnpConfigAddress(System.getProperty("upnp.config.address", addressString));
        bridgeSettings.setUpnpDeviceDb(System.getProperty("upnp.device.db", Configuration.DEVICE_DB_DIRECTORY));
        bridgeSettings.setUpnpResponsePort(System.getProperty("upnp.response.port", Configuration.UPNP_RESPONSE_PORT));
        bridgeSettings.setVeraAddress(System.getProperty("vera.address", Configuration.DEFAULT_VERA_ADDRESS));
        IpList theHarmonyList;

        try {
        	theHarmonyList = new Gson().fromJson(System.getProperty("harmony.address", Configuration.DEFAULT_HARMONY_ADDRESS_LIST), IpList.class);
        } catch (Exception e) {
        	try {
        		theHarmonyList = new Gson().fromJson("{devices:[{name:default,ip:" + System.getProperty("harmony.address", Configuration.DEFAULT_HARMONY_ADDRESS) + "}]}", IpList.class);
        	} catch (Exception et) {
    	        log.error("Cannot parse harmony.address, Exiting with message: " + e.getMessage(), e);
    	        return;
        	}
        }
        bridgeSettings.setHarmonyAddress(theHarmonyList);
        bridgeSettings.setHarmonyUser(System.getProperty("harmony.user", Configuration.DEFAULT_HARMONY_USER));
        bridgeSettings.setHarmonyPwd(System.getProperty("harmony.pwd", Configuration.DEFAULT_HARMONY_PWD));
        bridgeSettings.setUpnpStrict(Boolean.parseBoolean(System.getProperty("upnp.strict", "true")));
        bridgeSettings.setTraceupnp(Boolean.parseBoolean(System.getProperty("trace.upnp", "false")));
        bridgeSettings.setDevMode(Boolean.parseBoolean(System.getProperty("dev.mode", "false")));
        bridgeSettings.setUpnpResponseDevices(Integer.parseInt(System.getProperty("upnp.response.devices", Configuration.UPNP_RESPONSE_DEVICES)));
        
        // sparkjava config directive to set ip address for the web server to listen on
        // ipAddress("0.0.0.0"); // not used
        // sparkjava config directive to set port for the web server to listen on
        port(Integer.valueOf(bridgeSettings.getServerPort()));
        // sparkjava config directive to set html static file location for Jetty
        staticFileLocation("/public");
        //setup the harmony connection if available
        harmonyHome = new HarmonyHome(bridgeSettings);
        // setup the class to handle the resource setup rest api
        theResources = new DeviceResource(bridgeSettings, theVersion, harmonyHome);
        // setup the class to handle the hue emulator rest api
        theHueMulator = new HueMulator(bridgeSettings, theResources.getDeviceRepository(), harmonyHome);
        theHueMulator.setupServer();
        // setup the class to handle the upnp response rest api
        theSettingResponder = new UpnpSettingsResource(bridgeSettings);
        theSettingResponder.setupServer();
        // wait for the sparkjava initialization of the rest api classes to be complete
        awaitInitialization();

        // start the upnp ssdp discovery listener
        theUpnpListener = new UpnpListener(bridgeSettings);
        theUpnpListener.startListening();
    }
}
