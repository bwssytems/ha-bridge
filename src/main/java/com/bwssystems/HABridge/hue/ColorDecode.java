package com.bwssystems.HABridge.hue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.hue.ColorData;

public class ColorDecode {
	private static final Logger log = LoggerFactory.getLogger(ColorDecode.class);
	private static final String COLOR_R = "${color.r}";
	private static final String COLOR_G = "${color.g}";
	private static final String COLOR_B = "${color.b}";
	private static final String COLOR_RX = "${color.rx}";
	private static final String COLOR_GX = "${color.gx}";
	private static final String COLOR_BX = "${color.bx}";
	private static final String COLOR_RGBX = "${color.rgbx}";
	private static final String COLOR_HSL = "${color.hsl}";
	private static final String COLOR_H = "${color.h}";
	private static final String COLOR_S = "${color.s}";
	private static final String COLOR_L = "${color.l}";
	private static final String COLOR_XY = "${color.xy}";
	private static final String COLOR_BRI = "${colorbri}";
	private static final Pattern COLOR_MILIGHT = Pattern.compile("\\$\\{color.milight\\:([01234])\\}");

	public static List<Integer> convertHSLtoRGB(HueSatBri hsl) {
		List<Integer> rgb;
		int[] rgbInt = ColorConverter.normalizeRGB(ColorConverter.HSLtoRGB((float)((float)hsl.getHue()/65535.0f), (float)((float)hsl.getSat()/254.0f), (float)((float)hsl.getBri()/254)));
		rgb = new ArrayList<Integer>();
		rgb.add(rgbInt[0]);
		rgb.add(rgbInt[1]);
		rgb.add(rgbInt[2]);

		log.info("Color change with HSL: " + hsl + ". Resulting RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " "
				+ rgb.get(2));
		return rgb;
	}

	public static List<Integer> convertCIEtoRGB(List<Double> xy, int brightness) {
		List<Integer> rgb;
		XYColorSpace xyColor = new XYColorSpace();
		xyColor.setBrightness(brightness);
		float[] xyFloat = new float[2];
		xyFloat[0] = xy.get(0).floatValue();
		xyFloat[1] = xy.get(1).floatValue();
		xyColor.setXy(xyFloat);
		float[] xyz = ColorConverter.XYtoXYZ(xyColor);
		int[] rgbInt = ColorConverter.normalizeRGB(ColorConverter.XYZtoRGB(xyz[0], xyz[1], xyz[2]));
		rgb = new ArrayList<Integer>();
		rgb.add(rgbInt[0]);
		rgb.add(rgbInt[1]);
		rgb.add(rgbInt[2]);
		log.debug("Color change with XY: " + xy.get(0) + " " + xy.get(1) + " Resulting RGB Values: " + rgb.get(0) + " " + rgb.get(1)
				+ " " + rgb.get(2));
		return rgb;
	}

	// took that approximation from
	// http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
	public static List<Integer> convertCTtoRGB(Integer ct) {
		double temperature = 1000000.0 / (double) ct;
		temperature /= 100;
		double r, g, b;
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
		rgb.add((int) Math.round(r));
		rgb.add((int) Math.round(g));
		rgb.add((int) Math.round(b));
		log.debug("Color change with CT: " + ct + ". Resulting RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " "
				+ rgb.get(2));
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
			rgb = convertCIEtoRGB((List<Double>) colorData.getData(), setIntensity);
		} else if (colorMode == ColorData.ColorMode.CT) {
			rgb = convertCTtoRGB((Integer) colorData.getData());
		} else if (colorMode == ColorData.ColorMode.HS) {
			rgb = convertHSLtoRGB((HueSatBri) colorData.getData());
		}

		while (notDone) {
			notDone = false;
			if (request.contains(COLOR_R)) {
				request = request.replace(COLOR_R,
						isHex ? String.format("%02X", rgb.get(0)) : String.valueOf(rgb.get(0)));
				notDone = true;
			}

			if (request.contains(COLOR_G)) {
				request = request.replace(COLOR_G,
						isHex ? String.format("%02X", rgb.get(1)) : String.valueOf(rgb.get(1)));
				notDone = true;
			}

			if (request.contains(COLOR_B)) {
				request = request.replace(COLOR_B,
						isHex ? String.format("%02X", rgb.get(2)) : String.valueOf(rgb.get(2)));
				notDone = true;
			}

			if (request.contains(COLOR_RX)) {
				request = request.replace(COLOR_RX, String.format("%02X", rgb.get(0)));
				notDone = true;
			}

			if (request.contains(COLOR_GX)) {
				request = request.replace(COLOR_GX, String.format("%02X", rgb.get(1)));
				notDone = true;
			}

			if (request.contains(COLOR_BX)) {
				request = request.replace(COLOR_BX, String.format("%02X", rgb.get(2)));
				notDone = true;
			}

			if (request.contains(COLOR_RGBX)) {
				request = request.replace(COLOR_RGBX,
						String.format("%02X%02X%02X", rgb.get(0), rgb.get(1), rgb.get(2)));
				notDone = true;
			}

			if (request.contains(COLOR_XY)) {
				if (colorMode == ColorData.ColorMode.XY) {
					List<Double> xyData = (List<Double>) colorData.getData();
					request = request.replace(COLOR_XY, String.format("%f,%f", xyData.get(0), xyData.get(1)));
				} else {
					List<Double> xyData = (List<Double>) colorData.getData();
					request = request.replace(COLOR_XY, String.format("%f,%f", xyData.get(0), xyData.get(1)));
				}
				notDone = true;
			}

			if (request.contains(COLOR_H)) {
				if (colorMode == ColorData.ColorMode.HS) {
					HueSatBri hslData = (HueSatBri) colorData.getData();
					request = request.replace(COLOR_H, String.format("%d", hslData.getHue()));
				} else {
					float[] hsb = new float[3];
					Color.RGBtoHSB(rgb.get(0), rgb.get(1), rgb.get(2), hsb);
					float hue = hsb[0] * (float) 360.0;
					request = request.replace(COLOR_H, String.format("%f", hue));
				}
				notDone = true;
			}

			if (request.contains(COLOR_S)) {
				if (colorMode == ColorData.ColorMode.HS) {
					HueSatBri hslData = (HueSatBri) colorData.getData();
					request = request.replace(COLOR_S, String.format("%d", hslData.getSat()));
				} else {
					float[] hsb = new float[3];
					Color.RGBtoHSB(rgb.get(0), rgb.get(1), rgb.get(2), hsb);
					float sat = hsb[1] * (float) 100.0;
					request = request.replace(COLOR_S, String.format("%f", sat));
				}
				notDone = true;
			}

			if (request.contains(COLOR_L)) {
				if (colorMode == ColorData.ColorMode.HS) {
					HueSatBri hslData = (HueSatBri) colorData.getData();
					request = request.replace(COLOR_L, String.format("%d", hslData.getBri()));
				} else {
					float[] hsb = new float[3];
					Color.RGBtoHSB(rgb.get(0), rgb.get(1), rgb.get(2), hsb);
					float bright = hsb[2] * (float) 100.0;
					request = request.replace(COLOR_L, String.format("%f", bright));
				}
				notDone = true;
			}

			if (request.contains(COLOR_HSL)) {
				if (colorMode == ColorData.ColorMode.HS) {
					HueSatBri hslData = (HueSatBri) colorData.getData();
					request = request.replace(COLOR_HSL,
							String.format("%d,%d,%d", hslData.getHue(), hslData.getSat(), hslData.getBri()));
				} else {
					float[] hsb = new float[3];
					Color.RGBtoHSB(rgb.get(0), rgb.get(1), rgb.get(2), hsb);
					float hue = hsb[0] * (float) 360.0;
					float sat = hsb[1] * (float) 100.0;
					float bright = hsb[2] * (float) 100.0;
					request = request.replace(COLOR_HSL, String.format("%f,%f,%f", hue, sat, bright));
				}
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
		double r = (double) rgb.get(0);
		double g = (double) rgb.get(1);
		double b = (double) rgb.get(2);
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
			log.debug("Convert RGB to Milight. Result: WHITE. RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " "
					+ rgb.get(2));
			return retVal + "0055";
		} else { // normal color
			r /= (double) 0xFF;
			g /= (double) 0xFF;
			b /= (double) 0xFF;
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
				} else if (max == b) {
					h = ((r - g) / d + 4);
				}
				h = Math.round(h * 60);
			}
			int milight = (int) ((256 + 176 - Math.floor(h / 360.0 * 255.0)) % 256);
			log.debug("Convert RGB to Milight. Result: " + milight + " RGB Values: " + rgb.get(0) + " " + rgb.get(1)
					+ " " + rgb.get(2));
			return "40" + String.format("%02X", milight) + "55";
		}
	}

	@SuppressWarnings("unchecked")
	public static int getIntRGB(ColorData colorData, int setIntensity) {
		ColorData.ColorMode colorMode = colorData.getColorMode();
		List<Integer> rgb = null;
		if (colorMode == ColorData.ColorMode.XY) {
			rgb = convertCIEtoRGB((List<Double>) colorData.getData(), setIntensity);
		} else if (colorMode == ColorData.ColorMode.CT) {
			rgb = convertCTtoRGB((Integer) colorData.getData());
		}

		int rgbIntVal = Integer.parseInt(String.format("%02X%02X%02X", rgb.get(0), rgb.get(1), rgb.get(2)), 16);
		log.debug("Convert RGB to int. Result: " + rgbIntVal + " RGB Values: " + rgb.get(0) + " " + rgb.get(1) + " "
				+ rgb.get(2));
		return rgbIntVal;
	}
}
