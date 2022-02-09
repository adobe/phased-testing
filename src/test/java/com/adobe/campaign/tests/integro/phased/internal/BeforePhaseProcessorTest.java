package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.internal.samples.PhasesSample;
import java.lang.reflect.Method;

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

  @Test(expectedExceptions = PhasedTestConfigurationException.class)
  public void testBeforePhaseNegativeFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("beforeMethod");
    PhaseProcessorFactory.getProcessor(method).canProcessPhase();
  }

}
