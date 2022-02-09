package com.adobe.campaign.tests.integro.phased.internal;

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
  }
}
