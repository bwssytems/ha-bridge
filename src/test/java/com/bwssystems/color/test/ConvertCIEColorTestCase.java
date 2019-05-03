package com.bwssystems.color.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;

public class ConvertCIEColorTestCase {

	@Test
	public void testColorConversion() {
		ArrayList<Double> xy = new ArrayList<Double>(Arrays.asList(Double.parseDouble("0.671254"), Double.parseDouble("0.303273")));
		
		List<Integer> colorDecode = ColorDecode.convertCIEtoRGB(xy, 254);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 47);
		assertDecode.add(2, 43);
		Assert.assertEquals(colorDecode, assertDecode);
		
		ColorData colorData = new ColorData(ColorData.ColorMode.XY, xy);
		int rgbIntVal = ColorDecode.getIntRGB(colorData, 254);
		Assert.assertEquals(rgbIntVal, 16723755);
	}

}
