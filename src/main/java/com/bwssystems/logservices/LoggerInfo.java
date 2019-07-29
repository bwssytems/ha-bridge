package com.bwssystems.logservices;

import java.io.Serializable;

import com.bwssystems.logservices.LoggingUtil.LogLevels;

/**
 * Logger information.
 *
 *
 */
public class LoggerInfo implements Serializable {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1085935297588739585L;

	private String    loggerName;
    private LogLevels logLevel;
    private LogLevels newLogLevel;
    
    //~~~~~Constructors~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
    /**
	 * 
	 */
	public LoggerInfo() {
		super();
	}
	public LoggerInfo(String loggerName, int logLevelAsInt) {
		super();
		this.loggerName = loggerName;
		this.logLevel = LogLevels.getLogLevelFromId(logLevelAsInt);
	}
    
	//~~~~~Getters and Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public String getLoggerName() {
		return loggerName;
	}
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	public LogLevels getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(LogLevels logLevel) {
		this.logLevel = logLevel;
	}
	public LogLevels getNewLogLevel() {
		return newLogLevel;
	}
	public void setNewLogLevel(LogLevels newLogLevel) {
		this.newLogLevel = newLogLevel;
	}
	
}
