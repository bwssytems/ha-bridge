package com.bwssystems.HABridge;

import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.dao.BackupFilename;
import com.google.gson.Gson;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;

public class SystemControl {
    private static final Logger log = LoggerFactory.getLogger(SystemControl.class);
    public static final String CYCLIC_BUFFER_APPENDER_NAME = "CYCLIC";
    private LoggerContext lc; 
    private static final String SYSTEM_CONTEXT = "/system";
    private BridgeSettings bridgeSettings;
    private Version version;
    private CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender;

	public SystemControl(BridgeSettings theBridgeSettings, Version theVersion) {
        this.bridgeSettings = theBridgeSettings;
		this.version = theVersion;
		this.lc = (LoggerContext) LoggerFactory.getILoggerFactory(); 
		reacquireCBA();
	}

//	This function sets up the sparkjava rest calls for the hue api
    public void setupServer() {
    	log.info("System control service started....");
	    // http://ip_address:port/system/habridge/version gets the version of this bridge instance
    	get (SYSTEM_CONTEXT + "/habridge/version", "application/json", (request, response) -> {
	    	log.debug("Get HA Bridge version: v" + version.getVersion());
			response.status(HttpStatus.SC_OK);
	        return "{\"version\":\"" + version.getVersion() + "\"}";
	    });

	    // http://ip_address:port/system/logmsgs gets the version of this bridge instance
    	get (SYSTEM_CONTEXT + "/logmsgs", "application/json", (request, response) -> {
			log.debug("Get logmsgs.");
			response.status(HttpStatus.SC_OK);
			String logMsgs;
		    int count = -1;
		    if(cyclicBufferAppender == null)
		    	reacquireCBA();
		    if (cyclicBufferAppender != null) {
		      count = cyclicBufferAppender.getLength();
		    }
		    logMsgs = "[";
		    if (count == -1) {
		      logMsgs = logMsgs + "{\"message\":\"Failed to locate CyclicBuffer\"}";
		    } else if (count == 0) {
		    	logMsgs = logMsgs + "{\"message\":\"No logging events to display\"}";
		    } else {
				LoggingEvent le;
				for (int i = 0; i < count; i++) {
					le = (LoggingEvent) cyclicBufferAppender.get(i);
					logMsgs = logMsgs + ( i > 0?",{":"{") + "\"message\":\"" + le.getFormattedMessage() + "\"}";
				}
		    }
		    logMsgs = logMsgs + "]";
			return logMsgs;
	    });

//      http://ip_address:port/system/settings which returns the bridge configuration settings
		get(SYSTEM_CONTEXT + "/settings", "application/json", (request, response) -> {
			log.debug("bridge settings requested from " + request.ip());

			response.status(200);

            return bridgeSettings.getBridgeSettingsDescriptor();
        }, new JsonTransformer());
		
//      http://ip_address:port/system/settings CORS request
	    options(SYSTEM_CONTEXT + "/settings", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/settings which returns the bridge configuration settings
		put(SYSTEM_CONTEXT + "/settings", "application/json", (request, response) -> {
			log.debug("save bridge settings requested from " + request.ip() + " with body: " + request.body());
			BridgeSettingsDescriptor newBridgeSettings = new Gson().fromJson(request.body(), BridgeSettingsDescriptor.class);
			bridgeSettings.save(newBridgeSettings);
			response.status(200);

            return bridgeSettings.getBridgeSettingsDescriptor();
        }, new JsonTransformer());
		
	    // http://ip_address:port/system/control/reinit CORS request
	    options(SYSTEM_CONTEXT + "/control/reinit", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
	    // http://ip_address:port/system/control/reinit sets the parameter reinit the server
	    put(SYSTEM_CONTEXT + "/control/reinit", "application/json", (request, response) -> {
	    	return reinit();
	    });

	    // http://ip_address:port/system/control/stop CORS request
	    options(SYSTEM_CONTEXT + "/control/stop", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
	    // http://ip_address:port/system/control/stop sets the parameter stop the server
	    put(SYSTEM_CONTEXT + "/control/stop", "application/json", (request, response) -> {
	    	return stop();
	    });

	    // http://ip_address:port/system/backup/available returns a list of config backup filenames
	    get (SYSTEM_CONTEXT + "/backup/available", "application/json", (request, response) -> {
        	log.debug("Get backup filenames");
          	response.status(HttpStatus.SC_OK);
          	return bridgeSettings.getBackups();
        }, new JsonTransformer());

	    // http://ip_address:port/system/backup/create CORS request
	    options(SYSTEM_CONTEXT + "/backup/create", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	put (SYSTEM_CONTEXT + "/backup/create", "application/json", (request, response) -> {
	    	log.debug("Create backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	BackupFilename returnFilename = new BackupFilename();
        	returnFilename.setFilename(bridgeSettings.backup(aFilename.getFilename()));
	        return returnFilename;
    	}, new JsonTransformer());

	    // http://ip_address:port/system/backup/delete CORS request
	    options(SYSTEM_CONTEXT + "/backup/delete", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "POST");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post (SYSTEM_CONTEXT + "/backup/delete", "application/json", (request, response) -> {
	    	log.debug("Delete backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	if(aFilename != null)
        		bridgeSettings.deleteBackup(aFilename.getFilename());
        	else
        		log.warn("No filename given for delete backup.");
	        return null;
	    }, new JsonTransformer());

	    // http://ip_address:port/system/backup/restore CORS request
	    options(SYSTEM_CONTEXT + "/backup/restore", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "POST");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post (SYSTEM_CONTEXT + "/backup/restore", "application/json", (request, response) -> {
	    	log.debug("Restore backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	if(aFilename != null) {
        		bridgeSettings.restoreBackup(aFilename.getFilename());
        		bridgeSettings.loadConfig();
        	}
        	else
        		log.warn("No filename given for restore backup.");
	        return null;
	    }, new JsonTransformer());
    }
    
    void reacquireCBA() {
        cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) lc.getLogger(
            Logger.ROOT_LOGGER_NAME).getAppender(CYCLIC_BUFFER_APPENDER_NAME);
      }

    protected void pingListener() {
        try {
            byte[] buf = new byte[256];
            String testData = "M-SEARCH * HTTP/1.1\nHOST: " + Configuration.UPNP_MULTICAST_ADDRESS + ":" + Configuration.UPNP_DISCOVERY_PORT + "ST: urn:schemas-upnp-org:device:CloudProxy:1\nMAN: \"ssdp:discover\"\nMX: 3";
            buf = testData.getBytes();
            MulticastSocket socket = new MulticastSocket(Configuration.UPNP_DISCOVERY_PORT);

            InetAddress group = InetAddress.getByName(Configuration.UPNP_MULTICAST_ADDRESS);
            DatagramPacket packet;
            packet = new DatagramPacket(buf, buf.length, group, Configuration.UPNP_DISCOVERY_PORT);
            socket.send(packet);

            socket.close();
        }
        catch (IOException e) {
        	log.warn("Error pinging listener. " + e.getMessage());
        }
    }
    
    public String reinit() {
    	bridgeSettings.getBridgeControl().setReinit(true);
    	pingListener();
    	return "{\"control\":\"reiniting\"}";
    }
    
    public String stop() {
    	bridgeSettings.getBridgeControl().setStop(true);
    	pingListener();
    	return "{\"control\":\"stopping\"}";    	
    }
}