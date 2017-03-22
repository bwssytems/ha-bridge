package com.bwssystems.HABridge;

public class BridgeSecurityDescriptor {
	private String uiPassword;
	private boolean passwordSet;
	private boolean useLinkButton;
	private String execGarden;
	private boolean settingsChanged;

	public BridgeSecurityDescriptor() {
		super();
		this.setUiPassword(null);
		this.setPasswordSet(false);
		this.setUseLinkButton(false);
	}

	public String getUiPassword() {
		return uiPassword;
	}

	public void setUiPassword(String uiPassword) {
		this.uiPassword = uiPassword;
	}

	public boolean isPasswordSet() {
		return passwordSet;
	}

	public void setPasswordSet(boolean passwordSet) {
		this.passwordSet = passwordSet;
	}

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

	public boolean isSettingsChanged() {
		return settingsChanged;
	}

	public void setSettingsChanged(boolean settingsChanged) {
		this.settingsChanged = settingsChanged;
	}
}
