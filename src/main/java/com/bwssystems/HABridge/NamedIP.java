package com.bwssystems.HABridge;

import com.google.gson.JsonObject;

import org.apache.commons.codec.binary.Base64;

public class NamedIP {
	private String name;
	private String ip;
	private String webhook;
	private String port;
	private String username;
	private String password;
	private JsonObject extensions;
	private Boolean secure;
	private String httpPreamble;
	private String encodedLogin;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getWebhook() {
		return webhook;
	}

	public void setWebhook(final String webhook) {
		this.webhook = webhook;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getSecure() {
		return secure;
	}

	public void setSecure(Boolean secure) {
		this.secure = secure;
	}

	public JsonObject getExtensions() {
		return extensions;
	}

	public void setExtensions(JsonObject extensions) {
		this.extensions = extensions;
	}

	public String getHttpPreamble() {
		if (httpPreamble == null || !httpPreamble.trim().isEmpty()) {
			if (getSecure() != null && getSecure())
				httpPreamble = "https://";
			else
				httpPreamble = "http://";

			httpPreamble = httpPreamble + getIp();
			if (getPort() != null && !getPort().trim().isEmpty()) {
				httpPreamble = httpPreamble + ":" + getPort();
			}
		}
		return httpPreamble;
	}

	public void setHttpPreamble(String httpPreamble) {
		this.httpPreamble = httpPreamble;
	}

	public String getUserPass64() {
		if (encodedLogin == null || !encodedLogin.trim().isEmpty()) {
			if (getUsername() != null && !getUsername().isEmpty() && getPassword() != null
					&& !getPassword().isEmpty()) {
				String userPass = getUsername() + ":" + getPassword();
				encodedLogin = new String(Base64.encodeBase64(userPass.getBytes()));
			}
		}

		return encodedLogin;
	}
}
