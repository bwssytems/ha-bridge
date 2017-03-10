package com.bwssystems.HABridge;

public class NamedIP {
	private String name;
        private String ip;
        private String webhook;
	private String port;
	private String username;
	private String password;
	private Boolean secure;
	
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
}
