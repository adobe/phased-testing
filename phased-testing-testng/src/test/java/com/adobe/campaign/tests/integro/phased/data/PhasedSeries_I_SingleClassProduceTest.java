/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.PhaseEvent;
import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;



@Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class, groups = {
        "UPGRADE2" }, description = "This is an archetypical test for the phased tests")
@PhasedTest(canShuffle = false)
public class PhasedSeries_I_SingleClassProduceTest {

    /**The first step*/
    
    public void step1(String data) {
        System.out.println("data is : "+data);
        PhasedTestManager.produce("MyVal", "A");

    }

    @PhaseEvent
    
    public void step2(String data) {
        String l_fetchedValue = PhasedTestManager.consume("MyVal");
        
        assertEquals(l_fetchedValue, "A");
    }
    

}
