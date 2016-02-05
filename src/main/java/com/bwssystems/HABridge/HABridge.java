package com.bwssystems.HABridge;

import static spark.Spark.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.devicemanagmeent.*;
import com.bwssystems.HABridge.hue.HueMulator;
import com.bwssystems.HABridge.upnp.UpnpListener;
import com.bwssystems.HABridge.upnp.UpnpSettingsResource;
import com.bwssystems.NestBridge.NestHome;
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
        NestHome nestHome;
        HueMulator theHueMulator;
        UpnpSettingsResource theSettingResponder;
        UpnpListener theUpnpListener;
        InetAddress address = null;
        String addressString = null;
        BridgeSettings bridgeSettings;
        Version theVersion;
        
        theVersion = new Version();

        log.info("HA Bridge (v" + theVersion.getVersion() + ") starting setup....");
        
        bridgeSettings = new BridgeSettings();
        bridgeSettings.setServerPort(System.getProperty("server.port", Configuration.DFAULT_WEB_PORT));
        bridgeSettings.setUpnpConfigAddress(System.getProperty("upnp.config.address", Configuration.DEFAULT_ADDRESS));
        if(bridgeSettings.getUpnpConfigAddress().equalsIgnoreCase(Configuration.DEFAULT_ADDRESS)) {
	        try {
	        	log.info("Getting an IP address for this host....");
				Enumeration<NetworkInterface> ifs =	NetworkInterface.getNetworkInterfaces();
	
				while (ifs.hasMoreElements() && addressString == null) {
					NetworkInterface xface = ifs.nextElement();
					Enumeration<InetAddress> addrs = xface.getInetAddresses();
					String name = xface.getName();
					int IPsPerNic = 0;
	
					while (addrs.hasMoreElements() && IPsPerNic == 0) {
						address = addrs.nextElement();
						if (InetAddressUtils.isIPv4Address(address.getHostAddress())) {
							log.debug(name + " ... has IPV4 addr " + address);
							if(!name.equalsIgnoreCase(Configuration.LOOP_BACK_INTERFACE)|| !address.getHostAddress().equalsIgnoreCase(Configuration.LOOP_BACK_ADDRESS)) {
								IPsPerNic++;
								addressString = address.getHostAddress();
								log.info("Adding " + addressString + " from interface " + name + " as our default upnp config address.");
							}
						}
					}
				}
			} catch (SocketException e) {
		        log.error("Cannot get ip address of this host, Exiting with message: " + e.getMessage(), e);
		        return;
			}
	        
	        bridgeSettings.setUpnpConfigAddress(addressString);
        }
        
        bridgeSettings.setUpnpDeviceDb(System.getProperty("upnp.device.db", Configuration.DEVICE_DB_DIRECTORY));
        bridgeSettings.setUpnpResponsePort(System.getProperty("upnp.response.port", Configuration.UPNP_RESPONSE_PORT));
        IpList theVeraList;

        try {
        	theVeraList = new Gson().fromJson(System.getProperty("vera.address", Configuration.DEFAULT_HARMONY_ADDRESS_LIST), IpList.class);
        } catch (Exception e) {
        	try {
        		theVeraList = new Gson().fromJson("{devices:[{name:default,ip:" + System.getProperty("vera.address", Configuration.DEFAULT_ADDRESS) + "}]}", IpList.class);
        	} catch (Exception et) {
    	        log.error("Cannot parse vera.address, Exiting with message: " + e.getMessage(), e);
    	        return;
        	}
        }
        bridgeSettings.setVeraAddress(theVeraList);
        IpList theHarmonyList;

        try {
        	theHarmonyList = new Gson().fromJson(System.getProperty("harmony.address", Configuration.DEFAULT_HARMONY_ADDRESS_LIST), IpList.class);
        } catch (Exception e) {
        	try {
        		theHarmonyList = new Gson().fromJson("{devices:[{name:default,ip:" + System.getProperty("harmony.address", Configuration.DEFAULT_ADDRESS) + "}]}", IpList.class);
        	} catch (Exception et) {
    	        log.error("Cannot parse harmony.address, Exiting with message: " + e.getMessage(), e);
    	        return;
        	}
        }
        bridgeSettings.setHarmonyAddress(theHarmonyList);
        bridgeSettings.setHarmonyUser(System.getProperty("harmony.user", Configuration.DEFAULT_USER));
        bridgeSettings.setHarmonyPwd(System.getProperty("harmony.pwd", Configuration.DEFAULT_PWD));
        bridgeSettings.setUpnpStrict(Boolean.parseBoolean(System.getProperty("upnp.strict", "true")));
        bridgeSettings.setTraceupnp(Boolean.parseBoolean(System.getProperty("trace.upnp", "false")));
        bridgeSettings.setDevMode(Boolean.parseBoolean(System.getProperty("dev.mode", "false")));
        bridgeSettings.setButtonsleep(Integer.parseInt(System.getProperty("button.sleep", Configuration.DFAULT_BUTTON_SLEEP)));
        bridgeSettings.setNestuser(System.getProperty("nest.user", Configuration.DEFAULT_USER));
        bridgeSettings.setNestpwd(System.getProperty("nest.pwd", Configuration.DEFAULT_PWD));
        
        // sparkjava config directive to set ip address for the web server to listen on
        // ipAddress("0.0.0.0"); // not used
        // sparkjava config directive to set port for the web server to listen on
        port(Integer.valueOf(bridgeSettings.getServerPort()));
        // sparkjava config directive to set html static file location for Jetty
        staticFileLocation("/public");
        //setup the harmony connection if available
        harmonyHome = new HarmonyHome(bridgeSettings);
        //setup the nest connection if available
        nestHome = new NestHome(bridgeSettings);
        // setup the class to handle the resource setup rest api
        theResources = new DeviceResource(bridgeSettings, theVersion, harmonyHome, nestHome);
        // setup the class to handle the hue emulator rest api
        theHueMulator = new HueMulator(bridgeSettings, theResources.getDeviceRepository(), harmonyHome, nestHome);
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
