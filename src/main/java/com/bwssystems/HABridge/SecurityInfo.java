package com.bwssystems.HABridge;

public class SecurityInfo {
	private boolean useLinkButton;
	private String execGarden;
	private boolean seucreHueApi;
	private boolean isSecure;
	
	public boolean isUseLinkButton() {
		return useLinkButton;
	}
	public void setUseLinkButton(boolean useLinkButton) {
		this.useLinkButton = useLinkButton;
	}
	public String getExecGarden() {
		return execGarden;
	}
	public void setExecGarden(String execGarden) {
		this.execGarden = execGarden;
	}
	public boolean isSeucreHueApi() {
		return seucreHueApi;
	}
	public void setSeucreHueApi(boolean seucreHueApi) {
		this.seucreHueApi = seucreHueApi;
	}
	public boolean isSecure() {
		return isSecure;
	}
	public void setSecure(boolean isSecure) {
		this.isSecure = isSecure;
	}
}
