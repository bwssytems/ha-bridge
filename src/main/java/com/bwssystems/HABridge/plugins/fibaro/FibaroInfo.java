package com.bwssystems.HABridge.plugins.fibaro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.fibaro.json.Device;
import com.bwssystems.HABridge.plugins.fibaro.json.Room;
import com.bwssystems.HABridge.plugins.fibaro.json.Scene;
import com.google.gson.Gson;

public class FibaroInfo
{
	private static final Logger log = LoggerFactory.getLogger(FibaroInfo.class);

	private final NamedIP fibaroAddress;
	private final String fibaroAuth;
	private final Gson gson;
	private Boolean isDevMode;
	private FibaroFilter theFilters;

	public FibaroInfo(NamedIP addressName)
	{
		super();
		fibaroAddress = addressName;
		fibaroAuth = "Basic " + addressName.getUserPass64();
        isDevMode = Boolean.parseBoolean(System.getProperty("dev.mode", "false"));
		gson = new Gson();
		theFilters = null;
		if(fibaroAddress.getExtensions() != null) {
			try {
				theFilters = gson.fromJson(fibaroAddress.getExtensions(), FibaroFilter.class);
			} catch(Exception e) {
				log.warn("Could not read fibaro filters - continuing with defaults.");
				theFilters = null;
			}
		}

		if(theFilters == null) {
	        theFilters = new FibaroFilter();
	        theFilters.setUseSaveLogs(false);
	        theFilters.setUseUserDescription(false);
	        theFilters.setScenesLiliCmddOnly(false);
	        theFilters.setReplaceTrash(true);
		}
	}

	private String request(String request)
	{
		String result = null;
		try
		{
			URL url = new URL("http://" + fibaroAddress.getIp() + ":" + fibaroAddress.getPort() + request);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", fibaroAuth);
			connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			connection.connect();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder buffer = new StringBuilder();
			String line;
			while((line = br.readLine()) != null)
			{
				buffer.append(line).append("\n");
			}
			br.close();
			result = buffer.toString();
		}
		catch(IOException e)
		{
			log.warn("Error while get getJson: {} ", request, e);
		}
		return result;
	}

	private String replaceTrash(String name)
	{
		String sanitizedName = name.replaceAll("[0-9:/-]", "");
		sanitizedName = name.replaceAll("\\s+", " ");
		return sanitizedName.trim();
	}
	
	private Room[] getRooms()
	{
		String result = null;
		if(isDevMode)
			result = FibaroTestData.RoomTestData;
		else
			result = request("/api/rooms");
		log.debug("getRooms response: <<<" + result + ">>>");
		Room[] rooms = result == null ? new Room[0] : gson.fromJson(result, Room[].class);
		if(theFilters.isReplaceTrash())
			for(Room r : rooms)
				r.setName(replaceTrash(r.getName()));
		return rooms;
	}

	public Device[] getDevices()
	{
		Room[] rooms = getRooms();

		log.debug("getDevices Found: " + rooms.length + " rooms");

		String result = null;
		if(isDevMode)
			result = FibaroTestData.DeviceTestData;
		else
			result = request("/api/devices?enabled=true&visible=true");
		log.debug("getDevices response: <<<" + result + ">>>");
		Device[] all_devices = result == null ? new Device[0] : gson.fromJson(result, Device[].class);

		int count = 0;
		for(Device d : all_devices)
			if(d.getRoomID() > 0 && (theFilters.isUseSaveLogs() ? "true".equals(d.getProperties().getSaveLogs()) : true))
				count++;

		Device[] devices = new Device[count];
		int i = 0;
		for(Device d : all_devices)
			if(d.getRoomID() > 0 && (theFilters.isUseSaveLogs() ? "true".equals(d.getProperties().getSaveLogs()) : true))
			{
				if(theFilters.isUseUserDescription() && d.getProperties().getUserDescription() != null && !d.getProperties().getUserDescription().isEmpty())
					d.setName(d.getProperties().getUserDescription());
				if(theFilters.isReplaceTrash())
					d.setName(replaceTrash(d.getName()));
				
				devices[i++] = d;

				for(Room room : rooms)
					if(d.getRoomID() == room.getId())
						d.setRoomName(room.getName());

				d.fibaroaddress = fibaroAddress.getIp();
				d.fibaroport = fibaroAddress.getPort();
				d.fibaroAuth = fibaroAuth;
				d.fibaroname = fibaroAddress.getName();
			}

		log.debug("getDevices Found: " + devices.length + " devices");

		return devices;
	}

	public Scene[] getScenes()
	{
		Room[] rooms = getRooms();
		
		String result = null;
		if(isDevMode)
			result = FibaroTestData.SceneTestData;
		else
			result = request("/api/scenes");
		log.debug("getScenes response: <<<" + result + ">>>");
		Scene[] all_scenes = result == null ? new Scene[0] : gson.fromJson(result, Scene[].class);
		
		int count = 0;
		for(Scene s : all_scenes)
			if(!theFilters.isScenesLiliCmddOnly() || s.getLiliStartCommand() != null && !s.getLiliStartCommand().isEmpty())
				count++;
		Scene[] scenes = new Scene[count];
		int i = 0;
		for(Scene s : all_scenes)
			if(!theFilters.isScenesLiliCmddOnly() || s.getLiliStartCommand() != null && !s.getLiliStartCommand().isEmpty())
			{
				if(theFilters.isReplaceTrash())
					s.setName(replaceTrash(s.getName()));
				
				scenes[i++] = s;

				for(Room room : rooms)
					if(s.getRoomID() == room.getId())
						s.setRoomName(room.getName());

				s.fibaroaddress = fibaroAddress.getIp();
				s.fibaroport = fibaroAddress.getPort();
				s.fibaroAuth = fibaroAuth;
				s.fibaroname = fibaroAddress.getName();
			}
		log.debug("getScenes Found: " + count + " scenes");
		return scenes;
	}
}
