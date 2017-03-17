package com.bwssystems.HABridge.hue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeDecode {
	private static final Logger log = LoggerFactory.getLogger(TimeDecode.class);
	private static final String TIME_FORMAT = "${time.format(";
	private static final String TIME_FORMAT_CLOSE = ")}";

	/*
	 * light weight templating here, was going to use free marker but it was a
	 * bit too heavy for what we were trying to do.
	 *
	 * currently provides: time format using Java DateTimeFormatter options
	 */
	public static String replaceTimeValue(String request) {
		if (request == null) {
			return null;
		}
		boolean notDone = true;
		
		while(notDone) {
			notDone = false;
			if (request.contains(TIME_FORMAT)) {
				String timeFormatDescriptor = request.substring(request.indexOf(TIME_FORMAT) + TIME_FORMAT.length(),
						request.indexOf(TIME_FORMAT_CLOSE));
	
				try {
					log.debug("Time eval is: " + timeFormatDescriptor);
				    SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormatDescriptor);
					request = request.replace(TIME_FORMAT + timeFormatDescriptor + TIME_FORMAT_CLOSE, dateFormat.format(new Date()));
					notDone = true;
				} catch (Exception e) {
					log.warn("Could not format current time: " + timeFormatDescriptor, e);
				}
			}
		}
		return request;
	}
}
