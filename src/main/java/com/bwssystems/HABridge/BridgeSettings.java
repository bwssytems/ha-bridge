package com.bwssystems.HABridge;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.util.BackupHandler;
import com.bwssystems.util.JsonTransformer;
import com.google.gson.Gson;

public class BridgeSettings extends BackupHandler {
	private BridgeSettingsDescriptor theBridgeSettings;
	private BridgeControlDescriptor bridgeControl;
	
	public BridgeSettings() {
		super();
		bridgeControl = new BridgeControlDescriptor();
		theBridgeSettings = new BridgeSettingsDescriptor();
	}
	public BridgeControlDescriptor getBridgeControl() {
		return bridgeControl;
	}
	public BridgeSettingsDescriptor getBridgeSettingsDescriptor() {
		return theBridgeSettings;
	}
	public void buildSettings() {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
        InetAddress address = null;
        String addressString = null;
        String theVeraAddress = null;
        String theHarmonyAddress = null;

        String configFileProperty = System.getProperty("config.file");
        if(configFileProperty == null) {
        	Path filePath = Paths.get(Configuration.CONFIG_FILE);
        	if(Files.exists(filePath) && Files.isReadable(filePath))
        		configFileProperty = Configuration.CONFIG_FILE;
        }

        if(configFileProperty != null)
        {
        	log.info("reading from config file: " + configFileProperty);
        	theBridgeSettings.setConfigfile(configFileProperty);
        	_loadConfig();
        }
        else
        {
        	log.info("reading from system properties");
        	theBridgeSettings.setNumberoflogmessages(Configuration.NUMBER_OF_LOG_MESSAGES);
        	theBridgeSettings.setFarenheit(true);
        	theBridgeSettings.setConfigfile(Configuration.CONFIG_FILE);
        	theBridgeSettings.setServerPort(System.getProperty("server.port", Configuration.DEFAULT_WEB_PORT));
        	theBridgeSettings.setUpnpConfigAddress(System.getProperty("upnp.config.address"));
        	theBridgeSettings.setUpnpDeviceDb(System.getProperty("upnp.device.db"));
        	theBridgeSettings.setUpnpResponsePort(System.getProperty("upnp.response.port", Configuration.UPNP_RESPONSE_PORT));
	        
	        theVeraAddress = System.getProperty("vera.address");
        	IpList theVeraList = null;	
	        if(theVeraAddress != null) {
		        try {
		        	theVeraList = new Gson().fromJson(theVeraAddress, IpList.class);
		        } catch (Exception e) {
		        	try {
		        		theVeraList = new Gson().fromJson("{devices:[{name:default,ip:" + theVeraAddress + "}]}", IpList.class);
		        	} catch (Exception et) {
		    	        log.error("Cannot parse vera.address, not set with message: " + e.getMessage(), e);
		    	        theVeraList = null;
		        	}
		        }
	        }
	        theBridgeSettings.setVeraAddress(theVeraList);

	        theHarmonyAddress = System.getProperty("harmony.address");
	        IpList theHarmonyList = null;	
	        if(theHarmonyAddress != null) {
		        try {
		        	theHarmonyList = new Gson().fromJson(theHarmonyAddress, IpList.class);
		        } catch (Exception e) {
		        	try {
		        		theHarmonyList = new Gson().fromJson("{devices:[{name:default,ip:" + theHarmonyAddress + "}]}", IpList.class);
		        	} catch (Exception et) {
		    	        log.error("Cannot parse harmony.address, not set with message: " + e.getMessage(), e);
		    	        theHarmonyList = null;
		        	}
		        }
	        }
	        theBridgeSettings.setHarmonyAddress(theHarmonyList);
	        theBridgeSettings.setHarmonyUser(System.getProperty("harmony.user"));
	        theBridgeSettings.setHarmonyPwd(System.getProperty("harmony.pwd"));
	        theBridgeSettings.setUpnpStrict(Boolean.parseBoolean(System.getProperty("upnp.strict", "true")));
	        theBridgeSettings.setTraceupnp(Boolean.parseBoolean(System.getProperty("trace.upnp", "false")));
	        theBridgeSettings.setButtonsleep(Integer.parseInt(System.getProperty("button.sleep", Configuration.DEFAULT_BUTTON_SLEEP)));
	        theBridgeSettings.setNestuser(System.getProperty("nest.user"));
	        theBridgeSettings.setNestpwd(System.getProperty("nest.pwd"));
        }

        if(theBridgeSettings.getUpnpConfigAddress() == null || theBridgeSettings.getUpnpConfigAddress().equals("")) {
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
	        
	        theBridgeSettings.setUpnpConfigAddress(addressString);
        }
        
        if(theBridgeSettings.getUpnpResponsePort() == null)
        	theBridgeSettings.setUpnpResponsePort(Configuration.UPNP_RESPONSE_PORT);
        
        if(theBridgeSettings.getServerPort() == null)
        	theBridgeSettings.setServerPort(Configuration.DEFAULT_WEB_PORT);
        
        if(theBridgeSettings.getUpnpDeviceDb() == null)
        	theBridgeSettings.setUpnpDeviceDb(Configuration.DEVICE_DB_DIRECTORY);
        
        if(theBridgeSettings.getNumberoflogmessages() == null)
        	theBridgeSettings.setNumberoflogmessages(Configuration.NUMBER_OF_LOG_MESSAGES);
        
        if(theBridgeSettings.getButtonsleep() <= 0)
        	theBridgeSettings.setButtonsleep(Integer.parseInt(Configuration.DEFAULT_BUTTON_SLEEP));

        theBridgeSettings.setVeraconfigured(theBridgeSettings.isValidVera());
        theBridgeSettings.setHarmonyconfigured(theBridgeSettings.isValidHarmony());
        theBridgeSettings.setNestConfigured(theBridgeSettings.isValidNest());
		setupParams(Paths.get(theBridgeSettings.getConfigfile()), ".cfgbk", "habridge.config-");
	}

	public void loadConfig() {
		if(theBridgeSettings.getConfigfile() != null)
			_loadConfig();
	}
	private void _loadConfig() {
		Path configPath = Paths.get(theBridgeSettings.getConfigfile());
		_loadConfig(configPath);
    }

	private void _loadConfig(Path aPath) {
		String jsonContent = configReader(aPath);
		BridgeSettingsDescriptor aBridgeSettings = new Gson().fromJson(jsonContent, BridgeSettingsDescriptor.class);
		theBridgeSettings.setButtonsleep(aBridgeSettings.getButtonsleep());
		theBridgeSettings.setUpnpConfigAddress(aBridgeSettings.getUpnpConfigAddress());
		theBridgeSettings.setServerPort(aBridgeSettings.getServerPort());
		theBridgeSettings.setUpnpResponsePort(aBridgeSettings.getUpnpResponsePort());
		theBridgeSettings.setUpnpDeviceDb(aBridgeSettings.getUpnpDeviceDb());
		theBridgeSettings.setVeraAddress(aBridgeSettings.getVeraAddress());
		theBridgeSettings.setHarmonyAddress(aBridgeSettings.getHarmonyAddress());
		theBridgeSettings.setHarmonyUser(aBridgeSettings.getHarmonyUser());
		theBridgeSettings.setHarmonyPwd(aBridgeSettings.getHarmonyPwd());
		theBridgeSettings.setUpnpStrict(aBridgeSettings.isUpnpStrict());
		theBridgeSettings.setTraceupnp(aBridgeSettings.isTraceupnp());
		theBridgeSettings.setNestuser(aBridgeSettings.getNestuser());
		theBridgeSettings.setNestpwd(aBridgeSettings.getNestpwd());
		theBridgeSettings.setVeraconfigured(aBridgeSettings.isValidVera());
		theBridgeSettings.setHarmonyconfigured(aBridgeSettings.isValidHarmony());
		theBridgeSettings.setNestConfigured(aBridgeSettings.isValidNest());
		theBridgeSettings.setNumberoflogmessages(aBridgeSettings.getNumberoflogmessages());
		theBridgeSettings.setFarenheit(aBridgeSettings.isFarenheit());
    }

	public void save(BridgeSettingsDescriptor newBridgeSettings) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
        log.debug("Save HA Bridge settings.");
		Path configPath = Paths.get(theBridgeSettings.getConfigfile());
    	JsonTransformer aRenderer = new JsonTransformer();
    	String  jsonValue = aRenderer.render(newBridgeSettings);
    	configWriter(jsonValue, configPath);
    	_loadConfig(configPath);
    }
    

	private void configWriter(String content, Path filePath) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
		if(Files.exists(filePath) && !Files.isWritable(filePath)){
			log.error("Error file is not writable: " + filePath);
			return;
		}
		
		if(Files.notExists(filePath.getParent())) {
			try {
				Files.createDirectories(filePath.getParent());
			} catch (IOException e) {
				log.error("Error creating the directory: " + filePath + " message: " + e.getMessage(), e);
			}
		}

		try {
			Path target = null;
			if(Files.exists(filePath)) {
				target = FileSystems.getDefault().getPath(filePath.getParent().toString(), "habridge.config.old");
				Files.move(filePath, target);
			}
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
			if(target != null)
				Files.delete(target);
		} catch (IOException e) {
			log.error("Error writing the file: " + filePath + " message: " + e.getMessage(), e);
		}
	}
	
	private String configReader(Path filePath) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);

		String content = null;
		if(Files.notExists(filePath) || !Files.isReadable(filePath)){
			log.warn("Error reading the file: " + filePath + " - Does not exist or is not readable. continuing...");
			return null;
		}

		
		try {
			content = new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			log.error("Error reading the file: " + filePath + " message: " + e.getMessage(), e);
		}
		
		return content;
	}
}
