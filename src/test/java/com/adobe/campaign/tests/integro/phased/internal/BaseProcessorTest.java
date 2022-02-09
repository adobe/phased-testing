package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

public class BaseProcessorTest implements IHookable {

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    System.setProperty(PhasedTestManager.PROP_SELECTED_PHASE, "PRODUCER");
    callBack.runTestMethod(testResult);
    System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
  }
}
