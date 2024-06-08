/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import java.util.Arrays;

public enum Phases {
    PRODUCER(true), CONSUMER(true), NON_PHASED(false), ASYNCHRONOUS(false), PERMUTATIONAL(false);

    boolean hasSplittingEvent;

    Phases(boolean in_isInPhase) {
        hasSplittingEvent = in_isInPhase;
    }

    /**
     * Returns the Phased Test state in which the current test session is being executed
     * <p>
     * Author : gandomi
     *
     * @return The phase which is currently being executed
     */
    public static Phases getCurrentPhase() {
        return fetchCorrespondingPhase(ConfigValueHandlerPhased.PROP_SELECTED_PHASE.fetchValue());
    }

    /**
     * We find a corresponding PhasedTest state given a string. If none are found we return INACTIVE
     * <p>
     * Author : gandomi
     *
     * @param in_stateValue Returns a Phase given a string representation of its value
     * @return A state corresponding to the given Phased State, if none found we return inactive
     */
    public static Phases fetchCorrespondingPhase(String in_stateValue) {
        for (Phases lt_ptState : Phases.values()) {
            if (lt_ptState.toString().equalsIgnoreCase(in_stateValue)) {
                return lt_ptState;
            }
        }
        return NON_PHASED;
    }

    /**
     * Provides an array of Phases that contain a plittingEvent aka PhasedEvent
     * <p>
     * Author : gandomi
     *
     * @return An array of Phases that have a Splitting Event
     */
    public static Phases[] fetchPhasesWithEvents() {
        return Arrays.stream(Phases.values()).filter(p -> p.hasSplittingEvent).toArray(Phases[]::new);
    }

    /**
     * Checks if the current entry is active. I.e. either producer or consumer
     * <p>
     * Author : gandomi
     *
     * @return true if we are the active state
     */
    public boolean isSelected() {
        return this.equals(getCurrentPhase());
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
        ConfigValueHandlerPhased.PROP_SELECTED_PHASE.activate(this.name());
    }

}
