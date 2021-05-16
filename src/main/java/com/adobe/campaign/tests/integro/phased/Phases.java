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

public enum Phases {
    PRODUCER(true), CONSUMER(true), NON_PHASED(false);

    
    boolean hasSplittingEvent;
    
    private Phases(boolean in_isInPhase) {
        hasSplittingEvent=in_isInPhase;
    }
    
    /**
     * Returns the Phased Test state in which the current test session is being executed
     *
     * Author : gandomi
     *
     * @return The phase which is currently being executed
     *
     */
    public static Phases getCurrentPhase() {
        
        return fetchCorrespondingPhase(System.getProperty(PhasedTestManager.PROP_SELECTED_PHASE));
    }

    /**
     * We find a corresponding PhasedTest state given a string. If none are found we return INACTIVE
     *
     * Author : gandomi
     *
     * @param in_stateValue
     * @return A state corresponding to the given Phased State, if none found we return inactive
     *
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
     * Checks if the current entry is active. I.e. either producer or consumer
     *
     * Author : gandomi
     *
     * @return true if we are the active state
     *
     */
    public boolean isSelected() {
        
        return this.equals(getCurrentPhase());
    }

    /**
     * Lets us know if the current phase will include a splitting event
     *
     * Author : gandomi
     *
     * @return
     *
     */
    public boolean hasSplittingEvent() {
        
        return this.hasSplittingEvent;
    }

    /**
     * Activates the given phase
     *
     * Author : gandomi
     *
     *
     */
    protected void activate() {
        if (this.hasSplittingEvent) {
            System.setProperty(PhasedTestManager.PROP_SELECTED_PHASE, this.name());
        } else {
            System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        }
        
    }

}
