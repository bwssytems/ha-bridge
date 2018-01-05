package com.bwssystems.HABridge.plugins.fhem;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class FHEMInstance {
	private static final Logger log = LoggerFactory.getLogger(FHEMInstance.class);
	private NamedIP theFhem;

	public FHEMInstance(NamedIP fhemLocation) {
		super();
		theFhem = fhemLocation;
	}

	public NamedIP getFhemAddress() {
		return theFhem;
	}

	public void setFhemAddress(NamedIP fhemAddress) {
		this.theFhem = fhemAddress;
	}

	public Boolean callCommand(String aCommand, String commandData, HTTPHandler httpClient) {
		log.debug("calling FHEM: " + theFhem.getIp() + ":" + theFhem.getPort() + aCommand);
		String aUrl = null;
		NameValue[] headers = null;
		if(theFhem.getSecure() != null && theFhem.getSecure())
			aUrl = "https://";
		else
			aUrl = "http://";
		if(theFhem.getUsername() != null && !theFhem.getUsername().isEmpty() && theFhem.getPassword() != null && !theFhem.getPassword().isEmpty()) {
			aUrl = aUrl + theFhem.getUsername() + ":" + theFhem.getPassword() + "@";
		}
		aUrl = aUrl + theFhem.getIp() + ":" + theFhem.getPort() + "/" + aCommand;
		String theData = httpClient.doHttpRequest(aUrl, HttpPost.METHOD_NAME, "text/plain", commandData, headers);
   		log.debug("call Command return is: <" + theData + ">");
		return true;
	}

	public List<FHEMDevice> getDevices(HTTPHandler httpClient) {
		List<FHEMDevice> deviceList = null;
		FHEMItem theFhemStates;
		String theUrl = null;
    	String theData;
		NameValue[] headers = null;
		if(theFhem.getSecure() != null && theFhem.getSecure())
			theUrl = "https://";
		else
			theUrl = "http://";
		if(theFhem.getUsername() != null && !theFhem.getUsername().isEmpty() && theFhem.getPassword() != null && !theFhem.getPassword().isEmpty()) {
			theUrl = theUrl + theFhem.getUsername() + ":" + theFhem.getPassword() + "@";
		}
   		theUrl = theUrl + theFhem.getIp() + ":" + theFhem.getPort() + "/fhem?cmd=jsonlist2";
   		if(theFhem.getWebhook() != null && !theFhem.getWebhook().trim().isEmpty())
   			theUrl = theUrl + "%20room=" + theFhem.getWebhook().trim();
   		theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
    	if(theData != null) {
    		log.debug("GET FHEM States - data: " + theData);
    		theData = getJSONData(theData);
    		theFhemStates = new Gson().fromJson(theData, FHEMItem.class);
	    	if(theFhemStates == null) {
	    		log.warn("Cannot get any devices for FHEM " + theFhem.getName() + " as response is not parsable.");
	    	}
	    	else {
		    	deviceList = new ArrayList<FHEMDevice>();
		    	
		    	for (Result aResult:theFhemStates.getResults()) {
		    		FHEMDevice aNewFhemDeviceDevice = new FHEMDevice();
		    		aNewFhemDeviceDevice.setItem(aResult);
		    		aNewFhemDeviceDevice.setAddress(theFhem.getIp() + ":" + theFhem.getPort());
		    		aNewFhemDeviceDevice.setName(theFhem.getName());
					deviceList.add(aNewFhemDeviceDevice);
		    	}
	    	}
    	}
    	else
    		log.warn("Cannot get an devices for FHEM " + theFhem.getName() + " http call failed.");
		return deviceList;
	}

	public List<FHEMDevice> testGetDevices(HTTPHandler httpClient) {
		String TestData = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + 
				"    <head root=\"/fhem\">\n" + 
				"        <title>Home, Sweet Home</title>\n" + 
				"        <link rel=\"shortcut icon\" href=\"/fhem/icons/favicon\" />\n" + 
				"        <meta charset=\"UTF-8\">\n" + 
				"        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" + 
				"        <link href=\"/fhem/pgm2/style.css?v=1513026539\" rel=\"stylesheet\"/>\n" + 
				"        <link href=\"/fhem/pgm2/jquery-ui.min.css\" rel=\"stylesheet\"/>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/jquery.min.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/jquery-ui.min.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_colorpicker.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_fbcalllist.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_knob.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_readingsGroup.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_readingsHistory.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_sortable.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_uzsu.js\"></script>\n" + 
				"        <script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_weekprofile.js\"></script>\n" + 
				"    </head>\n" + 
				"    <body name='Home, Sweet Home' fw_id='7880' generated=\"1513272732\" longpoll=\"1\"  data-confirmDelete='1' data-confirmJSError='1' data-webName='haBridgeWeb '>\n" + 
				"        <div id=\"menuScrollArea\">\n" + 
				"            <div>\n" + 
				"                <a href=\"/fhem?\">\n" + 
				"                    <div id=\"logo\"></div>\n" + 
				"                </a>\n" + 
				"            </div>\n" + 
				"            <div id=\"menu\">\n" + 
				"                <table>\n" + 
				"                    <tr>\n" + 
				"                        <td>\n" + 
				"                            <table class=\"room roomBlock1\">\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Save_config\">\n" + 
				"                                            <a href=\"/fhem?cmd=save\">Save config</a>\n" + 
				"                                            <a id=\"saveCheck\" class=\"changed\" style=\"visibility:visible\">?</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                            </table>\n" + 
				"                        </td>\n" + 
				"                    </tr>\n" + 
				"                    <tr>\n" + 
				"                        <td>\n" + 
				"                            <table class=\"room roomBlock2\">\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Alexa\">\n" + 
				"                                            <a href=\"/fhem?room=Alexa\">Alexa</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_System\">\n" + 
				"                                            <a href=\"/fhem?room=System\">System</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_WG_Zimmer\">\n" + 
				"                                            <a href=\"/fhem?room=WG%2dZimmer\">WG-Zimmer</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_habridge\">\n" + 
				"                                            <a href=\"/fhem?room=habridge\">habridge</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Everything\">\n" + 
				"                                            <a href=\"/fhem?room=all\">\n" + 
				"                                                <img class='icon icoEverything' src=\"/fhem/images/default/icoEverything.png\" alt=\"icoEverything\" title=\"icoEverything\">&nbsp;Everything\n" + 
				"                                            </a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                            </table>\n" + 
				"                        </td>\n" + 
				"                    </tr>\n" + 
				"                    <tr>\n" + 
				"                        <td>\n" + 
				"                            <table class=\"room roomBlock3\">\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Logfile\">\n" + 
				"                                            <a href=\"/fhem/FileLog_logWrapper?dev=Logfile&type=text&file=fhem-2017-12.log\">Logfile</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div>\n" + 
				"                                            <a href=\"/fhem/docs/commandref.html\" target=\"_blank\" >Commandref</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div>\n" + 
				"                                            <a href=\"http://fhem.de/fhem.html#Documentation\" target=\"_blank\" >Remote doc</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Edit_files\">\n" + 
				"                                            <a href=\"/fhem?cmd=style%20list\">Edit files</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Select_style\">\n" + 
				"                                            <a href=\"/fhem?cmd=style%20select\">Select style</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                                <tr>\n" + 
				"                                    <td>\n" + 
				"                                        <div class=\"menu_Event_monitor\">\n" + 
				"                                            <a href=\"/fhem?cmd=style%20eventMonitor\">Event monitor</a>\n" + 
				"                                        </div>\n" + 
				"                                    </td>\n" + 
				"                                </tr>\n" + 
				"                            </table>\n" + 
				"                        </td>\n" + 
				"                    </tr>\n" + 
				"                </table>\n" + 
				"            </div>\n" + 
				"        </div>\n" + 
				"        <div id=\"hdr\">\n" + 
				"            <table border=\"0\" class=\"header\">\n" + 
				"                <tr>\n" + 
				"                    <td style=\"padding:0\">\n" + 
				"                        <form method=\"post\" action=\"/fhem\">\n" + 
				"                            <input type=\"hidden\" name=\"fw_id\" value=\"7880\"/>\n" + 
				"                            <input type=\"text\" name=\"cmd\" class=\"maininput\" size=\"40\" value=\"\"/>\n" + 
				"                        </form>\n" + 
				"                    </td>\n" + 
				"                </tr>\n" + 
				"            </table>\n" + 
				"        </div>\n" + 
				"        <div id='content' >\n" + 
				"            <pre>{ \n" + 
				"  \"Arg\":\"room=habridge\", \n" + 
				"  \"Results\": [ \n" + 
				"  { \n" + 
				"    \"Name\":\"Arbeitslicht\", \n" + 
				"    \"PossibleSets\":\"on off\", \n" + 
				"    \"PossibleAttrs\":\"alias comment:textField-long eventMap group room suppressReading userReadings:textField-long verbose:0,1,2,3,4,5 readingList setList useSetExtensions disable disabledForIntervals event-on-change-reading event-on-update-reading event-aggregator event-min-interval stateFormat:textField-long timestamp-on-change-reading alexaName alexaRoom cmdIcon devStateIcon devStateStyle fhem_widget_command fhem_widget_command_2 fhem_widget_command_3 genericDeviceType:security,ignore,switch,outlet,light,blind,thermometer,thermostat,contact,garage,window,lock homebridgeMapping:textField-long icon sortby webCmd widgetOverride userattr\", \n" + 
				"    \"Internals\": { \n" + 
				"      \"NAME\": \"Arbeitslicht\", \n" + 
				"      \"NR\": \"28\", \n" + 
				"      \"STATE\": \"-\", \n" + 
				"      \"TYPE\": \"dummy\" \n" + 
				"    }, \n" + 
				"    \"Readings\": {      \"state\": { \"Value\":\"on\", \"Time\":\"2017-12-14 15:41:05\" } }, \n" + 
				"    \"Attributes\": { \n" + 
				"      \"alexaName\": \"Arbeitslicht\", \n" + 
				"      \"alexaRoom\": \"alexaroom\", \n" + 
				"      \"fhem_widget_command\": \"{  \\u0022allowed_values\\u0022 : [    \\u0022on\\u0022  ],  \\u0022order\\u0022 : 0}\", \n" + 
				"      \"icon\": \"scene_office\", \n" + 
				"      \"room\": \"Alexa,habridge\", \n" + 
				"      \"setList\": \"on off\", \n" + 
				"      \"stateFormat\": \"-\", \n" + 
				"      \"webCmd\": \"on\" \n" + 
				"    } \n" + 
				"  }, \n" + 
				"  { \n" + 
				"    \"Name\":\"DeckenlampeLinks\", \n" + 
				"    \"PossibleSets\":\"on off dim dimup dimdown HSV RGB sync pair unpair\", \n" + 
				"    \"PossibleAttrs\":\"alias comment:textField-long eventMap group room suppressReading userReadings:textField-long verbose:0,1,2,3,4,5 gamma dimStep defaultColor defaultRamp colorCast whitePoint event-on-change-reading event-on-update-reading event-aggregator event-min-interval stateFormat:textField-long timestamp-on-change-reading alexaName alexaRoom cmdIcon devStateIcon devStateStyle fhem_widget_command fhem_widget_command_2 fhem_widget_command_3 genericDeviceType:security,ignore,switch,outlet,light,blind,thermometer,thermostat,contact,garage,window,lock homebridgeMapping:textField-long icon sortby webCmd widgetOverride \n" + 
				"                <a href=\"/fhem?detail=AlleLampen\">AlleLampen</a> AlleLampen_map\n" + 
				"                <a href=\"/fhem?detail=DeckenLampen\">DeckenLampen</a> DeckenLampen_map structexclude userattr\", \n" + 
				"    \"Internals\": { \n" + 
				"      \"CONNECTION\": \"bridge-V3\", \n" + 
				"      \"DEF\": \"RGBW2 bridge-V3:10.2.3.3\", \n" + 
				"      \"IP\": \"10.2.3.3\", \n" + 
				"      \"LEDTYPE\": \"RGBW2\", \n" + 
				"      \"NAME\": \"DeckenlampeLinks\", \n" + 
				"      \"NR\": \"18\", \n" + 
				"      \"NTFY_ORDER\": \"50-DeckenlampeLinks\", \n" + 
				"      \"PORT\": \"8899\", \n" + 
				"      \"PROTO\": \"0\", \n" + 
				"      \"SLOT\": \"5\", \n" + 
				"      \"STATE\": \"off\", \n" + 
				"      \"TYPE\": \"WifiLight\" \n" + 
				"    }, \n" + 
				"    \"Readings\": { \n" + 
				"      \"RGB\": { \"Value\":\"000000\", \"Time\":\"2017-12-14 15:41:10\" }, \n" + 
				"      \"brightness\": { \"Value\":\"0\", \"Time\":\"2017-12-14 15:41:10\" }, \n" + 
				"      \"hue\": { \"Value\":\"0\", \"Time\":\"2017-12-14 15:41:10\" }, \n" + 
				"      \"saturation\": { \"Value\":\"0\", \"Time\":\"2017-12-14 15:41:10\" }, \n" + 
				"      \"state\": { \"Value\":\"off\", \"Time\":\"2017-12-14 15:41:10\" } \n" + 
				"    }, \n" + 
				"    \"Attributes\": { \n" + 
				"      \"AlleLampen\": \"AlleLampen\", \n" + 
				"      \"DeckenLampen\": \"DeckenLampen\", \n" + 
				"      \"fhem_widget_command\": \"{ \\u0022locations\\u0022 : [ \\u0022APP\\u0022, \\u0022WATCH\\u0022, \\u0022WIDGET\\u0022 ], \\u0022allowed_values\\u0022 : [ \\u0022off\\u0022, \\u0022on\\u0022 ], \\u0022order\\u0022 : 6}\", \n" + 
				"      \"room\": \"habridge,Alexa,WG-Zimmer\", \n" + 
				"      \"userattr\": \"AlleLampen AlleLampen_map\n" + 
				"                <a href=\"/fhem?detail=DeckenLampen\">DeckenLampen</a> DeckenLampen_map structexclude\", \n" + 
				"      \"webCmd\": \"RGB\", \n" + 
				"      \"widgetOverride\": \"RGB:colorpicker,RGB\" \n" + 
				"    } \n" + 
				"  }  ], \n" + 
				"  \"totalResultsReturned\":2 \n" + 
				"}\n" + 
				"            </pre>\n" + 
				"        </div>\n" + 
				"    </body>\n" + 
				"</html>";

		String TestData2 = "        <div id='content' >\n" + 
				"            <pre>\n" + "{   \"Arg\":\"room=HaBridge\",   \"Results\": [   {     \"Name\":\"wifi_steckdose3\",     \"PossibleSets\":\"on:noArg off:noArg off on toggle\",     \"PossibleAttrs\":\"alias comment:textField-long eventMap group room suppressReading userReadings:textField-long verbose:0,1,2,3,4,5 IODev qos retain publishSet publishSet_.* subscribeReading_.* autoSubscribeReadings event-on-change-reading event-on-update-reading event-aggregator event-min-interval stateFormat:textField-long timestamp-on-change-reading alarmDevice:Actor,Sensor alarmSettings cmdIcon devStateIcon devStateStyle icon lightSceneParamsToSave lightSceneRestoreOnlyIfChanged:1,0 sortby structexclude webCmd webCmdLabel:textField-long widgetOverride userattr\",     \"Internals\": {       \"CHANGED\": \"null\",       \"NAME\": \"wifi_steckdose3\",       \"NR\": \"270\",       \"STATE\": \"off\",       \"TYPE\": \"MQTT_DEVICE\",       \"retain\": \"*:1 \"     },     \"Readings\": {       \"state\": { \"Value\":\"OFF\", \"Time\":\"2018-01-01 23:01:21\" },       \"transmission-state\": { \"Value\":\"subscription acknowledged\", \"Time\":\"2018-01-03 22:34:00\" }     },     \"Attributes\": {       \"IODev\": \"myBroker\",       \"alias\": \"Stecki\",       \"devStateIcon\": \"on:black_Steckdose.on off:black_Steckdose.off\",       \"event-on-change-reading\": \"state\",       \"eventMap\": \"ON:on OFF:off\",       \"publishSet\": \"on off toggle /SmartHome/az/stecker/cmnd/POWER\",       \"retain\": \"1\",       \"room\": \"HaBridge,Arbeitszimmer,mqtt\",       \"stateFormat\": \"state\",       \"subscribeReading_state\": \"/SmartHome/az/stecker/stat/POWER\",       \"webCmd\": \"on:off:toggle\"     }   }  ],   \"totalResultsReturned\":1 }" + 
				"            </pre>\n" + 
				"        </div>\n" + 
				"    </body>\n" + 
				"</html>";
		List<FHEMDevice> deviceList = null;
		FHEMItem theFhemStates;
		String theUrl = null;
    	String theData;
		NameValue[] headers = null;
		if(theFhem.getSecure() != null && theFhem.getSecure())
			theUrl = "https://";
		else
			theUrl = "http://";
		if(theFhem.getUsername() != null && !theFhem.getUsername().isEmpty() && theFhem.getPassword() != null && !theFhem.getPassword().isEmpty()) {
			theUrl = theUrl + theFhem.getUsername() + ":" + theFhem.getPassword() + "@";
		}
   		theUrl = theUrl + theFhem.getIp() + ":" + theFhem.getPort() + "/fhem?cmd=jsonlist2";
   		if(theFhem.getWebhook() != null && !theFhem.getWebhook().trim().isEmpty())
   			theUrl = theUrl + "%20room=" + theFhem.getWebhook().trim();
//   		theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
   		theData = TestData;
    	if(theData != null) {
    		log.debug("GET FHEM States - data: " + theData);
    		theData = getJSONData(theData);
    		theFhemStates = new Gson().fromJson(theData, FHEMItem.class);
	    	if(theFhemStates == null) {
	    		log.warn("Cannot get any devices for FHEM " + theFhem.getName() + " as response is not parsable.");
	    	}
	    	else {
		    	deviceList = new ArrayList<FHEMDevice>();
		    	
		    	for (Result aResult:theFhemStates.getResults()) {
		    		FHEMDevice aNewFhemDeviceDevice = new FHEMDevice();
		    		aNewFhemDeviceDevice.setItem(aResult);
		    		aNewFhemDeviceDevice.setAddress(theFhem.getIp() + ":" + theFhem.getPort());
		    		aNewFhemDeviceDevice.setName(theFhem.getName());
					deviceList.add(aNewFhemDeviceDevice);
		    	}
	    	}
    	}
    	else
    		log.warn("Cannot get an devices for FHEM " + theFhem.getName() + " http call failed.");
		return deviceList;
	}

	public String getJSONData(String response) {
		String theData;
		theData = response.substring(response.indexOf("<pre>") + 4);
		theData = theData.substring(1, theData.indexOf("</pre>") - 1);
		theData = theData.replace("\n", "");
		theData = theData.replace("\r", "");
		theData = theData.replace("<a href=\"", "<a href=\\\"");
		theData = theData.replace("\">", "\\\">");
		return theData;
	}
	
	protected void closeClient() {
	}
}
