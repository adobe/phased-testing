package com.adobe.campaign.tests.integro.phased.events.data;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.events.PhasedParent;
import org.testng.Assert;
import org.testng.annotations.Test;

//@Test(groups = "aaa", dataProvider = "MUTATIONAL", dataProviderClass = PhasedDataProvider.class)
@PhasedTest
@Test
public class PhasedChild2 extends PhasedParent {

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
