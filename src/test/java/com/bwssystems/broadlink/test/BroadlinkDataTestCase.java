package com.bwssystems.broadlink.test;

import org.junit.Assert;
import org.junit.Test;

public class BroadlinkDataTestCase {

	@Test
	public void testValidateStructure() {
		BroadlinkDataConstructor aTestService = new BroadlinkDataConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
