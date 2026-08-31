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

import java.util.concurrent.Future;

public abstract class NonInterruptiveEvent implements Runnable {

    Future<?> threadFuture = null;

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
     * Waits until the event has started (i.e. the startUp stage has been finalized).
     * @return true if the event successfully started
     */
    public boolean waitTillStarted() {
        return waitTillFinished();
    }

    /**
     * Waits until the event has concluded
     * @return true if it successfully finished
     * @deprecated Use {@link #waitTillStarted()} instead. This method will be removed in a future major version.
     */
    @Deprecated(since = "9.0.0", forRemoval = true)
    public boolean waitTillFinished() {
        return true;
    }

    public enum states {DEFINED , STARTED, FAILURE, FINISHED};

    protected states state = states.DEFINED;

    @Override
    public void run() {
        try {
            startEvent();
            state = states.STARTED;
        } catch (Exception e) {
            state = states.FAILURE;
            throw new PhasedTestingEventException("There was a problem starting this event.", e);
        }

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
