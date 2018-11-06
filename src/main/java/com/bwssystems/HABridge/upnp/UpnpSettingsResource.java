package com.bwssystems.HABridge.upnp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.api.hue.HueConstants;
import com.bwssystems.HABridge.api.hue.HuePublicConfig;

import static spark.Spark.get;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * 
 */
public class UpnpSettingsResource {
    private Logger log = LoggerFactory.getLogger(UpnpSettingsResource.class);
    
    private BridgeSettingsDescriptor theSettings;
    private BridgeSettings bridgeSettings;

	private String hueTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
			+ "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n"
			+ "<specVersion>\n"
				+ "<major>1</major>\n"
				+ "<minor>0</minor>\n"
			+ "</specVersion>\n"
			+ "<URLBase>http://%s:%s/</URLBase>\n"
			+ "<device>\n"
				+ "<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>\n"
				+ "<friendlyName>Philips hue (%s)</friendlyName>\n"
				+ "<manufacturer>Royal Philips Electronics</manufacturer>\n"
				+ "<manufacturerURL>http://www.philips.com</manufacturerURL>\n"
				+ "<modelDescription>Philips hue Personal Wireless Lighting</modelDescription>\n"
				+ "<modelName>Philips hue bridge 2015</modelName>\n"
				+ "<modelNumber>" + HueConstants.MODEL_ID + "</modelNumber>\n"
				+ "<modelURL>http://www.meethue.com</modelURL>\n"
				+ "<serialNumber>%s</serialNumber>\n"
				+ "<UDN>uuid:" + HueConstants.UUID_PREFIX + "%s</UDN>\n"
				+ "<presentationURL>index.html</presentationURL>\n"
					+ "<iconList>\n"
						+ "<icon>\n"
							+ "<mimetype>image/png</mimetype>\n"
							+ "<height>48</height>\n"
							+ "<width>48</width>\n"
							+ "<depth>24</depth>\n"
							+ "<url>hue_logo_0.png</url>\n"
						+ "</icon>\n"
						+ "<icon>\n"
							+ "<mimetype>image/png</mimetype>\n"
							+ "<height>120</height>\n"
							+ "<width>120</width>\n"
							+ "<depth>24</depth>\n"
							+ "<url>hue_logo_3.png</url>\n"
						+ "</icon>\n"
					+ "</iconList>\n"
				+ "</device>\n"
			+ "</root>\n";

	public UpnpSettingsResource(BridgeSettings theBridgeSettings) {
		super();
		this.bridgeSettings = theBridgeSettings;
		this.theSettings = theBridgeSettings.getBridgeSettingsDescriptor();
	}

	public void setupServer() {
		log.info("Description xml service started....");
//      http://ip_adress:port/description.xml which returns the xml configuration for the hue emulator
		get("/description.xml", "application/xml; charset=utf-8", (request, response) -> {
			if(bridgeSettings.getBridgeControl().isReinit() || bridgeSettings.getBridgeControl().isStop()) {
				log.info("Get description.xml called while in re-init or stop state");
		         response.status(503);
				return null;
			}
			
			String portNumber = Integer.toString(request.port());
			String filledTemplate = null;
			String httpLocationAddr = getOutboundAddress(request.ip(), request.port()).getHostAddress();
			String bridgeIdMac = HuePublicConfig.createConfig("temp", httpLocationAddr, HueConstants.HUB_VERSION, theSettings.getHubmac()).getSNUUIDFromMac();
			filledTemplate = String.format(hueTemplate, httpLocationAddr, portNumber, httpLocationAddr, bridgeIdMac, bridgeIdMac);
			if(theSettings.isTraceupnp())
				log.info("Traceupnp: request of description.xml from: " + request.ip() + ":" + request.port() + " filled in with address: " + httpLocationAddr + ":" + portNumber);
			else
				log.debug("request of description.xml from: " + request.ip() + ":" + request.port() + " filled in with address: " + httpLocationAddr + ":" + portNumber);
//			response.header("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
//			response.header("Pragma", "no-cache");
//			response.header("Expires", "Mon, 1 Aug 2011 09:00:00 GMT");
//			response.header("Connection", "close");  // Not sure if the server will actually close the connections by just setting the header
//			response.header("Access-Control-Max-Age", "0");
//			response.header("Access-Control-Allow-Origin", "*");
//			response.header("Access-Control-Allow-Credentials", "true");
//			response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
//			response.header("Access-Control-Allow-Headers", "Content-Type");
//			response.header("Content-Type", "application/xml; charset=utf-8"); 
			response.type("application/xml; charset=utf-8"); 
            response.status(200);

            return filledTemplate;
        } );
//      http://ip_adress:port/favicon.ico
		get("/favicon.ico", "application/xml; charset=utf-8", (request, response) -> {
			return "";
        } );
//      http://ip_adress:port/hue_logo_0.png 
		get("/hue_logo_0.png", "application/xml; charset=utf-8", (request, response) -> {
			return "";
        } );
//      http://ip_adress:port/hue_logo_3.png 
		get("/hue_logo_3.png", "application/xml; charset=utf-8", (request, response) -> {
			return "";
        } );
	}
	// added by https://github.com/pvint
	// Ruthlessly stolen from https://stackoverflow.com/questions/22045165/java-datagrampacket-receive-how-to-determine-local-ip-interface
	// Try to get a source IP that makes sense for the requester to contact for use in the LOCATION header in replies
	private InetAddress getOutboundAddress(String remoteAddress, int remotePort) throws SocketException {
		InetAddress localAddress = null;
		try {
		DatagramSocket sock = new DatagramSocket();
		// connect is needed to bind the socket and retrieve the local address
		// later (it would return 0.0.0.0 otherwise)
		sock.connect(InetSocketAddress.createUnresolved(remoteAddress, remotePort));
		localAddress = sock.getLocalAddress();
		sock.disconnect();
		sock.close();
		sock = null;
		} catch(Exception e)  {

		}
		return localAddress;
	}
}
