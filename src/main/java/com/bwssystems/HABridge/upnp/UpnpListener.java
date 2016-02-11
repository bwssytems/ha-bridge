package com.bwssystems.HABridge.upnp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Configuration;

import java.io.IOException;
import java.net.*;

import java.util.Enumeration;
import org.apache.http.conn.util.*;


public class UpnpListener {
	private Logger log = LoggerFactory.getLogger(UpnpListener.class);

	private int upnpResponsePort;

	private int httpServerPort;

	private String responseAddress;
	
	private boolean strict;
	
	private boolean traceupnp;
	private BridgeSettings bridgeSettings;
	
	public UpnpListener(BridgeSettings theSettings) {
		super();
		upnpResponsePort = Integer.valueOf(theSettings.getUpnpResponsePort());
		httpServerPort = Integer.valueOf(theSettings.getServerPort());
		responseAddress = theSettings.getUpnpConfigAddress();
		strict = theSettings.isUpnpStrict();
		traceupnp = theSettings.isTraceupnp();
		bridgeSettings = theSettings;
	}

	public boolean startListening(){
		log.info("UPNP Discovery Listener starting....");

		try (DatagramSocket responseSocket = new DatagramSocket(upnpResponsePort);
				MulticastSocket upnpMulticastSocket  = new MulticastSocket(Configuration.UPNP_DISCOVERY_PORT);) {
			InetSocketAddress socketAddress = new InetSocketAddress(Configuration.UPNP_MULTICAST_ADDRESS, Configuration.UPNP_DISCOVERY_PORT);
			Enumeration<NetworkInterface> ifs =	NetworkInterface.getNetworkInterfaces();

			while (ifs.hasMoreElements()) {
				NetworkInterface xface = ifs.nextElement();
				Enumeration<InetAddress> addrs = xface.getInetAddresses();
				String name = xface.getName();
				int IPsPerNic = 0;

				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					if(traceupnp)
						log.info("Traceupnp: " + name + " ... has addr " + addr);
					else
						log.debug(name + " ... has addr " + addr);
					if (InetAddressUtils.isIPv4Address(addr.getHostAddress())) {
						IPsPerNic++;
					}
				}
				log.debug("Checking " + name + " to our interface set");
				if (IPsPerNic > 0) {
					upnpMulticastSocket.joinGroup(socketAddress, xface);
					if(traceupnp)
						log.info("Traceupnp: Adding " + name + " to our interface set");
					else
						log.debug("Adding " + name + " to our interface set");
				}
			}

			log.info("UPNP Discovery Listener running and ready....");
			boolean loopControl = true;
			while(loopControl){ //trigger shutdown here
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				upnpMulticastSocket.receive(packet);
				if(isSSDPDiscovery(packet)){
					sendUpnpResponse(responseSocket, packet.getAddress(), packet.getPort());
				}
				if(bridgeSettings.isReinit() || bridgeSettings.isStop()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// noop
					}
					loopControl = false;
				}
			}
			upnpMulticastSocket.close();
			responseSocket.close();
		}  catch (IOException e) {
			log.error("UpnpListener encountered an error opening sockets. Shutting down", e);
		}
		if(bridgeSettings.isReinit())
			log.info("UPNP Discovery Listener - ended, restart found");
		if(bridgeSettings.isStop())
			log.info("UPNP Discovery Listener - ended, stop found");
		if(!bridgeSettings.isStop()&& !bridgeSettings.isReinit())
			log.info("UPNP Discovery Listener - ended, error found");
		return bridgeSettings.isReinit();
	}

	/**
	 * ssdp discovery packet detection
	 */
	protected boolean isSSDPDiscovery(DatagramPacket packet){
		//Only respond to discover request for strict upnp form
		String packetString = new String(packet.getData());
		if(packetString != null && packetString.startsWith("M-SEARCH * HTTP/1.1") && packetString.contains("\"ssdp:discover\"")){
			log.debug("isSSDPDiscovery Found message to be an M-SEARCH message.");
			log.debug("isSSDPDiscovery Got SSDP packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ", body: " + packetString);

			if(strict && (packetString.contains("ST: urn:schemas-upnp-org:device:basic:1") || packetString.contains("ST: upnp:rootdevice") || packetString.contains("ST: ssdp:all")))
			{
				if(traceupnp) {
					log.info("Traceupnp: isSSDPDiscovery found message to be an M-SEARCH message.");
					log.info("Traceupnp: isSSDPDiscovery found message to be valid under strict rules - strict: " + strict);
					log.info("Traceupnp: SSDP packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ", body: " + packetString);
				}
				log.debug("isSSDPDiscovery found message to be valid under strict rules - strict: " + strict);
				return true;
			}
			else if (!strict)
			{
				if(traceupnp) {
					log.info("Traceupnp: isSSDPDiscovery found message to be an M-SEARCH message.");
					log.info("Traceupnp: isSSDPDiscovery found message to be valid under loose rules - strict: " + strict);
					log.info("Traceupnp: SSDP packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ", body: " + packetString);
				}
				log.debug("isSSDPDiscovery found message to be valid under loose rules - strict: " + strict);
				return true;
			}
		}
		else {
//			log.debug("isSSDPDiscovery found message to not be valid - strict: " + strict);
//			log.debug("SSDP packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ", body: " + packetString);
		}
		return false;
	}

	String discoveryTemplate = "HTTP/1.1 200 OK\r\n" +
			"CACHE-CONTROL: max-age=86400\r\n" +
			"EXT:\r\n" +
			"LOCATION: http://%s:%s/description.xml\r\n" +
			"SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" + 
			"ST: urn:schemas-upnp-org:device:basic:1\r\n" +
			"USN: uuid:Socket-1_0-221438K0100073::urn:schemas-upnp-org:device:basic:1\r\n\r\n";
	protected void sendUpnpResponse(DatagramSocket socket, InetAddress requester, int sourcePort) throws IOException {
		String discoveryResponse = null;
		discoveryResponse = String.format(discoveryTemplate, responseAddress, httpServerPort);
		if(traceupnp)
			log.info("Traceupnp: sendUpnpResponse: " + discoveryResponse);
		else
			log.debug("sendUpnpResponse: " + discoveryResponse);
		DatagramPacket response = new DatagramPacket(discoveryResponse.getBytes(), discoveryResponse.length(), requester, sourcePort);
		socket.send(response);
	}
}
