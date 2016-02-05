package com.bwssystems.HABridge.upnp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.JsonTransformer;

import static spark.Spark.get;

/**
 * 
 */
public class UpnpSettingsResource {
    private static final String UPNP_CONTEXT = "/upnp";

    private Logger log = LoggerFactory.getLogger(UpnpSettingsResource.class);
    
    private BridgeSettings theSettings;

	private String hueTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n"
			+ "<specVersion>\n" + "<major>1</major>\n" + "<minor>0</minor>\n" + "</specVersion>\n"
			+ "<URLBase>http://%s:%s/</URLBase>\n" + // hostname string
			"<device>\n" + "<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>\n"
			+ "<friendlyName>HA-Bridge (%s)</friendlyName>\n"
			+ "<manufacturer>Royal Philips Electronics</manufacturer>\n"
			+ "<manufacturerURL>http://www.bwssystems.com</manufacturerURL>\n"
			+ "<modelDescription>Hue Emulator for HA bridge</modelDescription>\n"
			+ "<modelName>Philips hue bridge 2012</modelName>\n" + "<modelNumber>929000226503</modelNumber>\n"
			+ "<modelURL>http://www.bwssystems.com/apps.html</modelURL>\n"
			+ "<serialNumber>0017880ae670</serialNumber>\n"
			+ "<UDN>uuid:88f6698f-2c83-4393-bd03-cd54a9f8595</UDN>\n" + "<serviceList>\n" + "<service>\n"
			+ "<serviceType>(null)</serviceType>\n" + "<serviceId>(null)</serviceId>\n"
			+ "<controlURL>(null)</controlURL>\n" + "<eventSubURL>(null)</eventSubURL>\n"
			+ "<SCPDURL>(null)</SCPDURL>\n" + "</service>\n" + "</serviceList>\n"
			+ "<presentationURL>index.html</presentationURL>\n" + "<iconList>\n" + "<icon>\n"
			+ "<mimetype>image/png</mimetype>\n" + "<height>48</height>\n" + "<width>48</width>\n"
			+ "<depth>24</depth>\n" + "<url>hue_logo_0.png</url>\n" + "</icon>\n" + "<icon>\n"
			+ "<mimetype>image/png</mimetype>\n" + "<height>120</height>\n" + "<width>120</width>\n"
			+ "<depth>24</depth>\n" + "<url>hue_logo_3.png</url>\n" + "</icon>\n" + "</iconList>\n" + "</device>\n"
			+ "</root>\n";

	public UpnpSettingsResource(BridgeSettings theBridgeSettings) {
		super();
		this.theSettings = new BridgeSettings();
		this.theSettings.setDevMode(theBridgeSettings.isDevMode());
		this.theSettings.setHarmonyAddress(theBridgeSettings.getHarmonyAddress());
		this.theSettings.setServerPort(theBridgeSettings.getServerPort());
		this.theSettings.setTraceupnp(theBridgeSettings.isTraceupnp());
		this.theSettings.setUpnpConfigAddress(theBridgeSettings.getUpnpConfigAddress());
		this.theSettings.setUpnpDeviceDb(theBridgeSettings.getUpnpDeviceDb());
		this.theSettings.setButtonsleep(theBridgeSettings.getButtonsleep());
		this.theSettings.setUpnpResponsePort(theBridgeSettings.getUpnpResponsePort());
		this.theSettings.setUpnpStrict(theBridgeSettings.isUpnpStrict());
		this.theSettings.setVeraAddress(theBridgeSettings.getVeraAddress());
		this.theSettings.setNestConfigured(theBridgeSettings.isValidNest());
	}

	public void setupServer() {
		log.info("Hue description service started....");
//      http://ip_adress:port/description.xml which returns the xml configuration for the hue emulator
		get("/description.xml", "application/xml; charset=utf-8", (request, response) -> {
			if(theSettings.isTraceupnp())
				log.info("Traceupnp: upnp device settings requested: " + request.params(":id") + " from " + request.ip() + ":" + request.port());
			else
				log.debug("upnp device settings requested: " + request.params(":id") + " from " + request.ip() + ":" + request.port());
			String portNumber = Integer.toString(request.port());
			String filledTemplate = null;
			filledTemplate = String.format(hueTemplate, theSettings.getUpnpConfigAddress(), portNumber, theSettings.getUpnpConfigAddress());
			if(theSettings.isTraceupnp())
				log.info("Traceupnp: upnp device settings response: " + filledTemplate);
			else
				log.debug("upnp device settings response: " + filledTemplate);
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

//      http://ip_address:port/upnp/settings which returns the bridge configuration settings
		get(UPNP_CONTEXT + "/settings", "application/json", (request, response) -> {
			log.debug("bridge settings requested from " + request.ip());

			response.status(200);

            return theSettings;
        }, new JsonTransformer());
	}
}
