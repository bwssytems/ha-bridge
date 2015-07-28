package com.bwssytems.HABridge.upnp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;

/**
 * 
 */
public class UpnpSettingsResource {
    private static final String UPNP_CONTEXT = "/upnp";

    private Logger log = LoggerFactory.getLogger(UpnpSettingsResource.class);

	private String hueTemplate = "<?xml version=\"1.0\"?>\n" + "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n"
			+ "<specVersion>\n" + "<major>1</major>\n" + "<minor>0</minor>\n" + "</specVersion>\n"
			+ "<URLBase>http://%s:%s/</URLBase>\n" + // hostname string
			"<device>\n" + "<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>\n"
			+ "<friendlyName>Amazon-Echo-HA-Bridge (%s)</friendlyName>\n"
			+ "<manufacturer>Royal Philips Electronics</manufacturer>\n"
			+ "<manufacturerURL>http://www.armzilla..com</manufacturerURL>\n"
			+ "<modelDescription>Hue Emulator for Amazon Echo bridge</modelDescription>\n"
			+ "<modelName>Philips hue bridge 2012</modelName>\n" + "<modelNumber>929000226503</modelNumber>\n"
			+ "<modelURL>http://www.armzilla.com/amazon-echo-ha-bridge</modelURL>\n"
			+ "<serialNumber>01189998819991197253</serialNumber>\n"
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

	public UpnpSettingsResource() {
		super();
		setupListener();
	}

	private void setupListener () {
//      http://ip_address:port/upnp/:id/setup.xml which returns the xml configuration for the location of the hue emulator
		get(UPNP_CONTEXT + "/:id/setup.xml", "application/xml", (request, response) -> {
			log.info("upnp device settings requested: " + request.params(":id") + " from " + request.ip());
			String hostName = System.getProperty("upnp.config.address",  "192.168.1.1");
			String portNumber = Integer.toString(request.port());
			String filledTemplate = String.format(hueTemplate, hostName, portNumber, hostName);
			log.debug("upnp device settings response: " + filledTemplate);
            response.status(201);

            return filledTemplate;
        } );
	}
}
