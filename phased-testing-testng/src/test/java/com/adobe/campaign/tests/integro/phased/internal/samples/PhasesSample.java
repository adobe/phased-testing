/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal.samples;

import com.adobe.campaign.tests.integro.phased.AfterPhase;
import com.adobe.campaign.tests.integro.phased.BeforePhase;
import com.adobe.campaign.tests.integro.phased.Phases;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

public class PhasesSample {

  @BeforePhase(appliesToPhases = Phases.NON_PHASED)
  @BeforeTest
  public void beforeTest() {}

  @BeforePhase
  @BeforeClass
  public void beforeClass() {}

  @BeforePhase
  @BeforeMethod
  public void beforeMethod() {}

  @AfterPhase(appliesToPhases = Phases.NON_PHASED)
  @AfterTest
  public void afterTest() {}

  @AfterPhase
  @AfterClass
  public void afterClass() {}

  @AfterPhase
  @AfterMethod
  public void afterMethod() {}

  public void regularMethod() {}

}
