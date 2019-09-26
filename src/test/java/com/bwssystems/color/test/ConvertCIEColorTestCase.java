package com.bwssystems.color.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.HueSatBri;
import com.bwssystems.HABridge.hue.XYColorSpace;
import com.bwssystems.HABridge.hue.ColorConverter;

public class ConvertCIEColorTestCase {

	@Test
	public void testColorConverterXYtoRGB() {
		ArrayList<Double> xy = new ArrayList<Double>(Arrays.asList(Double.parseDouble("0.671254"), Double.parseDouble("0.303273")));
		
		XYColorSpace xyColor = new XYColorSpace();
		xyColor.setBrightness(50);
		float[] xyFloat = new float[2];
		xyFloat[0] = xy.get(0).floatValue();
		xyFloat[1] = xy.get(1).floatValue();
		xyColor.setXy(xyFloat);
		float[] xyz = ColorConverter.XYtoXYZ(xyColor);
		int[] rgb = ColorConverter.normalizeRGB(ColorConverter.XYZtoRGB(xyz[0], xyz[1], xyz[2]));
		List<Integer> rgbDecode = new ArrayList<Integer>();
		rgbDecode.add(0, rgb[0]);
		rgbDecode.add(1, rgb[1]);
		rgbDecode.add(2, rgb[2]);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 0);
		assertDecode.add(2, 5);
		Assert.assertEquals(rgbDecode, assertDecode);
	}

	@Test
	public void testColorConverterHSLtoRGB() {
		int[] rgb = ColorConverter.normalizeRGB(ColorConverter.HSLtoRGB(0.0f, 1.0f, 0.50000f));
		List<Integer> rgbDecode = new ArrayList<Integer>();
		rgbDecode.add(0, rgb[0]);
		rgbDecode.add(1, rgb[1]);
		rgbDecode.add(2, rgb[2]);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 0);
		assertDecode.add(2, 0);
		Assert.assertEquals(rgbDecode, assertDecode);
	}

	@Test
	public void testColorConversionXYtoRGB() {
		ArrayList<Double> xy = new ArrayList<Double>(Arrays.asList(Double.parseDouble("0.671254"), Double.parseDouble("0.303273")));
		
		List<Integer> colorDecode = ColorDecode.convertCIEtoRGB(xy, 50);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 0);
		assertDecode.add(2, 5);
		Assert.assertEquals(colorDecode, assertDecode);
		
		ColorData colorData = new ColorData(ColorData.ColorMode.XY, xy);
		int rgbIntVal = ColorDecode.getIntRGB(colorData, 50);
		Assert.assertEquals(rgbIntVal, 16711685);
	}

	@Test
	public void testColorConversionHSLtoRGB() {
		HueSatBri hsb = new HueSatBri();
		hsb.setHue(0);
		hsb.setSat(254);
		hsb.setBri((int)Math.round(0.50000 * 254));
		
		List<Integer> colorDecode = ColorDecode.convertHSLtoRGB(hsb);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 0);
		assertDecode.add(2, 0);
		Assert.assertEquals(colorDecode, assertDecode);
	}

	@Test
	public void testColorConversionCTtoRGB() {
		Integer ct = 500;

		List<Integer> colorDecode = ColorDecode.convertCTtoRGB(ct);
		List<Integer> assertDecode = new ArrayList<Integer>();
		assertDecode.add(0, 255);
		assertDecode.add(1, 214);
		assertDecode.add(2, 73);
		Assert.assertEquals(colorDecode, assertDecode);
	}

}
