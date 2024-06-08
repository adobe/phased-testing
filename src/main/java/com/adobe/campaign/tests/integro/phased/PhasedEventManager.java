/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestException;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhasedEventManager {
    private static final Logger log = LogManager.getLogger();

    private static ExecutorService eventExecutor = null;

    /**
     * Returns the declared event.  if declared on the method. The declarations have the following precedence:
     * <ol>
     *  <li>Declaration in @PhaseEvent</li>
     *  <li>Declaration in @PhasedTest</li>
     *  <li>When the property PHASED.EVENTS.NONINTERRUPTIVE is set</li>
     * </ol>Null is returned if no such declaration is present.
     * @param in_method The method we are examining
     * @return The event that is declared on the method. Null if there is no event declared for the method
     */
    public static String fetchApplicableEvent(Method in_method) {
        if (in_method.isAnnotationPresent(PhaseEvent.class) && (in_method.getDeclaredAnnotation(PhaseEvent.class).eventClasses().length > 0)) {
            return in_method.getDeclaredAnnotation(PhaseEvent.class).eventClasses()[0];
        } else if (PhasedTestManager.isPhasedTest(in_method) && (in_method.getDeclaringClass().getDeclaredAnnotation(PhasedTest.class).eventClasses().length > 0)) {
            return in_method.getDeclaringClass().getDeclaredAnnotation(PhasedTest.class).eventClasses()[0];
        } else if (ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.isSet()) {
            return ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.fetchValue();
        }
        return null;
    }

    protected static enum EventMode {START, END};

    static Map<String, NonInterruptiveEvent> events = new HashMap<>();


    private static List<PhasedEventLogEntry> eventLogs = new ArrayList();

    /**
     * Used for logging events
     *
     * @param in_eventMode       The mode of the event. If it is starting or finishing.
     * @param in_event           The event that is logged
     * @param in_onAccountOfStep The step responsable for the event
     */
    static void logEvent(EventMode in_eventMode, String in_event, String in_onAccountOfStep) {
        eventLogs.add(new PhasedEventLogEntry(in_eventMode, in_event, in_onAccountOfStep));
    }

    /**
     * This method starts the given event for the given step.
     *
     * @param in_event           The event that is logged
     * @param in_onAccountOfStep The step responsible for the event
     * @return The Non-Interruptive Event that is started by this call
     */
    protected static NonInterruptiveEvent startEvent(String in_event, String in_onAccountOfStep) {
        //Lazy load the service when needed
        if (eventExecutor == null) {
            eventExecutor = Executors.newSingleThreadExecutor();
        }
        log.debug("Starting event {} for step {}.",in_event,in_onAccountOfStep);
        NonInterruptiveEvent nie = instantiateClassFromString(in_event);
        logEvent(EventMode.START, in_event, in_onAccountOfStep);
        events.put(in_onAccountOfStep, nie);
        eventExecutor.submit(nie);
        while (nie.getState().equals(NonInterruptiveEvent.states.DEFINED)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
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
    protected static NonInterruptiveEvent finishEvent(String in_event, String in_onAccountOfStep) {
        log.debug("Finishing event {} for step {}.", in_event, in_onAccountOfStep);
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

    public static List<PhasedEventLogEntry> getEventLogs() {
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

    /**
     * Extracts the event for a given method. The choice of the event is based on where the event is declared.
     * @param in_testResult A result object for a test containing the annotation {@link PhaseEvent}
     * @return An event that can be executed with this method. Null if no event is applicable
     */
    public static String fetchEvent(ITestResult in_testResult) {
        Method l_currentMethod = in_testResult.getMethod().getConstructorOrMethod().getMethod();

        if (PhasedTestManager.isPhasedTestSingleMode(l_currentMethod)) {
            //Check if the current method is subject to event
            if (l_currentMethod.isAnnotationPresent(PhaseEvent.class)) {
                return fetchApplicableEvent(l_currentMethod);
            } else {
                return null;
            }
        } else {

            //Use Phase Context instead
            //String l_currentShuffleGroup = in_testResult.getParameters()[0].toString();

            int l_currentShuffleGroupNr = PhasedTestManager.asynchronousExtractIndex(in_testResult);

            int l_currentStep = PhasedTestManager.getMethodMap().get(ClassPathParser.fetchFullName(l_currentMethod)).methodOrderInExecution;

            if (l_currentStep  == l_currentShuffleGroupNr) {
                return fetchApplicableEvent(l_currentMethod);
            }
            return null;
        }
    }

    public static ExecutorService getEventExecutor() {
        return eventExecutor;
    }

    public static void stopEventExecutor() {
        if (eventExecutor != null)
            eventExecutor.shutdown();
    }


}
