package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.net.DatagramPacket;

import com.bwssystems.HABridge.util.HexLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.SP2Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.CmdPayload;

public class TestSP2Device extends SP2Device {
	private static final Logger log = LoggerFactory.getLogger(TestSP2Device.class);
	
	protected TestSP2Device(String host, Mac mac) throws IOException {
		super(host, mac);
	}

	public void setState(boolean aState) {
		log.info("setState called with " + aState);
	}

	public DatagramPacket sendCmdPkt(int timeout, CmdPayload aCmd) {
		log.info("sendCmdPkt called with " + HexLibrary.encodeHexString(aCmd.getPayload().getData()));
		return null;		
	}
}
