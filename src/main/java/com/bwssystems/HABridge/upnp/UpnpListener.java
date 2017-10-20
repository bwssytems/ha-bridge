package com.bwssystems.HABridge.upnp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeControlDescriptor;
import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Configuration;
import com.bwssystems.HABridge.api.hue.HueConstants;
import com.bwssystems.HABridge.api.hue.HuePublicConfig;
import com.bwssystems.HABridge.util.UDPDatagramSender;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import org.apache.http.conn.util.*;


public class UpnpListener {
	private Logger log = LoggerFactory.getLogger(UpnpListener.class);
	private MulticastSocket upnpMulticastSocket;
	private int httpServerPort;
	private String responseAddress;
	private boolean strict;
	private boolean traceupnp;
	private boolean useUpnpIface;
	private BridgeControlDescriptor bridgeControl;
	private String bridgeId;
	private String bridgeSNUUID;
	private HuePublicConfig aHueConfig;
	private String responseTemplate1 = "HTTP/1.1 200 OK\r\n" +
			"HOST: %s:%s\r\n" +
			"CACHE-CONTROL: max-age=100\r\n" +
			"EXT:\r\n" +
			"LOCATION: http://%s:%s/description.xml\r\n" +
			"SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/" + HueConstants.API_VERSION + "\r\n" + 
			"hue-bridgeid: %s\r\n" +
			"ST: upnp:rootdevice\r\n" +
			"USN: uuid:" + HueConstants.UUID_PREFIX + "%s::upnp:rootdevice\r\n\r\n";
	private String responseTemplate2 = "HTTP/1.1 200 OK\r\n" +
			"HOST: %s:%s\r\n" +
			"CACHE-CONTROL: max-age=100\r\n" +
			"EXT:\r\n" +
			"LOCATION: http://%s:%s/description.xml\r\n" +
			"SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/" + HueConstants.API_VERSION + "\r\n" + 
			"hue-bridgeid: %s\r\n" +
			"ST: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n" +
			"USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";
	private String responseTemplate3 = "HTTP/1.1 200 OK\r\n" +
			"HOST: %s:%s\r\n" +
			"CACHE-CONTROL: max-age=100\r\n" +
			"EXT:\r\n" +
			"LOCATION: http://%s:%s/description.xml\r\n" +
			"SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/" + HueConstants.API_VERSION + "\r\n" + 
			"hue-bridgeid: %s\r\n" +
			"ST: urn:schemas-upnp-org:device:basic:1\r\n" +
			"USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";

	private String notifyTemplate = "NOTIFY * HTTP/1.1\r\n" +
			"HOST: %s:%s\r\n" +
			"CACHE-CONTROL: max-age=100\r\n" +
			"LOCATION: http://%s:%s/description.xml\r\n" +
			"SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/" + HueConstants.API_VERSION + "\r\n" + 
			"NTS: ssdp:alive\r\n" +
			"hue-bridgeid: %s\r\n" +
			"NT: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n" +
			"USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";

	public UpnpListener(BridgeSettingsDescriptor theSettings, BridgeControlDescriptor theControl, UDPDatagramSender aUdpDatagramSender) throws IOException {
		super();
		upnpMulticastSocket = null;
		httpServerPort = Integer.valueOf(theSettings.getServerPort());
		responseAddress = theSettings.getUpnpConfigAddress();
		strict = theSettings.isUpnpStrict();
		traceupnp = theSettings.isTraceupnp();
		useUpnpIface = theSettings.isUseupnpiface();
		bridgeControl = theControl;
		aHueConfig = HuePublicConfig.createConfig("temp", responseAddress, HueConstants.HUB_VERSION);
		bridgeId = aHueConfig.getBridgeid();
		bridgeSNUUID = aHueConfig.getSNUUIDFromMac();
		try {
			upnpMulticastSocket  = new MulticastSocket(Configuration.UPNP_DISCOVERY_PORT);
		} catch(IOException e){
			log.error("Upnp Discovery Port is in use, or restricted by admin (try running with sudo or admin privs): " + Configuration.UPNP_DISCOVERY_PORT + " with message: " + e.getMessage());
			throw(e);
		}
		
	}

	public boolean startListening(){
		log.info("UPNP Discovery Listener starting....");
		Enumeration<NetworkInterface> ifs = null;

		InetSocketAddress socketAddress = new InetSocketAddress(Configuration.UPNP_MULTICAST_ADDRESS, Configuration.UPNP_DISCOVERY_PORT);
		try {
			ifs =	NetworkInterface.getNetworkInterfaces();
		}  catch (SocketException e) {
			log.error("Could not get network interfaces for this machine: " + e.getMessage());
			return false;
		}

		while (ifs.hasMoreElements()) {
			NetworkInterface xface = ifs.nextElement();
			Enumeration<InetAddress> addrs = xface.getInetAddresses();
			String name = xface.getName();
			int IPsPerNic = 0;

			while (addrs.hasMoreElements()) {
				InetAddress addr = addrs.nextElement();
				if (traceupnp)
					log.info("Traceupnp: " + name + " ... has addr " + addr);
				else
					log.debug(name + " ... has addr " + addr);
				if (InetAddressUtils.isIPv4Address(addr.getHostAddress())) {
					if(!useUpnpIface)
						IPsPerNic++;
					else if(addr.getHostAddress().equals(responseAddress))
						IPsPerNic++;						
				}
			}
			log.debug("Checking " + name + " to our interface set");
			if (IPsPerNic > 0) {
				try {
					upnpMulticastSocket.joinGroup(socketAddress, xface);
					if (traceupnp)
						log.info("Traceupnp: Adding " + name + " to our interface set");
					else
						log.debug("Adding " + name + " to our interface set");
				} catch (IOException e) {
					log.warn("Multicast join failed for: " + socketAddress.getHostName() + " to interface: "
							+ xface.getName() + " with message: " + e.getMessage());
				}
			}
		}

		log.info("UPNP Discovery Listener running and ready....");
		boolean loopControl = true;
		boolean error = false;
		try {
			upnpMulticastSocket.setSoTimeout((int) Configuration.UPNP_NOTIFY_TIMEOUT);
		} catch (SocketException e1) {
			log.warn("Could not sent soTimeout on multi-cast socket");
		}
		Instant current, previous;
		previous = Instant.now();
		while (loopControl) { // trigger shutdown here
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				upnpMulticastSocket.receive(packet);
				if (isSSDPDiscovery(packet)) {
					try {
						sendUpnpResponse(packet.getAddress(), packet.getPort());
					} catch (IOException e) {
						log.warn("UpnpListener encountered an error sending upnp response packet. IP: " + packet.getAddress().getHostAddress() + " with message: " + e.getMessage());
						log.debug("UpnpListener send upnp exception: ", e);
					}
				}

				current = Instant.now();
				if(ChronoUnit.MILLIS.between(previous, current) > Configuration.UPNP_NOTIFY_TIMEOUT) {
					sendUpnpNotify(socketAddress.getAddress());
					previous = Instant.now();
				}

			} catch (SocketTimeoutException e) {
				sendUpnpNotify(socketAddress.getAddress());
			} catch (IOException e) {
				log.error("UpnpListener encountered an error reading socket. Shutting down", e);
				error = true;
			}
			if (error || bridgeControl.isReinit() || bridgeControl.isStop()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// noop
				}
				loopControl = false;
			}
		}
		upnpMulticastSocket.close();
		if (bridgeControl.isReinit())
			log.info("UPNP Discovery Listener - ended, restart found");
		if (bridgeControl.isStop())
			log.info("UPNP Discovery Listener - ended, stop found");
		if (!bridgeControl.isStop() && !bridgeControl.isReinit()) {
			log.info("UPNP Discovery Listener - ended, error found");
			return false;
		}
		return bridgeControl.isReinit();
	}

	/**
	 * ssdp discovery packet detection
	 */
	protected boolean isSSDPDiscovery(DatagramPacket packet){
		//Only respond to discover request for strict upnp form
		String packetString = new String(packet.getData(), 0, packet.getLength());
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
				else
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
				else
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

	protected void sendUpnpResponse(InetAddress requester, int sourcePort) throws IOException {
		String discoveryResponse = null;
		discoveryResponse = String.format(responseTemplate1, Configuration.UPNP_MULTICAST_ADDRESS, Configuration.UPNP_DISCOVERY_PORT, responseAddress, httpServerPort, bridgeId, bridgeSNUUID);
		if(traceupnp) {
			log.info("Traceupnp: sendUpnpResponse discovery responseTemplate1 is <<<" + discoveryResponse + ">>>");
		}
		else
			log.debug("sendUpnpResponse discovery responseTemplate1 is <<<" + discoveryResponse + ">>>");
		sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);

		discoveryResponse = String.format(responseTemplate2, Configuration.UPNP_MULTICAST_ADDRESS, Configuration.UPNP_DISCOVERY_PORT, responseAddress, httpServerPort, bridgeId, bridgeSNUUID, bridgeSNUUID);
		if(traceupnp) {
			log.info("Traceupnp: sendUpnpResponse discovery responseTemplate2 is <<<" + discoveryResponse + ">>>");
		}
		else
			log.debug("sendUpnpResponse discovery responseTemplate2 is <<<" + discoveryResponse + ">>>");
		sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);

		discoveryResponse = String.format(responseTemplate3, Configuration.UPNP_MULTICAST_ADDRESS, Configuration.UPNP_DISCOVERY_PORT, responseAddress, httpServerPort, bridgeId, bridgeSNUUID);
		if(traceupnp) {
			log.info("Traceupnp: sendUpnpResponse discovery responseTemplate3 is <<<" + discoveryResponse + ">>>");
		}
		else
			log.debug("sendUpnpResponse discovery responseTemplate3 is <<<" + discoveryResponse + ">>>");
		sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);
	}

	private void sendUDPResponse(byte[] udpMessage, InetAddress requester, int sourcePort) throws IOException {
		log.debug("Sending response string: <<<" + new String(udpMessage) + ">>>");
		if(upnpMulticastSocket == null)
			throw new IOException("Socket not initialized");
		DatagramPacket response = new DatagramPacket(udpMessage, udpMessage.length, requester, sourcePort);
		upnpMulticastSocket.send(response);
	}
	
	protected void sendUpnpNotify(InetAddress aSocketAddress) {
		String notifyData = null;
		log.debug("Sending notify packet for upnp.");
		notifyData = String.format(notifyTemplate, Configuration.UPNP_MULTICAST_ADDRESS, Configuration.UPNP_DISCOVERY_PORT, responseAddress, httpServerPort, bridgeId, bridgeSNUUID, bridgeSNUUID);
		if(traceupnp) {
			log.info("Traceupnp: sendUpnpNotify notifyTemplate is <<<" + notifyData + ">>>");
		}
		DatagramPacket notifyPacket = new DatagramPacket(notifyData.getBytes(), notifyData.length(), aSocketAddress, Configuration.UPNP_DISCOVERY_PORT);
		try {
			upnpMulticastSocket.send(notifyPacket);
		} catch (IOException e1) {
			log.warn("UpnpListener encountered an error sending upnp notify packet. IP: " + notifyPacket.getAddress().getHostAddress() + " with message: " + e1.getMessage());
			log.debug("UpnpListener send upnp notify exception: ", e1);
		}

	}
}
