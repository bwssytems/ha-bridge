package com.bwssystems.logservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Form for the Logging Action.
 */
public class LoggingForm implements Serializable {

    /** serialVersionUID. */
	private static final long serialVersionUID = 5970927715241338665L;

	/** List of Loggers, simplified for display purposes */
    private List<LoggerInfo> updatedLoggers = new ArrayList<LoggerInfo>();

    /** The user can enter a new logger to be configured */
    private LoggerInfo newLogger;
    
    /** Used for requesting a logfile download */
    private String fileName;
    
    //~~~~~Constructors~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public LoggingForm() {
		super();
	}

    
	//~~~~~Getters and Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	
	public List<LoggerInfo> getUpdatedLoggers() {
		return updatedLoggers;
	}

	public void setUpdatedLoggers(List<LoggerInfo> updatedLoggers) {
		this.updatedLoggers = updatedLoggers;
	}

	public LoggerInfo getNewLogger() {
		return newLogger;
	}

	public void setNewLogger(LoggerInfo newLogger) {
		this.newLogger = newLogger;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
