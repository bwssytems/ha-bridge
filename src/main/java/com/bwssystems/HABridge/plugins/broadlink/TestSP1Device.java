package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.net.DatagramPacket;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.SP1Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.CmdPayload;

public class TestSP1Device extends SP1Device {
	private static final Logger log = LoggerFactory.getLogger(TestSP1Device.class);
	
	protected TestSP1Device(String host, Mac mac) throws IOException {
		super(host, mac);
	}

	public void setPower(boolean aState) {
		log.info("setPower called with " + aState);
	}
	
	public DatagramPacket sendCmdPkt(int timeout, CmdPayload aCmd) {
		log.info("sendCmdPkt called with " + DatatypeConverter.printHexBinary(aCmd.getPayload().getData()));
		return null;		
	}
}
