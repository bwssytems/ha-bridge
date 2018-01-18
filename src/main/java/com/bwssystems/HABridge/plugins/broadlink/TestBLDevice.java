package com.bwssystems.HABridge.plugins.broadlink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mob41.blapi.BLDevice;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.CmdPayload;

public class TestBLDevice extends BLDevice {
	private static final Logger log = LoggerFactory.getLogger(TestBLDevice.class);
	short adeviceType;
	String adeviceDesc;
	String ahost;
	Mac amac;
	
	protected TestBLDevice(short deviceType, String deviceDesc, String host, Mac mac) throws IOException {
		super(deviceType, host, host, mac);
		adeviceType = deviceType;
		adeviceDesc = deviceDesc;
		ahost = host;
		
	}

	public void setState(boolean aState) {
		log.info("setState called with " + aState);
	}

	public void setState(int anIndex, boolean aState) {
		log.info("setState called with index " + anIndex + " and state " + aState);
	}

	public void setPower(boolean aState) {
		log.info("setPower called with " + aState);
	}
	
	public DatagramPacket sendCmdPkt(int timeout, CmdPayload aCmd) {
		log.info("sendCmdPkt called with " + DatatypeConverter.printHexBinary(aCmd.getPayload().getData()));
		return null;		
	}
	
	public static BLDevice[] discoverDevices(InetAddress theAddress, int aport, int timeout) {
		TestBLDevice mp1Device = null;
		TestBLDevice sp1Device = null;
		TestBLDevice sp2Device = null;
		TestBLDevice rm2Device = null;
		try {
			mp1Device = new TestBLDevice(BLDevice.DEV_MP1, BLDevice.DESC_MP1, "mp1host", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sp1Device = new TestBLDevice(BLDevice.DEV_SP1, BLDevice.DESC_SP1, "sp1host", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sp2Device = new TestBLDevice(BLDevice.DEV_SP2, BLDevice.DESC_SP2, "sp2host", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			rm2Device = new TestBLDevice(BLDevice.DEV_RM_2, BLDevice.DESC_RM_2, "rm2host", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BLDevice[] devices = { mp1Device, sp1Device, sp2Device, rm2Device };
		log.info("Created test devices");
		return devices;	
	}
}
