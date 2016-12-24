package com.bwssystems.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.api.NameValue;

public class HTTPHandler {
	private static final Logger log = LoggerFactory.getLogger(HTTPHandler.class);
	private HttpClient httpClient;
	private CloseableHttpClient httpclientSSL;
	private SSLContext sslcontext;
	private SSLConnectionSocketFactory sslsf;
	private RequestConfig globalConfig;
	
	
	public HTTPHandler() {
		httpClient = HttpClients.createDefault();
		// Trust own CA and all self-signed certs
		sslcontext = SSLContexts.createDefault();
		// Allow TLSv1 protocol only
		sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		httpclientSSL = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(globalConfig).build();
	}


	// This function executes the url from the device repository against the
	// target as http or https as defined
	public String doHttpRequest(String url, String httpVerb, String contentType, String body, NameValue[] headers) {
		HttpUriRequest request = null;
		String theContent = null;
		URI theURI = null;
		ContentType parsedContentType = null;
		StringEntity requestBody = null;
		if (contentType != null && contentType.length() > 0) {
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
			if (HttpGet.METHOD_NAME.equalsIgnoreCase(httpVerb) || httpVerb == null) {
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
			}
		} catch (IllegalArgumentException e) {
			log.warn("Error creating outbound http request: IllegalArgumentException in log", e);
			return null;
		}
		log.debug("Making outbound call in doHttpRequest: " + request);
		if (headers != null && headers.length > 0) {
			for (int i = 0; i < headers.length; i++) {
				request.setHeader(headers[i].getName(), headers[i].getValue());
			}
		}
		try {
			HttpResponse response;
			if (url.startsWith("https"))
				response = httpclientSSL.execute(request);
			else
				response = httpClient.execute(request);
			log.debug((httpVerb == null ? "GET" : httpVerb) + " execute on URL responded: "
					+ response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
				if (response.getEntity() != null) {
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
						log.debug(
								"Error ocurred in handling response entity after successful call, still responding success. "
										+ e.getMessage(),
								e);
					}
				}
				if (theContent == null)
					theContent = "";
			}
		} catch (IOException e) {
			log.warn("Error calling out to HA gateway: IOException in log", e);
		}
		return theContent;
	}
	
	public void closeHandler() {
		httpClient = null;
		try {
			httpclientSSL.close();
		} catch (IOException e) {
			// noop
		}
		httpclientSSL = null;
	}
}
