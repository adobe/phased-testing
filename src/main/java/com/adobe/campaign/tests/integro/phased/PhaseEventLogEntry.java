package com.adobe.campaign.tests.integro.phased;

import java.util.Date;

public class PhaseEventLogEntry {
    Date eventDate;
    PhasedEventManager.EventMode eventMode;
    String eventName;
    String phasedStepName;

    public PhaseEventLogEntry(PhasedEventManager.EventMode eventMode, String eventName, String phasedStepName) {
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
