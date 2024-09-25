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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ExecutionMode {
    DEFAULT(false,  new ArrayList<>()),
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
    };

    private static final ConfigValueHandlerPhased USED_PROPERTY = ConfigValueHandlerPhased.PROP_EXECUTION_MODE;

    boolean hasSplittingEvent;
    List<String> phaseTypes;

    ExecutionMode(boolean in_isInPhase, List<String> in_phaseTypes) {
        hasSplittingEvent = in_isInPhase;
        phaseTypes = in_phaseTypes;
    }

    /**
     * Returns the Phased Test state in which the current test session is being executed
     * <p>
     * Author : gandomi
     *
     * @return The phase which is currently being executed
     */
    public static ExecutionMode getCurrentMode() {
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
    public static ExecutionMode fetchCorrespondingMode(String in_stateValue) {
        for (ExecutionMode lt_ptState : ExecutionMode.values()) {
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
    public static ExecutionMode[] fetchPhasesWithEvents() {
        return Arrays.stream(ExecutionMode.values())
                .filter(p -> p.hasSplittingEvent)
                .toArray(ExecutionMode[]::new);
    }

    public boolean isTypeValid() {
        String l_currentType = fetchType();
        if (phaseTypes.isEmpty()) {
            return l_currentType.isEmpty();
        }
        return phaseTypes.contains(l_currentType);
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
        USED_PROPERTY.activate(this.name());
    }

    /**
     * Activates the given phase with the given type
     * @param in_phaseType
     */
    public void activate(String in_phaseType) {
        if (!phaseTypes.contains(in_phaseType)) {
            throw new IllegalArgumentException("The given phase type is not valid for this mode.");
        }

        USED_PROPERTY.activate(this.name() + "(" + in_phaseType + ")");
    }

    public String fetchType() {
        String l_value = USED_PROPERTY.fetchValue();
        int l_startIndex = l_value.indexOf("(");
        int l_endIndex = l_value.indexOf(")");

        if (l_startIndex != -1 && l_endIndex != -1) {
            return l_value.substring(l_startIndex + 1, l_endIndex);
        }
        return "";
    }

}
