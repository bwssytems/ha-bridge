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

import com.google.gson.Gson;

public class BridgeSettings extends BackupHandler {
	private BridgeSettingsDescriptor theBridgeSettings;
	private boolean reinit;
	private boolean stop;
	
	public BridgeSettings() {
		super();
		this.reinit = false;
		this.stop = false;
		theBridgeSettings = new BridgeSettingsDescriptor();
		theBridgeSettings.setUpnpStrict(true);
		theBridgeSettings.setDevMode(false);
		theBridgeSettings.setTraceupnp(false);
		theBridgeSettings.setNestConfigured(false);
		theBridgeSettings.setVeraconfigured(false);
		theBridgeSettings.setHarmonyconfigured(false);
	}
	public BridgeSettingsDescriptor getBridgeSettingsDescriptor() {
		return theBridgeSettings;
	}
	public String getUpnpConfigAddress() {
		return theBridgeSettings.getUpnpConfigAddress();
	}
	public void setUpnpConfigAddress(String upnpConfigAddress) {
		theBridgeSettings.setUpnpConfigAddress(upnpConfigAddress);
	}
	public String getServerPort() {
		return theBridgeSettings.getServerPort();
	}
	public void setServerPort(String serverPort) {
		theBridgeSettings.setServerPort(serverPort);
	}
	public String getUpnpResponsePort() {
		return theBridgeSettings.getUpnpResponsePort();
	}
	public void setUpnpResponsePort(String upnpResponsePort) {
		theBridgeSettings.setUpnpResponsePort(upnpResponsePort);
	}
	public String getUpnpDeviceDb() {
		return theBridgeSettings.getUpnpDeviceDb();
	}
	public void setUpnpDeviceDb(String upnpDeviceDb) {
		theBridgeSettings.setUpnpDeviceDb(upnpDeviceDb);
	}
	public IpList getVeraAddress() {
		return theBridgeSettings.getVeraAddress();
	}
	public void setVeraAddress(IpList veraAddress) {
		theBridgeSettings.setVeraAddress(veraAddress);
	}
	public IpList getHarmonyAddress() {
		return theBridgeSettings.getHarmonyAddress();
	}
	public void setHarmonyAddress(IpList harmonyaddress) {
		theBridgeSettings.setHarmonyAddress(harmonyaddress);
	}
	public String getHarmonyUser() {
		return theBridgeSettings.getHarmonyUser();
	}
	public void setHarmonyUser(String harmonyuser) {
		theBridgeSettings.setHarmonyUser(harmonyuser);
	}
	public String getHarmonyPwd() {
		return theBridgeSettings.getHarmonyPwd();
	}
	public void setHarmonyPwd(String harmonypwd) {
		theBridgeSettings.setHarmonyPwd(harmonypwd);
	}
	public boolean isUpnpStrict() {
		return theBridgeSettings.isUpnpStrict();
	}
	public void setUpnpStrict(boolean upnpStrict) {
		theBridgeSettings.setUpnpStrict(upnpStrict);
	}
	public boolean isTraceupnp() {
		return theBridgeSettings.isTraceupnp();
	}
	public void setTraceupnp(boolean traceupnp) {
		theBridgeSettings.setTraceupnp(traceupnp);
	}
	public boolean isDevMode() {
		return theBridgeSettings.isDevMode();
	}
	public void setDevMode(boolean devmode) {
		theBridgeSettings.setDevMode(devmode);
	}
	public String getNestuser() {
		return theBridgeSettings.getNestuser();
	}
	public void setNestuser(String nestuser) {
		theBridgeSettings.setNestuser(nestuser);
	}
	public String getNestpwd() {
		return theBridgeSettings.getNestpwd();
	}
	public void setNestpwd(String nestpwd) {
		theBridgeSettings.setNestpwd(nestpwd);
	}
	public boolean isVeraconfigured() {
		return theBridgeSettings.isVeraconfigured();
	}
	public void setVeraconfigured(boolean veraconfigured) {
		theBridgeSettings.setVeraconfigured(veraconfigured);
	}
	public boolean isHarmonyconfigured() {
		return theBridgeSettings.isHarmonyconfigured();
	}
	public void setHarmonyconfigured(boolean harmonyconfigured) {
		theBridgeSettings.setHarmonyconfigured(harmonyconfigured);
	}
	public boolean isNestConfigured() {
		return theBridgeSettings.isNestConfigured();
	}
	public void setNestConfigured(boolean isNestConfigured) {
		theBridgeSettings.setNestConfigured(isNestConfigured);
	}
	public Integer getButtonsleep() {
		return theBridgeSettings.getButtonsleep();
	}
	public void setButtonsleep(Integer buttonsleep) {
		theBridgeSettings.setButtonsleep(buttonsleep);
	}
	public String getConfigfile() {
		return theBridgeSettings.getConfigfile();
	}
	public void setConfigfile(String configfile) {
		theBridgeSettings.setConfigfile(configfile);
	}
	public boolean isReinit() {
		return reinit;
	}
	public void setReinit(boolean reinit) {
		this.reinit = reinit;
	}
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
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
        	setConfigfile(configFileProperty);
        	_loadConfig();
        }
        else
        {
        	log.info("reading from system properties");
        	setConfigfile(Configuration.CONFIG_FILE);
	        setServerPort(System.getProperty("server.port"));
	        setUpnpConfigAddress(System.getProperty("upnp.config.address"));
	        setUpnpDeviceDb(System.getProperty("upnp.device.db"));
	        setUpnpResponsePort(System.getProperty("upnp.response.port"));
	        
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
	        setVeraAddress(theVeraList);

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
	        setHarmonyAddress(theHarmonyList);
	        setHarmonyUser(System.getProperty("harmony.user"));
	        setHarmonyPwd(System.getProperty("harmony.pwd"));
	        setUpnpStrict(Boolean.parseBoolean(System.getProperty("upnp.strict", "true")));
	        setTraceupnp(Boolean.parseBoolean(System.getProperty("trace.upnp", "false")));
	        setDevMode(Boolean.parseBoolean(System.getProperty("dev.mode", "false")));
	        setButtonsleep(Integer.parseInt(System.getProperty("button.sleep", Configuration.DFAULT_BUTTON_SLEEP)));
	        setNestuser(System.getProperty("nest.user"));
	        setNestpwd(System.getProperty("nest.pwd"));
        }

        if(getUpnpConfigAddress() == null) {
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
	        
	        setUpnpConfigAddress(addressString);
        }
        
        if(this.getUpnpResponsePort() == null)
        	this.setUpnpResponsePort(Configuration.UPNP_RESPONSE_PORT);
        
        if(this.getServerPort() == null)
        	this.setServerPort(Configuration.DFAULT_WEB_PORT);
        
        if(this.getUpnpDeviceDb() == null)
        	this.setUpnpDeviceDb(Configuration.DEVICE_DB_DIRECTORY);
        
        if(this.getButtonsleep() <= 0)
        	this.setButtonsleep(Integer.parseInt(Configuration.DFAULT_BUTTON_SLEEP));

        this.setVeraconfigured(theBridgeSettings.isValidVera());
		this.setHarmonyconfigured(theBridgeSettings.isValidHarmony());
		this.setNestConfigured(theBridgeSettings.isValidNest());
		setupParams(Paths.get(getConfigfile()), ".cfgbk", "habridge.config-");
	}

	public void loadConfig() {
		if(getConfigfile() != null)
			_loadConfig();
	}
	private void _loadConfig() {
		Path configPath = Paths.get(getConfigfile());
		_loadConfig(configPath);
    }

	private void _loadConfig(Path aPath) {
		String jsonContent = configReader(aPath);
		BridgeSettingsDescriptor aBridgeSettings = new Gson().fromJson(jsonContent, BridgeSettingsDescriptor.class);
		this.setButtonsleep(aBridgeSettings.getButtonsleep());
		this.setUpnpConfigAddress(aBridgeSettings.getUpnpConfigAddress());
		this.setServerPort(aBridgeSettings.getServerPort());
		this.setUpnpResponsePort(aBridgeSettings.getUpnpResponsePort());
		this.setUpnpDeviceDb(aBridgeSettings.getUpnpDeviceDb());
		this.setVeraAddress(aBridgeSettings.getVeraAddress());
		this.setHarmonyAddress(aBridgeSettings.getHarmonyAddress());
		this.setHarmonyUser(aBridgeSettings.getHarmonyUser());
		this.setHarmonyPwd(aBridgeSettings.getHarmonyPwd());
		this.setUpnpStrict(aBridgeSettings.isUpnpStrict());
		this.setTraceupnp(aBridgeSettings.isTraceupnp());
		this.setDevMode(aBridgeSettings.isDevMode());
		this.setNestuser(aBridgeSettings.getNestuser());
		this.setNestpwd(aBridgeSettings.getNestpwd());
		this.setVeraconfigured(aBridgeSettings.isValidVera());
		this.setHarmonyconfigured(aBridgeSettings.isValidHarmony());
		this.setNestConfigured(aBridgeSettings.isValidNest());
    }

	public void save(BridgeSettings newBridgeSettings) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
        log.debug("Save HA Bridge settings.");
		Path configPath = Paths.get(getConfigfile());
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
