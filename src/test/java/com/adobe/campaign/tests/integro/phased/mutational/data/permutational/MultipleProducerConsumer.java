package com.adobe.campaign.tests.integro.phased.mutational.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.events.PhasedParent;
import org.testng.annotations.Test;

@PhasedTest
@Test(groups = "aaa")
public class MultipleProducerConsumer extends PhasedParent {
    public void ccccc(Object param) {
        PhasedTestManager.produce("keyA","bbbbValue");
    }

    public void bbbbb(Object param) {
        PhasedTestManager.produce("keyB","bbbbValue");
    }

    public void aaaaa(Object param) {
        PhasedTestManager.consume("keyB");

        PhasedTestManager.consume("keyA");
    }
}
