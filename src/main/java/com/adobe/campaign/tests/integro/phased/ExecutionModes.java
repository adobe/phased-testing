/*
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.exceptions.MutationRampUpException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ExecutionModes {
    DEFAULT(false,  new ArrayList<>()),
    //We need to revise this as execution in a suite may not require a precision
    NON_INTERRUPTIVE(false, Arrays.asList( "23", "33" )) {

        public boolean isSelected() {
            return this.equals(getCurrentExecutionType()) || Phases.ASYNCHRONOUS.isSelected();
        };
    },
    INTERRUPTIVE(false, Arrays.asList( "PRODUCER", "CONSUMER" )) {
        public boolean isSelected() {
            return this.equals(getCurrentExecutionType()) || Phases.PRODUCER.isSelected()
                    || Phases.CONSUMER.isSelected();
        };
        public boolean isSelected(String in_executionMode) {

            return this.fetchMode().equals(in_executionMode) || Phases.PRODUCER.isSelected()
                    || Phases.CONSUMER.isSelected();
        };
    },
    PERMUTATIONAL(true, Arrays.asList());

    private static final ConfigValueHandlerPhased USED_PROPERTY = ConfigValueHandlerPhased.PROP_EXECUTION_MODE;

    boolean hasSplittingEvent;
    List<String> behaviorTypes;

    ExecutionModes(boolean in_isInPhase, List<String> in_phaseTypes) {
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
    public static ExecutionModes getCurrentExecutionType() {
        return fetchCorrespondingMode(USED_PROPERTY.fetchValue());
    }

    /**
     * We find a corresponding PhasedTest state given a string. If none are found we return INACTIVE
     * <p>
     * Author : gandomi
     *
     * @param in_stateValue Returns a Phase given a string representation of its value
     * @return A state corresponding to the given Phased State, if none found we return inactive
     */
    public static ExecutionModes fetchCorrespondingMode(String in_stateValue) {
        for (ExecutionModes lt_ptState : ExecutionModes.values()) {
            if (in_stateValue.toUpperCase().startsWith(lt_ptState.toString().toUpperCase())) {
                return lt_ptState;
            }
        }
        return DEFAULT;
    }

    /**
     * Provides an array of Phases that contain a splitting Event aka PhasedEvent
     * <p>
     * Author : gandomi
     *
     * @return An array of Phases that have a Splitting Event
     */
    public static ExecutionModes[] fetchPhasesWithEvents() {
        return Arrays.stream(ExecutionModes.values())
                .filter(p -> p.hasSplittingEvent)
                .toArray(ExecutionModes[]::new);
    }

    public boolean isTypeValid() {
        String l_currentType = fetchMode();
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
        return this.equals(getCurrentExecutionType());
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
        USED_PROPERTY.activate(this.name());
    }

    /**
     * Activates the given execution type with the given mode
     * @param in_executionMode
     */
    public void activate(String in_executionMode) {
        if (!behaviorTypes.contains(in_executionMode)) {
            throw new MutationRampUpException("The given execution mode type is not valid for this execution type. Please use one of the following: " + behaviorTypes.toString());
        }

        USED_PROPERTY.activate(this.name() + "(" + in_executionMode + ")");
    }

    /**
     * Fetches the mode of the current execution type
     * @return The mode set at runtime
     */
    public String fetchMode() {
        String l_value = USED_PROPERTY.fetchValue();
        int l_startIndex = l_value.indexOf("(");
        int l_endIndex = l_value.indexOf(")");

        if (l_startIndex != -1 && l_endIndex != -1) {
            return l_value.substring(l_startIndex + 1, l_endIndex);
        }
        return "";
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
        return this.behaviorTypes.isEmpty() ? isSelected() : this.fetchMode().equals(in_executionMode);

    }
}
