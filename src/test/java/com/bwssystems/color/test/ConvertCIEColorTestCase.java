package com.bwssystems.color.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.XYColorSpace;
import com.bwssystems.HABridge.hue.ColorConverter;

public class ConvertCIEColorTestCase {

	@Test
	public void testColorConversion() {
		ArrayList<Double> xy = new ArrayList<Double>(Arrays.asList(Double.parseDouble("0.671254"), Double.parseDouble("0.303273")));
		
		XYColorSpace xyColor = new XYColorSpace();
		xyColor.setBrightness(254);
		float[] xyFloat = new float[2];
		xyFloat[0] = xy.get(0).floatValue();
		xyFloat[1] = xy.get(1).floatValue();
		xyColor.setXy(xyFloat);
		float[] xyz = ColorConverter.XYtoXYZ(xyColor);
		int[] rgb = ColorConverter.XYZtoRGB(xyz[0], xyz[1], xyz[2]);
		List<Integer> rgbDecode = new ArrayList<Integer>();
		rgbDecode.add(0, rgb[0]);
		rgbDecode.add(1, rgb[1]);
		rgbDecode.add(2, rgb[2]);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 47);
		assertDecode.add(2, 43);
		Assert.assertEquals(rgbDecode, assertDecode);
	}

	@Test
	public void testColorConversionRGB() {
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
