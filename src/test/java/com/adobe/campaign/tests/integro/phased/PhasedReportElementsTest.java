/*
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.samples.PhasedReportElementsSample;
import com.adobe.campaign.tests.integro.phased.PhasedTestConfigValueHandler;
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
    PhasedTestConfigValueHandler.resetAllValues();
  }

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    PhasedTestConfigValueHandler.PROP_SELECTED_PHASE.activate("PRODUCER");
    callBack.runConfigurationMethod(testResult);
  }

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    PhasedTestConfigValueHandler.PROP_SELECTED_PHASE.activate("PRODUCER");
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
