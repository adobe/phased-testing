/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.exceptions.MutationRampUpException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ExecutionModesTests {
    @BeforeClass
    public void cleanCache() {
        PhasedTestManager.clearCache();
        ConfigValueHandlerPhased.resetAllValues();
    }

    @AfterMethod
    public void clearAllData() {
        cleanCache();
    }


    @Test
    public void testSetModeNonInterruptive() {
        ExecutionModes.NON_INTERRUPTIVE.activate("23");

        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.isSelected("23"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.ASYNCHRONOUS));

        //assertThat("This should be the same as Non-interruptive", Phases.ASYNCHRONOUS.isSelected());

    }

    @Test
    public void testSetModeNonInterruptivePhased() {
        Phases.ASYNCHRONOUS.activate();

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.ASYNCHRONOUS));

        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.isSelected());
    }

    @Test
    public void test_Negative_SetBadMode() {
        Assert.assertThrows(MutationRampUpException.class, () -> ExecutionModes.INTERRUPTIVE.activate("23"));
    }

    @Test
    public void testSetModeInterruptiveProducer() {
        ExecutionModes.INTERRUPTIVE.activate("PRODUCER");

        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected("PRODUCER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.PRODUCER));

    }

    @Test
    public void testSetModeInterruptiveProducerPhased() {
        Phases.PRODUCER.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected("PRODUCER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.PRODUCER));

    }

    @Test
    public void testSetModeInterruptiveConsumer() {
        ExecutionModes.INTERRUPTIVE.activate("CONSUMER");

        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected("CONSUMER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.CONSUMER));
    }

    @Test
    public void testSetModeInterruptiveConsumerPhased() {
        Phases.CONSUMER.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.INTERRUPTIVE.isSelected("CONSUMER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.CONSUMER));

    }

    @Test
    public void testSDefault() {
        assertThat("This should be the same as Non-interruptive", ExecutionModes.DEFAULT.isSelected());
        assertThat("Even though we pass a bad value we should not throw an exception. It is simply ignored", ExecutionModes.DEFAULT.isSelected("CONSUMER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.NON_PHASED));
    }

    @Test
    public void testSMutational() {
        ExecutionModes.PERMUTATIONAL.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionModes.PERMUTATIONAL.isSelected());

        assertThat("Permutational does not exist in phased", Phases.getCurrentPhase().equals(Phases.NON_PHASED));
    }


}
