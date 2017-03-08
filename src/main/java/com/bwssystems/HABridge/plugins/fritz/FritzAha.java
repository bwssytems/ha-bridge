/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bwssystems.HABridge.plugins.fritz;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * partially taken from  https://github.com/openhab/openhab2-addons/blob/master/addons/binding/org.openhab.binding.avmfritz/src/main/java/org/openhab/binding/avmfritz/internal/hardware/FritzahaContentExchange.java
 * see spec @  http://avm.de/fileadmin/user_upload/Global/Service/Schnittstellen/AVM_Technical_Note_-_Session_ID.pdf
 *
 * @author Bob Schulze al.ias@gmx.de
 *
 */
public class FritzAha {

	private String sid;

	private String userName;
	private String password;
	private String url;
	private HttpService httpService;

	private void log(String msg) {
	    log(msg, null);
    }
	private void log(String msg, Throwable twb) {
        System.out.println("msg = " + msg);
        if (twb != null) twb.printStackTrace();
    }
	// Uses RegEx to handle bad FritzBox XML
	/**
	 * RegEx Pattern to grab the session ID from a login XML response
	 */
	protected static final Pattern SID_PATTERN = Pattern
			.compile("<SID>([a-fA-F0-9]*)</SID>");
	/**
	 * RegEx Pattern to grab the challenge from a login XML response
	 */
	protected static final Pattern CHALLENGE_PATTERN = Pattern
			.compile("<Challenge>(\\w*)</Challenge>");
	/**
	 * RegEx Pattern to grab the access privilege for home automation functions
	 * from a login XML response
	 */
	protected static final Pattern ACCESS_PATTERN = Pattern
			.compile("<Name>HomeAuto</Name>\\s*?<Access>([0-9])</Access>");

	interface HttpService {
		String sendGet(String url);
	}
	/**
	 * This method authenticates with the Fritz!OS Web Interface and updates the
	 * session ID accordingly
	 *
	 * @return New session ID
	 */
	public String authenticate() {
		
		String loginXml = null;
		try {
			loginXml = httpService.sendGet(getURL("login_sid.lua", addSID("")));
		} catch (Exception e) {
			log("Failed to get loginXML {}",e);
		}
		if (loginXml == null) {

			log("FRITZ!Box does not respond");
			return null;
		}
		Matcher sidmatch = SID_PATTERN.matcher(loginXml);
		if (!sidmatch.find()) {
            log("FRITZ!Box does not respond with SID");
			return null;
		}
		sid = sidmatch.group(1);
		Matcher accmatch = ACCESS_PATTERN.matcher(loginXml);
		if (accmatch.find()) {
			if ("2".equals(accmatch.group(1))) {
                log("Resuming FRITZ!Box connection with SID " + sid);
				return sid;
			}
		}
		Matcher challengematch = CHALLENGE_PATTERN.matcher(loginXml);
		if (!challengematch.find()) {
            log("FRITZ!Box does not respond with challenge for authentication");
			return null;
		}
		String challenge = challengematch.group(1);
		String response = createResponse(challenge);
		try {
			loginXml = httpService.sendGet(getURL("login_sid.lua","username=" + userName + "&response=" + response));
		} catch (Exception e) {
			log("Failed to get loginXML {}",e);
		}
		if (loginXml == null) {
            log("FRITZ!Box does not respond");
			return null;
		}
		sidmatch = SID_PATTERN.matcher(loginXml);
		if (!sidmatch.find()) {
            log("Resuming FRITZ!Box connection with SID");
			return null;
		}
		sid = sidmatch.group(1);
		accmatch = ACCESS_PATTERN.matcher(loginXml);
		if (accmatch.find()) {
			if ("2".equals(accmatch.group(1))) {
                log("Established FRITZ!Box connection with SID " + sid);
				return sid;
			}
		}
		//	"User " + this.config.getUser() + " has no access to FritzBox home automation functions");
		return null;
	}

	/**
	 * Checks the authentication status of the web interface
	 *
	 * @return
	 */
	public boolean isAuthenticated() {
		return !(sid == null);
	}



	/**
	 * Creates the proper response to a given challenge based on the password
	 * stored
	 *
	 * @param challenge
	 *            Challenge string as returned by the Fritz!OS login script
	 * @return Response to the challenge
	 */
	protected String createResponse(String challenge) {
		String handshake = challenge.concat("-").concat(password);
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			log("This version of Java does not support MD5 hashing");
			return "";
		}
		byte[] handshakeHash;
		try {
			handshakeHash = md5.digest(handshake.getBytes("UTF-16LE"));
		} catch (UnsupportedEncodingException e) {
			log("This version of Java does not understand UTF-16LE encoding");
			return "";
		}
		String response = challenge.concat("-");
		for (byte handshakeByte : handshakeHash)
			response = response.concat(String.format("%02x", handshakeByte));
		return response;
	}

	/**
	 * Constructor to set up interface
	 */
	public FritzAha(HttpService httpService, String user, String pass, String url) {
		this.httpService = httpService;
		this.url = url;
		this.userName = user;
		this.password = pass;

	}

	/**
	 * Constructs a URL from the stored information and a specified path
	 *
	 * @param path
	 *            Path to include in URL
	 * @return URL
	 */
	public String getURL(String path) {
		return this.url + "/" + path;
	}

	/**
	 * Constructs a URL from the stored information, a specified path and a
	 * specified argument string
	 *
	 * @param path
	 *            Path to include in URL
	 * @param args
	 *            String of arguments, in standard HTTP format
	 *            (arg1=value1&arg2=value2&...)
	 * @return URL
	 */
	public String getURL(String path, String args) {
		return getURL(path + "?" + args);
	}

	public String addSID(String args) {
		if (sid == null)
			return args;
		else
			return ("".equals(args) ? ("sid=") : (args + "&sid=")) + sid;
	}




}
