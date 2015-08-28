package com.bwssystems.HABridge.api.hue;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WhitelistEntry
{
	private String lastUseDate;
	private String createDate;
	private String name;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static WhitelistEntry createEntry(String devicetype) {
		WhitelistEntry anEntry = new WhitelistEntry();
		anEntry.setName(devicetype);
		anEntry.setCreateDate(getCurrentDate());
		anEntry.setLastUseDate(getCurrentDate());
		return anEntry;
	}
	
	 public static String getCurrentDate() {
		 return dateFormat.format(new Date());
	 }
	
	public String getLastUseDate() {
		return lastUseDate;
	}

	public void setLastUseDate(String lastUseDate) {
		this.lastUseDate = lastUseDate;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
