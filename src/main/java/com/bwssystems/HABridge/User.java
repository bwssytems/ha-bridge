package com.bwssystems.HABridge;

import spark.utils.StringUtils;

public class User {
	private int id;
	
	private String username;
	
	private String password;
	
	private String password2;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}
	
	public String validate() {
		String error = null;
		
		if(StringUtils.isEmpty(username)) {
			error = "You have to enter a username";
		} else if(StringUtils.isEmpty(password)) {
			error = "You have to enter a password";
		} else if(!password.equals(password2)) {
			error = "The two passwords do not match";
		}
		
		return error;
	}

	public boolean validatePassword() {
		if(password != null && password2 != null)
			return password.equals(password2);
		return false;
	}
}