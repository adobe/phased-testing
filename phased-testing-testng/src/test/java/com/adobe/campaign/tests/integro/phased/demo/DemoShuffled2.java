/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.demo;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

@Test
@PhasedTest(canShuffle = true)
public class DemoShuffled2 {

    public void step1(String val) {
        //Store value with key
        PhasedTestManager.produce("MyKey", "Z");
    }

    public void step2(String val) {
        
        //Fetch value using key
        String fetchedValue = PhasedTestManager.consume("MyKey");
        Assert.assertEquals(fetchedValue, "Z");
    }
}
