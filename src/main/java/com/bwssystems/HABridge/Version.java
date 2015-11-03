package com.bwssystems.HABridge;

import java.io.InputStream;
import java.util.Properties;


public final class Version {

    private String version;
    private String groupId;
    private String artifactId;
    private Properties prop;
    
    public Version()
    {
        InputStream resourceAsStream =
          (InputStream) this.getClass().getResourceAsStream(
            "/version.properties"
          );
        this.prop = new Properties();

        try
        {
            this.prop.load( resourceAsStream );
        	this.version = this.prop.getProperty("version");
        	this.groupId = this.prop.getProperty("groupId");
        	this.artifactId = this.prop.getProperty("artifactId");
        }
        catch (Exception e)
        {
        	this.version = "0.0.0";
        	this.groupId = "no group";
        	this.artifactId = "no artifact";
        }

    }

	public String getVersion() {
		return version;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

}