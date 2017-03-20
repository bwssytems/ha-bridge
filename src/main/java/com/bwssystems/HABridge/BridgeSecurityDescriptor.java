package com.bwssystems.HABridge;

public class BridgeSecurityDescriptor {
	private boolean linkButton;
	private String uiPassword;

	public BridgeSecurityDescriptor() {
		super();
	}

	public boolean isLinkButton() {
		return linkButton;
	}

	public void setLinkButton(boolean linkButton) {
		this.linkButton = linkButton;
	}

	public String getUiPassword() {
		return uiPassword;
	}

	public void setUiPassword(String uiPassword) {
		this.uiPassword = uiPassword;
	}
}
