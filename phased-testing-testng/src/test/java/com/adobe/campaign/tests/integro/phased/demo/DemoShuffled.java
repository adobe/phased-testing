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
public class DemoShuffled {

    public void step1(String val) {
        //Perform actions
    }

    public void step2(String val) {
        //Perform Actions
        
        //Perform Assertions
    }

    public void step3(String val) {
        //Perform Actions
        
        //Perform Assertions
    }   
}
