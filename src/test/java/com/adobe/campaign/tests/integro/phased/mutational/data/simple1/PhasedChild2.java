/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.mutational.data.simple1;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.Mutational;
import org.testng.Assert;
import org.testng.annotations.Test;

//@Test(groups = "aaa", dataProvider = "MUTATIONAL", dataProviderClass = PhasedDataProvider.class)
//@PhasedTest
@Test(groups = "aaa")
public class PhasedChild2 extends Mutational {

    public void step1(String phaseGroup) {
        System.out.println("PG2 : Executing step1 with"+phaseGroup);
        Assert.assertEquals(1,1);
    }

    public void step2(String phaseGroup) {
        System.out.println("PG2 : Executing step2 with"+phaseGroup);

        Assert.assertEquals(1,1);
    }

    public void step3(String phaseGroup) {
        System.out.println("PG2 : Executing step3 with"+phaseGroup);

        Assert.assertEquals(1,1);
    }
}
