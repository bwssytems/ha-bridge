package com.bwssystems.HABridge.api.hue;

import java.util.ArrayList;

public class HueErrorResponse {
	private ArrayList<HueError> theErrors;

	public static HueErrorResponse createResponse(String type, String address, String description, String method_name, String resource_name, String value) {
		HueErrorResponse theErrorResp = new HueErrorResponse();
		theErrorResp.addError(new HueError(new HueErrorDetails(type, address, description, method_name, resource_name, value)));
		return theErrorResp;
	}
	public HueErrorResponse() {
		super();
		theErrors = new ArrayList<HueError>();
	}

	public void addError(HueError anError) {
		theErrors.add(anError);
	}
	
	public HueError[] getTheErrors() {
		HueError theList[] = new HueError[theErrors.size()];
		theList = theErrors.toArray(theList);
		return theList;
	}

	public void setTheErrors(ArrayList<HueError> theErrors) {
		this.theErrors = theErrors;
	}

}