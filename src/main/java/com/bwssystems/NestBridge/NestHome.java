package com.bwssystems.NestBridge;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.nest.controller.Nest;
import com.bwssystems.nest.controller.NestSession;
import com.bwssystems.nest.protocol.error.LoginException;

public class NestHome {
    private static final Logger log = LoggerFactory.getLogger(NestHome.class);
    private NestSession theSession;
    private Nest theNest;
    
	public NestHome(BridgeSettings bridgeSettings) {
        theSession = null;
        theNest = null;
        
        if(bridgeSettings.isValidNest())
        	return;

        try {
            theSession = new NestSession(bridgeSettings.getNestuser(), bridgeSettings.getNestpwd());
        	theNest = new Nest(theSession);
        } catch (LoginException e) {
            log.error("Caught Login Exception, exiting....");
            theSession = null;
        }
	}
	
	public List<String> getHomeNames() {
		if(theNest == null)
			return null;
		return new ArrayList<String>(theNest.getHomeNames()); /* list of home structures  i.e. MyHouse */
	}

	public List<String> getThermostatNames() {
		if(theNest == null)
			return null;
        return new ArrayList<String>(theNest.getThermostatNames()); /* list of thermostats in all structure */
	}

	public Nest getTheNest() {
		return theNest;
	}
}

