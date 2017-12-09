package com.bwssystems.HABridge.plugins.homewizard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.homewizard.json.Device;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Control HomeWizard devices over HomeWizard Cloud
 * 
 * @author Björn Rennfanz (bjoern@fam-rennfanz.de)
 *
 */
public class HomeWizzardSmartPlugInfo {

	private static final Logger log = LoggerFactory.getLogger(HomeWizardHome.class);
	
	private static final String HOMEWIZARD_LOGIN_URL = "https://cloud.homewizard.com/account/login";
	private static final String HOMEWIZARD_API_URL = "https://plug.homewizard.com/plugs";
	private static final String EMPTY_STRING = "";
	
	private final String cloudAuth;
	private final Gson gson;
	
	private String cloudSessionId;
	private String cloudPlugName;
	private String cloudPlugId;
	
	public HomeWizzardSmartPlugInfo(NamedIP gateway, String name) {

		super();
		
		cloudAuth = "Basic " + new String(Base64.encodeBase64((gateway.getUsername() + ":" + DigestUtils.sha1Hex(gateway.getPassword())).getBytes()));
		cloudPlugName = name;
		gson = new Gson();
	}
	
	public boolean login()
	{
		try
		{
			URL url = new URL(HOMEWIZARD_LOGIN_URL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", cloudAuth);
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
			
			// Get session id from result JSON
			JSONObject json = new JSONObject(buffer.toString());
			cloudSessionId = json.get("session").toString();
		}
		catch(IOException | JSONException e)
		{
			log.warn("Error while login to cloud service ", e);
			return false;
		}
		
		return true;
	}

	private String requestJson(String request)
	{
		String result = null;
		
		// Check login was successful
		if (login()) {
			
			// Request JSON from Cloud service
			try
			{
				URL url = new URL(HOMEWIZARD_API_URL + request);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				connection.setRequestMethod("GET");
				connection.setRequestProperty("X-Session-Token", cloudSessionId);
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
				result = StringUtils.strip(result, "[]");
			}
			catch(IOException e)
			{
				log.warn("Error while get json request: {} ", request, e);
			}
		}
		
		return result;
	}
	
	private boolean sendAction(String request, String action)
	{
		boolean result = true;
		
		// Check login was successful
		if (login()) {
						
			// Post action into Cloud service
			try
			{
				URL url = new URL(HOMEWIZARD_API_URL + request);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				JsonObject actionJson = new JsonObject();
				actionJson.addProperty("action", StringUtils.capitalize(action));			
				
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("X-Session-Token", cloudSessionId);
				connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				
				OutputStream os = connection.getOutputStream();
				os.write(actionJson.toString().getBytes("UTF-8"));
				os.close();	
				
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder buffer = new StringBuilder();
				String line;
				
				while((line = br.readLine()) != null)
				{
					buffer.append(line).append("\n");
				}
				
				br.close();
				connection.disconnect();
				
				// Check if request was Ok
				if (!buffer.toString().contains("Success"))
				{
					result = false;
				}
			}
			catch(IOException e)
			{
				log.warn("Error while post json action: {} ", request, e);
				result = false;
			}
		}
		else
		{
			result = false;
		}
		
		return result;
	}
	
	public List<HomeWizardSmartPlugDevice> getDevices() 
	{
		List<HomeWizardSmartPlugDevice> homewizardDevices = new ArrayList<>();	
		try {
			
			String result = requestJson(EMPTY_STRING);
			JSONObject resultJson = new JSONObject(result);
			cloudPlugId = resultJson.getString("id");
		
			String all_devices_json = resultJson.get("devices").toString();
			Device[] devices = gson.fromJson(all_devices_json, Device[].class);
			
			// Fix names from JSON
			for (Device device : devices) {
				device.setTypeName(StringUtils.capitalize(device.getTypeName().replace("_", " ")));
				homewizardDevices.add(mapDeviceToHomeWizardSmartPlugDevice(device));
			}
		}
		catch(JSONException e) {
			log.warn("Error while get devices from cloud service ", e);
		}
		
		log.info("Found: " + homewizardDevices.size() + " devices");
		return homewizardDevices;
	}

	public void execApply(String jsonToPost) throws JSONException, IOException {
			
		// Extract 
		JSONObject resultJson = new JSONObject(jsonToPost);
		String deviceId = resultJson.getString("deviceid");
		String action = resultJson.getString("action");
		
		// Check if we have an plug id stored
		if (StringUtils.isBlank(cloudPlugId)) {
			getDevices();
		}
		
		// Send request to HomeWizard cloud
		if (!sendAction("/" + cloudPlugId + "/devices/" + deviceId + "/action", action))
		{
			throw new IOException("Send action to HomeWizard Cloud failed.");
		}
	}

	protected HomeWizardSmartPlugDevice mapDeviceToHomeWizardSmartPlugDevice(Device device) {
		HomeWizardSmartPlugDevice homewizardDevice = new HomeWizardSmartPlugDevice();
		homewizardDevice.setId(device.getId());
		homewizardDevice.setGateway(cloudPlugName);
		homewizardDevice.setName(device.getName());
		homewizardDevice.setTypeName(device.getTypeName());

		return homewizardDevice;
	}
}
