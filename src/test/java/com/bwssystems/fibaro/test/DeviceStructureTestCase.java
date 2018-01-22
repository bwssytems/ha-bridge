package com.bwssystems.fibaro.test;

import org.junit.Assert;
import org.junit.Test;

public class DeviceStructureTestCase {

	@Test
	public void testValidateStructure() {
		DeviceDataConstructor aTestService = new DeviceDataConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
