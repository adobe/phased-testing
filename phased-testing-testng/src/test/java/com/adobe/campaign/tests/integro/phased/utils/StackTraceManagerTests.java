/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import com.adobe.campaign.tests.integro.phased.utils.StackTraceManager;

public class StackTraceManagerTests {
    @Test(description = "In this example we pass through another method, the results should be this method")
    public void testFetchCalledBy() {

        assertThat(nestedCallForFetchCalledBy(), equalTo("testFetchCalledBy"));
    }

    String nestedCallForFetchCalledBy() {
        return StackTraceManager.fetchCalledBy().getMethodName();
    }
    
    @Test(groups = "TRACE")
    public void testFetchCalledTestInAfterMethod() {

        // Empty test as the actual code is in the AfterMethod
    }
    

}
