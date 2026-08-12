/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.befaft;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PhasedSeries_M_SimpleClass {

    @Test
    public void myTest1() {
        System.out.println("myTest ");
    }

    @Test
    public void myTest12() {
        System.out.println("myTest 2");
        assertTrue(false);
    }
}
