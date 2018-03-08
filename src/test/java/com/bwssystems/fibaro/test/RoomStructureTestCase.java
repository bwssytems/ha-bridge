package com.bwssystems.fibaro.test;

import org.junit.Assert;
import org.junit.Test;

public class RoomStructureTestCase {

	@Test
	public void testValidateStructure() {
		RoomDataConstructor aTestService = new RoomDataConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
