package com.bwssystems.hass.test;

import org.junit.Assert;
import org.junit.Test;

public class StateStructureTestCase {

	@Test
	public void test() {
		StateDataConstructor aTestTarget = new StateDataConstructor();
		Assert.assertEquals(aTestTarget.validateStructure(), true);
	}

}
