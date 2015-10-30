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

public class HarmonyServer {
    @Inject
    private HarmonyClient harmonyClient;
    
    private HarmonyHandler myHarmony;
    
    private Logger log = LoggerFactory.getLogger(HarmonyServer.class);

	public HarmonyServer() {
		super();
		myHarmony = null;
	}

	public static HarmonyServer setup(BridgeSettings bridgeSettings) throws Exception {
		if(!bridgeSettings.isValidHarmony()) {
			return new HarmonyServer();
		}
    	Injector injector = null;
        injector = Guice.createInjector(new HarmonyClientModule());
        HarmonyServer mainObject = new HarmonyServer();
  		injector.injectMembers(mainObject);
  		mainObject.execute(bridgeSettings);
  		return mainObject;
  	}

	private void execute(BridgeSettings mySettings) throws Exception {
		log.info("setup initiated....");
		harmonyClient.addListener(new ActivityChangeListener() {
			@Override
			public void activityStarted(Activity activity) {
				log.info(format("activity changed: [%d] %s", activity.getId(), activity.getLabel()));
			}
		});
		harmonyClient.connect(mySettings.getHarmonyAddress(), mySettings.getHarmonyUser(), mySettings.getHarmonyPwd());
        Boolean noopCalls = Boolean.parseBoolean(System.getProperty("noop.calls", "false"));
        myHarmony = new HarmonyHandler(harmonyClient, noopCalls);
	}

	public HarmonyHandler getMyHarmony() {
		return myHarmony;
	}
}
