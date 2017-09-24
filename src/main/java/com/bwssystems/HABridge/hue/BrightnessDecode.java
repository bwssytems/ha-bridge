package com.bwssystems.HABridge.hue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.Conversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.java.dev.eval.Expression;

public class BrightnessDecode {
	private static final Logger log = LoggerFactory.getLogger(BrightnessDecode.class);
	private static final String INTENSITY_PERCENT = "${intensity.percent}";
	private static final String INTENSITY_DECIMAL_PERCENT = "${intensity.decimal_percent}";
	private static final String INTENSITY_BYTE = "${intensity.byte}";
	private static final String INTENSITY_MATH = "${intensity.math(";
	private static final String INTENSITY_MATH_VALUE = "X";
	private static final String INTENSITY_MATH_CLOSE = ")}";
	private static final String INTENSITY_MATH_CLOSE_HEX = ").hex}";
	private static final String INTENSITY_PERCENT_HEX = "${intensity.percent.hex}";
	private static final String INTENSITY_BYTE_HEX = "${intensity.byte.hex}";

	public static int calculateIntensity(int setIntensity, Integer targetBri, Integer targetBriInc) {
		if (targetBri != null) {
			setIntensity = targetBri;
		} else if (targetBriInc != null) {
			if ((setIntensity + targetBriInc) <= 0)
				setIntensity = targetBriInc;
			else if ((setIntensity + targetBriInc) > 254)
				setIntensity = targetBriInc;
			else
				setIntensity = setIntensity + targetBriInc;
		}
		return setIntensity;
	}

	/*
	 * light weight templating here, was going to use free marker but it was a
	 * bit too heavy for what we were trying to do.
	 *
	 * currently provides: intensity.byte : 0-254 brightness. this is raw from
	 * the echo intensity.percent : 0-100, adjusted for the vera
	 * intensity.math(X*1) : where X is the value from the interface call and
	 * can use net.java.dev.eval math
	 */
	public static String replaceIntensityValue(String request, int intensity, boolean isHex) {
		if (request == null) {
			return null;
		}
		boolean notDone = true;
		String replaceValue = null;
		String replaceTarget = null;
		int percentBrightness = (int) Math.round(intensity / 255.0 * 100);
		float decimalBrightness = (float) (intensity / 255.0);
		Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
		String mathDescriptor = null;
		
		while(notDone) {
			notDone = false;
			if (request.contains(INTENSITY_BYTE)) {
				if (isHex) {
					replaceValue = convertToHex(intensity);
				} else {
					replaceValue = String.valueOf(intensity);
				}
				replaceTarget = INTENSITY_BYTE;
				notDone = true;
			} else if (request.contains(INTENSITY_BYTE_HEX)) {
				replaceValue = convertToHex(intensity);
				replaceTarget = INTENSITY_BYTE_HEX;
				notDone = true;
			} else if (request.contains(INTENSITY_PERCENT)) {
				if (isHex) {
					replaceValue = convertToHex(percentBrightness);
				} else {
					replaceValue = String.valueOf(percentBrightness);
				}
				replaceTarget = INTENSITY_PERCENT;
				notDone = true;
			} else if (request.contains(INTENSITY_PERCENT_HEX)) {
				replaceValue = convertToHex(percentBrightness);
				replaceTarget = INTENSITY_PERCENT_HEX;
				notDone = true;
			} else if (request.contains(INTENSITY_DECIMAL_PERCENT)) {
				replaceValue = String.format(Locale.ROOT, "%1.2f", decimalBrightness);
				replaceTarget = INTENSITY_DECIMAL_PERCENT;
				notDone = true;
			} else if (request.contains(INTENSITY_MATH_CLOSE)) {
				mathDescriptor = request.substring(request.indexOf(INTENSITY_MATH) + INTENSITY_MATH.length(),
						request.indexOf(INTENSITY_MATH_CLOSE));
				variables.put(INTENSITY_MATH_VALUE, new BigDecimal(intensity));
	
				log.debug("Math eval is: " + mathDescriptor + ", Where " + INTENSITY_MATH_VALUE + " is: "
						+ String.valueOf(intensity));
				Integer endResult = calculateMath(variables, mathDescriptor);
				if(endResult != null) {
					if (isHex) {
						replaceValue = convertToHex(endResult);
					} else {
						replaceValue = endResult.toString();
					}
					replaceTarget = INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE;
					notDone = true;
				}
			} else if (request.contains(INTENSITY_MATH_CLOSE_HEX)) {
				mathDescriptor = request.substring(request.indexOf(INTENSITY_MATH) + INTENSITY_MATH.length(),
						request.indexOf(INTENSITY_MATH_CLOSE_HEX));
				variables.put(INTENSITY_MATH_VALUE, new BigDecimal(intensity));
	
				Integer endResult = calculateMath(variables, mathDescriptor);
				if(endResult != null) {
					if (isHex) {
						replaceValue = convertToHex(endResult);
					} else {
						replaceValue = endResult.toString();
					}
					replaceTarget = INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE_HEX;
					notDone = true;
				}
			}
			if(notDone)
				request = request.replace(replaceTarget, replaceValue);
		}
		return request;
	}

	// Helper Method
	public static String calculateReplaceIntensityValue(String request, int theIntensity, Integer targetBri, Integer targetBriInc, boolean isHex) {
		return replaceIntensityValue(request, calculateIntensity(theIntensity, targetBri, targetBriInc), isHex);
	}
	
	// Apache Commons Conversion utils likes little endian too much
	private static String convertToHex(int theValue) {
		String destHex = "00";
		String hexValue = Conversion.intToHex(theValue, 0, destHex, 0, 2);
		byte[] theBytes = hexValue.getBytes();
		byte[] newBytes = new byte[2];
		newBytes[0] = theBytes[1];
		newBytes[1] = theBytes[0];
		return new String(newBytes);
	}
	
	private static Integer calculateMath(Map<String, BigDecimal> variables, String mathDescriptor) {
		Integer endResult = null;
		try {
			Expression exp = new Expression(mathDescriptor);
			BigDecimal result = exp.eval(variables);
			endResult = Math.round(result.floatValue());
		} catch (Exception e) {
			log.warn("Could not execute Math: " + mathDescriptor, e);
			endResult = null;
		}
		return endResult;
	}
}