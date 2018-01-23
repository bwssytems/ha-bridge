package com.bwssystems.HABridge.plugins.http;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;

public class HttpTestHandler extends HTTPHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpTestHandler.class);
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
		log.info("doHttpRequest with url <<<" + url + ">>>, verb: " + httpVerb + ", contentType: " + contentType + ", body <<<" + body + ">>>" );
		if(headers != null && headers.length > 0)
			for(int i = 0; i < headers.length; i++)
				log.info("header index " + i + " name: <<<" + headers[i].getName() + ">>>, value: <<<" + headers[i].getValue() + ">>>");
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
