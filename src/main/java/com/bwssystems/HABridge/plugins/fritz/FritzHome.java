package com.bwssystems.HABridge.plugins.fritz;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.IpList;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

	private static final String path = "/webservices/homeautoswitch.lua?";

	private static final Logger log = LoggerFactory.getLogger(FritzHome.class);
	private HTTPHandler myHttpHandler;

	private FritzAha.HttpService myHttpService;
	private IpList fritzAddress;

	public FritzHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity, Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		log.debug("Fritz Request called with url: " +  anItem.getItem().getAsString());
		String responseString = null;
		String intermediate;
		if (anItem.getItem().getAsString().contains("fritz://"))
			intermediate = anItem.getItem().getAsString().substring(anItem.getItem().getAsString().indexOf("://") + 3);
		else
			intermediate = anItem.getItem().getAsString();
		intermediate = BrightnessDecode.calculateReplaceIntensityValue(intermediate, intensity, targetBri, targetBriInc, false);
		intermediate = TimeDecode.replaceTimeValue(intermediate);
		String anError = doFritzRequest(intermediate, lightId);
		if (anError != null) {
			responseString = anError;
		}
		return responseString;
	}

	private String doFritzRequest(String anItem, String lightId) {
		log.debug("Executing request: " + anItem);

		String user = null, pass = null, host = null, ain = null, cmd = null, param = null;

		if (anItem.contains("@")) {
			// we expect the format fritz://ha:haha_17@fritz.box/119590051600/sethkrkomfort
			// disassemble the uri
			int inx = anItem.indexOf(":");
			user = anItem.substring(0, inx);
			anItem = anItem.substring(inx + 1);
			inx = anItem.indexOf("@");
			pass = anItem.substring(0, inx);
			anItem = anItem.substring(inx + 1);
			inx = anItem.indexOf("/");
			host = anItem.substring(0, inx);
			anItem = anItem.substring(inx + 1);
			inx = anItem.indexOf("/");
			ain = anItem.substring(0, inx);
			anItem = anItem.substring(inx + 1);
			inx = anItem.indexOf("/");
			if (inx > -1) {
				cmd = anItem.substring(0, inx);
				anItem = anItem.substring(inx + 1);
				param = anItem;
			} else {
				cmd = anItem;
			}
		} else {
			// we expect that the config was given, and the comman just  119590051600/sethkrkomfort
			final List<NamedIP> devices = fritzAddress.getDevices();
			if (devices != null && devices.size() > 0) {
				final NamedIP device = devices.get(0); // we expect only one here
				if (device != null) {
					user = device.getUsername();
					pass = device.getPassword();
					host = device.getIp();
					int inx = anItem.indexOf("/");
					if (inx > -1) {
						cmd = anItem.substring(0, inx);
						anItem = anItem.substring(inx + 1);
						param = anItem;
					} else {
						cmd = anItem;
					}
				}
			}
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
		String fullUrl = "http://" + host + path + (ain == null? "" : ("ain=" + ain)) + "&switchcmd=" + cmd + "&sid=" + sid + p;
		fullUrl = fullUrl.trim();
		log.info("Sending >" + fullUrl + "<");
		return myHttpService.sendGet(fullUrl);
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		log.info("Command Home for fritz box access created.");
		//fritzAddress = bridgeSettings.getBridgeSettingsDescriptor().getFritzAddress();
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
		/* to be finished, but there too much DRY int he rest of the code
		final List<NamedIP> devices = fritzAddress.getDevices();     // impl in descriptor first
		if (devices == null || devices.size()== 0) return null;
		final NamedIP device = devices.get(0); // we expect only one here
		if (device == null) return null;
		String user = device.getUsername();
		String pass = device.getPassword();
		String host = device.getIp();
		String ret = null;
		try {
			FritzAha fritz = new FritzAha(myHttpService, user, pass, "http://" + host );
			String sid = fritz.authenticate();
			ret = sendCommand(host, null, "sethkrtsoll", null, sid);
		} catch (Throwable twb) {
			log.error("Error calling fritz: " + twb.getMessage());
			twb.printStackTrace();
		}
		log.info("Dev list got " + ret);     // an xml, todo: parse it and
        */
		// noop
		return null;
	}

	@Override
	public void closeHome() {
		if (myHttpHandler != null) myHttpHandler.closeHandler();
	}

}
