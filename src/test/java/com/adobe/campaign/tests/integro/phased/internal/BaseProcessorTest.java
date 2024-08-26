/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.Phases;
import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

public class BaseProcessorTest implements IHookable {

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.PRODUCER.name());
    callBack.runTestMethod(testResult);
    ConfigValueHandlerPhased.PROP_SELECTED_PHASE.reset();
  }
}
