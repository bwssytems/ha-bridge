package com.bwssystems.HABridge.plugins.somfy;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup.Device;
import com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup.GetSetup;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;
import us.monoid.json.JSONException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;


public class SomfyInfo {
    private static final Logger log = LoggerFactory.getLogger(SomfyInfo.class);
	private final String somfyName;
	private final NamedIP namedIP;
	private HttpClient httpClient;
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
			httpClient = HttpClients.createDefault();
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
		HttpPost httpPost = new HttpPost(BASE_URL + "json/login");
		httpPost.addHeader("User-Agent","mine");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("userId", username));
		nvps.add(new BasicNameValuePair("userPassword", password));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		log.debug("Making SOMFY http login call");
		HttpResponse response = httpClient.execute(httpPost);

		log.debug(response.getStatusLine().toString());
		HttpEntity entity = response.getEntity();
		EntityUtils.consume(entity);
	}

	public GetSetup getSetup() throws IOException, JSONException {
		HttpGet httpGet = new HttpGet(BASE_URL + "json/getSetup");
		httpGet.addHeader("User-Agent","mine");
		log.info("Making SOMFY http setup call");

		HttpResponse response = httpClient.execute(httpGet);
		log.debug(response.getStatusLine().toString());
		HttpEntity entity = response.getEntity();

		String json = IOUtils.toString(entity.getContent());
		log.debug(json);
		GetSetup setupData = new Gson().fromJson(json, GetSetup.class);
		return setupData;
	}

	public void execApply(String jsonToPost) throws Exception {
		login("house@edonica.com", "drawde123");
		HttpPost httpPost = new HttpPost(BASE_URL_ENDUSER + "exec/apply");

		RequestConfig config = RequestConfig.custom().build();
		httpPost.setConfig(config);
		httpPost.addHeader("User-Agent", "mine");
		httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

		httpPost.setEntity(new StringEntity(jsonToPost, "UTF-8"));
		log.info("Making SOMFY http exec call");
		HttpResponse response = httpClient.execute(httpPost);
		
		log.info(response.getStatusLine().toString());
		HttpEntity entity = response.getEntity();
		EntityUtils.consume(entity);
		
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
