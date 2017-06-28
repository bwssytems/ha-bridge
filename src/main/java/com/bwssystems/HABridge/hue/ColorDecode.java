package com.bwssystems.HABridge.hue;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorDecode {
	private static final Logger log = LoggerFactory.getLogger(ColorDecode.class);
	private static final String COLOR_R = "${color.r}";
	private static final String COLOR_G = "${color.g}";
	private static final String COLOR_B = "${color.b}";

	public static List<Double> convertCIEtoRGB(List<Double> xy, int brightness) {
		List<Double> rgb;
		double x = xy.get(0); // the given x value
		double y = xy.get(1); // the given y value
		double z = 1.0 - x - y;
		double Y = (double)brightness/(double)254.00; // The given brightness value
		double X = (Y / y) * x;
		double Z = (Y / y) * z;

		double r =  X * 1.656492 - Y * 0.354851 - Z * 0.255038;
		double g = -X * 0.707196 + Y * 1.655397 + Z * 0.036152;
		double b =  X * 0.051713 - Y * 0.121364 + Z * 1.011530;

		if (r > b && r > g && r > 1.0) {

			g = g / r;
			b = b / r;
			r = 1.0;
		}
		else if (g > b && g > r && g > 1.0) {

			r = r / g;
			b = b / g;
			g = 1.0;
		}
		else if (b > r && b > g && b > 1.0) {

			r = r / b;
			g = g / b;
			b = 1.0;
		}


		r = r <= 0.0031308 ? 12.92 * r : (1.0 + 0.055) * Math.pow(r, (1.0 / 2.4)) - 0.055;
		g = g <= 0.0031308 ? 12.92 * g : (1.0 + 0.055) * Math.pow(g, (1.0 / 2.4)) - 0.055;
		b = b <= 0.0031308 ? 12.92 * b : (1.0 + 0.055) * Math.pow(b, (1.0 / 2.4)) - 0.055;
		
	    if (r > b && r > g) {
	        // red is biggest
	        if (r > 1.0) {
	            g = g / r;
	            b = b / r;
	            r = 1.0;
	        }
	    }
	    else if (g > b && g > r) {
	        // green is biggest
	        if (g > 1.0) {
	            r = r / g;
	            b = b / g;
	            g = 1.0;
	        }
	    }
	    else if (b > r && b > g) {
	        // blue is biggest
	        if (b > 1.0) {
	            r = r / b;
	            g = g / b;
	            b = 1.0;
	        }
	    }
	    if(r < 0.0)
	    	r = 0;
	    if(g < 0.0)
	    	g = 0;
	    if(b < 0.0)
	    	b = 0;

	    rgb = new ArrayList<Double>();
	    rgb.add(0, r);
	    rgb.add(1, g);
	    rgb.add(2, b);
	    rgb.add(3, Math.round(r * 255));
	    rgb.add(4, Math.round(g * 255));
	    rgb.add(5, Math.round(b * 255));
		return rgb;
	}

	public static String replaceColorData(String request, List<Double> xy, int setIntensity) {
		if (request == null) {
			return null;
		}
		boolean notDone = true;
		List<Double> rgb = convertCIEtoRGB(xy, setIntensity);
		
		while(notDone) {
			notDone = false;
			if (request.contains(COLOR_R)) {
				request = request.replace(COLOR_R, String.valueOf(rgb.get(0)));
				notDone = true;
			}

			if (request.contains(COLOR_G)) {
				request = request.replace(COLOR_G, String.valueOf(rgb.get(1)));
				notDone = true;
			}

			if (request.contains(COLOR_B)) {
				request = request.replace(COLOR_B, String.valueOf(rgb.get(2)));
				notDone = true;
			}

			log.debug("Request <<" + request + ">>, not done: " + notDone);
		}
		return request;
	}
}
