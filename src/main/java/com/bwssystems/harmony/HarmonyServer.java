package com.bwssystems.harmony;

import static java.lang.String.format;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.whistlingfish.harmony.ActivityChangeListener;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.HarmonyClientModule;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.protocol.OAReplyProvider;

public class HarmonyServer {
    @Inject
    private HarmonyClient harmonyClient;
    
    private HarmonyHandler myHarmony;
    private DevModeResponse devResponse;
    private OAReplyProvider dummyProvider;
    
    private Logger log = LoggerFactory.getLogger(HarmonyServer.class);

	public HarmonyServer() {
		super();
		myHarmony = null;
		dummyProvider = null;
	}

	public static HarmonyServer setup(BridgeSettings bridgeSettings) throws Exception {
		if(!bridgeSettings.isValidHarmony()) {
			return new HarmonyServer();
		}
    	Injector injector = null;
    	if(!bridgeSettings.isDevMode())
    		injector = Guice.createInjector(new HarmonyClientModule());
        HarmonyServer mainObject = new HarmonyServer();
    	if(!bridgeSettings.isDevMode())
    		injector.injectMembers(mainObject);
  		mainObject.execute(bridgeSettings);
  		return mainObject;
  	}

	private void execute(BridgeSettings mySettings) throws Exception {
        Boolean noopCalls = Boolean.parseBoolean(System.getProperty("noop.calls", "false"));
        String modeString = "";
        if(dummyProvider != null)
        	log.debug("something is very wrong as dummyProvider is not null...");
        if(mySettings.isDevMode())
        	modeString = " (development mode)";
        if(noopCalls)
        	modeString = " (no op calls to harmony)";
		log.info("setup initiated " + modeString + "....");
        if(mySettings.isDevMode())
        {
        	harmonyClient = null;
        	devResponse = new DevModeResponse();
        }
        else {
        	devResponse = null;
			harmonyClient.addListener(new ActivityChangeListener() {
				@Override
				public void activityStarted(Activity activity) {
					log.info(format("activity changed: [%d] %s", activity.getId(), activity.getLabel()));
				}
			});
			harmonyClient.connect(mySettings.getHarmonyAddress(), mySettings.getHarmonyUser(), mySettings.getHarmonyPwd());
        }
        myHarmony = new HarmonyHandler(harmonyClient, noopCalls, devResponse);
	}

	public HarmonyHandler getMyHarmony() {
		return myHarmony;
	}
}
