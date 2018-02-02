package com.bwssystems.broadlink.test;

import javax.xml.bind.DatatypeConverter;

public class BroadlinkDataConstructor {
	public final static byte[] testData1 = { 84, 104, 105, 115, 73, 115, 65, 84, 101, 115, 116, 83, 116, 114, 105, 110, 103, 49 };
	public final static String hexString1 = "5468697349734154657374537472696E6731";
	public final static byte[] testData2 = { 110, 0, 110, 1, 13, 4, 40, 0, 1, 79, 0, 1, 26, (byte) 139, 18, 17, 18, 52, 17, 53, 17, 18, 27, 8, 17, 18, 30, 5, 17, 18, 18, 51};
	public final static String hexString2 = "6e006e010d042800014f00011a8b12111234113511121b0811121e051112123312111211123412331234123412331212111212111112123312121211111212341134113412341211113511341135110004db00011a8c11121134123412111112111212111212113411121112113511341135113511341112111211121112123411121112121111351135113412331212113412341135110004db00011a8c11121135113412111211111211131112113411121211123411351134123411341211121112111212113411121212111211341135113412341112113511341234110004dc0001198d11121134113511121112111211121112113511121112113511341135113412341112111212111211123411121112111211351134123411341212113412341134120004db00011a8c11121135113411121112121211111211123411121112123411341234123411341211121112111212113412111211121112341234113412341112113511341234110005dc00000000000000000000";
	public final static byte[] testData3 = { 38, 0, 120, 0, 88, 28};
	public final static String hexString3 = "26007800581c111b0f0f0e0f2b2b100e0f0f100d0f0f0e0f100d0f0f0e0f100e1d0f0e100e0f0e1e1f000add571c111b0f0f110c2c2b100e0e0f100e0e0f0e0f100e0e0f0e0f110d1d100e0f0e0f0e1e1f000add571d0f1c100e100d2c2c0e0f0e0f0f0f0e0f0e0f110d0f0e100d100f1c100e0f0f0e0f1e1d000d05";
	public Boolean validateStructure() {
		String theHexString = hexString3;
		byte[] theBytes = testData3;
		byte[] theData;
		
		
		System.out.println("----------------------------------");
		System.out.println("This is the test hex string: <<<" + theHexString + ">>>");
		try {
			theData = DatatypeConverter.parseHexBinary(theHexString);
			System.out.println("This is the test hex string from data bytes: <<<" + DatatypeConverter.printHexBinary(theData) + ">>>");
		} catch(Exception e) {
			System.out.println("Error parsing he string: " + e.getMessage());
			return false;
		}
		for( int i = 0; i < theBytes.length; i++) {
			if(theBytes[i] != theData[i]) {
				System.out.println("the compare data is not the same length at index: " + i);
				return false;
			}
		}
		System.out.println("----------------------------------");
		return true;
	}

}