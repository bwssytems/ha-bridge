package com.bwssystems.HABridge.hue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.hue.ColorData;

public class ColorDecode {
	private static final Logger log = LoggerFactory.getLogger(ColorDecode.class);
	private static final String COLOR_R = "${color.r}";
	private static final String COLOR_G = "${color.g}";
	private static final String COLOR_B = "${color.b}";
	private static final Pattern COLOR_MILIGHT = Pattern.compile("\\$\\{color.milight\\:([01234])\\}");

	public static List<Integer> convertCIEtoRGB(List<Double> xy, int brightness) {
		List<Integer> rgb;
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

	    rgb = new ArrayList<Integer>();
	    rgb.add((int)Math.round(r * 255));
	    rgb.add((int)Math.round(g * 255));
	    rgb.add((int)Math.round(b * 255));
	    log.debug("Color change with XY: " + x + " " + y + " Resulting RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " " + rgb.get(2));
		return rgb;
	}

	// took that approximation from http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
	public static List<Integer> convertCTtoRGB(Integer ct) {
		double temperature = 1000000.0 / (double)ct;
		temperature /= 100;
		double r,g,b;
		if (temperature <= 66) {
			r = 255;
			g = temperature;
	        g = 99.4708025861 * Math.log(g) - 161.1195681661;
		} else {
			r = temperature - 60;
			r = 329.698727446 * (Math.pow(r, -0.1332047592));
			g = temperature - 60;
			g = 288.1221695283 * (Math.pow(g, -0.0755148492));
		}

		if (temperature >= 66) {
			b = 255;
		} else {
			if (temperature <= 19) {
				b = 0;
			} else {
				b = temperature - 10;
				b = 138.5177312231 * Math.log(b) - 305.0447927307;
			}
		}
        r = assureBounds(r);
        g = assureBounds(g);
        b = assureBounds(b);
        List<Integer> rgb = new ArrayList<Integer>();
        rgb.add((int)Math.round(r));
        rgb.add((int)Math.round(g));
        rgb.add((int)Math.round(b));
        log.debug("Color change with CT: " + ct + ". Resulting RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " " + rgb.get(2));
        return rgb;
	}

	private static double assureBounds(double value) {
		if (value < 0.0) {
        	value = 0;
        }
        if (value > 255.0) {
        	value = 255;
        }
        return value;
	}

	@SuppressWarnings("unchecked")
	public static String replaceColorData(String request, ColorData colorData, int setIntensity, boolean isHex) {
		if (request == null) {
			return null;
		}
		if (colorData == null) {
			return request;
		}
		boolean notDone = true;
		ColorData.ColorMode colorMode = colorData.getColorMode();
		List<Integer> rgb = null;
		if (colorMode == ColorData.ColorMode.XY) {
			rgb = convertCIEtoRGB((List<Double>)colorData.getData(), setIntensity);
		} else if (colorMode == ColorData.ColorMode.CT) {
			rgb = convertCTtoRGB((Integer)colorData.getData());
		}
		
		while(notDone) {
			notDone = false;
			if (request.contains(COLOR_R)) {
				request = request.replace(COLOR_R, isHex ? String.format("%02X", rgb.get(0)) : String.valueOf(rgb.get(0)));
				notDone = true;
			}

			if (request.contains(COLOR_G)) {
				request = request.replace(COLOR_G, isHex ? String.format("%02X", rgb.get(1)) : String.valueOf(rgb.get(1)));
				notDone = true;
			}

			if (request.contains(COLOR_B)) {
				request = request.replace(COLOR_B, isHex ? String.format("%02X", rgb.get(2)) : String.valueOf(rgb.get(2)));
				notDone = true;
			}

			Matcher m = COLOR_MILIGHT.matcher(request);
			while (m.find()) {
			    int group = Integer.parseInt(m.group(1));
			    request = m.replaceFirst(getMilightV5FromRgb(rgb, group));
			    m.reset(request);
			}
			
			log.debug("Request <<" + request + ">>, not done: " + notDone);
		}
		return request;
	}

	private static String getMilightV5FromRgb(List<Integer> rgb, int group) {
		double r = (double)rgb.get(0);
	    double g = (double)rgb.get(1);
	    double b = (double)rgb.get(2);
	    if (r > 245 && g > 245 && b > 245) { // it's white
	    	String retVal = "";
	    	if (group == 0) {
	    		retVal += "C2";
	    	} else if (group == 1) {
	    		retVal += "C5";
	    	} else if (group == 2) {
	    		retVal += "C7";
	    	} else if (group == 3) {
	    		retVal += "C9";
	    	} else if (group == 4) {
	    		retVal += "CB";
	    	}
	    	log.debug("Convert RGB to Milight. Result: WHITE. RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " " + rgb.get(2));
	    	return retVal + "0055";
	    } else { // normal color
	    	r /= (double)0xFF;
		    g /= (double)0xFF;
		    b /= (double)0xFF;
		    double max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
		    double h = 0;
		    double d = max - min;

		    if (max == min) {
		    	h = 0;
		    } else {
		    	if (max == r) {
					h = ((g - b) / d + (g < b ? 6 : 0)); 
		    	} else if (max == g) {
		    		h = ((b - r) / d + 2); 
		    	} else if (max == b){
		    		h = ((r - g) / d + 4); 
		    	}
		    	h = Math.round(h * 60);
		    }
		    int milight = (int)((256 + 176 - Math.floor(h / 360.0 * 255.0)) % 256);
		    log.debug("Convert RGB to Milight. Result: " + milight + " RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " " + rgb.get(2));
		    return "40" + String.format("%02X", milight) + "55";
	    }
	}
}
