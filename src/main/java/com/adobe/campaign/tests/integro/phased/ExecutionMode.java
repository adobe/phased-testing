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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ExecutionMode {
    STANDARD(false,  new ArrayList<>()),
    //We need to revise this as execution in a suite may not require a precision
    NON_INTERRUPTIVE(false, Arrays.asList( "23", "33" )) {

        public boolean isSelected() {
            return this.equals(getCurrentMode()) || Phases.ASYNCHRONOUS.isSelected();
        };
    },
    INTERRUPTIVE(false, Arrays.asList( "PRODUCER", "CONSUMER" )) {
        public boolean isSelected() {
            return this.equals(getCurrentMode()) || Phases.PRODUCER.isSelected()
                    || Phases.CONSUMER.isSelected();
        };
        public boolean isSelected(String in_executionMode) {

            return this.fetchBehavior().equals(in_executionMode) || (in_executionMode.equals("PRODUCER") && Phases.PRODUCER.isSelected())
                    || (in_executionMode.equals("CONSUMER") && Phases.CONSUMER.isSelected());
        };
    },
    PERMUTATIONAL(true, Arrays.asList());

    boolean hasSplittingEvent;
    List<String> behaviorTypes;

    ExecutionMode(boolean in_isInPhase, List<String> in_phaseTypes) {
        hasSplittingEvent = in_isInPhase;
        behaviorTypes = in_phaseTypes;
    }

    /**
     * Returns the Phased Test state in which the current test session is being executed
     * <p>
     * Author : gandomi
     *
     * @return The phase which is currently being executed
     */
    public static ExecutionMode getCurrentMode() {
        if (ConfigValueHandlerPhased.PROP_EXECUTION_MODE.isSet()) {
            return fetchCorrespondingMode(ConfigValueHandlerPhased.PROP_EXECUTION_MODE.fetchValue());
        }
        return Phases.getCurrentPhase().fetchRunValues().getExecutionMode();
    }

    /**
     * We find a corresponding PhasedTest state given a string. If none are found we return INACTIVE
     * <p>
     * Author : gandomi
     *
     * @param in_stateValue Returns a Phase given a string representation of its value
     * @return A state corresponding to the given Phased State, if none found we return inactive
     */
    public static ExecutionMode fetchCorrespondingMode(String in_stateValue) {
        for (ExecutionMode lt_ptState : ExecutionMode.values()) {
            if (in_stateValue.toUpperCase().startsWith(lt_ptState.toString().toUpperCase())) {
                return lt_ptState;
            }
        }
        return STANDARD;
    }

    /**
     * Provides an array of Phases that contain a splitting Event aka PhasedEvent
     * <p>
     * Author : gandomi
     *
     * @return An array of Phases that have a Splitting Event
     */
    public static ExecutionMode[] fetchPhasesWithEvents() {
        return Arrays.stream(ExecutionMode.values())
                .filter(p -> p.hasSplittingEvent)
                .toArray(ExecutionMode[]::new);
    }

    /**
     * Checks if the selected Execution Mode is the given one
     * @param in_executionMode A given execution mode to check
     * @return
     */
    public static boolean is(ExecutionMode in_executionMode) {
        return in_executionMode.equals(getCurrentMode());
    }

    public static String getCurrentModeAsString() {
        return getCurrentMode().fetchRunValues().toString();
    }

    public RunValues fetchRunValues() {
        return new RunValues(getCurrentMode(), getCurrentMode().fetchBehavior());
    }

    public boolean isTypeValid() {
        String l_currentType = fetchBehavior();
        if (behaviorTypes.isEmpty()) {
            return l_currentType.isEmpty();
        }
        return behaviorTypes.contains(l_currentType);
    }

    /**
     * Checks if the current entry is active. I.e. either producer or consumer
     * <p>
     * Author : gandomi
     *
     * @return true if we are the active state
     */
    public boolean isSelected() {
        return this.equals(getCurrentMode());
    }

    /**
     * Lets us know if the current phase will include a splitting event
     * <p>
     * Author : gandomi
     *
     * @return True if the the phase could have a splitting event.
     */
    public boolean hasSplittingEvent() {
        return this.hasSplittingEvent;
    }

    /**
     * Activates the given phase
     * <p>
     * Author : gandomi
     */
    void activate() {
        ConfigValueHandlerPhased.PROP_EXECUTION_MODE.activate(this.name());
    }

    /**
     * Activates the given execution type with the given mode
     * @param in_executionMode
     */
    public void activate(String in_executionMode) {
        if (!behaviorTypes.contains(in_executionMode)) {
            throw new MutationRampUpException("The given execution mode type is not valid for this execution type. Please use one of the following: " + behaviorTypes.toString());
        }

        ConfigValueHandlerPhased.PROP_EXECUTION_MODE.activate(this.name() + "(" + in_executionMode + ")");
    }

    /**
     * Fetches the mode of the current execution type
     * @return The mode set at runtime
     */
    public String fetchBehavior() {
        if (ConfigValueHandlerPhased.PROP_EXECUTION_MODE.isSet()) {
            String l_value = ConfigValueHandlerPhased.PROP_EXECUTION_MODE.fetchValue();
            int l_startIndex = l_value.indexOf("(");
            int l_endIndex = l_value.indexOf(")");

            if (l_startIndex != -1 && l_endIndex != -1) {
                return l_value.substring(l_startIndex + 1, l_endIndex);
            }
            return "";
        } else {
            return Phases.getCurrentPhase().behavior;
        }

    }

    /**
     * Checks if the given Type and mode are selected. If the execution type does not expect a mode, we simply ignore
     * the given mode.
     *
     * @param in_executionMode The mode that is expected to be selected
     * @return True if the given execution type and mode are selected
     */
    public boolean isSelected(String in_executionMode) {
        //Ignore the argument if none are expected
        return this.behaviorTypes.isEmpty() ? isSelected() : this.fetchBehavior().equals(in_executionMode);

    }

}
