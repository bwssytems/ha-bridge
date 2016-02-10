package com.bwssystems.HABridge;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class BridgeSettings {
	private String upnpconfigaddress;
	private String serverport;
	private String upnpresponseport;
	private String upnpdevicedb;
	private IpList veraaddress;
	private IpList harmonyaddress;
	private String harmonyuser;
	private String harmonypwd;
	private Integer buttonsleep;
	private boolean upnpstrict;
	private boolean traceupnp;
	private boolean devmode;
	private String nestuser;
	private String nestpwd;
	private boolean veraconfigured;
	private boolean harmonyconfigured;
	private boolean nestconfigured;
	private String configfile;
	private boolean restart;
	private boolean stop;
	
	public BridgeSettings() {
		super();
		this.upnpstrict = true;
		this.devmode = false;
		this.traceupnp = false;
		this.restart = false;
		this.stop = false;
		this.nestconfigured = false;
		this.veraconfigured = false;
		this.harmonyconfigured = false;
	}
	public String getUpnpConfigAddress() {
		return upnpconfigaddress;
	}
	public void setUpnpConfigAddress(String upnpConfigAddress) {
		this.upnpconfigaddress = upnpConfigAddress;
	}
	public String getServerPort() {
		return serverport;
	}
	public void setServerPort(String serverPort) {
		this.serverport = serverPort;
	}
	public String getUpnpResponsePort() {
		return upnpresponseport;
	}
	public void setUpnpResponsePort(String upnpResponsePort) {
		this.upnpresponseport = upnpResponsePort;
	}
	public String getUpnpDeviceDb() {
		return upnpdevicedb;
	}
	public void setUpnpDeviceDb(String upnpDeviceDb) {
		this.upnpdevicedb = upnpDeviceDb;
	}
	public IpList getVeraAddress() {
		return veraaddress;
	}
	public void setVeraAddress(IpList veraAddress) {
		this.veraaddress = veraAddress;
	}
	public IpList getHarmonyAddress() {
		return harmonyaddress;
	}
	public void setHarmonyAddress(IpList harmonyaddress) {
		this.harmonyaddress = harmonyaddress;
	}
	public String getHarmonyUser() {
		return harmonyuser;
	}
	public void setHarmonyUser(String harmonyuser) {
		this.harmonyuser = harmonyuser;
	}
	public String getHarmonyPwd() {
		return harmonypwd;
	}
	public void setHarmonyPwd(String harmonypwd) {
		this.harmonypwd = harmonypwd;
	}
	public boolean isUpnpStrict() {
		return upnpstrict;
	}
	public void setUpnpStrict(boolean upnpStrict) {
		this.upnpstrict = upnpStrict;
	}
	public boolean isTraceupnp() {
		return traceupnp;
	}
	public void setTraceupnp(boolean traceupnp) {
		this.traceupnp = traceupnp;
	}
	public boolean isDevMode() {
		return devmode;
	}
	public void setDevMode(boolean devmode) {
		this.devmode = devmode;
	}
	public String getNestuser() {
		return nestuser;
	}
	public void setNestuser(String nestuser) {
		this.nestuser = nestuser;
	}
	public String getNestpwd() {
		return nestpwd;
	}
	public void setNestpwd(String nestpwd) {
		this.nestpwd = nestpwd;
	}
	public boolean isVeraconfigured() {
		return veraconfigured;
	}
	public void setVeraconfigured(boolean veraconfigured) {
		this.veraconfigured = veraconfigured;
	}
	public boolean isHarmonyconfigured() {
		return harmonyconfigured;
	}
	public void setHarmonyconfigured(boolean harmonyconfigured) {
		this.harmonyconfigured = harmonyconfigured;
	}
	public boolean isNestConfigured() {
		return nestconfigured;
	}
	public void setNestConfigured(boolean isNestConfigured) {
		this.nestconfigured = isNestConfigured;
	}
	public Integer getButtonsleep() {
		return buttonsleep;
	}
	public void setButtonsleep(Integer buttonsleep) {
		this.buttonsleep = buttonsleep;
	}
	public boolean isRestart() {
		return restart;
	}
	public void setRestart(boolean restart) {
		this.restart = restart;
	}
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public String getConfigfile() {
		return configfile;
	}
	public void setConfigfile(String configfile) {
		this.configfile = configfile;
	}
	public Boolean isValidVera() {
		if(this.veraaddress == null)
			return false;
		List<NamedIP> devicesList = this.veraaddress.getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;
		return true;
	}
	public Boolean isValidHarmony() {
		if(this.harmonyaddress == null)
			return false;		
		List<NamedIP> devicesList = this.harmonyaddress.getDevices();
		if(devicesList.get(0).getIp().contains(Configuration.DEFAULT_ADDRESS))
			return false;		if(this.harmonypwd == null || this.harmonypwd == "")
			return false;
		if(this.harmonyuser == null || this.harmonyuser == "")
			return false;
		return true;
	}
	public Boolean isValidNest() {
		if(this.nestpwd == null || this.nestpwd == "")
			return false;
		if(this.nestuser == null || this.nestuser == "")
			return false;
		return true;
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
	}

	private void _loadConfig() {
		Path configPath = Paths.get(getConfigfile());
		_loadConfig(configPath);
    }

	private void _loadConfig(Path aPath) {
		String jsonContent = configReader(aPath);
		BridgeSettings aBridgeSettings = new Gson().fromJson(jsonContent, BridgeSettings.class);
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
		this.setVeraconfigured(aBridgeSettings.isVeraconfigured());
		this.setHarmonyconfigured(aBridgeSettings.isHarmonyconfigured());
		this.setNestConfigured(aBridgeSettings.isNestConfigured());
    }

	public void save() {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
		Path configPath = Paths.get(getConfigfile());
    	JsonTransformer aRenderer = new JsonTransformer();
    	String  jsonValue = aRenderer.render(this);
    	configWriter(jsonValue, configPath);
        log.debug("Save HA Bridge settings.");
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
				target = FileSystems.getDefault().getPath(filePath.getParent().toString(), "device.db.old");
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

	public String backupConfig(String aFilename) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
		Path configPath = Paths.get(getConfigfile());
        if(aFilename == null || aFilename.equalsIgnoreCase("")) {
        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        	aFilename = "habridge.config-" + dateFormat.format(Calendar.getInstance().getTime()) + ".bk"; 
        }
        else
        	aFilename = aFilename + ".bk";
    	try {
			Files.copy(configPath, FileSystems.getDefault().getPath(configPath.getParent().toString(), aFilename), StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			log.error("Could not backup to file: " + aFilename + " message: " + e.getMessage(), e);
		}
        log.debug("Backup config: " + aFilename);
        return aFilename;
    }

	public String deleteConfigBackup(String aFilename) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
		Path configPath = Paths.get(getConfigfile());
        log.debug("Delete backup config: " + aFilename);
        try {
			Files.delete(FileSystems.getDefault().getPath(configPath.getParent().toString(), aFilename));
		} catch (IOException e) {
			log.error("Could not delete file: " + aFilename + " message: " + e.getMessage(), e);
		}
        return aFilename;
    }

	public String restoreConfigBackup(String aFilename) {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
		Path configPath = Paths.get(getConfigfile());
        log.debug("Restore backup config: " + aFilename);
		try {
			Path target = null;
			if(Files.exists(configPath)) {
	        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				target = FileSystems.getDefault().getPath(configPath.getParent().toString(), "habridge.config-" + dateFormat.format(Calendar.getInstance().getTime()) + ".bk");
				Files.move(configPath, target);
			}
			Files.copy(FileSystems.getDefault().getPath(configPath.getParent().toString(), aFilename), configPath, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			log.error("Error restoring the file: " + aFilename + " message: " + e.getMessage(), e);
			return null;
		}
		_loadConfig(configPath);
        return aFilename;
    }

	public List<String> getConfigBackups() {
        Logger log = LoggerFactory.getLogger(BridgeSettings.class);
		Path configPath = Paths.get(getConfigfile());
		List<String> theFilenames = new ArrayList<String>();
		Path dir = configPath.getParent();
		try (DirectoryStream<Path> stream =
		     Files.newDirectoryStream(dir, "*.{bk}")) {
		    for (Path entry: stream) {
		        theFilenames.add(entry.getFileName().toString());
		    }
		} catch (IOException x) {
		    // IOException can never be thrown by the iteration.
		    // In this snippet, it can // only be thrown by newDirectoryStream.
			log.error("Issue getting directory listing for config backups - " + x.getMessage());
		}
		return theFilenames;
	}
}
