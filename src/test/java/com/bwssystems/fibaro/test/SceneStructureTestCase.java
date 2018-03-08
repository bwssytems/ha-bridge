package com.bwssystems.fibaro.test;

import org.junit.Assert;
import org.junit.Test;

public class SceneStructureTestCase {

	@Test
	public void testValidateStructure() {
		SceneDataConstructor aTestService = new SceneDataConstructor();
		Assert.assertEquals(aTestService.validateStructure(), true);
	}

}
