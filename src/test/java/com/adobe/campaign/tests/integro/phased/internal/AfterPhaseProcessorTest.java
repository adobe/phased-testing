/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.internal.samples.PhasesSample;
import java.lang.reflect.Method;
import org.testng.annotations.Test;

public class AfterPhaseProcessorTest extends BaseProcessorTest {

  @Test
  public void testAfterPhaseHappyFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("afterClass");
    AbstractPhaseProcessor processor = PhaseProcessorFactory.getProcessor(method);
    assertTrue(processor instanceof AfterPhaseProcessor);
    assertTrue(processor.canProcessPhase());
  }

  @Test
  public void testAfterPhaseHappyFlow2() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("afterTest");
    AbstractPhaseProcessor processor = PhaseProcessorFactory.getProcessor(method);
    assertTrue(processor instanceof AfterPhaseProcessor);
    assertFalse(processor.canProcessPhase());
  }

  @Test(expectedExceptions = PhasedTestConfigurationException.class)
  public void testAfterPhaseNegativeFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("afterMethod");
    PhaseProcessorFactory.getProcessor(method).canProcessPhase();
  }
}
