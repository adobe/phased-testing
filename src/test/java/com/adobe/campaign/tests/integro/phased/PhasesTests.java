/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import org.hamcrest.Matchers;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class PhasesTests {
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
    public void testSetPhase() {
        assertThat("We should have no phase value", System.getProperty(ConfigValueHandlerPhased.PROP_SELECTED_PHASE.name()),Matchers.nullValue());
        Phases.PRODUCER.activate();
        
        assertThat("We should have the correct phase", Phases.PRODUCER.isSelected());
    }
    
    @Test
    public void testSetPhasedNonPhased() {
        Phases.NON_PHASED.activate();
        
        assertThat("We should have the correct phase", Phases.NON_PHASED.isSelected());
    }
    
    @Test
    public void testSetPhaseToNonPhased() {
        Phases.PRODUCER.activate();
        
        assertThat("We should have the correct phase", Phases.PRODUCER.isSelected());
   
        Phases.NON_PHASED.activate();
        
        assertThat("We should have the correct phase", Phases.NON_PHASED.isSelected());
    }
    
    
    @Test
    public void testPhaseStates() {
        assertThat("We should have the state INACTIVE by default for the PhasedTestStates",
                Phases.getCurrentPhase(), is(equalTo(Phases.NON_PHASED)));

        assertThat("We should find the correct state PRODUCER",
                Phases.fetchCorrespondingPhase("producer"), is(equalTo(Phases.PRODUCER)));

        assertThat("We should find the correct state CONSUMER",
                Phases.fetchCorrespondingPhase("conSumer"), is(equalTo(Phases.CONSUMER)));

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate("PRODuCER");
        assertThat("We should now have the state Producer for the PhasedTestStates",
                Phases.getCurrentPhase(), is(equalTo(Phases.PRODUCER)));

        assertThat("We should have a check me ethod that works", Phases.PRODUCER.isSelected());

        assertThat("We should find the correct state ASYNCHRONOUS_EVENT",
                Phases.fetchCorrespondingPhase("asYnChronous"), is(equalTo(Phases.ASYNCHRONOUS)));

        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.ASYNCHRONOUS.name());
        assertThat("We should now have the state Producer for the PhasedTestStates",
                Phases.getCurrentPhase(), is(equalTo(Phases.ASYNCHRONOUS)));

        assertThat("We should have a check me ethod that works", Phases.ASYNCHRONOUS.isSelected());
    }

    
    @Test
    public void testPhasesFetchWithEvents() {
        Phases[] l_phasesWithEvents = Phases.fetchPhasesWithEvents();
        
        assertThat("All phases should have a splitting event", Arrays.stream(l_phasesWithEvents).allMatch(p -> p.hasSplittingEvent));

    }


    @Test
    public void testNonInterruptivePhase() {

        Phases.ASYNCHRONOUS.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", Phases.ASYNCHRONOUS.isSelected());

        ConfigValueHandlerPhased.resetAllValues();

        ExecutionModes.NON_INTERRUPTIVE.activate();

        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", Phases.ASYNCHRONOUS.isSelected());

        //ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(Phases.NON_INTERRUPTIVE.name()+"23");
    }

    @Test
    public void testNonInterruptivePhaseWithEvents() {
        //assertThat("We should be able to extract the phase value from the string", GeneralTestUtils);

        String l_selectedPhase = ExecutionModes.NON_INTERRUPTIVE.name() + "(23)";
        assertThat("We should detect the correct phase",
                ExecutionModes.fetchCorrespondingMode(l_selectedPhase), Matchers.equalTo(ExecutionModes.NON_INTERRUPTIVE));

        ConfigValueHandlerPhased.PROP_EXECUTION_MODE.activate(l_selectedPhase);

        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.isSelected());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.fetchMode(), Matchers.equalTo("23"));
        assertThat("We should detect that the given value is corrects", ExecutionModes.getCurrentExecutionType().isTypeValid());


        ConfigValueHandlerPhased.PROP_EXECUTION_MODE.activate(ExecutionModes.NON_INTERRUPTIVE.name());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.fetchMode(), Matchers.equalTo(""));
        assertThat("We should not accept an empty type",!ExecutionModes.getCurrentExecutionType().isTypeValid());


        ConfigValueHandlerPhased.PROP_EXECUTION_MODE.activate(ExecutionModes.NON_INTERRUPTIVE.name());
        assertThat("This should be the same as Non-interruptive", ExecutionModes.NON_INTERRUPTIVE.fetchMode(), Matchers.equalTo(""));

        ConfigValueHandlerPhased.PROP_EXECUTION_MODE.activate(ExecutionModes.INTERRUPTIVE.name()+"(jhfdhj)");
        assertThat("We should detect that given type is incorrect",!ExecutionModes.getCurrentExecutionType().isTypeValid());

        ExecutionModes.DEFAULT.activate();
        assertThat("This should be the same as Non-phased", ExecutionModes.getCurrentExecutionType().fetchMode(), Matchers.equalTo(""));
        assertThat("We should accept an empty type", ExecutionModes.getCurrentExecutionType().isTypeValid());

        ExecutionModes.NON_INTERRUPTIVE.activate("33");
        assertThat("This should be the same as Non-interruptive", ExecutionModes.getCurrentExecutionType().fetchMode(), Matchers.equalTo("33"));

    }

    @Test
    public void testingBackWardCompatibility() {
        Phases.PRODUCER.activate();
        assertThat("We should have the correct phase", !Phases.ASYNCHRONOUS.isSelected());
    }



}
