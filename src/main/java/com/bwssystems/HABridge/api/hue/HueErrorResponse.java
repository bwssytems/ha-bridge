package com.bwssystems.HABridge.api.hue;

import java.util.ArrayList;

public class HueErrorResponse {
	private ArrayList<HueError> theErrors;

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