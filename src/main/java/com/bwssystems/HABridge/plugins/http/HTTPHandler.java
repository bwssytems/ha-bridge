package com.bwssystems.HABridge.plugins.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.api.NameValue;

public class HTTPHandler {
	private static final Logger log = LoggerFactory.getLogger(HTTPHandler.class);
	private String callType;
	
	public HTTPHandler() {
		super();
		callType = null;
	}


	public HTTPHandler(String type) {
		super();
		callType = type;
	}


	// This function executes the url from the device repository against the
	// target as http or https as defined
	public String doHttpRequest(String url, String httpVerb, String contentType, String body, NameValue[] headers) {
		log.debug("doHttpRequest with url: " + url + " with http command: " + httpVerb + " with body: " + body);
		HttpUriRequest request = null;
		String theContent = null;
		URI theURI = null;
		ContentType parsedContentType = null;
		StringEntity requestBody = null;

		if (contentType != null && !contentType.trim().isEmpty()) {
			parsedContentType = ContentType.parse(contentType);
			if (body != null && body.length() > 0)
				requestBody = new StringEntity(body, parsedContentType);
		}
		try {
			theURI = new URI(url);
		} catch (URISyntaxException e1) {
			log.warn("Error creating URI http request: " + url + " with message: " + e1.getMessage());
			return null;
		}

		try {
			if (httpVerb == null || httpVerb.trim().isEmpty() || HttpGet.METHOD_NAME.equalsIgnoreCase(httpVerb)) {
				request = new HttpGet(theURI);
			} else if (HttpPost.METHOD_NAME.equalsIgnoreCase(httpVerb)) {
				HttpPost postRequest = new HttpPost(theURI);
				if (requestBody != null)
					postRequest.setEntity(requestBody);
				request = postRequest;
			} else if (HttpPut.METHOD_NAME.equalsIgnoreCase(httpVerb)) {
				HttpPut putRequest = new HttpPut(theURI);
				if (requestBody != null)
					putRequest.setEntity(requestBody);
				request = putRequest;
			} else
				request = new HttpGet(theURI);

		} catch (IllegalArgumentException e) {
			log.warn("Error creating outbound http request: IllegalArgumentException in log", e);
			return null;
		}
		log.debug("Making outbound call in doHttpRequest: <<<" + request.toString() + ">>>");
		if (headers != null && headers.length > 0) {
			for (int i = 0; i < headers.length; i++) {
				request.setHeader(headers[i].getName(), headers[i].getValue());
			}
		}
		CloseableHttpResponse response = null;
		for (int retryCount = 0; retryCount < 2; retryCount++) {
			try {
				response = HttpClientPool.getClient().execute(request);
				log.debug((httpVerb == null ? "GET" : httpVerb) + " execute (" + retryCount + ") on URL responded: "
						+ response.getStatusLine().getStatusCode());
				if (response != null && response.getEntity() != null) {
					try {

						theContent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")); // read
																											// content
																											// for
																											// data

						EntityUtils.consume(response.getEntity()); // close out
																	// inputstream
																	// ignore
																	// content
					} catch (Exception e) {
						log.debug("Error ocurred in handling response entity after successful call, still responding success. "
										+ e.getMessage(), e);
					}
				}
				if (response != null && response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
					if(theContent == null)
						theContent = "";
					log.debug("Successfull response - The http response is <<<" + theContent + ">>>");
					retryCount = 2;
				} else if (callType != null && callType == DeviceMapTypes.FHEM_DEVICE[DeviceMapTypes.typeIndex] && response.getStatusLine().getStatusCode() == 302) {
					if(theContent == null)
						theContent = "";
					log.debug("Successfull response - The http response is <<<" + theContent + ">>>");
					retryCount = 2;
				} else if (response != null) {
					log.warn("HTTP response code was not an expected successful response of between 200 - 299, the code was: "
									+ response.getStatusLine() + " with the content of <<<" + theContent + ">>>");
					if (response.getStatusLine().getStatusCode() == 504) {
						log.warn("HTTP response code was 504, retrying...");
						log.debug("The 504 error content is <<<" + theContent + ">>>");
						theContent = null;
					} else
						retryCount = 2;
				}
				
			} catch (ClientProtocolException e) {
				log.warn("Client Protocol Exception received, retyring....");
			}catch (IOException e) {
				log.warn("Error calling out to HA gateway: IOException in log: " + e.getMessage());
				retryCount = 2;
			}

			if(retryCount < 2) {
				theContent = null;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// noop
				}
			}
		}
		return theContent;
	}
	public void setCallType(String callType) {
		this.callType = callType;
	}


	public void closeHandler() {
	}
}
