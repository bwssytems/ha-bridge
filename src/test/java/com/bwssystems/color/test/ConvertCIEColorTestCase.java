package com.bwssystems.color.test;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.bwssystems.HABridge.hue.ColorDecode;

public class ConvertCIEColorTestCase {

	@Test
	public void testColorConversion() {
		ArrayList<Double> xy = new ArrayList<Double>(Arrays.asList(new Double(0.3972), new Double(0.4564)));
		
		String colorDecode = ColorDecode.convertCIEtoRGB(xy);
		Assert.assertEquals(colorDecode, null);
	}

}
