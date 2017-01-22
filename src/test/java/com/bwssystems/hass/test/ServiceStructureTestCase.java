package com.bwssystems.hass.test;

import org.junit.Assert;
import org.junit.Test;

public class ServiceStructureTestCase {

	@Test
	public void testValidateStructure() {
		ServiceDataConstructor aTestService = new ServiceDataConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
