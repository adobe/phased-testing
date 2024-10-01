/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestingEventException;

public abstract class NonInterruptiveEvent implements Runnable {

    /**
     * Starts the non-interruptive event
     * returns true if the event was successfully started
     */
    public abstract boolean startEvent();

    /**
     * Gives us information about the state of the event. whether or not is still on-going
     * @return false if the event is still on-going
     */
    public abstract boolean isFinished();

    /**
     * Waits until the event has concluded
     * @return true if it successfully finished
     */
    public abstract boolean waitTillFinished();

    public enum states {DEFINED , STARTED, FAILURE, FINISHED};

    protected states state = states.DEFINED;

    @Override
    public void run() {
        state = startEvent() ? states.STARTED : states.FAILURE;

        if (state.equals(states.FAILURE)) {
            throw new PhasedTestingEventException("There was a problem starting this event.");
        }

        waitTillFinished();

        if (!isFinished()) {
            throw new PhasedTestingEventException("This event did not finish as expected.");
        }
        state=states.FINISHED;
        Thread.currentThread().interrupt();
        return;
    }

    public states getState() {
        return state;
    }

    /**
     * Override this method to execute actions after the step subject to the event has been completed
     * @return true if the post step actions were successful
     */
    public boolean tearDownEvent() {
        return true;
    }

}
