package com.bwssystems.HABridge;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.util.BackupHandler;
import com.bwssystems.HABridge.util.JsonTransformer;
import com.bwssystems.HABridge.util.ParseRoute;
import com.google.gson.Gson;

public class BridgeSettings extends BackupHandler {
	private static final Logger log = LoggerFactory.getLogger(BridgeSettings.class);
	private BridgeSettingsDescriptor theBridgeSettings;
	private BridgeControlDescriptor bridgeControl;
	private BridgeSecurity bridgeSecurity;
	private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	
	public BridgeSettings() {
		super();
		bridgeControl = new BridgeControlDescriptor();
		theBridgeSettings = new BridgeSettingsDescriptor();
		bridgeSecurity = null;
		String theKey = System.getProperty("security.key");
		if(theKey == null)
			theKey = "IWantMyPasswordsToBeAbleToBeDecodedPleaseSeeTheReadme";
		String execGarden = System.getProperty("exec.garden");
		bridgeSecurity = new BridgeSecurity(theKey.toCharArray(), execGarden);
		String ipV6Stack = System.getProperty("ipV6Stack");
        if(ipV6Stack == null || !ipV6Stack.equalsIgnoreCase("true")) {
        	System.setProperty("java.net.preferIPv4Stack" , "true");
        }

	}
	public BridgeControlDescriptor getBridgeControl() {
		return bridgeControl;
	}
	public BridgeSettingsDescriptor getBridgeSettingsDescriptor() {
		return theBridgeSettings;
	}
	public BridgeSecurity getBridgeSecurity() {
		return bridgeSecurity;
	}
	public String getCurrentDate() {
		return LocalDateTime.now().format(dateTimeFormat);
	}

	public void buildSettings() {
        String addressString = null;
        String theVeraAddress = null;
        String theFibaroAddress = null;
        String theSomfyAddress = null;
        String theHarmonyAddress = null;
        String configFileProperty = System.getProperty("config.file");
        if(configFileProperty == null) {
        	Path filePath = Paths.get(Configuration.CONFIG_FILE);
        	if(Files.exists(filePath) && Files.isReadable(filePath))
        		configFileProperty = Configuration.CONFIG_FILE;
        }
        String serverPortOverride = System.getProperty("server.port");
        String serverIpOverride = System.getProperty("server.ip");
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
        	theBridgeSettings.setUpnpGroupDb(System.getProperty("upnp.group.db"));
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
	        
	        theFibaroAddress = System.getProperty("fibaro.address");
        	IpList theFibaroList = null;
	        if(theFibaroAddress != null) {
		        try {
		        	theFibaroList = new Gson().fromJson(theFibaroAddress, IpList.class);
		        } catch (Exception e) {
		        	try {
		        		theFibaroList = new Gson().fromJson("{devices:[{name:default,ip:" + theFibaroAddress + "}]}", IpList.class);
		        	} catch (Exception et) {
		    	        log.error("Cannot parse fibaro.address, not set with message: " + e.getMessage(), e);
		    	        theFibaroList = null;
		        	}
		        }
	        }
	        theBridgeSettings.setFibaroAddress(theFibaroList);

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

			theSomfyAddress = System.getProperty("somfy.address");
			IpList theSomfyList = null;
			if(theSomfyAddress != null) {
				try {
					theSomfyList = new Gson().fromJson(theSomfyAddress, IpList.class);
				} catch (Exception e) {
					try {
						theSomfyList = new Gson().fromJson("{devices:[{name:default,ip:" + theSomfyAddress + "}]}", IpList.class);
					} catch (Exception et) {
						log.error("Cannot parse somfy.address, not set with message: " + e.getMessage(), e);
						theSomfyList = null;
					}
				}
			}
			theBridgeSettings.setSomfyAddress(theSomfyList);

	        // theBridgeSettings.setUpnpStrict(Boolean.parseBoolean(System.getProperty("upnp.strict", "true")));
	        theBridgeSettings.setTraceupnp(Boolean.parseBoolean(System.getProperty("trace.upnp", "false")));
	        theBridgeSettings.setButtonsleep(Integer.parseInt(System.getProperty("button.sleep", Configuration.DEFAULT_BUTTON_SLEEP)));
	        theBridgeSettings.setNestuser(System.getProperty("nest.user"));
	        theBridgeSettings.setNestpwd(System.getProperty("nest.pwd"));
        }

		ParseRoute aDefaultRoute = ParseRoute.getInstance();
        if(theBridgeSettings.getUpnpConfigAddress() == null || theBridgeSettings.getUpnpConfigAddress().trim().equals("") || theBridgeSettings.getUpnpConfigAddress().trim().equals("0.0.0.0")) {
        	addressString = aDefaultRoute.getLocalIPAddress();
        	if(addressString != null) {
        		theBridgeSettings.setUpnpConfigAddress(addressString);
        		log.info("Adding " + addressString + " as our default upnp config address.");
        	}
        	else
		        log.error("Cannot get ip address of this host.");
        }
        else {
        	addressString = checkIpAddress(theBridgeSettings.getUpnpConfigAddress(), false);
        	if(addressString == null) {
				addressString = aDefaultRoute.getLocalIPAddress();
				log.warn("The upnp config address, " + theBridgeSettings.getUpnpConfigAddress() + ", does not match any known IP's on this host. Using default address: " + addressString);
			}
        }
        
        if(theBridgeSettings.getUpnpResponsePort() == null)
        	theBridgeSettings.setUpnpResponsePort(Configuration.UPNP_RESPONSE_PORT);
        
        if(theBridgeSettings.getServerPort() == null)
        	theBridgeSettings.setServerPort(Configuration.DEFAULT_WEB_PORT);
        
        if(theBridgeSettings.getUpnpDeviceDb() == null)
        	theBridgeSettings.setUpnpDeviceDb(Configuration.DEVICE_DB_DIRECTORY);

        if(theBridgeSettings.getUpnpGroupDb() == null)
        	theBridgeSettings.setUpnpGroupDb(Configuration.GROUP_DB_DIRECTORY);
        
        if(theBridgeSettings.getNumberoflogmessages() == null || theBridgeSettings.getNumberoflogmessages() <= 0)
        	theBridgeSettings.setNumberoflogmessages(Integer.valueOf(Configuration.NUMBER_OF_LOG_MESSAGES));

        if(theBridgeSettings.getButtonsleep() == null || theBridgeSettings.getButtonsleep() < 0)
			theBridgeSettings.setButtonsleep(Integer.parseInt(Configuration.DEFAULT_BUTTON_SLEEP));
			
		if(theBridgeSettings.getLinkbuttontimeout() < 30)
			theBridgeSettings.setLinkbuttontimeout(Configuration.LINK_BUTTON_TIMEOUT);

        theBridgeSettings.setVeraconfigured(theBridgeSettings.isValidVera());
        theBridgeSettings.setFibaroconfigured(theBridgeSettings.isValidFibaro());
        theBridgeSettings.setHarmonyconfigured(theBridgeSettings.isValidHarmony());
        theBridgeSettings.setNestConfigured(theBridgeSettings.isValidNest());
        theBridgeSettings.setHueconfigured(theBridgeSettings.isValidHue());
        theBridgeSettings.setHalconfigured(theBridgeSettings.isValidHal());
        theBridgeSettings.setMqttconfigured(theBridgeSettings.isValidMQTT());
        theBridgeSettings.setHassconfigured(theBridgeSettings.isValidHass());
        theBridgeSettings.setDomoticzconfigured(theBridgeSettings.isValidDomoticz());
		theBridgeSettings.setSomfyconfigured(theBridgeSettings.isValidSomfy());
		theBridgeSettings.setHomeWizardConfigured(theBridgeSettings.isValidHomeWizard());
		theBridgeSettings.setOpenhabconfigured(theBridgeSettings.isValidOpenhab());
		theBridgeSettings.setFhemconfigured(theBridgeSettings.isValidFhem());
		theBridgeSettings.setMoziotconfigured(theBridgeSettings.isValidMozIot());
		theBridgeSettings.setHomegenieconfigured(theBridgeSettings.isValidHomeGenie());
        // Lifx is either configured or not, so it does not need an update.
       if(serverPortOverride != null)
        	theBridgeSettings.setServerPort(serverPortOverride);
        if(serverIpOverride != null) {
        	theBridgeSettings.setWebaddress(serverIpOverride);
        	theBridgeSettings.setUpnpConfigAddress(serverIpOverride);
		}

		setupParams(Paths.get(theBridgeSettings.getConfigfile()), ".cfgbk", "habridge.config-");
		
		bridgeSecurity.setSecurityData(theBridgeSettings.getSecurityData());
		if(theBridgeSettings.getWhitelist() != null) {
			bridgeSecurity.convertWhitelist(theBridgeSettings.getWhitelist());
			theBridgeSettings.removeWhitelist();
			updateConfigFile();
		}
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
		if(jsonContent == null)
			return;
		try {
			theBridgeSettings = new Gson().fromJson(jsonContent, BridgeSettingsDescriptor.class);
			theBridgeSettings.setConfigfile(aPath.toString());
		} catch (Exception e) {
			log.warn("Issue loading values from file: " + aPath.toUri().toString() + ", Gson convert failed. Using default settings.");
			theBridgeSettings = new BridgeSettingsDescriptor();
		}
    }

	public void save(BridgeSettingsDescriptor newBridgeSettings) {
        log.debug("Save HA Bridge settings.");
		Path configPath = Paths.get(theBridgeSettings.getConfigfile());
    	JsonTransformer aRenderer = new JsonTransformer();
    	if(bridgeSecurity.isSettingsChanged()) {
    		try {
				newBridgeSettings.setSecurityData(bridgeSecurity.getSecurityDescriptorData());
			} catch (UnsupportedEncodingException e) {
				log.warn("could not get encoded security data: " + e.getMessage());
				return;
			} catch (GeneralSecurityException e) {
				log.warn("could not get encoded security data: " + e.getMessage());
				return;
			}
    		bridgeSecurity.setSettingsChanged(false);
    	}
    	String  jsonValue = aRenderer.render(newBridgeSettings);
    	configWriter(jsonValue, configPath);
    	_loadConfig(configPath);
    }
    

	public void updateConfigFile() {
        log.debug("Save HA Bridge settings.");
		Path configPath = Paths.get(theBridgeSettings.getConfigfile());
    	JsonTransformer aRenderer = new JsonTransformer();
    	if(bridgeSecurity.isSettingsChanged()) {
    		try {
				theBridgeSettings.setSecurityData(bridgeSecurity.getSecurityDescriptorData());
			} catch (UnsupportedEncodingException e) {
				log.warn("could not get encoded security data: " + e.getMessage());
				return;
			} catch (GeneralSecurityException e) {
				log.warn("could not get encoded security data: " + e.getMessage());
				return;
			}
    		bridgeSecurity.setSettingsChanged(false);
    	}
    	String  jsonValue = aRenderer.render(theBridgeSettings);
    	configWriter(jsonValue, configPath);
    	_loadConfig(configPath);
    }
    

	private synchronized void configWriter(String content, Path filePath) {
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
				target = FileSystems.getDefault().getPath(filePath.getParent().toString(), "habridge.config.old." + getCurrentDate());
				Files.move(filePath, target);
			}
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);

			// set attributes to be for user only
	        // using PosixFilePermission to set file permissions
	        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
	        // add owners permission 
	        perms.add(PosixFilePermission.OWNER_READ);
	        perms.add(PosixFilePermission.OWNER_WRITE);
	        
	        try {
	        	String osName = System.getProperty("os.name");
	        	if(osName.toLowerCase().indexOf("win") < 0)
	        		Files.setPosixFilePermissions(filePath, perms);
	        } catch(UnsupportedOperationException e) {
	        	log.info("Cannot set permissions for config file on this system as it is not supported. Continuing");
	        }
			if(target != null)
				Files.delete(target);
		} catch (IOException e) {
			log.error("Error writing the file: " + filePath + " message: " + e.getMessage(), e);
		}
	}
	
	private String configReader(Path filePath) {
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
	
	private String checkIpAddress(String ipAddress, boolean checkForLocalhost) {
		Enumeration<NetworkInterface> ifs =	null;
		try {
			ifs =	NetworkInterface.getNetworkInterfaces();
		} catch(SocketException e) {
	        log.error("checkIpAddress cannot get ip address of this host, Exiting with message: " + e.getMessage(), e);
	        return null;			
		}
		
		String addressString = null;
        InetAddress address = null;
		while (ifs.hasMoreElements() && addressString == null) {
			NetworkInterface xface = ifs.nextElement();
			Enumeration<InetAddress> addrs = xface.getInetAddresses();
			String name = xface.getName();
			int IPsPerNic = 0;

			while (addrs.hasMoreElements() && IPsPerNic == 0) {
				address = addrs.nextElement();
				if (InetAddressUtils.isIPv4Address(address.getHostAddress())) {
					log.debug(name + " ... has IPV4 addr " + address);
					if(checkForLocalhost && (!name.equalsIgnoreCase(Configuration.LOOP_BACK_INTERFACE) || !address.getHostAddress().equalsIgnoreCase(Configuration.LOOP_BACK_ADDRESS))) {
						IPsPerNic++;
						addressString = address.getHostAddress();
						log.debug("checkIpAddress found " + addressString + " from interface " + name);
					}
					else if(ipAddress != null && ipAddress.equalsIgnoreCase(address.getHostAddress())){
						addressString = ipAddress;
						IPsPerNic++;
					}
				}
			}
		}
		return addressString;
	}
}
