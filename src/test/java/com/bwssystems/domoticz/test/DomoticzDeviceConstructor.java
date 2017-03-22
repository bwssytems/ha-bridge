package com.bwssystems.domoticz.test;

import java.util.Iterator;

import com.bwssystems.HABridge.plugins.domoticz.DeviceResult;
import com.bwssystems.HABridge.plugins.domoticz.Devices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DomoticzDeviceConstructor {
	public final static String DevicesTestData = "{ \"ActTime\" : 1485295582, \"ServerTime\" : \"2017-01-24 16:06:22\", \"Sunrise\" : \"07:11\", \"Sunset\" : \"16:53\", \"result\" : [ { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 2, \"Data\" : \"On\", \"Description\" : \"\", \"Favorite\" : 1, \"HardwareID\" : 3, \"HardwareName\" : \"MyHue\", \"HardwareType\" : \"Philips Hue Bridge\", \"HardwareTypeVal\" : 38, \"HaveDimmer\" : true, \"HaveGroupCmd\" : false, \"HaveTimeout\" : false, \"ID\" : \"1\", \"Image\" : \"TV\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2017-01-23 17:15:22\", \"Level\" : 0, \"LevelInt\" : 0, \"MaxDimLevel\" : 100, \"Name\" : \"TV\", \"Notifications\" : \"false\", \"PlanID\" : \"0\", \"PlanIDs\" : [ 0 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"On\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"RGBW\", \"SwitchType\" : \"On/Off\", \"SwitchTypeVal\" : 0, \"Timers\" : \"false\", \"Type\" : \"Lighting Limitless/Applamp\", \"TypeImg\" : \"lightbulb\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"0\", \"YOffset\" : \"0\", \"idx\" : \"23\" }, { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 0, \"Data\" : \"On\", \"Description\" : \"\", \"Favorite\" : 1, \"HardwareID\" : 3, \"HardwareName\" : \"MyHue\", \"HardwareType\" : \"Philips Hue Bridge\", \"HardwareTypeVal\" : 38, \"HaveDimmer\" : true, \"HaveGroupCmd\" : false, \"HaveTimeout\" : false, \"ID\" : \"0000000B\", \"Image\" : \"Light\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2017-01-23 16:15:31\", \"Level\" : 0, \"LevelInt\" : 0, \"MaxDimLevel\" : 100, \"Name\" : \"lights\", \"Notifications\" : \"false\", \"PlanID\" : \"0\", \"PlanIDs\" : [ 0 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"On\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"RGBW\", \"SwitchType\" : \"On/Off\", \"SwitchTypeVal\" : 0, \"Timers\" : \"false\", \"Type\" : \"Lighting Limitless/Applamp\", \"TypeImg\" : \"lightbulb\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"0\", \"YOffset\" : \"0\", \"idx\" : \"25\" }, { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 0, \"Data\" : \"Off\", \"Description\" : \"\", \"Favorite\" : 1, \"HardwareID\" : 3, \"HardwareName\" : \"MyHue\", \"HardwareType\" : \"Philips Hue Bridge\", \"HardwareTypeVal\" : 38, \"HaveDimmer\" : true, \"HaveGroupCmd\" : false, \"HaveTimeout\" : false, \"ID\" : \"00000014\", \"Image\" : \"Light\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2017-01-23 11:25:59\", \"Level\" : 0, \"LevelInt\" : 0, \"MaxDimLevel\" : 100, \"Name\" : \"testUDP\", \"Notifications\" : \"false\", \"PlanID\" : \"0\", \"PlanIDs\" : [ 0 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"Off\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"RGBW\", \"SwitchType\" : \"Dimmer\", \"SwitchTypeVal\" : 7, \"Timers\" : \"false\", \"Type\" : \"Lighting Limitless/Applamp\", \"TypeImg\" : \"dimmer\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"0\", \"YOffset\" : \"0\", \"idx\" : \"35\" }, { \"AddjMulti\" : 1.0, \"AddjMulti2\" : 1.0, \"AddjValue\" : 0.0, \"AddjValue2\" : 0.0, \"BatteryLevel\" : 255, \"CustomImage\" : 0, \"Data\" : \"Off\", \"Description\" : \"\", \"Favorite\" : 1, \"HardwareID\" : 3, \"HardwareName\" : \"MyHue\", \"HardwareType\" : \"Philips Hue Bridge\", \"HardwareTypeVal\" : 38, \"HaveDimmer\" : true, \"HaveGroupCmd\" : false, \"HaveTimeout\" : false, \"ID\" : \"00000009\", \"Image\" : \"Light\", \"IsSubDevice\" : false, \"LastUpdate\" : \"2017-01-24 09:18:22\", \"Level\" : 94, \"LevelInt\" : 94, \"MaxDimLevel\" : 100, \"Name\" : \"Test Light on CM15 (PL) N1\", \"Notifications\" : \"false\", \"PlanID\" : \"0\", \"PlanIDs\" : [ 0 ], \"Protected\" : false, \"ShowNotifications\" : true, \"SignalLevel\" : \"-\", \"Status\" : \"Off\", \"StrParam1\" : \"\", \"StrParam2\" : \"\", \"SubType\" : \"RGBW\", \"SwitchType\" : \"Dimmer\", \"SwitchTypeVal\" : 7, \"Timers\" : \"false\", \"Type\" : \"Lighting Limitless/Applamp\", \"TypeImg\" : \"dimmer\", \"Unit\" : 1, \"Used\" : 1, \"UsedByCamera\" : false, \"XOffset\" : \"0\", \"YOffset\" : \"0\", \"idx\" : \"44\" } ], \"status\" : \"OK\", \"title\" : \"Devices\" }";
	public final static String ScenesTestData = "{ \"ActTime\" : 1485295431, \"AllowWidgetOrdering\" : true, \"ServerTime\" : \"2017-01-24 16:03:51\", \"Sunrise\" : \"07:11\", \"Sunset\" : \"16:53\", \"result\" : [ { \"Description\" : \"\", \"Favorite\" : 0, \"LastUpdate\" : \"2017-01-23 11:06:31\", \"Name\" : \"Watch TV\", \"OffAction\" : \"\", \"OnAction\" : \"\", \"Protected\" : false, \"Status\" : \"On\", \"Timers\" : \"false\", \"Type\" : \"Scene\", \"UsedByCamera\" : false, \"idx\" : \"1\" }, { \"Description\" : \"\", \"Favorite\" : 0, \"LastUpdate\" : \"2017-01-23 11:25:58\", \"Name\" : \"TestScene\", \"OffAction\" : \"\", \"OnAction\" : \"\", \"Protected\" : false, \"Status\" : \"Off\", \"Timers\" : \"false\", \"Type\" : \"Scene\", \"UsedByCamera\" : false, \"idx\" : \"2\" } ], \"status\" : \"OK\", \"title\" : \"Scenes\" }";
	public static void main(String[] args){
		DomoticzDeviceConstructor aTestService = new DomoticzDeviceConstructor();
		if(aTestService.validateStructure())
			System.out.println("Test Successful");
	}

	public Boolean validateStructure() {
		Gson aGson;
		try {
			aGson = new GsonBuilder()
//	                .registerTypeAdapter(Service.class, new ServiceDeserializer())
//	                .registerTypeHierarchyAdapter(Field.class, new FieldDeserializer())
	                .create();
			System.out.println("Decode Domoticz Devices Data");
			Devices aDeviceContainer = aGson.fromJson(DevicesTestData, Devices.class);
			Iterator<DeviceResult> aList = aDeviceContainer.getResult().iterator();
			while(aList.hasNext()) {
				DeviceResult theResult = aList.next();
				System.out.println("    " + theResult.getName() + " - " + theResult.getDescription() + " - " + theResult.getType());
			}
			System.out.println("Decode Domoticz Sceness Data");
			aDeviceContainer = aGson.fromJson(ScenesTestData, Devices.class);
			aList = aDeviceContainer.getResult().iterator();
			while(aList.hasNext()) {
				DeviceResult theResult = aList.next();
				System.out.println("    " + theResult.getName() + " - " + theResult.getDescription() + " - " + theResult.getType());
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
