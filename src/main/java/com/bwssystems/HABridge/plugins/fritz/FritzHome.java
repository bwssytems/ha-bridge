package com.bwssystems.HABridge.plugins.fritz;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control Smart things through a AVM Fritzbox.
 * Note: for a more holistic approach to your home automation, you may be better off with openhab.
 *
 * @author Bob Schulze al.ias@gmx.de
 *
 * We expect this item String:
 *
 * fritz://<user>:<pass>@<fritzboxhost>/<ain>/<command>[/<param>]
 *
 * user: user that can access the fritzbox (locally) with smarthome permissions
 * pass:
 * fritzboxhost: where to find the fritzbox, usually fritz.box
 * ain: the actor id
 * command: one of the supported commands (see below)
 * param: some commands need a parameter
 *
 */
public class FritzHome implements Home {

	private static String path = "/webservices/homeautoswitch.lua?";

	private static final Logger log = LoggerFactory.getLogger(FritzHome.class);
	private HTTPHandler myHttpHandler;

	private FritzAha.HttpService myHttpService;

	public FritzHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int itensity, Integer targetBri, Integer targetBriInc, DeviceDescriptor device, String body) {
		log.debug("Fritz Request called with url: " +  anItem.getItem().getAsString());
		String responseString = null;
		String intermediate;
		if (anItem.getItem().getAsString().contains("fritz://"))
			intermediate = anItem.getItem().getAsString().substring(anItem.getItem().getAsString().indexOf("://") + 3);
		else
			intermediate = anItem.getItem().getAsString();
		intermediate = BrightnessDecode.calculateReplaceIntensityValue(intermediate, itensity, targetBri, targetBriInc, false);
		intermediate = TimeDecode.replaceTimeValue(intermediate);
		String anError = doFritzRequest(intermediate, lightId);
		if (anError != null) {
			responseString = anError;
		}
		return responseString;
	}

	private String doFritzRequest(String anItem, String lightId) {
		log.debug("Executing request: " + anItem);
		// disassemble the uri
		int inx = anItem.indexOf(":");
		String user = anItem.substring(0, inx);
		anItem = anItem.substring(inx+1);
		inx = anItem.indexOf("@");
		String pass = anItem.substring(0, inx);
		anItem = anItem.substring(inx+1);
		inx = anItem.indexOf("/");
		String host = anItem.substring(0, inx);
		anItem = anItem.substring(inx+1);
		inx = anItem.indexOf("/");
		String ain = anItem.substring(0, inx);
		anItem = anItem.substring(inx+1);
		inx = anItem.indexOf("/");
		String cmd;
		String param = null;
		if (inx > -1) {
			cmd = anItem.substring(0, inx);
			anItem = anItem.substring(inx+1);
			param = anItem;
		} else {
			cmd = anItem;
		}
		log.debug("Fritz call en detail: " + user + ":" + pass + "@" + host + "/ " + ain + "/ " + cmd + "/ " + (param != null ? param : "NO PAR"));

		// perhaps keeping the session (the FritzAha Object) around may be better; but remember, each call could address another fritz box
		try {
			FritzAha fritz = new FritzAha(myHttpService, user, pass, "http://" + host );
			String sid = fritz.authenticate();
			// provide a few chained commands as convienience
			if (cmd.equalsIgnoreCase("sethkrkomfort")) {
				String temperatureInt = sendCommand(host, ain, "gethkrkomfort", param, sid);  // in 0.5C steps
				sendCommand(host, ain, "sethkrtsoll", temperatureInt, sid);
			} else if (cmd.equalsIgnoreCase("sethkrabsenk")) {
				String temperatureInt = sendCommand(host, ain, "gethkrabsenk", param, sid);  // in 0.5C steps
				sendCommand(host, ain, "sethkrtsoll", temperatureInt, sid);
			} else {
				sendCommand(host, ain, cmd, param, sid);
			}
		} catch (Throwable twb) {
			log.error("Error calling fritz: " + twb.getMessage());
			twb.printStackTrace();
		}

		return "ok";
	}

	private String sendCommand(String host, String ain, String cmd, String param, String sid) {
		String p = param != null && param.trim().length() > 0? "&param=" + param : "";
		String fullUrl = "http://" + host + path + "ain=" + ain + "&switchcmd=" + cmd + "&sid=" + sid + p;
		fullUrl = fullUrl.trim();
		log.info("Sending >" + fullUrl + "<");
		return myHttpService.sendGet(fullUrl);
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		log.info("Command Home for fritz box access created.");
		myHttpHandler = new HTTPHandler();
		myHttpService = new FritzAha.HttpService() {
			@Override
			public String sendGet(String url) {
				return myHttpHandler.doHttpRequest(url, "GET", null, null, null);
			}
		};
		return this;
	}

	@Override
	public Object getItems(String type) {
		// noop
		return null;
	}

	@Override
	public void closeHome() {
		if (myHttpHandler != null) myHttpHandler.closeHandler();
	}

}
