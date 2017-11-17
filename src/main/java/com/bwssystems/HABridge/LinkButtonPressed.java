package com.bwssystems.HABridge;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkButtonPressed extends TimerTask {
    private static final Logger log = LoggerFactory.getLogger(LinkButtonPressed.class);
	private BridgeControlDescriptor linkDescriptor;
	private Timer myTimer;
	private boolean isSilent;

	public LinkButtonPressed(BridgeControlDescriptor theDescriptor, Timer aTimer, boolean keepSilent) {
		linkDescriptor = theDescriptor;
		myTimer = aTimer;
		isSilent = keepSilent;
	}

	@Override
	public void run() {
		if(!isSilent)
			log.info("Link button time ended....");
		linkDescriptor.setLinkButton(false);
		myTimer.cancel();
	}

}
