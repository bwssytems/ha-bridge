package com.bwssystems.HABridge;

import spark.Request;

public abstract class AuthFramework {
	private static final String USER_SESSION_ID = "user";

	public AuthFramework() {
		// TODO Auto-generated constructor stub
	}

	public void addAuthenticatedUser(Request request, User u) {
		request.session().attribute(USER_SESSION_ID, u);
		
	}

	public void removeAuthenticatedUser(Request request) {
		request.session().removeAttribute(USER_SESSION_ID);
		
	}

	public User getAuthenticatedUser(Request request) {
		return request.session().attribute(USER_SESSION_ID);
	}
}
