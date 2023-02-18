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

import com.adobe.campaign.tests.integro.phased.utils.ConfigValueHandler;
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

    protected static enum EventMode {START, END};

    static Map<String, NonInterruptiveEvent> events = new HashMap<String, NonInterruptiveEvent>();


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
        NonInterruptiveEvent nie = instantiateClassFromString(in_event);
        logEvent(EventMode.START, in_event, in_onAccountOfStep);
        events.put(in_onAccountOfStep, nie);
        eventExecutor.submit(nie);
        while (nie.getState().equals(NonInterruptiveEvent.states.DEFINED)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
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

    public static String fetchEvent(ITestResult in_testResult) {
        return  fetchEvent(in_testResult, true);
    }

    /**
     * Extracts the event for a given method
     * @param in_testResult A result object for a test containing the annotation {@link PhaseEvent}
     * @return An event that can be executed with this method. Null if no event is applicable
     */
    public static String fetchEvent(ITestResult in_testResult, boolean in_inStart) {
        Method in_methodWithEventAnnotation = in_testResult.getMethod().getConstructorOrMethod().getMethod();

        if (PhasedTestManager.isPhasedTestSingleMode(in_methodWithEventAnnotation)) {
            if (in_methodWithEventAnnotation.isAnnotationPresent(PhaseEvent.class)) {

                if (in_methodWithEventAnnotation.getDeclaredAnnotation(PhaseEvent.class).eventClasses().length > 0) {
                    return in_methodWithEventAnnotation.getDeclaredAnnotation(PhaseEvent.class).eventClasses()[0];
                } else {
                    return ConfigValueHandler.EVENTS_NONINTERRUPTIVE.fetchValue();
                }
            }

            return null;
        } else {
            int l_incrementValue = in_inStart ? 1 : 0;
            //Use Phase Context instead
            String l_currentShuffleGroup = in_testResult.getParameters()[0].toString();

            int l_currentShuffleGroupNr = PhasedTestManager.asynchronousExtractIndex(in_testResult);

            String l_currentScenario = PhasedTestManager.fetchScenarioName(in_testResult);
            int l_currentStep = PhasedTestManager.getScenarioContext().containsKey(l_currentScenario) ? PhasedTestManager.getScenarioContext()
                    .get(l_currentScenario).getStepNr() : 0;

            if (l_currentStep + l_incrementValue == l_currentShuffleGroupNr) {
                return ConfigValueHandler.EVENTS_NONINTERRUPTIVE.fetchValue();
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
