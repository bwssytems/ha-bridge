package com.bwssytems.HABridge;

import static spark.Spark.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssytems.HABridge.devicemanagmeent.*;
import com.bwssytems.HABridge.hue.HueMulator;
import com.bwssytems.HABridge.upnp.UpnpListener;
import com.bwssytems.HABridge.upnp.UpnpSettingsResource;

public class AmazonEchoBridge {
	
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
        Logger log = LoggerFactory.getLogger(AmazonEchoBridge.class);
        DeviceResource theResources;
        HueMulator theHueMulator;
        UpnpSettingsResource theSettingResponder;
        UpnpListener theUpnpListener;
        InetAddress address;
        String addressString;
        String upnpAddressString;
        String serverPort;
        //get ip address for upnp requests
        try {
			address = InetAddress.getLocalHost();
			addressString = address.getHostAddress();
		} catch (UnknownHostException e) {
	        log.error("Cannot get ip address of this host, Exiting with message: " + e.getMessage(), e);
	        return;
		}
        
        upnpAddressString = System.getProperty("upnp.config.address", addressString);

        // sparkjava config directive to set ip address for the web server to listen on
        // ipAddress("0.0.0.0"); // not used
        // sparkjava config directive to set port for the web server to listen on
        serverPort = System.getProperty("server.port", "8080");
        port(Integer.valueOf(serverPort));
        // sparkjava config directive to set html static file location for Jetty
        staticFileLocation("/public");
        log.info("Starting setup....");
        // setup the class to handle the resource setup rest api
        theResources = new DeviceResource();
        // setup the class to handle the hue emulator rest api
        theHueMulator = new HueMulator(theResources.getDeviceRepository());
        // setup the class to handle the upnp response rest api
        theSettingResponder = new UpnpSettingsResource(upnpAddressString);
        // wait for the sparkjava initialization of the rest api classes to be complete
        awaitInitialization();

        // start the upnp ssdp discovery listener
        theUpnpListener = new UpnpListener(upnpAddressString, serverPort);
        log.info("Done setup, application to run....");
        theUpnpListener.startListening();
    }
}
