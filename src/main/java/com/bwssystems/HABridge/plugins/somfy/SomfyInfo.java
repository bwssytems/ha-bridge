package com.bwssystems.HABridge.plugins.somfy;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup.Device;
import com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup.GetSetup;
import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class SomfyInfo {
    private static final Logger log = LoggerFactory.getLogger(SomfyInfo.class);
	private final String somfyName;
	private final NamedIP namedIP;
	private HTTPHandler httpClient;
	private static final String CONNECT_HOST = "https://www.tahomalink.com/";
	private static final String BASE_URL = CONNECT_HOST + "enduser-mobile-web/externalAPI/";
	private static final String BASE_URL_ENDUSER = CONNECT_HOST + "enduser-mobile-web/enduserAPI/";

	public SomfyInfo(NamedIP namedIP, String somfyName) {
		super();
		this.somfyName = somfyName;
		this.namedIP = namedIP;
	}

	private void initHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		if(httpClient==null) {
			httpClient = new HTTPHandler();
		}
	}

	

	public List<SomfyDevice> getSomfyDevices() {

		List<SomfyDevice> somfyDevices = new ArrayList<>();
		try {
			login(namedIP.getUsername(), namedIP.getPassword());
			GetSetup setupData = getSetup();
			for(Device device : setupData.getSetup().getDevices()) {
				somfyDevices.add(mapDeviceToSomfyDevice(device));
			}
		} catch (Exception e) {
			log.error("Could not get Somfy devices", e);
		}
		return somfyDevices;
	}


	public void login(String username, String password) throws Exception {

		initHttpClient();
		NameValue[] httpHeader = getHttpHeaders();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("userId", username));
		nvps.add(new BasicNameValuePair("userPassword", password));
		log.debug("Making SOMFY http login call");
		UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nvps);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		urlEncodedFormEntity.writeTo(bos);
		String body = bos.toString();
		String response = httpClient.doHttpRequest(BASE_URL + "json/login",HttpPost.METHOD_NAME, "application/x-www-form-urlencoded", body,httpHeader);
		log.debug(response);
	}

	private NameValue[] getHttpHeaders() {
		NameValue userAgentHeader = new NameValue();
		userAgentHeader.setName("User-Agent");
		userAgentHeader.setValue("mine");
		return new NameValue[]{userAgentHeader};
	}

	public GetSetup getSetup() throws IOException {
		NameValue[] httpHeader = getHttpHeaders();
		log.info("Making SOMFY http setup call");
		String response = httpClient.doHttpRequest(BASE_URL + "json/getSetup", HttpGet.METHOD_NAME, "", "", httpHeader );
		log.debug(response);
		GetSetup setupData = new Gson().fromJson(response, GetSetup.class);
		return setupData;
	}

	public void execApply(String jsonToPost) throws Exception {
		login(namedIP.getUsername(), namedIP.getPassword());
		log.info("Making SOMFY http exec call");
		String response = httpClient.doHttpRequest(BASE_URL_ENDUSER + "exec/apply", HttpPost.METHOD_NAME, "application/json;charset=UTF-8", jsonToPost, getHttpHeaders());
		log.info(response);
	}


	protected SomfyDevice mapDeviceToSomfyDevice(Device device) {
		SomfyDevice somfyDevice = new SomfyDevice();
		somfyDevice.setId(device.getOid());
		somfyDevice.setCategory(device.getUiClass());
		somfyDevice.setRoom("");
		somfyDevice.setSomfyname(somfyName);
		somfyDevice.setName(device.getLabel());
		somfyDevice.setDeviceUrl(device.getDeviceURL());
		somfyDevice.setDeviceType(device.getWidget());
		return somfyDevice;
	}

}
