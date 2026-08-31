/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
@PhasedTest
public class SimpleNonProduceConsumeTest {
    public static int value = 0;

    public void zzzz(String val) {
        Assert.assertEquals(value, 0);
        value+=11;
    }

    public void yyyyy(String val) {
        Assert.assertEquals(value, 11);
        value+=13;
    }

    public void xxxxx(String val) {
        Assert.assertEquals(value, 24);
    }

}
