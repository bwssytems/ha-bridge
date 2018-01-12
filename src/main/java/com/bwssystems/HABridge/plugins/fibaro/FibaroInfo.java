package com.bwssystems.HABridge.plugins.fibaro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
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

	// You can disable it if you want TODO config
	boolean useSaveLogs = true; // This can be used to exclude some devices from list
	boolean useUserDescription = true;
	boolean replaceTrash = true;
	boolean scenesWithLiliCommandOnly = true;

	public FibaroInfo(NamedIP addressName)
	{
		super();
		fibaroAddress = addressName;
		fibaroAuth = "Basic " + new String(Base64.encodeBase64((addressName.getUsername() + ":" + addressName.getPassword()).getBytes()));
		gson = new Gson();
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

	protected boolean sendCommand(String request)
	{
		try
		{
			URL url = new URL("http://" + fibaroAddress.getIp() + ":" + fibaroAddress.getPort() + request);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", fibaroAuth);
			String aResponse = connection.getResponseMessage();
			log.debug("sendCommand response: <<<" + aResponse + ">>>");
		}
		catch(IOException e)
		{
			log.warn("Error while get getJson: {} ", request, e);
			return false;
		}
		return true;
	}

	private String replaceTrash(String name)
	{
		String sanitizedName = name.replaceAll("[0-9:/-]", "");
		sanitizedName = name.replaceAll("\\s+", " ");
		return sanitizedName.trim();
	}
	
	private Room[] getRooms()
	{
		String result = request("/api/rooms");
		log.debug("getRooms response: <<<" + result + ">>>");
		Room[] rooms = result == null ? new Room[0] : gson.fromJson(result, Room[].class);
		if(replaceTrash)
			for(Room r : rooms)
				r.setName(replaceTrash(r.getName()));
		return rooms;
	}

	public Device[] getDevices()
	{
		Room[] rooms = getRooms();

		log.info("Found: " + rooms.length + " rooms");

		String result = request("/api/devices?enabled=true&visible=true");
		log.debug("getDevices response: <<<" + result + ">>>");
		Device[] all_devices = result == null ? new Device[0] : gson.fromJson(result, Device[].class);

		int count = 0;
		for(Device d : all_devices)
			if(d.getRoomID() > 0 && (useSaveLogs ? "true".equals(d.getProperties().getSaveLogs()) : true))
				count++;

		Device[] devices = new Device[count];
		int i = 0;
		for(Device d : all_devices)
			if(d.getRoomID() > 0 && (useSaveLogs ? "true".equals(d.getProperties().getSaveLogs()) : true))
			{
				if(useUserDescription && d.getProperties().getUserDescription() != null && !d.getProperties().getUserDescription().isEmpty())
					d.setName(d.getProperties().getUserDescription());
				if(replaceTrash)
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

		log.info("Found: " + devices.length + " devices");

		return devices;
	}

	public Scene[] getScenes()
	{
		Room[] rooms = getRooms();
		
		String result = request("/api/scenes?enabled=true&visible=true");
		log.debug("getScenes response: <<<" + result + ">>>");
		Scene[] all_scenes = result == null ? new Scene[0] : gson.fromJson(result, Scene[].class);
		
		int count = 0;
		for(Scene s : all_scenes)
			if(!scenesWithLiliCommandOnly || s.getLiliStartCommand() != null && !s.getLiliStartCommand().isEmpty())
				count++;
		Scene[] scenes = new Scene[count];
		int i = 0;
		for(Scene s : all_scenes)
			if(!scenesWithLiliCommandOnly || s.getLiliStartCommand() != null && !s.getLiliStartCommand().isEmpty())
			{
				if(replaceTrash)
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
		log.info("Found: " + count + " scenes");
		return scenes;
	}
}
