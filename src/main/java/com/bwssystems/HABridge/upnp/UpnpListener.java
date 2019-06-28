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
	private String upnpConfigIP;
	// private boolean strict;
	private boolean upnpOriginal;
	private boolean traceupnp;
	private boolean useUpnpIface;
	private BridgeControlDescriptor bridgeControl;
	private String bridgeId;
	private String bridgeSNUUID;
	private HuePublicConfig aHueConfig;
	private Integer theUpnpSendDelay;
	private String responseTemplateOriginal = "HTTP/1.1 200 OK\r\n" + "CACHE-CONTROL: max-age=86400\r\n" + "EXT:\r\n"
			+ "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "ST: urn:schemas-upnp-org:device:basic:1\r\n" + "USN: uuid:"
			+ HueConstants.UUID_PREFIX + "%s::urn:schemas-upnp-org:device:basic:1\r\n\r\n";
	private String responseTemplate1 = "HTTP/1.1 200 OK\r\n" + "HOST: %s:%s\r\n" + "CACHE-CONTROL: max-age=100\r\n"
			+ "EXT:\r\n" + "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "HUE-BRIDGEID: %s\r\n" + "ST: upnp:rootdevice\r\n" + "USN: uuid:"
			+ HueConstants.UUID_PREFIX + "%s::upnp:rootdevice\r\n\r\n";
	private String responseTemplate2 = "HTTP/1.1 200 OK\r\n" + "HOST: %s:%s\r\n" + "CACHE-CONTROL: max-age=100\r\n"
			+ "EXT:\r\n" + "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "HUE-BRIDGEID: %s\r\n" + "ST: uuid:" + HueConstants.UUID_PREFIX
			+ "%s\r\n" + "USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";
	private String responseTemplate3 = "HTTP/1.1 200 OK\r\n" + "HOST: %s:%s\r\n" + "CACHE-CONTROL: max-age=100\r\n"
			+ "EXT:\r\n" + "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "HUE-BRIDGEID: %s\r\n" + "ST: urn:schemas-upnp-org:device:basic:1\r\n"
			+ "USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";

	private String notifyTemplate = "NOTIFY * HTTP/1.1\r\n" + "HOST: %s:%s\r\n" + "CACHE-CONTROL: max-age=100\r\n"
			+ "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "NTS: ssdp:alive\r\n" + "HUE-BRIDGEID: %s\r\n" + "NT: uuid:"
			+ HueConstants.UUID_PREFIX + "%s\r\n" + "USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";

	private String notifyTemplate2 = "NOTIFY * HTTP/1.1\r\n" + "HOST: %s:%s\r\n" + "CACHE-CONTROL: max-age=100\r\n"
			+ "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "NTS: ssdp:alive\r\n" + "HUE-BRIDGEID: %s\r\n"
			+ "NT: upnp:rootdevice\r\n" + "USN: uuid:" + HueConstants.UUID_PREFIX + "%s::upnp:rootdevice\r\n\r\n";

	private String notifyTemplate3 = "NOTIFY * HTTP/1.1\r\n" + "HOST: %s:%s\r\n" + "CACHE-CONTROL: max-age=100\r\n"
			+ "LOCATION: http://%s:%s/description.xml\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/"
			+ HueConstants.API_VERSION + "\r\n" + "NTS: ssdp:alive\r\n" + "HUE-BRIDGEID: %s\r\n"
			+ "NT: urn:schemas-upnp-org:device:basic:1\r\n" + "USN: uuid:" + HueConstants.UUID_PREFIX + "%s\r\n\r\n";

	public UpnpListener(BridgeSettingsDescriptor theSettings, BridgeControlDescriptor theControl,
			UDPDatagramSender aUdpDatagramSender) throws IOException {
		super();
		upnpMulticastSocket = null;
		httpServerPort = Integer.valueOf(theSettings.getServerPort());
		upnpConfigIP = theSettings.getUpnpConfigAddress();
		// strict = theSettings.isUpnpStrict();
		upnpOriginal = theSettings.isUpnporiginal();
		traceupnp = theSettings.isTraceupnp();
		useUpnpIface = theSettings.isUseupnpiface();
		theUpnpSendDelay = theSettings.getUpnpsenddelay();
		bridgeControl = theControl;
		aHueConfig = HuePublicConfig.createConfig("temp", upnpConfigIP, HueConstants.HUB_VERSION,
				theSettings.getHubmac());
		bridgeId = aHueConfig.getBridgeid();
		bridgeSNUUID = aHueConfig.getSNUUIDFromMac();
		try {
			if (useUpnpIface)
				upnpMulticastSocket = new MulticastSocket(
						new InetSocketAddress(upnpConfigIP, Configuration.UPNP_DISCOVERY_PORT));
			else
				upnpMulticastSocket = new MulticastSocket(Configuration.UPNP_DISCOVERY_PORT);
		} catch (IOException e) {
			log.error("Upnp Discovery Port is in use, or restricted by admin (try running with sudo or admin privs): "
					+ Configuration.UPNP_DISCOVERY_PORT + " with message: " + e.getMessage());
			throw (e);
		}

	}

	public boolean startListening() {
		log.info("UPNP Discovery Listener starting....");
		Enumeration<NetworkInterface> ifs = null;

		InetSocketAddress socketAddress = new InetSocketAddress(Configuration.UPNP_MULTICAST_ADDRESS,
				Configuration.UPNP_DISCOVERY_PORT);
		try {
			ifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
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
				log.debug(name + " ... has addr " + addr);
				if (InetAddressUtils.isIPv4Address(addr.getHostAddress())) {
					if (!useUpnpIface) {
						if (traceupnp)
							log.info("Traceupnp: Interface: " + name + " valid usable IP address: " + addr);
						IPsPerNic++;
					} else if (addr.getHostAddress().equals(upnpConfigIP)) {
						if (traceupnp)
							log.info("Traceupnp: Interface: " + name + " matches upnp config address of IP address: "
									+ addr);
						IPsPerNic++;
					}
				}
			}
			log.debug("Checking " + name + " to our interface set");
			if (IPsPerNic > 0) {
				try {
					upnpMulticastSocket.joinGroup(socketAddress, xface);
					if (traceupnp)
						log.info("Traceupnp: Adding " + name + " to our upnp join interface set.");
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
						sendUpnpResponse(packet);
					} catch (IOException e) {
						log.warn("UpnpListener encountered an error sending upnp response packet. IP: "
								+ packet.getAddress().getHostAddress() + " with message: " + e.getMessage());
						log.debug("UpnpListener send upnp exception: ", e);
					}
				}

				current = Instant.now();
				if (ChronoUnit.MILLIS.between(previous, current) > Configuration.UPNP_NOTIFY_TIMEOUT) {
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
	protected boolean isSSDPDiscovery(DatagramPacket packet) {
		// Only respond to discover request for strict upnp form
		String packetString = new String(packet.getData(), 0, packet.getLength());
		if (packetString != null && packetString.startsWith("M-SEARCH * HTTP/1.1")
				&& packetString.contains("\"ssdp:discover\"")) {
			if ((packetString.contains("ST: urn:schemas-upnp-org:device:basic:1")
					|| packetString.contains("ST: upnp:rootdevice") || packetString.contains("ST: ssdp:all"))) {
				if (traceupnp) {
					log.info("Traceupnp: SSDP M-SEARCH packet from " + packet.getAddress().getHostAddress() + ":"
							+ packet.getPort());
				} else
					log.debug("SSDP M-SEARCH packet from " + packet.getAddress().getHostAddress() + ":"
							+ packet.getPort() + ", body: <<<" + packetString + ">>>");
				return true;
			} /*
				 * else if (!strict) { if(traceupnp) {
				 * log.info("Traceupnp: SSDP M-SEARCH packet (!strict) from " +
				 * packet.getAddress().getHostAddress() + ":" + packet.getPort()); } else
				 * log.debug("SSDP M-SEARCH packet (!strict) from " +
				 * packet.getAddress().getHostAddress() + ":" + packet.getPort() + ", body: <<<"
				 * + packetString + ">>>"); return true; }
				 */
		} else {
			// log.debug("isSSDPDiscovery found message to not be valid - strict: " +
			// strict);
			log.debug("SSDP packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ", body: "
					+ packetString);
		}
		return false;
	}

	protected void sendUpnpResponse(DatagramPacket aPacket) throws IOException {
		SocketAddress requesterAddress = aPacket.getSocketAddress();
		InetAddress requester = aPacket.getAddress();
		int sourcePort = aPacket.getPort();
		String discoveryResponse = null;
		// refactored suggestion by https://github.com/pvint
		String httpLocationAddress = getOutboundAddress(requesterAddress).getHostAddress();
		try {
			Thread.sleep(theUpnpSendDelay);
		} catch (InterruptedException e) {
			// noop
		}

		if (upnpOriginal) {
			discoveryResponse = String.format(responseTemplateOriginal, Configuration.UPNP_MULTICAST_ADDRESS,
					Configuration.UPNP_DISCOVERY_PORT, httpLocationAddress, httpServerPort, bridgeId, bridgeSNUUID);
			if (traceupnp) {
				log.info("Traceupnp: send upnp discovery template Original with response address: "
						+ httpLocationAddress + ":" + httpServerPort + " to address: " + requester + ":" + sourcePort);
			}
			log.debug("sendUpnpResponse to address: " + requester + ":" + sourcePort
					+ " with discovery responseTemplateOriginal is <<<" + discoveryResponse + ">>>");
			sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);
		} else {
			discoveryResponse = String.format(responseTemplateOriginal, Configuration.UPNP_MULTICAST_ADDRESS,
					Configuration.UPNP_DISCOVERY_PORT, httpLocationAddress, httpServerPort, bridgeId, bridgeSNUUID);
			if (traceupnp) {
				log.info("Traceupnp: send upnp discovery template Original with response address: "
						+ httpLocationAddress + ":" + httpServerPort + " to address: " + requester + ":" + sourcePort);
			}
			log.debug("sendUpnpResponse to address: " + requester + ":" + sourcePort
					+ " with discovery responseTemplateOriginal is <<<" + discoveryResponse + ">>>");
			sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);
			try {
				Thread.sleep(theUpnpSendDelay);
			} catch (InterruptedException e) {
				// noop
			}
			discoveryResponse = String.format(responseTemplate1, Configuration.UPNP_MULTICAST_ADDRESS,
					Configuration.UPNP_DISCOVERY_PORT, httpLocationAddress, httpServerPort, bridgeId, bridgeSNUUID);
			if (traceupnp) {
				log.info("Traceupnp: send upnp discovery template 1 with response address: " + httpLocationAddress + ":"
						+ httpServerPort + " to address: " + requester + ":" + sourcePort);
			}
			log.debug("sendUpnpResponse to address: " + requester + ":" + sourcePort
					+ " with discovery responseTemplate1 is <<<" + discoveryResponse + ">>>");
			sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);

			try {
				Thread.sleep(theUpnpSendDelay);
			} catch (InterruptedException e) {
				// noop
			}
			discoveryResponse = String.format(responseTemplate2, Configuration.UPNP_MULTICAST_ADDRESS,
					Configuration.UPNP_DISCOVERY_PORT, httpLocationAddress, httpServerPort, bridgeId, bridgeSNUUID,
					bridgeSNUUID);
			if (traceupnp) {
				log.info("Traceupnp: send upnp discovery template 2 with response address: " + httpLocationAddress + ":"
						+ httpServerPort + " to address: " + requester + ":" + sourcePort);
			}
			log.debug("sendUpnpResponse to address: " + requester + ":" + sourcePort
					+ " discovery responseTemplate2 is <<<" + discoveryResponse + ">>>");
			sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);

			try {
				Thread.sleep(theUpnpSendDelay);
			} catch (InterruptedException e) {
				// noop
			}
			discoveryResponse = String.format(responseTemplate3, Configuration.UPNP_MULTICAST_ADDRESS,
					Configuration.UPNP_DISCOVERY_PORT, httpLocationAddress, httpServerPort, bridgeId, bridgeSNUUID);
			if (traceupnp) {
				log.info("Traceupnp: send upnp discovery template 3 with response address: " + httpLocationAddress + ":"
						+ httpServerPort + " to address: " + requester + ":" + sourcePort);
			}
			log.debug("sendUpnpResponse to address: " + requester + ":" + sourcePort
					+ " discovery responseTemplate3 is <<<" + discoveryResponse + ">>>");
			sendUDPResponse(discoveryResponse.getBytes(), requester, sourcePort);
		}
	}

	private void sendUDPResponse(byte[] udpMessage, InetAddress requester, int sourcePort) throws IOException {
		log.debug("Sending response string: <<<" + new String(udpMessage) + ">>>");
		if (upnpMulticastSocket == null)
			throw new IOException("Socket not initialized");
		DatagramPacket response = new DatagramPacket(udpMessage, udpMessage.length, requester, sourcePort);
		upnpMulticastSocket.send(response);
	}

	protected void sendUpnpNotify(InetAddress aSocketAddress) {
		String notifyData = null;
		try {
			Thread.sleep(theUpnpSendDelay);
		} catch (InterruptedException e) {
			// noop
		}

		notifyData = String.format(notifyTemplate, Configuration.UPNP_MULTICAST_ADDRESS,
				Configuration.UPNP_DISCOVERY_PORT, upnpConfigIP, httpServerPort, bridgeId, bridgeSNUUID, bridgeSNUUID);
		sendNotifyDatagram(notifyData, aSocketAddress, "notifyTemplate1");

		try {
			Thread.sleep(theUpnpSendDelay);
		} catch (InterruptedException e) {
			// noop
		}

		notifyData = String.format(notifyTemplate2, Configuration.UPNP_MULTICAST_ADDRESS,
				Configuration.UPNP_DISCOVERY_PORT, upnpConfigIP, httpServerPort, bridgeId, bridgeSNUUID);
		sendNotifyDatagram(notifyData, aSocketAddress, "notifyTemplate2");

		try {
			Thread.sleep(theUpnpSendDelay);
		} catch (InterruptedException e) {
			// noop
		}

		notifyData = String.format(notifyTemplate3, Configuration.UPNP_MULTICAST_ADDRESS,
				Configuration.UPNP_DISCOVERY_PORT, upnpConfigIP, httpServerPort, bridgeId, bridgeSNUUID);
		sendNotifyDatagram(notifyData, aSocketAddress, "notifyTemplate3");
	}

	public void sendNotifyDatagram(String notifyData, InetAddress aSocketAddress, String templateNumber) {
		if (traceupnp) {
			log.info("Traceupnp: sendUpnpNotify {}", templateNumber);
		}
		log.debug("sendUpnpNotify {} is <<<{}>>>", templateNumber, notifyData);
		DatagramPacket notifyPacket = new DatagramPacket(notifyData.getBytes(), notifyData.length(), aSocketAddress,
				Configuration.UPNP_DISCOVERY_PORT);
		try {
			upnpMulticastSocket.send(notifyPacket);
		} catch (IOException e1) {
			log.warn("UpnpListener encountered an error sending upnp {}. IP: {} with message: {}", templateNumber,
					notifyPacket.getAddress().getHostAddress(), e1.getMessage());
			log.debug("UpnpListener send {} exception: ", templateNumber, e1);
		}
	}

	// added by https://github.com/pvint
	// Ruthlessly stolen from
	// https://stackoverflow.com/questions/22045165/java-datagrampacket-receive-how-to-determine-local-ip-interface
	// Try to get a source IP that makes sense for the requestor to contact for use
	// in the LOCATION header in replies
	private InetAddress getOutboundAddress(SocketAddress remoteAddress) throws SocketException {
		DatagramSocket sock = new DatagramSocket();
		// connect is needed to bind the socket and retrieve the local address
		// later (it would return 0.0.0.0 otherwise)
		sock.connect(remoteAddress);
		final InetAddress localAddress = sock.getLocalAddress();
		sock.disconnect();
		sock.close();
		sock = null;
		return localAddress;
	}
}
