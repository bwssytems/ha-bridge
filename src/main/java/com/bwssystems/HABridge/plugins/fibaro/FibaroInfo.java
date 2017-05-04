package com.bwssystems.HABridge.plugins.fibaro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class FibaroInfo
{
	private static final Logger log = LoggerFactory.getLogger(FibaroInfo.class);
	private HTTPHandler httpClient;
	private NamedIP fibaroAddress;

	// You can disable it if you want
	boolean useSaveLogs = true; // This can be used to exclude some devices from list
	boolean useUserDescription = true;
	boolean removeDigits = true;
	boolean scenesWithLiliCommandOnly = true;

	public FibaroInfo(NamedIP addressName)
	{
		super();
		httpClient = new HTTPHandler();
		fibaroAddress = addressName;
	}

	public Device[] getDevices()
	{
		Room[] rooms = getRooms();

		log.debug("Founded: " + rooms.length + ", rooms");

		Device[] all_devices = getAllDevices();
		int count = 0;
		for(Device d : all_devices)
		{
			if(d.roomID > 0 && useSaveLogs ? "true".equals(d.properties.saveLogs) : true)
				count++;
		}

		Device[] devices = new Device[count];
		int i = 0;
		for(Device d : all_devices)
			if(d.roomID > 0 && useSaveLogs ? "true".equals(d.properties.saveLogs) : true)
			{
				if(useUserDescription && d.properties.userDescription != null && !d.properties.userDescription.isEmpty())
					d.name = d.properties.userDescription;
				if(removeDigits)
					d.name = replaceDigits(d.name);
				devices[i++] = d;

				for(Room room : rooms)
					if(d.roomID == room.id)
					{
						d.room = room.name.trim();
						d.category = String.valueOf(room.sectionID); // TODO load name of section
					}

				d.fibaroaddress = fibaroAddress.getIp();
				d.fibaroname = fibaroAddress.getName();
			}

		log.debug("Founded: " + devices.length + " devices");

		return devices;
	}

	public Scene[] getScenes()
	{
		Room[] rooms = getRooms();
		int count = 0;
		Scene[] scenes = getAllScenes();
		for(Scene s : scenes)
			if(!scenesWithLiliCommandOnly || s.liliStartCommand != null && !s.liliStartCommand.isEmpty())
				count++;
		Scene[] result = new Scene[count];
		int i = 0;
		for(Scene s : scenes)
			if(!scenesWithLiliCommandOnly || s.liliStartCommand != null && !s.liliStartCommand.isEmpty())
			{
				result[i++] = s;

				for(Room room : rooms)
					if(s.roomID == room.id)
					{
						s.room = room.name.trim();
						s.category = String.valueOf(room.sectionID); // TODO load name of section
					}

				s.fibaroaddress = fibaroAddress.getIp();
				s.fibaroname = fibaroAddress.getName();
			}
		System.out.println("Founded: " + count + " scenes");
		return result;
	}

	private Device[] getAllDevices()
	{
		String result = request("devices?enabled=true&visible=true");
		if(result == null)
			return new Device[0];
		Device[] devices = new Gson().fromJson(result, Device[].class);
		return devices;
	}

	private Scene[] getAllScenes()
	{
		String result = request("scenes?enabled=true&visible=true");
		if(result == null)
			return new Scene[0];
		Scene[] scenes = new Gson().fromJson(result, Scene[].class);
		return scenes;
	}

	private Room[] getRooms()
	{
		String result = request("rooms");
		if(result == null)
			return new Room[0];
		return new Gson().fromJson(result, Room[].class);
	}

	private String request(String theUrl)
	{
		theUrl = "http://" + fibaroAddress.getIp() + "/api/" + theUrl;
		return httpClient.doHttpRequest(theUrl, null, null, null, null);
	}

	private String replaceDigits(String name)
	{
		name = name.replaceAll("1", "");
		name = name.replaceAll("2", "");
		name = name.replaceAll("3", "");
		name = name.replaceAll("4", "");
		name = name.replaceAll("5", "");
		name = name.replaceAll("6", "");
		name = name.replaceAll("7", "");
		name = name.replaceAll("8", "");
		name = name.replaceAll("9", "");
		name = name.replaceAll("0", "");
		name = name.trim();
		return name;
	}
}
