package com.bwssystems.HABridge.util;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressUtil {
    final static Logger log = LoggerFactory.getLogger(AddressUtil.class);

    // added by https://github.com/pvint
    // Ruthlessly stolen from
    // https://stackoverflow.com/questions/22045165/java-datagrampacket-receive-how-to-determine-local-ip-interface
    // Try to get a source IP that makes sense for the requester to contact for use
    // in the LOCATION header in replies
    public static InetAddress getOutboundAddress(String remoteAddress, int remotePort) {
        InetAddress localAddress = null;
        log.debug("Entering getOutboundAddress with args.");
        try {
            localAddress = getOutboundAddress(new InetSocketAddress(remoteAddress, remotePort));
        } catch (Exception e) {
            log.debug("getOutboundAddress(SocketAddress) Threw an Exception: " + e.getMessage());
            ParseRoute theRoute = ParseRoute.getInstance();
            try {
                localAddress = InetAddress.getByName(theRoute.getLocalIPAddress());
                log.warn("Error <" + e.getMessage() + "> on determining interface to reply for <" + remoteAddress
                + ">. Using default route IP Address of " + localAddress.getHostAddress());
            } catch (Exception e1) {
                log.error("Cannot find address for parsed local ip address: " + e.getMessage());
            }
        }

        if(localAddress != null)
            log.debug("localAddress is IP Address of " + localAddress.getHostAddress());
        else
            log.debug("localAddress returning NULL");
        return localAddress;
    }

	// added by https://github.com/pvint
	// Ruthlessly stolen from
	// https://stackoverflow.com/questions/22045165/java-datagrampacket-receive-how-to-determine-local-ip-interface
	// Try to get a source IP that makes sense for the requestor to contact for use
	// in the LOCATION header in replies
	public static InetAddress getOutboundAddress(SocketAddress remoteAddress) throws SocketException {
		DatagramSocket sock = new DatagramSocket();
		// connect is needed to bind the socket and retrieve the local address
		// later (it would return 0.0.0.0 otherwise)
        log.debug("Entering getOutboundAddress with socket arg.");
		sock.connect(remoteAddress);
        log.debug("getOutboundAddress(SocketAddress) getLocalAddress.");
		final InetAddress localAddress = sock.getLocalAddress();
		sock.disconnect();
		sock.close();
		sock = null;
        if(localAddress != null)
            log.debug("getOutbountAddress(SocketAddress) returning IP Address of " + localAddress.getHostAddress());
        else
            log.debug("getOutboundAddress(SocketAddress) returning NULL");
		return localAddress;
	}
}