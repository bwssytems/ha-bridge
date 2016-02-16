package com.bwssystems.NestBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.nest.controller.Home;
import com.bwssystems.nest.controller.Nest;
import com.bwssystems.nest.controller.NestSession;
import com.bwssystems.nest.controller.Thermostat;
import com.bwssystems.nest.protocol.error.LoginException;
import com.bwssystems.nest.protocol.status.WhereDetail;
import com.bwssystems.nest.protocol.status.WhereItem;

public class NestHome {
    private static final Logger log = LoggerFactory.getLogger(NestHome.class);
    private NestSession theSession;
    private Nest theNest;
    private ArrayList<NestItem> nestItems;
    
	public NestHome(BridgeSettingsDescriptor bridgeSettings) {
        theSession = null;
        theNest = null;
        nestItems = null;
        
        if(!bridgeSettings.isValidNest()) {
        	log.debug("not a valid nest");
        	return;
        }

        try {
            theSession = new NestSession(bridgeSettings.getNestuser(), bridgeSettings.getNestpwd());
        	theNest = new Nest(theSession);
        } catch (LoginException e) {
            log.error("Caught Login Exception, exiting....");
            theSession = null;
        }
	}
	
	public List<NestItem> getItems() {
		if(theNest == null)
			return null;
		
		if(nestItems == null) {
			nestItems = new ArrayList<NestItem>();
			Set<String> homeNames = theNest.getHomeNames();
			Home aHome = null;
			NestItem anItem = null;
			for(String name : homeNames) {
				aHome = theNest.getHome(name);
				anItem = new NestItem();
				anItem.setId(name);
				anItem.setName(aHome.getDetail().getName());
				anItem.setType("Home");
				anItem.setLocation(aHome.getDetail().getLocation());
				nestItems.add(anItem);
			}
			Thermostat thermo = null;
			Set<String> thermoNames = theNest.getThermostatNames();
			for(String name : thermoNames) {
				thermo = theNest.getThermostat(name);
				anItem = new NestItem();
				anItem.setId(name);
				anItem.setType("Thermostat");
				String where = null;
				String homeName= null;
				Boolean found = false;
				for(String aHomeName : homeNames) {
					WhereDetail aDetail = theNest.getWhere(aHomeName);
					ListIterator<WhereItem> anIterator = aDetail.getWheres().listIterator();
					while(anIterator.hasNext()) {
						WhereItem aWhereItem = (WhereItem) anIterator.next();
						if(aWhereItem.getWhereId().equals(thermo.getDeviceDetail().getWhereId())) {
							where = aWhereItem.getName();
							homeName = theNest.getHome(aHomeName).getDetail().getName();
							found = true;
							break;
						}
					}
					if(found)
						break;
				}
				anItem.setName(where + "(" + name.substring(name.length() - 4) + ")");
				anItem.setLocation(where + " - " + homeName);
				nestItems.add(anItem);
			}
		}
		
		return nestItems;
	}
	
	public Nest getTheNest() {
		return theNest;
	}
	
	public void closeTheNest() {
		theNest.endNestSession();
		theNest = null;
		theSession = null;
		nestItems = null;
	}
	
}

