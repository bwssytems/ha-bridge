package com.bwssystems.HABridge.plugins.harmony;

import static java.lang.String.format;

import javax.inject.Inject;

import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.NamedIP;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.whistlingfish.harmony.ActivityChangeListener;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.HarmonyClientModule;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.protocol.OAReplyProvider;

import java.net.URLEncoder;

public class HarmonyServer {

    private static final String ACTIVIY_ID = "${activity.id}";
    private static final String ACTIVIY_LABEL = "${activity.label}";

    @Inject
    private HarmonyClient harmonyClient;

    private HarmonyHandler myHarmony;
    private DevModeResponse devResponse;
    private OAReplyProvider dummyProvider;
    private NamedIP myNameAndIP;
    private Boolean isDevMode;
    private HTTPHandler httpClient;
    private Logger log = LoggerFactory.getLogger(HarmonyServer.class);

    public HarmonyServer(NamedIP theHarmonyAddress) {
        super();
        myHarmony = null;
        dummyProvider = null;
        myNameAndIP = theHarmonyAddress;
        isDevMode = false;
        httpClient = new HTTPHandler();
    }

    public static HarmonyServer setup(
            BridgeSettingsDescriptor bridgeSettings,
            Boolean harmonyDevMode,
            NamedIP theHarmonyAddress
    ) throws Exception {
        if (!bridgeSettings.isValidHarmony() && harmonyDevMode) {
            return new HarmonyServer(theHarmonyAddress);
        }
        Injector injector = null;
        if (!harmonyDevMode) {
            injector = Guice.createInjector(new HarmonyClientModule());
        }
        HarmonyServer mainObject = new HarmonyServer(theHarmonyAddress);
        if (!harmonyDevMode) {
            injector.injectMembers(mainObject);
        }
        mainObject.execute(bridgeSettings, harmonyDevMode);
        return mainObject;
    }

    private void execute(BridgeSettingsDescriptor mySettings, Boolean harmonyDevMode) throws Exception {
        Boolean noopCalls = Boolean.parseBoolean(System.getProperty("noop.calls", "false"));
        isDevMode = harmonyDevMode;
        String modeString = "";
        if (dummyProvider != null) {
            log.debug("something is very wrong as dummyProvider is not null...");
        }
        if (isDevMode) {
            modeString = " (development mode)";
        } else if (noopCalls) {
            modeString = " (no op calls to harmony)";
        }
        log.info("setup initiated " + modeString + "....");
        if (isDevMode) {
            harmonyClient = null;
            devResponse = new DevModeResponse();
        } else {
            devResponse = null;
            harmonyClient.addListener(new ActivityChangeListener() {
                @Override
                public void activityStarted(Activity activity) {
                    String webhook = myNameAndIP.getWebhook();
                    if(webhook != null) {
                        try {
                            // Replacing variables
                            webhook = webhook.replace(ACTIVIY_ID, activity.getId().toString());
                            webhook = webhook.replace(ACTIVIY_LABEL, URLEncoder.encode(activity.getLabel(), "UTF-8"));

                            log.info(format("calling webhook: %s", webhook));

                            // Calling webhook
                            httpClient.doHttpRequest(webhook, HttpGet.METHOD_NAME, null, null, null);
                        } catch (Exception e) {
                            log.warn("could not call webhook: " + webhook, e);
                        }
                    }
                    log.info(format("activity changed: [%d] %s", activity.getId(), activity.getLabel()));
                }
            });
            harmonyClient.connect(myNameAndIP.getIp());
        }
        myHarmony = new HarmonyHandler(harmonyClient, noopCalls, devResponse);
    }

    public HarmonyHandler getMyHarmony() {
        return myHarmony;
    }
}
