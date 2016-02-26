package com.bwssystems.logservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.bwssystems.logservices.LoggingUtil;
import com.bwssystems.logservices.LoggingUtil.LogLevels;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Show log files and allow to set log levels for the configured loggers (logback)
 * to be changed dynamically at runtime.
 */

public class LoggingManager {

	/** Show all loggers or only the configured loggers */
    private boolean showAll = false;
    
    /** List of log files and associated information */
    private List<LogFileInfo> logFileInfos = new ArrayList<LogFileInfo>();
    
    /** List of Loggers, simplified for display purposes */
    private List<LoggerInfo> configuredLoggers =  new ArrayList<LoggerInfo>();
    
    /** Used to stream a logfile back to the client */
    private transient InputStream fileToDownLoad;
    
    private LoggingForm model = new LoggingForm();
    
    //~~~~~Reference Data~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Defines a collection of log levels. Unfortunately logback does not use 
     *  enums to define its log levels. */
    private Set<LogLevels> logLevels = EnumSet.allOf(LogLevels.class); 
    
    //~~~~~Prepare data~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
	public void init() {
		loadLoggers();
		loadLogFiles();
	}
	
	//~~~~~Helper Methods~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private void loadLoggers() {
		this.configuredLoggers.clear();
		
    	final List<Logger> loggers = LoggingUtil.getLoggers(this.showAll);
    	
    	for (Logger logger : loggers) {
    		
    		this.configuredLoggers.add(new LoggerInfo(logger.getName(), logger.getEffectiveLevel().levelInt));
    	}
	}
	
	private void loadLogFiles() {
		this.logFileInfos = LoggingUtil.getLogFileInfos();
	}
	
	/** Updates loglevels for loggers */
    public String updateLogLevels() {

        if (this.model.getUpdatedLoggers() != null && !this.model.getUpdatedLoggers().isEmpty()) {
        	
        	for (LoggerInfo loggerInfo : this.model.getUpdatedLoggers()) {
        		
        		if (loggerInfo != null && loggerInfo.getNewLogLevel() != null) {
        			
        			LoggingUtil.getLogger(loggerInfo.getLoggerName())
        			           .setLevel(Level.toLevel(loggerInfo.getNewLogLevel().getLogLevel()));
        			
        		}
        	}
        	
        	//Need to refresh the loggers
        	loadLoggers();
        }
        
        
        return "successRedirect";
        
    }
	
    /** Adds a new logger at runtime.  */
	public String addNewLogger() {
	
        if (this.model.getNewLogger() != null 
        		&& this.model.getNewLogger().getLoggerName() != null 
        		&& this.model.getNewLogger().getNewLogLevel() != null) {
        	final Logger newLogger = LoggingUtil.getLogger(this.model.getNewLogger().getLoggerName());
        	newLogger.setLevel(Level.toLevel(this.model.getNewLogger().getNewLogLevel().getLogLevel()));  
            
        	//Need to refresh the loggers
        	loadLoggers();
        }
		
		return "successRedirect";
	}
	
    /**
     * Retrieve the requested log file.
     * 
     * @return
     * @throws Exception
     */
    public String download() throws Exception {

        if (this.model.getFileName() == null) {
            throw new IllegalArgumentException("FileName must not be null.");
        }
        
        final File logFile = LoggingUtil.getLogFile(this.model.getFileName());
        
        if (logFile != null) {
        	this.fileToDownLoad = new FileInputStream(logFile);
        }

        return "download";
    }

    //~~~~~Getters and Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
	public List<LogFileInfo> getLogFileInfos() {
        return logFileInfos;
    }

    public InputStream getFileToDownLoad() {
        return fileToDownLoad;
    }

    public List<LoggerInfo> getConfiguredLoggers() {
		return configuredLoggers;
	}

	public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }
    
	public Set<LogLevels> getLogLevels() {
		return logLevels;
	}

	public LoggingForm getModel() {
		return model;
	}

	public void setModel(LoggingForm model) {
		this.model = model;
	}

}
