package com.bwssystems.HABridge;

import static spark.Spark.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.devicemanagmeent.*;
import com.bwssystems.HABridge.hue.HueMulator;
import com.bwssystems.HABridge.plugins.http.HttpClientPool;
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
    	@SuppressWarnings("unused")
		HttpClientPool thePool;
       
        theVersion = new Version();
        // Singleton initialization
        thePool = new HttpClientPool();

        log.info("HA Bridge (v" + theVersion.getVersion() + ") starting....");
        
        bridgeSettings = new BridgeSettings();
    	// sparkjava config directive to set html static file location for Jetty
        while(!bridgeSettings.getBridgeControl().isStop()) {
        	bridgeSettings.buildSettings();
            bridgeSettings.getBridgeSecurity().removeTestUsers();
            log.info("HA Bridge initializing....");
	        // sparkjava config directive to set ip address for the web server to listen on
	        ipAddress(bridgeSettings.getBridgeSettingsDescriptor().getWebaddress());
	        // sparkjava config directive to set port for the web server to listen on
	        port(bridgeSettings.getBridgeSettingsDescriptor().getServerPort());
	    	staticFileLocation("/public");
//	    	initExceptionHandler((e) -> HABridge.theExceptionHandler(e, bridgeSettings.getBridgeSettingsDescriptor().getServerPort()));
	        if(!bridgeSettings.getBridgeControl().isReinit())
	        	init();
	        bridgeSettings.getBridgeControl().setReinit(false);
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
		        homeManager.buildHomes(bridgeSettings, udpSender);
		        // setup the class to handle the resource setup rest api
		        theResources = new DeviceResource(bridgeSettings, homeManager);
		        // setup the class to handle the hue emulator rest api
		        theHueMulator = new HueMulator(bridgeSettings, theResources.getDeviceRepository(), theResources.getGroupRepository(), homeManager);
		        theHueMulator.setupServer();
		        // wait for the sparkjava initialization of the rest api classes to be complete
		        awaitInitialization();

		        if(bridgeSettings.getBridgeSettingsDescriptor().isTraceupnp())
		        	log.info("Traceupnp: upnp config address: " + bridgeSettings.getBridgeSettingsDescriptor().getUpnpConfigAddress() + "-useIface:" +
		        			bridgeSettings.getBridgeSettingsDescriptor().isUseupnpiface() + " on web server: " +
		        			bridgeSettings.getBridgeSettingsDescriptor().getWebaddress() + ":" + bridgeSettings.getBridgeSettingsDescriptor().getServerPort());
		        // setup the class to handle the upnp response rest api
		        theSettingResponder = new UpnpSettingsResource(bridgeSettings.getBridgeSettingsDescriptor());
		        theSettingResponder.setupServer();

		        // start the upnp ssdp discovery listener
		        theUpnpListener = null;
		        try {
					theUpnpListener = new UpnpListener(bridgeSettings.getBridgeSettingsDescriptor(), bridgeSettings.getBridgeControl(), udpSender);
				} catch (IOException e) {
					log.error("Could not initialize UpnpListener, exiting....", e);
					theUpnpListener = null;
				}
		        if(theUpnpListener != null && theUpnpListener.startListening())
		        	log.info("HA Bridge (v" + theVersion.getVersion() + ") reinitialization requessted....");
		        else
		        	bridgeSettings.getBridgeControl().setStop(true);
		        if(bridgeSettings.getBridgeSettingsDescriptor().isSettingsChanged())
		        	bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
		        log.info("Going to close all homes");
		        homeManager.closeHomes();
		        udpSender.closeResponseSocket();
		        udpSender = null;
	        }
	        stop();
	        if(!bridgeSettings.getBridgeControl().isStop()) {
	        	try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					log.error("Sleep error: " + e.getMessage());
				}
	        }
        }
        bridgeSettings.getBridgeSecurity().removeTestUsers();
        if(bridgeSettings.getBridgeSecurity().isSettingsChanged())
        	bridgeSettings.updateConfigFile();
		try {
			HttpClientPool.shutdown();
			thePool = null;
		} catch (InterruptedException e) {
			log.warn("Error shutting down http pool: " + e.getMessage());;
		} catch (IOException e) {
			log.warn("Error shutting down http pool: " + e.getMessage());;
		}
        log.info("HA Bridge (v" + theVersion.getVersion() + ") exiting....");
        System.exit(0);
    }
    
    private static void theExceptionHandler(Exception e, Integer thePort) {
        Logger log = LoggerFactory.getLogger(HABridge.class);
    	log.error("Could not start ha-bridge webservice on port [" + thePort + "] due to: " + e.getMessage());
    	System.exit(0);
    }
}
