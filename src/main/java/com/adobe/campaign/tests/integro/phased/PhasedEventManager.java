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

        NonInterruptiveEvent nie = instantiateClassFromString(in_event);
        logEvent(EventMode.START, in_event, in_onAccountOfStep);
        events.put(in_onAccountOfStep, nie);
        nie.startEvent();

        return nie;
    }

    private static NonInterruptiveEvent instantiateClassFromString(String in_event) {
        NonInterruptiveEvent nie = null;

        try {
            Class<?> eventClass = Class.forName(in_event);

            if (!NonInterruptiveEvent.class.isAssignableFrom(eventClass)) {
                throw new PhasedTestConfigurationException("The given event "+in_event+ " should be a sub-class of the abstract class "+NonInterruptiveEvent.class.getTypeName()+".");
            }
            nie = (NonInterruptiveEvent) eventClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {

            throw new PhasedTestConfigurationException("We have had a problem instantiating the event "+in_event+".", e);
        } catch (ClassNotFoundException e) {
            throw new PhasedTestConfigurationException("The given event class "+in_event+" could not be found.", e);
        }
        return nie;
    }

    /**
     * This method finished the given event for the given step.
     *
     * @param in_event           The event that is logged
     * @param in_onAccountOfStep The step responsible for the event
     * @return The NonInterruptive Event that is started by this call
     */
    public static NonInterruptiveEvent finishEvent(String in_event, String in_onAccountOfStep) {
        NonInterruptiveEvent l_activeEvent = events.get(in_onAccountOfStep);
        if (l_activeEvent == null) {
            throw new PhasedTestException("No event of the type "+in_event+" was stored for the test step "+in_onAccountOfStep);
        }

        try {
            if (Class.forName(in_event) != l_activeEvent.getClass()) {
                throw new PhasedTestException("The given class "+in_event+" does not exist.");
            }
        } catch (ClassNotFoundException e) {
            throw new PhasedTestConfigurationException("Class "+in_event+" not found.",e);
        }

        l_activeEvent.waitTillFinished();

        logEvent(EventMode.END, in_event, in_onAccountOfStep);
        return l_activeEvent;
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
