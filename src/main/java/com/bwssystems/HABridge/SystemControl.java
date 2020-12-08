package com.bwssystems.HABridge;

import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.before;
import static spark.Spark.halt;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.Base64;
//import java.util.Iterator;
//import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.api.hue.WhitelistEntry;
import com.bwssystems.HABridge.dao.BackupFilename;
import com.bwssystems.HABridge.util.JsonTransformer;
import com.bwssystems.HABridge.util.TextStringFormatter;
import com.bwssystems.logservices.LoggerInfo;
import com.bwssystems.logservices.LoggingForm;
import com.bwssystems.logservices.LoggingManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
//import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.CyclicBufferAppender;

public class SystemControl {
    private static final Logger log = LoggerFactory.getLogger(SystemControl.class);
    public static final String CYCLIC_BUFFER_APPENDER_NAME = "CYCLIC";
    private LoggerContext lc; 
    private static final String SYSTEM_CONTEXT = "/system";
    private BridgeSettings bridgeSettings;
    private Version version;
    private CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender;
    private DateFormat dateFormat;
    private LoggingManager theLogServiceMgr;


	public SystemControl(BridgeSettings theBridgeSettings, Version theVersion) {
        this.bridgeSettings = theBridgeSettings;
		this.version = theVersion;
		this.lc = (LoggerContext) LoggerFactory.getILoggerFactory(); 
		this.dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");
		setupLoggerSettings();
		theLogServiceMgr = new LoggingManager();
		theLogServiceMgr.init();
	}

//	This function sets up the sparkjava rest calls for the hue api
    public void setupServer() {
    	log.info("System control service started....");
		before(SYSTEM_CONTEXT + "/*", (request, response) -> {
			if(bridgeSettings.getBridgeSecurity().isSecure()) {
				String pathInfo = request.pathInfo();
				if(pathInfo == null || (!pathInfo.equals(SYSTEM_CONTEXT + "/login") && !pathInfo.equals(SYSTEM_CONTEXT + "/habridge/version"))) {
					User authUser = bridgeSettings.getBridgeSecurity().getAuthenticatedUser(request);
					if(authUser == null) {
						halt(401, "{\"message\":\"User not authenticated\"}");
					}
				}
			}
		});
	    // http://ip_address:port/system/habridge/version gets the version of this bridge instance
    	get (SYSTEM_CONTEXT + "/habridge/version", (request, response) -> {
	    	log.debug("Get HA Bridge version: v" + version.getVersion());
			response.status(HttpStatus.SC_OK);
			response.type("application/json");
	        return "{\"version\":\"" + version.getVersion() + "\",\"isSecure\":" + bridgeSettings.getBridgeSecurity().isSecure() + "}";
	    });

	    // http://ip_address:port/system/logmsgs gets the log messages for the bridge
    	get (SYSTEM_CONTEXT + "/logmsgs", (request, response) -> {
			log.debug("Get logmsgs.");
			String logMsgs;
		    int count = -1;
		    if(cyclicBufferAppender == null)
		    	setupLoggerSettings();
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
					logMsgs = logMsgs + ( i > 0?",{":"{") + "\"time\":\"" + dateFormat.format(le.getTimeStamp()) + "\",\"level\":\"" + le.getLevel().levelStr + "\",\"component\":\"" + le.getLoggerName() + "\",\"message\":\"" + TextStringFormatter.forJSON(le.getFormattedMessage()) + "\"}";
				}
		    }
		    logMsgs = logMsgs + "]";
			response.status(HttpStatus.SC_OK);
			response.type("application/json");
			return logMsgs;
	    });

	    // http://ip_address:port/system/logmgmt/loggers gets the logger info for the bridge
    	get (SYSTEM_CONTEXT + "/logmgmt/loggers/:all", (request, response) -> {
			log.debug("Get loggers info with showAll argument: " + request.params(":all"));
			Boolean showAll = false;
			if(request.params(":all").equals("true"))
				showAll = true;
			theLogServiceMgr.setShowAll(showAll);
			theLogServiceMgr.init();
			response.status(HttpStatus.SC_OK);
			response.type("application/json");
			return theLogServiceMgr.getConfiguredLoggers();
	    }, new JsonTransformer());

//      http://ip_address:port/system/setpassword CORS request
	    options(SYSTEM_CONTEXT + "/setpassword", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/setpassword which sets a password for a given user
		post(SYSTEM_CONTEXT + "/setpassword", (request, response) -> {
			log.debug("setpassword....");
			String theDecodedPayload = new String(Base64.getDecoder().decode(request.body()));
			User theUser = new Gson().fromJson(theDecodedPayload, User.class);
			String errorMessage = bridgeSettings.getBridgeSecurity().setPassword(theUser);
			if(errorMessage != null) {
		        response.status(HttpStatus.SC_BAD_REQUEST);
		        errorMessage = "{\"message\":\"" + errorMessage + "\"}";
			} else {
		        response.status(HttpStatus.SC_OK);
		        bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
			}

			if(errorMessage == null)
				errorMessage = "{}";
			response.type("application/json");
            return errorMessage;
        });

//      http://ip_address:port/system/adduser CORS request
	    options(SYSTEM_CONTEXT + "/adduser", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/adduser which adds a new user
		put(SYSTEM_CONTEXT + "/adduser", (request, response) -> {
			log.debug("adduser....");
			String theDecodedPayload = new String(Base64.getDecoder().decode(request.body()));
			User theUser = new Gson().fromJson(theDecodedPayload, User.class);
			String errorMessage = theUser.validate();
			if(errorMessage != null) {
		        response.status(HttpStatus.SC_BAD_REQUEST);
		        errorMessage = "{\"message\":\"" + errorMessage + "\"}";
			} else {
		        errorMessage = bridgeSettings.getBridgeSecurity().addUser(theUser);
				if(errorMessage == null) {
			        response.status(HttpStatus.SC_OK);
			        bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
				} else {
			        response.status(HttpStatus.SC_BAD_REQUEST);
			        errorMessage = "{\"message\":\"" + errorMessage + "\"}";
				}
			}

			if(errorMessage == null)
				errorMessage = "{}";
			response.type("application/json");
            return errorMessage;
        });

//      http://ip_address:port/system/deluser CORS request
	    options(SYSTEM_CONTEXT + "/deluser", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/deluser which dels a user
		put(SYSTEM_CONTEXT + "/deluser", (request, response) -> {
			log.debug("deluser....");
			String theDecodedPayload = new String(Base64.getDecoder().decode(request.body()));
			User theUser = new Gson().fromJson(theDecodedPayload, User.class);
	        String errorMessage = bridgeSettings.getBridgeSecurity().delUser(theUser);
			if(errorMessage != null) {
		        response.status(HttpStatus.SC_BAD_REQUEST);
		        errorMessage = "{\"message\":\"" + errorMessage + "\"}";
			} else {
		        response.status(HttpStatus.SC_OK);
				bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
			}

			if(errorMessage == null)
				errorMessage = "{}";
			response.type("application/json");
            return errorMessage;
        });

//      http://ip_address:port/system/login CORS request
	    options(SYSTEM_CONTEXT + "/login", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/login validates the login
		post(SYSTEM_CONTEXT + "/login", (request, response) -> {
			log.debug("login....");
			String theDecodedPayload = new String(Base64.getDecoder().decode(request.body()));
			User theUser = new Gson().fromJson(theDecodedPayload, User.class);
			LoginResult result = bridgeSettings.getBridgeSecurity().validatePassword(theUser);
			if(result.getUser() != null)
				bridgeSettings.getBridgeSecurity().addAuthenticatedUser(request, theUser);
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
            return result;
        }, new JsonTransformer());

		// http://ip_address:port/system/logout CORS request
		options(SYSTEM_CONTEXT + "/logout", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html; charset=utf-8");
			return "";
		});
		// http://ip_address:port/system/logout invalidates user session
		put(SYSTEM_CONTEXT + "/logout", (request, response) -> {
			log.debug("logout....");
			bridgeSettings.getBridgeSecurity().removeAuthenticatedUser(request);
			response.status(HttpStatus.SC_OK);
			response.type("application/json");
			return "";
		});

//      http://ip_address:port/system/presslinkbutton CORS request
	    options(SYSTEM_CONTEXT + "/presslinkbutton", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/presslinkbutton which sets the link button for device registration
		put(SYSTEM_CONTEXT + "/presslinkbutton", (request, response) -> {
			LinkParams linkParams = null;
			if(!request.body().isEmpty()) {
				linkParams = new Gson().fromJson(request.body(), LinkParams.class);
				if(linkParams.getSeconds() <= 0)
					linkParams.setSeconds(3);
			}
			else {
				linkParams = new LinkParams();
				linkParams.setSilent(false);
				linkParams.setSeconds(bridgeSettings.getBridgeSettingsDescriptor().getLinkbuttontimeout());
			}
			if(!linkParams.isSilent())
				log.info("Link button pressed....");
			bridgeSettings.getBridgeControl().setLinkButton(true);
			Timer theTimer = new Timer();
			theTimer.schedule(new LinkButtonPressed(bridgeSettings.getBridgeControl(), theTimer, linkParams.isSilent()), (linkParams.getSeconds() * 1000));
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
            return "";
        }, new JsonTransformer());

	    // http://ip_address:port/system/securityinfo gets the security info for the bridge
    	get (SYSTEM_CONTEXT + "/securityinfo", (request, response) -> {
			log.debug("Get security info");
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
			return bridgeSettings.getBridgeSecurity().getSecurityInfo();
	    }, new JsonTransformer());

//      http://ip_address:port/system/changesecurityinfo CORS request
	    options(SYSTEM_CONTEXT + "/changesecurityinfo", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/changesecurityinfo which sets the security settings other than passwords and users
		post(SYSTEM_CONTEXT + "/changesecurityinfo", (request, response) -> {
			log.debug("changesecurityinfo....");
			SecurityInfo theInfo = new Gson().fromJson(request.body(), SecurityInfo.class);
			bridgeSettings.getBridgeSecurity().setSecurityDataByInfo(theInfo);
			bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
            return bridgeSettings.getBridgeSecurity().getSecurityInfo();
        }, new JsonTransformer());

	    // http://ip_address:port/system/whitelist gets the whitelist for the bridge
    	get (SYSTEM_CONTEXT + "/whitelist", (request, response) -> {
			log.debug("Get whitelist");
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
			return bridgeSettings.getBridgeSecurity().getWhitelist();
	    }, new JsonTransformer());

//      http://ip_address:port/system/setwhitelist CORS request
	    options(SYSTEM_CONTEXT + "/setwhitelist", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/setwhitelist which sets the whitelist after being managed
		post(SYSTEM_CONTEXT + "/setwhitelist", (request, response) -> {
			log.debug("setwhitelist....");
			Type listType = new TypeToken<Map<String, WhitelistEntry>>() {
			}.getType();
			Map<String, WhitelistEntry> aWhitelist = new Gson().fromJson(request.body(), listType);
			bridgeSettings.getBridgeSecurity().setWhitelist(aWhitelist);
	        bridgeSettings.save(bridgeSettings.getBridgeSettingsDescriptor());
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
            return bridgeSettings.getBridgeSecurity().getWhitelist();
        }, new JsonTransformer());

//      http://ip_address:port/system/logmgmt/update CORS request
	    options(SYSTEM_CONTEXT + "/logmgmt/update", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/logmgmt/update which changes logging parameters for the process
		put(SYSTEM_CONTEXT + "/logmgmt/update", (request, response) -> {
			log.debug("update loggers: " + request.body());
			LoggerInfo updateLoggers[];
			updateLoggers = new Gson().fromJson(request.body(), LoggerInfo[].class);
			LoggingForm theModel = theLogServiceMgr.getModel();
			theModel.setUpdatedLoggers(Arrays.asList(updateLoggers));
			theLogServiceMgr.updateLogLevels();
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
            return theLogServiceMgr.getConfiguredLoggers();
        }, new JsonTransformer());

    	//      http://ip_address:port/system/settings which returns the bridge configuration settings
		get(SYSTEM_CONTEXT + "/settings", (request, response) -> {
			log.debug("bridge settings requested from " + request.ip());
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
//			if(bridgeSettings.getBridgeSettingsDescriptor().getActiveloggers() == null)
//				bridgeSettings.getBridgeSettingsDescriptor().setActiveloggers(getLogAppenders());
            return bridgeSettings.getBridgeSettingsDescriptor();
        }, new JsonTransformer());
		
//      http://ip_address:port/system/settings CORS request
	    options(SYSTEM_CONTEXT + "/settings", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
//      http://ip_address:port/system/settings which returns the bridge configuration settings
		put(SYSTEM_CONTEXT + "/settings", (request, response) -> {
			log.debug("save bridge settings requested from " + request.ip() + " with body: " + request.body());
			BridgeSettingsDescriptor newBridgeSettings = new Gson().fromJson(request.body(), BridgeSettingsDescriptor.class);
			if(newBridgeSettings.getUpnpsenddelay() > 15000)
				newBridgeSettings.setUpnpsenddelay(15000);
			bridgeSettings.save(newBridgeSettings);
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
            return bridgeSettings.getBridgeSettingsDescriptor();
        }, new JsonTransformer());
		
	    // http://ip_address:port/system/control/reinit CORS request
	    options(SYSTEM_CONTEXT + "/control/reinit", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
	    // http://ip_address:port/system/control/reinit sets the parameter reinit the server
	    put(SYSTEM_CONTEXT + "/control/reinit", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
	    	return reinit();
	    });

	    // http://ip_address:port/system/control/stop CORS request
	    options(SYSTEM_CONTEXT + "/control/stop", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
	    // http://ip_address:port/system/control/stop sets the parameter stop the server
	    put(SYSTEM_CONTEXT + "/control/stop", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
	    	return stop();
	    });

	    // http://ip_address:port/system/devices/backup/download CORS request
	    options(SYSTEM_CONTEXT + "/backup/download", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	put (SYSTEM_CONTEXT + "/backup/download", "application/json", (request, response) -> {
	    	log.debug("Create download: {}", request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	String backupContent = bridgeSettings.downloadBackup(aFilename.getFilename());
	        return backupContent;
    	}, new JsonTransformer());

	    // http://ip_address:port/system/devices/backup/upload CORS request
	    options(SYSTEM_CONTEXT + "/backup/upload/:filename", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	put (SYSTEM_CONTEXT + "/backup/upload/:filename", "application/json", (request, response) -> {
	    	log.debug("Create upload: {} - {}", request.params(":filename"), request.body());
			String theSuccess = bridgeSettings.uploadBackup(request.params(":filename"), request.body());
			if(theSuccess.contains("Error:"))
				response.status(HttpStatus.SC_METHOD_FAILURE);
			else
				response.status(HttpStatus.SC_OK);
	        return theSuccess;
    	}, new JsonTransformer());

	    // http://ip_address:port/system/backup/available returns a list of config backup filenames
	    get (SYSTEM_CONTEXT + "/backup/available", (request, response) -> {
        	log.debug("Get backup filenames");
          	response.status(HttpStatus.SC_OK);
			response.type("application/json");
          	return bridgeSettings.getBackups();
        }, new JsonTransformer());

	    // http://ip_address:port/system/backup/create CORS request
	    options(SYSTEM_CONTEXT + "/backup/create", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	put (SYSTEM_CONTEXT + "/backup/create", (request, response) -> {
	    	log.debug("Create backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	BackupFilename returnFilename = new BackupFilename();
        	returnFilename.setFilename(bridgeSettings.backup(aFilename.getFilename()));
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
	        return returnFilename;
    	}, new JsonTransformer());

	    // http://ip_address:port/system/backup/delete CORS request
	    options(SYSTEM_CONTEXT + "/backup/delete", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "POST");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post (SYSTEM_CONTEXT + "/backup/delete", (request, response) -> {
	    	log.debug("Delete backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	if(aFilename != null)
        		bridgeSettings.deleteBackup(aFilename.getFilename());
        	else
        		log.warn("No filename given for delete backup.");
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
	        return "";
	    }, new JsonTransformer());

	    // http://ip_address:port/system/backup/restore CORS request
	    options(SYSTEM_CONTEXT + "/backup/restore", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "POST");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post (SYSTEM_CONTEXT + "/backup/restore", (request, response) -> {
	    	log.debug("Restore backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	if(aFilename != null) {
        		bridgeSettings.restoreBackup(aFilename.getFilename());
        		bridgeSettings.loadConfig();
        	}
        	else
        		log.warn("No filename given for restore backup.");
	        response.status(HttpStatus.SC_OK);
			response.type("application/json");
	        return bridgeSettings.getBridgeSettingsDescriptor();
	    }, new JsonTransformer());
    }
    
    private void setupLoggerSettings() {
//		final ch.qos.logback.classic.Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);

       cyclicBufferAppender = (CyclicBufferAppender<ILoggingEvent>) lc.getLogger(
            Logger.ROOT_LOGGER_NAME).getAppender(CYCLIC_BUFFER_APPENDER_NAME);
        cyclicBufferAppender.setMaxSize(bridgeSettings.getBridgeSettingsDescriptor().getNumberoflogmessages());
//        if(bridgeSettings.getBridgeSettingsDescriptor().getActiveloggers() != null) {
//    		for (NameValue temp : bridgeSettings.getBridgeSettingsDescriptor().getActiveloggers()) {
//    			if(temp.getValue().equals("false"))
//    				logger.detachAppender(temp.getName());
//    		}
//       	
//        }
    }

//    private List<NameValue> getLogAppenders() {
//		final ch.qos.logback.classic.Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
//
//		final Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
//		
//		List<NameValue> theLoggers = new ArrayList<NameValue>();
//
//		while (it.hasNext()) {
//
//			final Appender<ILoggingEvent> appender = it.next();
//			
//			if (!(appender instanceof CyclicBufferAppender)) {
//				NameValue theLogger = new NameValue();
//				theLogger.setName(appender.getName());
//				theLogger.setValue("true");
//				theLoggers.add(theLogger);
//			}
//		}
//    	
//   	return theLoggers;
//  }

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
	
	public String removeHttpsSettings() {
		bridgeSettings.getBridgeSecurity().removeHttpsSettings();
    	return stop();
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