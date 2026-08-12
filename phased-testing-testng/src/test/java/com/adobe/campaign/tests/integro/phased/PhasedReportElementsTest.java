/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.samples.PhasedReportElementsSample;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PhasedReportElementsTest implements IConfigurable, IHookable {

  private final SimpleListener listener = new SimpleListener();

  @BeforeMethod
  @AfterMethod
  public void cleanup() {
    ConfigValueHandlerPhased.resetAllValues();
  }

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate("PRODUCER");
    callBack.runConfigurationMethod(testResult);
  }

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate("PRODUCER");
    callBack.runTestMethod(testResult);
  }

  @BeforeClass
  public void setup() {
    TestNG testng = TestTools.createTestNG(PhasedReportElementsSample.class);
    testng.addListener(listener);
    testng.setVerbose(2);
    testng.run();
  }

  @Test
  public void testScenarioName() {
    List<ITestResult> itr = listener.data.get("sampleMethodScenarioName");
    String text = PhasedReportElements.SCENARIO_NAME.fetchElement(itr.get(0));
    assertEquals(text, "phasedReportElementsSample");
  }

  @Test
  public void testPhase() {
    List<ITestResult> itr = listener.data.get("sampleMethodPhase");
    String text = PhasedReportElements.PHASE.fetchElement(itr.get(0));
    assertEquals(text, "PRODUCER");
  }

  @Test
  public void testPhaseGroupZeroArgMethod() {
    List<ITestResult> itr = listener.data.get("sampleMethodPhaseGroupNoParams");
    String text = PhasedReportElements.PHASE_GROUP.fetchElement(itr.get(0));
    assertEquals(text, "");
  }

  @Test
  public void testPhaseGroupOneArgMethod() {
    List<ITestResult> itr = listener.data.get("sampleMethodPhaseGroupWithParams");
    String text = PhasedReportElements.PHASE_GROUP.fetchElement(itr.get(0));
    assertEquals(text, "public void com.adobe.campaign.tests.integro.phased.samples.PhasedReportElementsSample.sampleMethodPhaseGroupWithParams(java.lang.reflect.Method)");
  }

  @Test
  public void testDataProvider() {
    List<ITestResult> itr = listener.data.get("sampleMethodDataProviders");
    String text = PhasedReportElements.DATA_PROVIDERS.fetchElement(itr.get(0));
    assertEquals(text, "20");
  }

  public static class SimpleListener implements ITestListener {

    final Map<String, List<ITestResult>> data = new HashMap<>();

    @Override
    public void onTestSuccess(ITestResult itr) {
      data.computeIfAbsent(itr.getMethod().getMethodName(), k -> new ArrayList<>())
          .add(itr);
    }
  }

}
