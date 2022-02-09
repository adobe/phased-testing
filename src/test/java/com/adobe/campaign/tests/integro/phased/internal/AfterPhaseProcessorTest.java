package com.adobe.campaign.tests.integro.phased.internal;

import static org.testng.Assert.assertTrue;

import com.adobe.campaign.tests.integro.phased.PhasedTestConfigurationException;
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

  @Test(expectedExceptions = PhasedTestConfigurationException.class)
  public void testAfterPhaseNegativeFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("afterMethod");
    PhaseProcessorFactory.getProcessor(method).canProcessPhase();
  }
}
