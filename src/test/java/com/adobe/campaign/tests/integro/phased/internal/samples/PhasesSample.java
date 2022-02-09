package com.adobe.campaign.tests.integro.phased.internal.samples;

import com.adobe.campaign.tests.integro.phased.AfterPhase;
import com.adobe.campaign.tests.integro.phased.BeforePhase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public class PhasesSample {

  @BeforePhase
  @BeforeClass
  public void beforeClass() {}

  @BeforePhase
  @BeforeMethod
  public void beforeMethod() {}

  @AfterPhase
  @AfterClass
  public void afterClass() {}

  @AfterPhase
  @AfterMethod
  public void afterMethod() {}

  public void regularMethod() {}

}
