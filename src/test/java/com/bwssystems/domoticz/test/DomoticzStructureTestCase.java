package com.bwssystems.domoticz.test;

import org.junit.Assert;
import org.junit.Test;

public class DomoticzStructureTestCase {

	@Test
	public void testValidateStructure() {
		DomoticzDeviceConstructor aTestService = new DomoticzDeviceConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
