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

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestingEventException;

public abstract class NonInterruptiveEvent implements Runnable {

    /**
     * Starts the non-interruptive event
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
    //
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
    public boolean runPostStepActions() {
        return true;
    }

}
