package com.bwssystems.fhem.test;

import java.util.List;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.fhem.FHEMDevice;
import com.bwssystems.HABridge.plugins.fhem.FHEMInstance;
import com.bwssystems.HABridge.plugins.fhem.FHEMItem;
import com.bwssystems.HABridge.plugins.fhem.Result;
import com.bwssystems.HABridge.plugins.http.HttpTestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FHEMInstanceConstructor {
	public final static String TestData = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
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

	public final static String TestData2 = "        <div id='content' >\n" + 
			"            <pre>\n" + "{   \"Arg\":\"room=HaBridge\",   \"Results\": [   {     \"Name\":\"wifi_steckdose3\",     \"PossibleSets\":\"on:noArg off:noArg off on toggle\",     \"PossibleAttrs\":\"alias comment:textField-long eventMap group room suppressReading userReadings:textField-long verbose:0,1,2,3,4,5 IODev qos retain publishSet publishSet_.* subscribeReading_.* autoSubscribeReadings event-on-change-reading event-on-update-reading event-aggregator event-min-interval stateFormat:textField-long timestamp-on-change-reading alarmDevice:Actor,Sensor alarmSettings cmdIcon devStateIcon devStateStyle icon lightSceneParamsToSave lightSceneRestoreOnlyIfChanged:1,0 sortby structexclude webCmd webCmdLabel:textField-long widgetOverride userattr\",     \"Internals\": {       \"CHANGED\": \"null\",       \"NAME\": \"wifi_steckdose3\",       \"NR\": \"270\",       \"STATE\": \"off\",       \"TYPE\": \"MQTT_DEVICE\",       \"retain\": \"*:1 \"     },     \"Readings\": {       \"state\": { \"Value\":\"OFF\", \"Time\":\"2018-01-01 23:01:21\" },       \"transmission-state\": { \"Value\":\"subscription acknowledged\", \"Time\":\"2018-01-03 22:34:00\" }     },     \"Attributes\": {       \"IODev\": \"myBroker\",       \"alias\": \"Stecki\",       \"devStateIcon\": \"on:black_Steckdose.on off:black_Steckdose.off\",       \"event-on-change-reading\": \"state\",       \"eventMap\": \"ON:on OFF:off\",       \"publishSet\": \"on off toggle /SmartHome/az/stecker/cmnd/POWER\",       \"retain\": \"1\",       \"room\": \"HaBridge,Arbeitszimmer,mqtt\",       \"stateFormat\": \"state\",       \"subscribeReading_state\": \"/SmartHome/az/stecker/stat/POWER\",       \"webCmd\": \"on:off:toggle\"     }   }  ],   \"totalResultsReturned\":1 }" + 
			"            </pre>\n" + 
			"        </div>\n" + 
			"    </body>\n" + 
			"</html>";
	public final static String TestData3 ="DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
			"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + 
			"<head root=\"/fhem\">\n" + 
			"<title>Home, Sweet Home</title>\n" + 
			"<link rel=\"shortcut icon\" href=\"/fhem/icons/favicon\" />\n" + 
			"<meta charset=\"UTF-8\">\n" + 
			"<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" + 
			"<link href=\"/fhem/pgm2/style.css?v=1515015198\" rel=\"stylesheet\"/>\n" + 
			"<link href=\"/fhem/pgm2/jquery-ui.min.css\" rel=\"stylesheet\"/>\n" + 
			"<script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/jquery.min.js\"></script>\n" + 
			"<script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/jquery-ui.min.js\"></script>\n" + 
			"<script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb.js\"></script>\n" + 
			"<script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/doif.js\"></script>\n" + 
			"<script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fronthemEditor.js\"></script>\n" + 
			"<script attr='' type=\"text/javascript\" src=\"/fhem/pgm2/fhemweb_readingsGroup.js\"></script>\n" + 
			"</head>\n" + 
			"<body name='Home, Sweet Home' fw_id='1490' generated=\"1515770038\" longpoll=\"websocket\"  data-confirmDelete='1' data-confirmJSError='1' data-addHtmlTitle='1' data-availableJs='sortable,iconLabel,readingsHistory,colorpicker,iconButtons,fbcalllist,knob,weekprofile,iconRadio,readingsGroup,iconSwitch,uzsu' data-webName='WEB '>\n" + 
			"<div id=\"menuScrollArea\">\n" + 
			"</div>\n" + 
			"<div id='content' >\n" + 
			"<pre>{\n" + 
			"\"Arg\":\"room=HaBridge\",\n" + 
			"\"Results\": [\n" + 
			"{\n" + 
			"\"Name\":\"<a href='/fhem?detail=wifi_steckdose3'>wifi_steckdose3</a>\",\n" + 
			"\"PossibleSets\":\"on:noArg off:noArg off on toggle\",\n" + 
			"\"PossibleAttrs\":\"alias comment:textField-long eventMap group room suppressReading userReadings:textField-long verbose:0,1,2,3,4,5 IODev qos retain publishSet publishSet_.* subscribeReading_.* autoSubscribeReadings event-on-change-reading event-on-update-reading event-aggregator event-min-interval stateFormat:textField-long timestamp-on-change-reading alarmDevice:Actor,Sensor alarmSettings cmdIcon devStateIcon devStateStyle icon lightSceneParamsToSave lightSceneRestoreOnlyIfChanged:1,0 sortby structexclude webCmd webCmdLabel:textField-long widgetOverride userattr\",\n" + 
			"\"Internals\": {\n" + 
			"\"NAME\": \"<a href='/fhem?detail=wifi_steckdose3'>wifi_steckdose3</a>\",\n" + 
			"\"NR\": \"270\",\n" + 
			"\"STATE\": \"off\",\n" + 
			"\"TYPE\": \"MQTT_DEVICE\",\n" + 
			"\"retain\": \"*:1 \"\n" + 
			"},\n" + 
			"\"Readings\": {\n" + 
			"\"state\": { \"Value\":\"OFF\", \"Time\":\"2018-01-07 05:16:01\" },\n" + 
			"\"transmission-state\": { \"Value\":\"incoming publish received\", \"Time\":\"2018-01-07 05:16:01\" }\n" + 
			"},\n" + 
			"\"Attributes\": {\n" + 
			"\"IODev\": \"<a href='/fhem?detail=myBroker'>myBroker</a>\",\n" + 
			"\"alias\": \"Stecki\",\n" + 
			"\"devStateIcon\": \"on:black_Steckdose.on off:black_Steckdose.off\",\n" + 
			"\"event-on-change-reading\": \"state\",\n" + 
			"\"eventMap\": \"ON:on OFF:off\",\n" + 
			"\"publishSet\": \"on off toggle /SmartHome/az/stecker/cmnd/POWER\",\n" + 
			"\"retain\": \"1\",\n" + 
			"\"room\": \"HaBridge,Arbeitszimmer,mqtt\",\n" + 
			"\"stateFormat\": \"state\",\n" + 
			"\"subscribeReading_state\": \"/SmartHome/az/stecker/stat/POWER\",\n" + 
			"\"webCmd\": \"on:off:toggle\"\n" + 
			"}\n" + 
			"}  ],\n" + 
			"\"totalResultsReturned\":1\n" + 
			"}\n" + 
			"</pre>\n" + 
			"</div>\n" + 
			"</body></html>";
	public static void main(String[] args){
		FHEMInstanceConstructor aTestService = new FHEMInstanceConstructor();
		if(aTestService.validateStructure())
			System.out.println("Test Successful");
	}

	public Boolean validateStructure() {
		Gson aGson;
		NamedIP anAddress = new NamedIP();
		anAddress.setName("TestData1");
		anAddress.setIp("10.0.0.1");
		FHEMInstance anInstance = new FHEMInstance(anAddress);
		HttpTestHandler theHttpTestClient = new HttpTestHandler();
		List<Result> services = null;
		List<FHEMDevice> deviceList = null;
		String decodeData = null;
		String theTestData = null;
		
		for(int i = 0; i < 3; i++) {
			if(i == 0)
				theTestData = TestData;
			else if(i == 1) {
				theTestData = TestData2;
				anAddress.setName(anAddress.getName().replace("1", "2"));
				anInstance = new FHEMInstance(anAddress);
			}
			else {
				anAddress.setName(anAddress.getName().replace("2", "3"));
				theTestData = TestData3;
			}
			decodeData = anInstance.getJSONData(theTestData);
			try {
				aGson = new GsonBuilder()
		                .create();
				
				FHEMItem aService = aGson.fromJson(decodeData, FHEMItem.class);
				services = aService.getResults();
				for(Result aResult:services) {
					System.out.println(anAddress.getName() + " - Json Test:");
					System.out.println("    " +  aResult.getName());
					System.out.println("    	" + aResult.getPossibleSets());
				}
			} catch (Exception e) {
				return false;
			}
			System.out.println("----------------------------------");
			try {
				theHttpTestClient.setTheData(theTestData);
				deviceList = anInstance.getDevices(theHttpTestClient);
				if(deviceList == null)
					return false;
				for(FHEMDevice aDevice:deviceList) {
					System.out.println(aDevice.getName() + " - FHEMDevice Class Test:");
					System.out.println("    " + aDevice.getItem().getName());
					System.out.println("    	" + aDevice.getItem().getPossibleSets());
				}
			} catch (Exception e) {
				return false;
			}
			System.out.println("----------------------------------");
		}
		return true;
	}

}
