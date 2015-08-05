package com.bwssytems.HABridge.upnp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

import java.util.Enumeration;
import org.apache.http.conn.util.*;


public class UpnpListener {
	private Logger log = LoggerFactory.getLogger(UpnpListener.class);
	private static final int UPNP_DISCOVERY_PORT = 1900;
	private static final String UPNP_MULTICAST_ADDRESS = "239.255.255.250";

	private int upnpResponsePort;

	private int httpServerPort;

	private String responseAddress;

	public UpnpListener(String upnpAddress, String upnpServerPort) {
		super();
		upnpResponsePort = Integer.valueOf(System.getProperty("upnp.response.port", "50000"));
		httpServerPort = Integer.valueOf(upnpServerPort);
		responseAddress = upnpAddress;
	}

	public void startListening(){
		log.info("Starting UPNP Discovery Listener");

		try (DatagramSocket responseSocket = new DatagramSocket(upnpResponsePort);
				MulticastSocket upnpMulticastSocket  = new MulticastSocket(UPNP_DISCOVERY_PORT);) {
			InetSocketAddress socketAddress = new InetSocketAddress(UPNP_MULTICAST_ADDRESS, UPNP_DISCOVERY_PORT);
			Enumeration<NetworkInterface> ifs =	NetworkInterface.getNetworkInterfaces();

			while (ifs.hasMoreElements()) {
				NetworkInterface xface = ifs.nextElement();
				Enumeration<InetAddress> addrs = xface.getInetAddresses();
				String name = xface.getName();
				int IPsPerNic = 0;

				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					log.debug(name + " ... has addr " + addr);
					if (InetAddressUtils.isIPv4Address(addr.getHostAddress())) {
						IPsPerNic++;
					}
				}
				log.debug("Checking " + name + " to our interface set");
				if (IPsPerNic > 0) {
					upnpMulticastSocket.joinGroup(socketAddress, xface);
					log.debug("Adding " + name + " to our interface set");
				}
			}

			while(true){ //trigger shutdown here
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				upnpMulticastSocket.receive(packet);
				String packetString = new String(packet.getData());
				if(isSSDPDiscovery(packetString)){
					log.debug("Got SSDP Discovery packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
					sendUpnpResponse(responseSocket, packet.getAddress(), packet.getPort());
				}
			}

		}  catch (IOException e) {
			log.error("UpnpListener encountered an error. Shutting down", e);

		}
		log.info("UPNP Discovery Listener Stopped");

	}

	/**
	 * very naive ssdp discovery packet detection
	 * @param body
	 * @return
	 */
	protected boolean isSSDPDiscovery(String body){
		if(body != null && body.startsWith("M-SEARCH * HTTP/1.1") && body.contains("MAN: \"ssdp:discover\"")){
			return true;
		}
		return false;
	}

	String discoveryTemplate = "HTTP/1.1 200 OK\r\n" +
			"CACHE-CONTROL: max-age=86400\r\n" +
			"EXT:\r\n" +
			"LOCATION: http://%s:%s/upnp/amazon-ha-bridge/setup.xml\r\n" +
			"OPT: \"http://schemas.upnp.org/upnp/1/0/\"; ns=01\r\n" +
			"01-NLS: %s\r\n" +
			"ST: urn:schemas-upnp-org:device:basic:1\r\n" +
			"USN: uuid:Socket-1_0-221438K0100073::urn:Belkin:device:**\r\n\r\n";
	protected void sendUpnpResponse(DatagramSocket socket, InetAddress requester, int sourcePort) throws IOException {
		String discoveryResponse = String.format(discoveryTemplate, responseAddress, httpServerPort, getRandomUUIDString());
		log.debug("sndUpnpResponse: " + discoveryResponse);
		DatagramPacket response = new DatagramPacket(discoveryResponse.getBytes(), discoveryResponse.length(), requester, sourcePort);
		socket.send(response);
	}

	protected String getRandomUUIDString(){
		return "88f6698f-2c83-4393-bd03-cd54a9f8595"; // https://xkcd.com/221/
	}
}
