/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.internal.samples.PhasesSample;
import java.lang.reflect.Method;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class BeforePhaseProcessorTest extends BaseProcessorTest {

  @Test
  public void testBeforePhaseHappyFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("beforeClass");
    AbstractPhaseProcessor processor = PhaseProcessorFactory.getProcessor(method);
    assertTrue(processor instanceof BeforePhaseProcessor);
    assertTrue(processor.canProcessPhase());
  }

  @Test
  public void testBeforePhaseHappyFlow2() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("beforeTest");
    AbstractPhaseProcessor processor = PhaseProcessorFactory.getProcessor(method);
    assertTrue(processor instanceof BeforePhaseProcessor);
    assertFalse(processor.canProcessPhase());
  }

  @Test(expectedExceptions = PhasedTestConfigurationException.class)
  public void testBeforePhaseNegativeFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("beforeMethod");
    PhaseProcessorFactory.getProcessor(method).canProcessPhase();
  }

}
