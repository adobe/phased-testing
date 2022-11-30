package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhasedEventManager {

    protected static enum EventMode {START, END};

    static Map<String, NonInterruptiveEvent> events = new HashMap<String, NonInterruptiveEvent>();


    private static List<PhaseEventLogEntry> eventLogs = new ArrayList();

    /**
     * Used for logging events
     *
     * @param in_eventMode       The mode of the event. If it is starting or finishing.
     * @param in_event           The event that is logged
     * @param in_onAccountOfStep The step responsable for the event
     */
    static void logEvent(EventMode in_eventMode, String in_event, String in_onAccountOfStep) {
        eventLogs.add(new PhaseEventLogEntry(in_eventMode, in_event, in_onAccountOfStep));
    }

    /**
     * This method starts the given event for the given step.
     *
     * @param in_event           The event that is logged
     * @param in_onAccountOfStep The step responsable for the event
     * @return The NonInterruptive Event that is started by this call
     */
    public static NonInterruptiveEvent startEvent(String in_event, String in_onAccountOfStep) {

        NonInterruptiveEvent nie = null;
        try {
            nie = (NonInterruptiveEvent) Class.forName(in_event).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {

            throw new RuntimeException(e);
        }
        logEvent(EventMode.START, in_event, in_onAccountOfStep);
        events.put(in_onAccountOfStep, nie);
        nie.startEvent();
        return nie;
    }

    /**
     * This method finished the given event for the given step.
     *
     * @param in_event           The event that is logged
     * @param in_onAccountOfStep The step responsable for the event
     * @return The NonInterruptive Event that is started by this call
     */
    public static NonInterruptiveEvent finishEvent(String in_event, String in_onAccountOfStep) {
        logEvent(EventMode.END, in_event, in_onAccountOfStep);
        events.get(in_onAccountOfStep).waitTillFinished();
        return events.get(in_onAccountOfStep);
    }

    public static List<PhaseEventLogEntry> getEventLogs() {
        return eventLogs;
    }

    /**
     * resets the events. Mostly used for testing
     */
    public static void resetEvents() {
        events = new HashMap<>();
        eventLogs = new ArrayList();
    }

    public static Map<String, NonInterruptiveEvent> getEvents() {
        return events;
    }
}
