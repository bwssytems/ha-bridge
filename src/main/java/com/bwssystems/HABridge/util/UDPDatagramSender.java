package com.bwssystems.HABridge.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPDatagramSender {
	private Logger log = LoggerFactory.getLogger(UDPDatagramSender.class);
	private DatagramSocket responseSocket = null;
	private int udpResponsePort;

	public UDPDatagramSender() {
		super();
		udpResponsePort = 0;
	}

	public static UDPDatagramSender createUDPDatagramSender(int udpResponsePort) {
		UDPDatagramSender aDatagramSender = new UDPDatagramSender();
		if(aDatagramSender.initializeSocket(udpResponsePort))
			return aDatagramSender;
		else
			return null;
	}

	private boolean initializeSocket(int port) {
		log.info("Initializing UDP response Socket...");
		udpResponsePort = port;
		boolean portLoopControl = true;
		int retryCount = 0;
		while(portLoopControl) {
			try {
				responseSocket = new DatagramSocket(udpResponsePort);
				portLoopControl = false;
			} catch(SocketException e) {
				if(retryCount == 0)
					log.warn("UDP Response Port is in use, starting loop to find open port for 20 tries - configured port is: " + udpResponsePort);
				if(retryCount >= 20) {
					portLoopControl = false;
					log.error("UDP Response Port issue, could not find open port - last port tried: " + udpResponsePort + " with message: " + e.getMessage());
					return false;
				}
			}
			if(portLoopControl) {
				retryCount++;
				udpResponsePort++;
			}
		}
		log.info("UDP response Seocket initialized to: " + udpResponsePort);
		return true;
	}
	
	public int getUdpResponsePort() {
		return udpResponsePort;
	}
	
	public void closeResponseSocket() {
		responseSocket.close();
	}

	public void sendUDPResponse(byte[] udpMessage, InetAddress requester, int sourcePort) throws IOException {
		log.debug("Sending response string: <<<" + new String(udpMessage) + ">>>");
		if(responseSocket == null)
			throw new IOException("Socket not initialized");
		DatagramPacket response = new DatagramPacket(udpMessage, udpMessage.length, requester, sourcePort);
		responseSocket.send(response);
	}
}
