package com.bwssystems.HABridge.plugins.http;

import java.util.ArrayList;
import java.util.List;

import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;

public class HttpTestHandler extends HTTPHandler {
	private List<NameValue> theData;

	public void setTheData(String compareValue, String testData) {
		if( this.theData == null )
			this.theData = new ArrayList<NameValue>();
		NameValue aValueSet = new NameValue();
		aValueSet.setName(compareValue);
		aValueSet.setValue(testData);
		this.theData.add(aValueSet);
	}

	public void updateTheData(String compareValue, String testData) {
		if( this.theData == null ) {
			this.theData = new ArrayList<NameValue>();
			NameValue aValueSet = new NameValue();
			aValueSet.setName(compareValue);
			aValueSet.setValue(testData);
			this.theData.add(aValueSet);
		}
		else {
			for(NameValue aTest:this.theData) {
				if(aTest.getName().equals(compareValue));
					aTest.setValue(testData);
			}
		}
	}

	public String doHttpRequest(String url, String httpVerb, String contentType, String body, NameValue[] headers) {
		String responseData = null;
		for(NameValue aTest:theData) {
			if(url.contains(aTest.getName()))
				responseData = aTest.getValue();
			else if(aTest.getName() == null || aTest.getName().isEmpty())
				responseData = aTest.getValue();
			
			if(responseData != null)
				break;
		}
		return responseData;
	}
}
