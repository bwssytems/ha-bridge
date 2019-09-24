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
        try {
            DatagramSocket sock = new DatagramSocket();
            // connect is needed to bind the socket and retrieve the local address
            // later (it would return 0.0.0.0 otherwise)
            sock.connect(new InetSocketAddress(remoteAddress, remotePort));
            localAddress = sock.getLocalAddress();
            sock.disconnect();
            sock.close();
            sock = null;
        } catch (Exception e) {
            ParseRoute theRoute = ParseRoute.getInstance();
            try {
                localAddress = InetAddress.getByName(theRoute.getLocalIPAddress());
            } catch (Exception e1) {
            }
            log.warn("Error <" + e.getMessage() + "> on determining interface to reply for <" + remoteAddress
                    + ">. Using default route IP Address of " + localAddress.getHostAddress());
        }
        log.debug("getOutbountAddress returning IP Address of " + localAddress.getHostAddress());
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
		sock.connect(remoteAddress);
		final InetAddress localAddress = sock.getLocalAddress();
		sock.disconnect();
		sock.close();
		sock = null;
		return localAddress;
	}
}