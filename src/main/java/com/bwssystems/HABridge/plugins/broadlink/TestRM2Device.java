package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.net.DatagramPacket;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.CmdPayload;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;

public class TestRM2Device extends RM2Device {
	private static final Logger log = LoggerFactory.getLogger(TestRM2Device.class);
	
	protected TestRM2Device(String host, Mac mac) throws IOException {
		super(host, mac);
	}

	public DatagramPacket sendCmdPkt(int timeout, CmdPayload aCmd) {
		log.info("sendCmdPkt called with " + DatatypeConverter.printHexBinary(((SendDataCmdPayload)aCmd).getData()));
		return null;		
	}
}
