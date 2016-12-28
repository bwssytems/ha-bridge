package com.bwssystems.HABridge;

import static spark.Spark.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.devicemanagmeent.*;
import com.bwssystems.HABridge.hue.HueMulator;
import com.bwssystems.HABridge.upnp.UpnpListener;
import com.bwssystems.HABridge.upnp.UpnpSettingsResource;
import com.bwssystems.HABridge.util.UDPDatagramSender;

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
        HomeManager homeManager;
        HueMulator theHueMulator;
        UDPDatagramSender udpSender;
        UpnpSettingsResource theSettingResponder;
        UpnpListener theUpnpListener;
        SystemControl theSystem;
        BridgeSettings bridgeSettings;
        Version theVersion;
        
        theVersion = new Version();

        log.info("HA Bridge (v" + theVersion.getVersion() + ") starting....");
        
        bridgeSettings = new BridgeSettings();
        while(!bridgeSettings.getBridgeControl().isStop()) {
        	bridgeSettings.buildSettings();
            log.info("HA Bridge initializing....");
	        // sparkjava config directive to set ip address for the web server to listen on
	        ipAddress(bridgeSettings.getBridgeSettingsDescriptor().getWebaddress());
	        // sparkjava config directive to set port for the web server to listen on
	        port(bridgeSettings.getBridgeSettingsDescriptor().getServerPort());
	        // sparkjava config directive to set html static file location for Jetty
	        staticFileLocation("/public");
	        // setup system control api first
	        theSystem = new SystemControl(bridgeSettings, theVersion);
	        theSystem.setupServer();
	        // setup the UDP Datagram socket to be used by the HueMulator and the upnpListener
	        udpSender = UDPDatagramSender.createUDPDatagramSender(bridgeSettings.getBridgeSettingsDescriptor().getUpnpResponsePort());
	        if(udpSender == null) {
	        	bridgeSettings.getBridgeControl().setStop(true);	        	
	        }
	        else {
		        //Setup the device connection homes through the manager
		        homeManager = new HomeManager();
		        homeManager.buildHomes(bridgeSettings.getBridgeSettingsDescriptor(), udpSender);
		        // setup the class to handle the resource setup rest api
		        theResources = new DeviceResource(bridgeSettings.getBridgeSettingsDescriptor(), homeManager);
		        // setup the class to handle the upnp response rest api
		        theSettingResponder = new UpnpSettingsResource(bridgeSettings.getBridgeSettingsDescriptor());
		        theSettingResponder.setupServer();
		        // setup the class to handle the hue emulator rest api
		        theHueMulator = new HueMulator(bridgeSettings.getBridgeSettingsDescriptor(), theResources.getDeviceRepository(), homeManager);
		        theHueMulator.setupServer();
		        // wait for the sparkjava initialization of the rest api classes to be complete
		        awaitInitialization();
		
		        // start the upnp ssdp discovery listener
		        theUpnpListener = new UpnpListener(bridgeSettings.getBridgeSettingsDescriptor(), bridgeSettings.getBridgeControl(), udpSender);
		        if(theUpnpListener.startListening())
		        	log.info("HA Bridge (v" + theVersion.getVersion() + ") reinitialization requessted....");
		        else
		        	bridgeSettings.getBridgeControl().setStop(true);
		        if(bridgeSettings.getBridgeSettingsDescriptor().isSettingsChanged())
		        	bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
		        homeManager.closeHomes();
		        udpSender.closeResponseSocket();
	        }
	        bridgeSettings.getBridgeControl().setReinit(false);
	        stop();
        }
        log.info("HA Bridge (v" + theVersion.getVersion() + ") exiting....");
        System.exit(0);
    }
}
