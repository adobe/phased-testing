/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public class PhasedEventLogEntry {
    Date eventDate;
    PhasedEventManager.EventMode eventMode;
    String eventName;
    String phasedStepName;

    public PhasedEventLogEntry(PhasedEventManager.EventMode eventMode, String eventName, String phasedStepName) {
        this.eventDate = new Date();
        this.eventMode = eventMode;
        this.eventName = eventName;
        this.phasedStepName = phasedStepName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public PhasedEventManager.EventMode getEventMode() {
        return eventMode;
    }

    public String getEventName() {
        return eventName;
    }

    public String getPhasedStepName() {
        return phasedStepName;
    }
}
