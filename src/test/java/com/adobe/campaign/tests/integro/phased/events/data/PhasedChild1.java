package com.adobe.campaign.tests.integro.phased.events.data;

import com.adobe.campaign.tests.integro.phased.PhasedDataProvider;
import com.adobe.campaign.tests.integro.phased.events.PhasedParent;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "aaa", dataProvider = "temp", dataProviderClass = PhasedDataProvider.class)
public class PhasedChild1 extends PhasedParent {

    public void step1(String phaseGroup) {
        System.out.println("Executing step1 with"+phaseGroup);
        Assert.assertEquals(1,1);
    }

    public void step2(String phaseGroup) {
        System.out.println("Executing step2 with"+phaseGroup);

        Assert.assertEquals(1,1);
    }
}
