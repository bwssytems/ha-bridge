package com.bwssystems.HABridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the shutdown hook used to properly stop server from the
 * command line (sending SIGTERM), or while shutting down the host machine.
 * 
 * @author gaudryc
 */
public class ShutdownHook extends Thread {

	private final BridgeSettings bridgeSettings;
	private final SystemControl theSystem;

	/**
	 * Constructor
	 * 
	 * @param bridgeSettings
	 *            bridge settings
	 * @param theSystem 
	 */
	public ShutdownHook(final BridgeSettings bridgeSettings, final SystemControl theSystem) {
		this.bridgeSettings = bridgeSettings;
		this.theSystem = theSystem;
	}

	@Override
	public void run() {
		Logger log = LoggerFactory.getLogger(ShutdownHook.class);
		log.info("Shutdown requested...");
		if (bridgeSettings != null) {
			if (!bridgeSettings.getBridgeControl().isStop() && (theSystem != null)) {
				log.info("Forcing system stop...");
				theSystem.stop();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					log.error("Sleep error: " + e.getMessage());
				}
			} else {
				log.info("Already stopped");
			}
		}
	}

}
