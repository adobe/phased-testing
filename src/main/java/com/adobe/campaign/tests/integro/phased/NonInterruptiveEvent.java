package com.adobe.campaign.tests.integro.phased;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public abstract class NonInterruptiveEvent {

    protected Thread eventThread;

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

}
