package com.bwssystems.HABridge.hue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;

import net.java.dev.eval.Expression;

public class BrightnessDecode {
	private static final Logger log = LoggerFactory.getLogger(BrightnessDecode.class);
	private static final String INTENSITY_PERCENT = "${intensity.percent}";
	private static final String INTENSITY_BYTE = "${intensity.byte}";
	private static final String INTENSITY_MATH = "${intensity.math(";
	private static final String INTENSITY_MATH_VALUE = "X";
	private static final String INTENSITY_MATH_CLOSE = ")}";

	public static int calculateIntensity(DeviceState state, StateChangeBody theChanges, boolean hasBri, boolean hasBriInc) {
		int setIntensity = state.getBri();
		if (hasBri) {
			setIntensity = theChanges.getBri();
		} else if (hasBriInc) {
			if ((setIntensity + theChanges.getBri_inc()) <= 0)
				setIntensity = theChanges.getBri_inc();
			else if ((setIntensity + theChanges.getBri_inc()) > 255)
				setIntensity = theChanges.getBri_inc();
			else
				setIntensity = setIntensity + theChanges.getBri_inc();
		}
		return setIntensity;
	}

	/*
	 * light weight templating here, was going to use free marker but it was a
	 * bit too heavy for what we were trying to do.
	 *
	 * currently provides: intensity.byte : 0-255 brightness. this is raw from
	 * the echo intensity.percent : 0-100, adjusted for the vera
	 * intensity.math(X*1) : where X is the value from the interface call and
	 * can use net.java.dev.eval math
	 */
	public static String replaceIntensityValue(String request, int intensity, boolean isHex) {
		if (request == null) {
			return null;
		}
		if (request.contains(INTENSITY_BYTE)) {
			if (isHex) {
				BigInteger bigInt = BigInteger.valueOf(intensity);
				byte[] theBytes = bigInt.toByteArray();
				String hexValue = DatatypeConverter.printHexBinary(theBytes);
				request = request.replace(INTENSITY_BYTE, hexValue);
			} else {
				String intensityByte = String.valueOf(intensity);
				request = request.replace(INTENSITY_BYTE, intensityByte);
			}
		} else if (request.contains(INTENSITY_PERCENT)) {
			int percentBrightness = (int) Math.round(intensity / 255.0 * 100);
			if (isHex) {
				BigInteger bigInt = BigInteger.valueOf(percentBrightness);
				byte[] theBytes = bigInt.toByteArray();
				String hexValue = DatatypeConverter.printHexBinary(theBytes);
				request = request.replace(INTENSITY_PERCENT, hexValue);
			} else {
				String intensityPercent = String.valueOf(percentBrightness);
				request = request.replace(INTENSITY_PERCENT, intensityPercent);
			}
		} else if (request.contains(INTENSITY_MATH)) {
			Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
			String mathDescriptor = request.substring(request.indexOf(INTENSITY_MATH) + INTENSITY_MATH.length(),
					request.indexOf(INTENSITY_MATH_CLOSE));
			variables.put(INTENSITY_MATH_VALUE, new BigDecimal(intensity));

			try {
				log.debug("Math eval is: " + mathDescriptor + ", Where " + INTENSITY_MATH_VALUE + " is: "
						+ String.valueOf(intensity));
				Expression exp = new Expression(mathDescriptor);
				BigDecimal result = exp.eval(variables);
				Integer endResult = Math.round(result.floatValue());
				if (isHex) {
					BigInteger bigInt = BigInteger.valueOf(endResult);
					byte[] theBytes = bigInt.toByteArray();
					String hexValue = DatatypeConverter.printHexBinary(theBytes);
					request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE, hexValue);
				} else {
					request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE,
							endResult.toString());
				}
			} catch (Exception e) {
				log.warn("Could not execute Math: " + mathDescriptor, e);
			}
		}
		return request;
	}

	// Helper Method
	public static String calculateReplaceIntensityValue(String request, DeviceState state, StateChangeBody theChanges, boolean hasBri, boolean hasBriInc, boolean isHex) {
		return replaceIntensityValue(request, calculateIntensity(state, theChanges, hasBri, hasBriInc), isHex);
	}
}
