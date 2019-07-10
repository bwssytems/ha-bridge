package com.bwssystems.HABridge;

public class SecurityInfo {
	private boolean useLinkButton;
	private boolean secureHueApi;
	private boolean isSecure;
	private String execGarden;
	private boolean useHttps;
	private String keyfilePath;
	private String keyfilePassword;
	
	public boolean isUseLinkButton() {
		return useLinkButton;
	}
	public void setUseLinkButton(boolean useLinkButton) {
		this.useLinkButton = useLinkButton;
	}
	public boolean isSecureHueApi() {
		return secureHueApi;
	}
	public void setSecureHueApi(boolean secureHueApi) {
		this.secureHueApi = secureHueApi;
	}
	public boolean isSecure() {
		return isSecure;
	}
	public void setSecure(boolean isSecure) {
		this.isSecure = isSecure;
	}

	public boolean isUseHttps() {
		return useHttps;
	}

	public void setUseHttps(boolean useHttps) {
		this.useHttps = useHttps;
	}

	public String getKeyfilePath() {
		return keyfilePath;
	}

	public void setKeyfilePath(String keyfilePath) {
		this.keyfilePath = keyfilePath;
	}

	public String getExecGarden() {
		return execGarden;
	}

	public void setExecGarden(String execGarden) {
		this.execGarden = execGarden;
	}

	public String getKeyfilePassword() {
		return keyfilePassword;
	}

	public void setKeyfilePassword(String keyfilePassword) {
		this.keyfilePassword = keyfilePassword;
	}
}
