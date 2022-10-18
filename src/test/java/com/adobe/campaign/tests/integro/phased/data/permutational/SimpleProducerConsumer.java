package com.adobe.campaign.tests.integro.phased.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTestManager;

public class SimpleProducerConsumer {
    public void bbbbb(Object param) {
        PhasedTestManager.produce("bbbbkey","bbbbValue");
    }

    public void aaaa(Object param) {
        PhasedTestManager.consume("bbbbkey");
    }
}
