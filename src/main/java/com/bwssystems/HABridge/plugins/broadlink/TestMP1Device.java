package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.net.DatagramPacket;
import com.bwssystems.HABridge.util.HexLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.MP1Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.CmdPayload;

public class TestMP1Device extends MP1Device {
	private static final Logger log = LoggerFactory.getLogger(TestMP1Device.class);
	
	protected TestMP1Device(String host, Mac mac) throws IOException {
		super(host, mac);
	}

	public void setState(int anIndex, boolean aState) {
		log.info("setState called with index " + anIndex + " and state " + aState);
	}

	public DatagramPacket sendCmdPkt(int timeout, CmdPayload aCmd) {
		log.info("sendCmdPkt called with " + HexLibrary.encodeHexString(aCmd.getPayload().getData()));
		return null;		
	}
}
