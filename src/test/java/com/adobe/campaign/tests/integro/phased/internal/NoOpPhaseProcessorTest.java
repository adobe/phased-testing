/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.adobe.campaign.tests.integro.phased.internal.samples.PhasesSample;
import java.lang.reflect.Method;
import org.testng.annotations.Test;

public class NoOpPhaseProcessorTest extends BaseProcessorTest {

  @Test
  public void testNoPhaseHappyFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("regularMethod");
    AbstractPhaseProcessor processor = PhaseProcessorFactory.getProcessor(method);
    assertTrue(processor instanceof NoOpPhaseProcessor);
    assertTrue(processor.canProcessPhase());
    assertEquals(processor.filterBy().apply(method).length, 0);
    assertEquals(processor.getPhaseName(), "Dummy Phase");
  }
}
