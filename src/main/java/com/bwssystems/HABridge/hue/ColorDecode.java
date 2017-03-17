package com.bwssystems.HABridge.hue;

import java.util.List;

public class ColorDecode {

	public static String convertCIEtoRGB(List<Double> xy) {
		double x;
		double y;
		double Y;
		
		x = xy.get(0) * 100;
		y = xy.get(1) * 100;
		Y= y;
		double R = 3.240479*((x*Y)/y) + -1.537150*Y + -0.498535*(((1-x-y)*Y)/y);
		double G = -0.969256*((x*Y)/y) + 1.875992*Y + 0.041556*(((1-x-y)*Y)/y);
		double B = 0.055648*((x*Y)/y) + -0.204043*Y + 1.057311*(((1-x-y)*Y)/y);
		
		return null;
	}
}
