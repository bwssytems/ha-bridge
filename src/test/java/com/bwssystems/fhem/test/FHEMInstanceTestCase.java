package com.bwssystems.fhem.test;

import org.junit.Assert;
import org.junit.Test;

public class FHEMInstanceTestCase {

	@Test
	public void testValidateStructure() {
		FHEMInstanceConstructor aTestService = new FHEMInstanceConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
