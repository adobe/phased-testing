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
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class ExecutionModeTests {
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
        ExecutionMode.NON_INTERRUPTIVE.activate("23");

        assertThat("This should be the same as Non-interruptive", ExecutionMode.NON_INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionMode.NON_INTERRUPTIVE.isSelected("23"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.ASYNCHRONOUS));

        var runMode = ExecutionMode.getCurrentMode().fetchRunValues();
        assertThat("The execution mode should be correct", runMode.getExecutionMode(),
                Matchers.equalTo(ExecutionMode.NON_INTERRUPTIVE));
        assertThat("The execution mode should be correct", runMode.getBehavior(), Matchers.equalTo("23"));

        assertThat("The both run values should be the same Phases and ExecutionModes",
                runMode.equals(Phases.ASYNCHRONOUS.fetchRunValues()));

        assertThat("The runMode toString should be correct", runMode.toString(),
                Matchers.equalTo("NON_INTERRUPTIVE(23)"));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(runMode.toString()));
    }

    @Test
    public void testSetModeNonInterruptivePhased() {
        Phases.ASYNCHRONOUS.activate();

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.ASYNCHRONOUS));

        assertThat("This should be the same as Non-interruptive", ExecutionMode.NON_INTERRUPTIVE.isSelected());

        assertThat("The current Mode should be non interruptive", ExecutionMode.getCurrentMode(), Matchers.equalTo(ExecutionMode.NON_INTERRUPTIVE));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(ExecutionMode.NON_INTERRUPTIVE.name()));

    }

    @Test
    public void test_Negative_SetBadMode() {
        Assert.assertThrows(MutationRampUpException.class, () -> ExecutionMode.INTERRUPTIVE.activate("23"));
    }

    @Test
    public void testSetModeInterruptiveProducer() {
        ExecutionMode.INTERRUPTIVE.activate("PRODUCER");

        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected("PRODUCER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.PRODUCER));

        var runMode = ExecutionMode.getCurrentMode().fetchRunValues();
        assertThat("The execution mode should be correct", runMode.getExecutionMode(),
                Matchers.equalTo(ExecutionMode.INTERRUPTIVE));
        assertThat("The execution mode should be correct", runMode.getBehavior(), Matchers.equalTo("PRODUCER"));

        assertThat("The both run values should be the same Phases and ExecutionModes",
                runMode.equals(Phases.PRODUCER.fetchRunValues()));

        assertThat("The both run values should NOT be the same Phases and ExecutionModes",
                !runMode.equals(Phases.CONSUMER.fetchRunValues()));

        assertThat("The runMode toString should be correct", runMode.toString(),
                Matchers.equalTo("INTERRUPTIVE(PRODUCER)"));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(runMode.toString()));

    }

    @Test
    public void testSetModeInterruptiveProducerPhased() {
        Phases.PRODUCER.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected("PRODUCER"));
        assertThat("This should be the same as Non-interruptive", !ExecutionMode.INTERRUPTIVE.isSelected("CONSUMER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.PRODUCER));

        var runMode = ExecutionMode.getCurrentMode().fetchRunValues();

        assertThat("The runMode toString should be correct", runMode.toString(),
                Matchers.equalTo("INTERRUPTIVE(PRODUCER)"));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(runMode.toString()));

    }

    @Test
    public void testSetModeInterruptiveConsumer() {
        ExecutionMode.INTERRUPTIVE.activate("CONSUMER");

        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected("CONSUMER"));
        assertThat("This should be the same as Non-interruptive", !ExecutionMode.INTERRUPTIVE.isSelected("PRODUCER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.CONSUMER));

        assertThat("We should have an execution mode instance", ExecutionMode.getCurrentMode().fetchRunValues(),
                Matchers.instanceOf(
                        RunValues.class));
        assertThat("The execution mode should be correct",
                ExecutionMode.getCurrentMode().fetchRunValues().getExecutionMode(),
                Matchers.equalTo(ExecutionMode.INTERRUPTIVE));
        assertThat("The execution mode should be correct",
                ExecutionMode.getCurrentMode().fetchRunValues().getBehavior(), Matchers.equalTo("CONSUMER"));

        assertThat("The both run values should be the same Phases and ExecutionModes",
                ExecutionMode.getCurrentMode().fetchRunValues().equals(Phases.CONSUMER.fetchRunValues()));
        assertThat("The both run values should NOT be the same Phases and ExecutionModes",
                !ExecutionMode.getCurrentMode().fetchRunValues().equals(Phases.PRODUCER.fetchRunValues()));

    }

    @Test
    public void testSetModeInterruptiveConsumerPhased() {
        Phases.CONSUMER.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionMode.INTERRUPTIVE.isSelected("CONSUMER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.CONSUMER));

        var runMode = ExecutionMode.getCurrentMode().fetchRunValues();

        assertThat("The runMode toString should be correct", runMode.toString(),
                Matchers.equalTo("INTERRUPTIVE(CONSUMER)"));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(runMode.toString()));

    }

    @Test
    public void testDefault() {
        assertThat("This should be the same as Non-interruptive", ExecutionMode.DEFAULT.isSelected());
        assertThat("Even though we pass a bad value we should not throw an exception. It is simply ignored",
                ExecutionMode.DEFAULT.isSelected("CONSUMER"));

        assertThat("We should have the correct phase", Phases.getCurrentPhase().equals(Phases.NON_PHASED));

        var runMode = ExecutionMode.getCurrentMode().fetchRunValues();
        assertThat("The execution mode should be correct", runMode.getExecutionMode(),
                Matchers.equalTo(ExecutionMode.DEFAULT));
        assertThat("The execution mode should be correct", runMode.getBehavior(), Matchers.equalTo(""));

        assertThat("is is default selected", Phases.NON_PHASED.isSelected());

        assertThat("The runMode toString should be correct", runMode.toString(),
                Matchers.equalTo("DEFAULT"));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(runMode.toString()));

    }

    @Test
    public void testMutational() {
        ExecutionMode.PERMUTATIONAL.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionMode.PERMUTATIONAL.isSelected());

        assertThat("Permutational does not exist in phased", Phases.getCurrentPhase().equals(Phases.PERMUTATIONAL));

        var runMode = ExecutionMode.getCurrentMode().fetchRunValues();
        assertThat("The execution mode should be correct", runMode.getExecutionMode(),
                Matchers.equalTo(ExecutionMode.PERMUTATIONAL));
        assertThat("The execution mode should be correct", runMode.getBehavior(), Matchers.equalTo(""));

        assertThat("The runMode toString should be correct", runMode.toString(),
                Matchers.equalTo("PERMUTATIONAL"));

        assertThat("We should properly present the execution mode", ExecutionMode.getCurrentModeAsString(),
                Matchers.equalTo(runMode.toString()));

    }

    @Test
    public void testIs() {

        assertThat("We should have equals", ExecutionMode.getCurrentMode().equals(ExecutionMode.DEFAULT));
        assertThat("We should have equals", ExecutionMode.is(ExecutionMode.DEFAULT));

    }

    @Test
    public void testIsPermutational() {
        ExecutionMode.PERMUTATIONAL.activate();
        assertThat("We should have equals", ExecutionMode.getCurrentMode().equals(ExecutionMode.PERMUTATIONAL));
        assertThat("We should have equals", ExecutionMode.is(ExecutionMode.PERMUTATIONAL));

    }

    @Test
    public void testIsInterruptive() {
        ExecutionMode.INTERRUPTIVE.activate("PRODUCER");
        assertThat("We should have equals", ExecutionMode.getCurrentMode().equals(ExecutionMode.INTERRUPTIVE));
        assertThat("We should have equals", ExecutionMode.is(ExecutionMode.INTERRUPTIVE));

    }

}
