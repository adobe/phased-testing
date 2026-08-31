/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.samples;

import java.lang.reflect.Method;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PhasedReportElementsSample {

  @Test
  public void sampleMethodScenarioName() {}

  @Test
  public void sampleMethodPhase() {
  }

  @Test
  public void sampleMethodPhaseGroupNoParams() {}

  @Test
  public void sampleMethodPhaseGroupWithParams(Method method) {}

  @Test(dataProvider = "dp")
  public void sampleMethodDataProviders(int i, int j) {}

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
        {10, 20}
    };
  }

}
