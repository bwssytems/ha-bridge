package com.bwssystems.HABridge;

public class Configuration {
	public final static String DEVICE_DB_DIRECTORY = "data/device.db"; 
	public final static String GROUP_DB_DIRECTORY = "data/group.db"; 
	public final static String UPNP_RESPONSE_PORT = "50000";
	public final static String DEFAULT_ADDRESS = "1.1.1.1";
	public final static String LOOP_BACK_ADDRESS = "127.0.0.1";
	public final static String LOOP_BACK_INTERFACE = "lo";
	public final static String DEFAULT_WEB_PORT = "80";
	public final static String DEFAULT_BUTTON_SLEEP = "100";
	public static final int UPNP_DISCOVERY_PORT = 1900;
	public static final String UPNP_MULTICAST_ADDRESS = "239.255.255.250";
	public static final String CONFIG_FILE = "data/habridge.config";
	public static final int NUMBER_OF_LOG_MESSAGES = 512;
	public static final long UPNP_NOTIFY_TIMEOUT = 20000;
	public static final int UPNP_SEND_DELAY = 650;
	public static final int BROADLINK_DISCOVER_PORT = 40000;
	public static final int BROADLINK_DISCONVER_TIMEOUT = 5000;
	public static final int LINK_BUTTON_TIMEOUT = 45;
}
