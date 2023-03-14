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

import com.adobe.campaign.tests.integro.phased.data.PhasedTestShuffledWithoutCanShuffle;
import com.adobe.campaign.tests.integro.phased.data.PhasedTestShuffledWithoutCanShuffleNested;
import com.adobe.campaign.tests.integro.phased.data.events.*;
import com.adobe.campaign.tests.integro.phased.utils.*;
import org.hamcrest.Matchers;
import org.testng.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertThrows;

public class TestPhasedNonInterruptive {
    @BeforeMethod
    @AfterMethod
    public void resetVariables() {

        ConfigValueHandler.resetAllValues();

        PhasedEventManager.resetEvents();

        PhasedTestManager.clearCache();

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.deactivateTestSelectionByProducerMode();

        PhasedTestManager.MergedReportData.resetReport();


        //Delete standard cache file
        File l_importCacheFile = new File(
                GeneralTestUtils.fetchCacheDirectory(PhasedTestManager.STD_STORE_DIR),
                PhasedTestManager.STD_STORE_FILE);

        if (l_importCacheFile.exists()) {
            l_importCacheFile.delete();
        }

        PhasedTestManager.MergedReportData.configureMergedReportName(new LinkedHashSet<>(),
                new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.DATA_PROVIDERS, PhasedReportElements.PHASE)));

        //PhasedEventManager.stopEventManager();
    }

    @Test(description = "A test to check that our non-interruptive event works as expected")
    public void testNonInterruptiveEventHelloWorld() {

        //Start event.
        NonInterruptiveEvent nie = new MyNonInterruptiveEvent();
        Date start = new Date();
        assertThat("We should successfully start the event", nie.startEvent());

        //Check that it is still running
        assertThat("The event should be currently on-going", !nie.isFinished());

        //Stop event
        assertThat("The event should no longer be on-going", nie.waitTillFinished());
        Date finish = new Date();

        //Make sure event is stopped
        assertThat("The event should still be stopped now", nie.isFinished());

        long executionWait = MyNonInterruptiveEvent.WAIT_TIME_MS;
        assertThat("The duration should be more than a second", (finish.getTime() - start.getTime()),
                greaterThanOrEqualTo(executionWait));
        assertThat("The duration should be less than 2 seconds", (finish.getTime() - start.getTime()),
                lessThan(executionWait + 100));

    }

    @Test(description = "A test to check that our non-interruptive event works as expected when instantiated as a string")
    public void testNonInterruptiveEventHelloWorld_asString()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        //Start event.
        NonInterruptiveEvent nie = (NonInterruptiveEvent) Class.forName(MyNonInterruptiveEvent.class.getTypeName())
                .newInstance();
        Date start = new Date();
        assertThat("We should successfully start the event", nie.startEvent());

        //Check that it is still running
        assertThat("The event should be currently on-going", !nie.isFinished());
        // assertThat("Make sure that this call is logged", nie.callList, Matchers.contains("testNonInterruptiveEventHelloWorld"));

        //Stop event
        assertThat("The event should no longer be on-going", nie.waitTillFinished());
        Date finish = new Date();

        long executionWait = (long) MyNonInterruptiveEvent.WAIT_TIME_MS;
        //Make sure event is stopped
        assertThat("The event should still be stopped now", nie.isFinished());

        assertThat("The duration should be more than a second", (finish.getTime() - start.getTime()),
                greaterThanOrEqualTo(executionWait));
        assertThat("The duration should be less than 2 seconds", (finish.getTime() - start.getTime()),
                lessThan(executionWait + 100));
    }

    @Test
    public void eventManagerTests() {
        String myEvent = MyNonInterruptiveEvent.class.getTypeName();
        assertThat("In the beginning executor should be null", PhasedEventManager.getEventExecutor(),nullValue());

        NonInterruptiveEvent nie = PhasedEventManager.startEvent(myEvent, "B");
        Date start = new Date();
        assertThat("Our event should be started", nie.getState().equals(NonInterruptiveEvent.states.STARTED));
        assertThat("We should have stored an event object", PhasedEventManager.getEvents().size(), equalTo(1));
        assertThat("We should have stored an event object", PhasedEventManager.getEvents().get("B"), notNullValue());
        assertThat("We should have stored our event object", PhasedEventManager.getEvents().get("B"), equalTo(nie));


        assertThat("There should be an event logged which is between the current and after dates",
                PhasedEventManager.getEventLogs().size(), equalTo(1));


        assertThat("The event should be currently on-going", !nie.isFinished());

        //Stop event
        NonInterruptiveEvent nieEND = PhasedEventManager.finishEvent(myEvent, "B");
        assertThat("The event should no longer be on-going", nie, Matchers.equalTo(nieEND));
        Date finish = new Date();

        assertThat("The event should still be stopped now", nieEND.isFinished());

        assertThat("The duration should be more than a second", (finish.getTime() - start.getTime()),
                greaterThan(450l));
        assertThat("The duration should be less than 2 seconds", (finish.getTime() - start.getTime()), lessThan(600l));
        assertThat("Our event should be finished", nie.getState().equals(NonInterruptiveEvent.states.FINISHED));

        assertThat("In the end executor should no longer be null (lazily instantiated)", PhasedEventManager.getEventExecutor(),notNullValue());

        //PhasedEventManager.stopEventManager();
    }

    @Test(description = "We start problematic instances")
    public void eventManagerTestsStart_negative() {

        String l_stepName = "stepA";

        //Stop event
        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedEventManager.startEvent("NonExistingEvent", l_stepName));

        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedEventManager.startEvent(NonInterruptiveEvent.class.getTypeName(), l_stepName));

        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedEventManager.startEvent(NI_Event3.class.getTypeName(), l_stepName));

        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedEventManager.startEvent(NI_Event1.class.getTypeName(), l_stepName));

    }

    @Test(description = "We finish an event for a step that has not been started")
    public void eventManagerTestsEnd_negative() {

        String myEvent = MyNonInterruptiveEvent.class.getTypeName();

        //Stop event
        assertThrows(PhasedTestException.class, () -> PhasedEventManager.finishEvent(myEvent, "stepB"));
    }

    @Test(description = "In this example, we finish the wrong event for our step")
    public void eventManagerTestsEnd_negativeBadEvent() {
        String l_stepName = "stepA";

        assertThrows(PhasedTestException.class,
                () -> PhasedEventManager.finishEvent(NI_Event2.class.getTypeName(), l_stepName));

        PhasedEventManager.events.put(l_stepName,new MyNonInterruptiveEvent());
        assertThrows(PhasedTestConfigurationException.class,
                () -> PhasedEventManager.finishEvent("NonExistantClass", l_stepName));
    }

    @Test(description = "Testing that we can extract the event for a step")
    public void testExtractEvent_SINGLE() throws NoSuchMethodException {
        //Case 1 Event Declared on PhasedEvent annotation
        Method l_methodWithEvent = TestSINGLEWithEvent_eventAsAnnotation.class.getMethod("step2", String.class);
        ITestResult l_itr1 = MockTestTools.generateTestResultMock(l_methodWithEvent, new Object[]{"D"});

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEvent), equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        //Case 2 Event Declared as Exec property
        Method l_methodWithNoEventSet = TestSINGLEWithEvent_eventAsExecProperty.class.getMethod("step2", String.class);
        ITestResult l_itr2 = MockTestTools.generateTestResultMock(l_methodWithNoEventSet, new Object[]{"D"});
        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchApplicableEvent(l_methodWithNoEventSet), nullValue());

        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent2.class.getTypeName());
        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr2),
                equalTo(MyNonInterruptiveEvent2.class.getTypeName()));

        //Case 3  Event Declared on PhasedTest
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.reset();
        Method l_methodWithEventDefinedOnPhasedTest = TestSINGLEWithEvent_eventConfiguredOnPhasedTestAnnotation.class.getMethod("step2", String.class);
        ITestResult l_itr3 = MockTestTools.generateTestResultMock(l_methodWithEventDefinedOnPhasedTest, new Object[]{"D"});

        assertThat("We should correctly extract the event from the Phased Test annotation",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEventDefinedOnPhasedTest), equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr3),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));

    }


    @Test(description = "Testing that events respect the set precedence order")
    public void testExtractEvent_SINGLE_precendenceTests() throws NoSuchMethodException {
        //Case 1   Event declared on PhasedEvent and on Exec Property
        Method l_methodWithEvent = TestSINGLEWithEvent_eventAsAnnotation.class.getMethod("step2", String.class);
        ITestResult l_itr1 = MockTestTools.generateTestResultMock(l_methodWithEvent, new Object[]{"D"});

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEvent), equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent2.class.getTypeName());

        assertThat("The Event annotation should have presedence here",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEvent), equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        //Case 2  Event declared on PhasedEvent and on PhasedTest
        Method l_methodWithEventOnBothPhasedEventAndPhasedTest = TestSINGLEWithEvent_eventAsAnnotationOnTwoLevels.class.getMethod("step2", String.class);
        ITestResult l_itr2 = MockTestTools.generateTestResultMock(l_methodWithEventOnBothPhasedEventAndPhasedTest, new Object[]{"D"});

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr2),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        //Case 3
        Method l_methodWithEventDefinedOnPhasedTest = TestSINGLEWithEvent_eventConfiguredOnPhasedTestAnnotation.class.getMethod("step2", String.class);
        ITestResult l_itr3 = MockTestTools.generateTestResultMock(l_methodWithEventDefinedOnPhasedTest, new Object[]{"D"});

        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent2.class.getTypeName());

        assertThat("We should correctly extract the event from the Phased Test annotation",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEventDefinedOnPhasedTest), equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr3),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));

    }

    @Test(description = "Testing that we correctly return null")
    public void testExtractEvent_SINGLE_Negative() throws NoSuchMethodException {
        Method l_methodWithEvent = TestSINGLEWithEvent_eventAsAnnotation.class.getMethod("step1", String.class);

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEvent), nullValue());

        Method l_methodWithNoEventSet = TestSINGLEWithEvent_eventAsExecProperty.class.getMethod("step2", String.class);
        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchApplicableEvent(l_methodWithNoEventSet), nullValue());

    }

    @Test(description = "Testing that we can extract the event for a step")
    public void testExtractEvent_SUFFLED() throws NoSuchMethodException {
        Method l_methodWithEvent = TestShuffled_eventPassedAsExecutionVariable.class.getMethod("step1", String.class);
        String l_phaseGroup = PhasedTestManager.STD_PHASED_GROUP_NIE_PREFIX + "1";
        ITestResult l_itr1 = MockTestTools.generateTestResultMock(l_methodWithEvent, new Object[]{ l_phaseGroup });

        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());

        PhasedTestManager.getMethodMap().put(ClassPathParser.fetchFullName(l_methodWithEvent), new MethodMapping(TestShuffled_eventPassedAsExecutionVariable.class, 3, 3, 1));

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr1),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        PhasedTestManager.scenarioStateStore(l_itr1);
        PhasedTestManager.storePhasedContext(ClassPathParser.fetchFullName(l_methodWithEvent), l_phaseGroup);

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr1),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));
    }

    @Test(description = "Testing the hierarchy of the event definitions on the shuffled mode")
    public void testExtractEvent_SUFFLED_definitionOnAnnotation() throws NoSuchMethodException {
        Method l_methodWithEvent = TestShuffled_eventDefinedOnPhasedTestAnnotation.class.getMethod("step2", String.class);
        String l_phaseGroup = PhasedTestManager.STD_PHASED_GROUP_NIE_PREFIX + "1";
        ITestResult l_itr1 = MockTestTools.generateTestResultMock(l_methodWithEvent, new Object[]{ l_phaseGroup });

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEvent),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent2.class.getTypeName());

        assertThat("The vent should not change since we have specified it on the Phased Test",
                PhasedEventManager.fetchApplicableEvent(l_methodWithEvent),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));

        PhasedTestManager.getMethodMap().put(ClassPathParser.fetchFullName(l_methodWithEvent), new MethodMapping(TestShuffled_eventDefinedOnPhasedTestAnnotation.class, 3, 3, 1));

        assertThat("We should correctly extract the event from the method",
                PhasedEventManager.fetchEvent(l_itr1),
                equalTo(MyNonInterruptiveEvent.class.getTypeName()));
    }


    @Test
    public void testFetchEventInformation() throws NoSuchMethodException {
        final Class<TestSINGLEWithEvent_eventAsAnnotation> l_testClass = TestSINGLEWithEvent_eventAsAnnotation.class;
        Method l_myEventMethod = l_testClass.getMethod("step2", String.class);

        assertThat("Step2 should have the Event annotation", l_myEventMethod.isAnnotationPresent(PhaseEvent.class));
        assertThat("Step2 should have the Event annotation event with an empty class list",
                l_myEventMethod.getDeclaredAnnotation(PhaseEvent.class).eventClasses().length, Matchers.equalTo(1));
        assertThat("Step2 should have the Event annotation event with an empty class list",
                l_myEventMethod.getDeclaredAnnotation(PhaseEvent.class).eventClasses()[0],
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
    }

    /**
     * In this example we pass an event as a system property. The class has an event
     */
    @Test
    public void testNonInterruptive_ParellelHardCoded_SINGLE()  {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestSINGLEWithEvent_eventAsAnnotation> l_testClass = TestSINGLEWithEvent_eventAsAnnotation.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        myTestNG.run();

        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(), equalTo(Phases.ASYNCHRONOUS));
        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(),
                not(equalTo(Phases.NON_PHASED)));

        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have 2 events in the logs", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(2));
        PhasedEventLogEntry l_eventStartLog = PhasedEventManager.getEventLogs().get(0);
        assertThat("The first element should be an event defined in step2", l_eventStartLog.getEventMode(),
                Matchers.equalTo(
                        PhasedEventManager.EventMode.START));

        assertThat("The first element should be an event defined in step2", l_eventStartLog.getEventName(),
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The first element should be an event started by step2", l_eventStartLog.getPhasedStepName(),
                Matchers.equalTo(
                        "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsAnnotation.step2(phased-singleRun)"));

        ITestResult l_testSubjectedToEvent = tla.getPassedTests().stream().filter(t -> t.getName().equals("step2"))
                .collect(Collectors.toList()).get(0);

        PhasedEventLogEntry l_eventEndLog = PhasedEventManager.getEventLogs().get(1);
        assertThat("The end of our event should be after the test's start", l_eventEndLog.getEventDate().getTime(),
                Matchers.greaterThanOrEqualTo(l_testSubjectedToEvent.getStartMillis()));
        assertThat("The second element should be an event defined in step2", l_eventEndLog.getEventMode(),
                Matchers.equalTo(
                        PhasedEventManager.EventMode.END));
        assertThat("The second element should be an event defined in step2", l_eventEndLog.getEventName(),
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The second element should be an event started by step2", l_eventEndLog.getPhasedStepName(),
                Matchers.equalTo(
                        "com.adobe.campaign.tests.integro.phased.data.events.TestSINGLEWithEvent_eventAsAnnotation.step2(phased-singleRun)"));

        assertThat("Our test should have started before the end of the event", l_testSubjectedToEvent.getStartMillis(),
                lessThan(l_eventEndLog.getEventDate().getTime()));

    }

    /**
     * In this example we pass an event as a system property. The class has an event
     *
     */
    @Test
    public void testNonInterruptive_ParellelConfigured_SINGLELegacy()  {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestSINGLEWithEvent_eventAsExecProperty> l_testClass = TestSINGLEWithEvent_eventAsExecProperty.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());
        myTestNG.run();

        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(), equalTo(Phases.ASYNCHRONOUS));
        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(),
                not(equalTo(Phases.NON_PHASED)));

        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have 2 events in the logs", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(2));
        PhasedEventLogEntry l_eventStartLog = PhasedEventManager.getEventLogs().get(0);
        assertThat("The first element should be an event defined in step2", l_eventStartLog.getEventMode(),
                Matchers.equalTo(
                        PhasedEventManager.EventMode.START));

        assertThat("The first element should be an event defined in step2", l_eventStartLog.getEventName(),
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The first element should be an event started by step2", l_eventStartLog.getPhasedStepName(),
                Matchers.equalTo(
                        l_testClass.getTypeName() + ".step2(phased-singleRun)"));

        ITestResult l_testSubjectedToEvent = tla.getPassedTests().stream().filter(t -> t.getName().equals("step2"))
                .collect(Collectors.toList()).get(0);

        PhasedEventLogEntry l_eventEndLog = PhasedEventManager.getEventLogs().get(1);
        assertThat("The end of our event should be after the test's start", l_eventEndLog.getEventDate().getTime(),
                Matchers.greaterThanOrEqualTo(l_testSubjectedToEvent.getStartMillis()));
        assertThat("The second element should be an event defined in step2", l_eventEndLog.getEventMode(),
                Matchers.equalTo(
                        PhasedEventManager.EventMode.END));
        assertThat("The second element should be an event defined in step2", l_eventEndLog.getEventName(),
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The second element should be an event started by step2", l_eventEndLog.getPhasedStepName(),
                Matchers.equalTo(
                        l_testClass.getTypeName() + ".step2(phased-singleRun)"));

        assertThat("Our test should have started before the end of the event", l_testSubjectedToEvent.getStartMillis(),
                lessThan(l_eventEndLog.getEventDate().getTime()));

    }


    /**
     * In this example we pass an event as a system property. The class has an event
     *
     */
    @Test
    public void testNonInterruptive_ParellelConfigured_SINGLE()  {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestSINGLEWithEvent_eventAsExecProperty> l_testClass = TestSINGLEWithEvent_eventAsExecProperty.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());
        myTestNG.run();

        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(), equalTo(Phases.ASYNCHRONOUS));
        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(),
                not(equalTo(Phases.NON_PHASED)));

        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have 2 events in the logs", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(2));
        PhasedEventLogEntry l_eventStartLog = PhasedEventManager.getEventLogs().get(0);
        assertThat("The first element should be an event defined in step2", l_eventStartLog.getEventMode(),
                Matchers.equalTo(
                        PhasedEventManager.EventMode.START));

        assertThat("The first element should be an event defined in step2", l_eventStartLog.getEventName(),
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The first element should be an event started by step2", l_eventStartLog.getPhasedStepName(),
                Matchers.equalTo(
                        l_testClass.getTypeName() + ".step2(phased-singleRun)"));

        ITestResult l_testSubjectedToEvent = tla.getPassedTests().stream().filter(t -> t.getName().equals("step2"))
                .collect(Collectors.toList()).get(0);

        PhasedEventLogEntry l_eventEndLog = PhasedEventManager.getEventLogs().get(1);
        assertThat("The end of our event should be after the test's start", l_eventEndLog.getEventDate().getTime(),
                Matchers.greaterThanOrEqualTo(l_testSubjectedToEvent.getStartMillis()));
        assertThat("The second element should be an event defined in step2", l_eventEndLog.getEventMode(),
                Matchers.equalTo(
                        PhasedEventManager.EventMode.END));
        assertThat("The second element should be an event defined in step2", l_eventEndLog.getEventName(),
                Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The second element should be an event started by step2", l_eventEndLog.getPhasedStepName(),
                Matchers.equalTo(
                        l_testClass.getTypeName() + ".step2(phased-singleRun)"));

        assertThat("Our test should have started before the end of the event", l_testSubjectedToEvent.getStartMillis(),
                lessThan(l_eventEndLog.getEventDate().getTime()));

    }

    /**
     * This is a test for non-intyerruptive events in shuffled classes. Using the legacy annotations
     */
    @Test
    public void testNonInterruptive_ParellelConfiguredAsExecutionVariable_ShuffledLegacy() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestShuffled_eventPassedAsExecutionVariable> l_testClass = TestShuffled_eventPassedAsExecutionVariable.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());

        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled", PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        assertThat("We should have 9 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(9)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));
    }

    /**
     * This is a test for non-intyerruptive events in shuffled classes. Using the legacy annotations
     */
    @Test
    public void testNonInterruptive_ParellelConfiguredAsExecutionVariable_Shuffled() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<PhasedTestShuffledWithoutCanShuffleNested.PhasedTestShuffledWithoutCanShuffleNestedInner> l_testClass = PhasedTestShuffledWithoutCanShuffleNested.PhasedTestShuffledWithoutCanShuffleNestedInner.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());

        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled", PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        assertThat("We should have 9 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(9)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));
    }


    /**
     * This is a test for non-intyerruptive events in shuffled classes
     */
    @Test
    public void testNonInterruptive_ParellelConfiguredAsExecutionVariable_Shuffled_Ordered() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestOrderedShuffled_eventPassedAsExecutionVariable> l_testClass = TestOrderedShuffled_eventPassedAsExecutionVariable.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());
        ConfigValueHandler.PHASED_TEST_DETECT_ORDER.activate("true");

        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled", PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        assertThat("We should have 9 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(9)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));
    }


    @Test
    public void testNonInterruptive_ParellelConfigured_Shuffled_AfterPhase() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestShuffled_eventConfiguredAfter> l_testClass = TestShuffled_eventConfiguredAfter.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        ConfigValueHandler.EVENTS_NONINTERRUPTIVE.activate(MyNonInterruptiveEvent.class.getTypeName());

        assertThat("The after phase should not yet have been updated", TestShuffled_eventConfiguredAfter.originalValue, Matchers.equalTo(0));
        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled", PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        assertThat("We should have 9 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(9)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));

        assertThat("The after phase should not yet have been updated", TestShuffled_eventConfiguredAfter.originalValue, Matchers.equalTo(1));
    }


    /**
     * Testing that the event is declared on the phased annotation level
     */
    @Test
    public void testNonInterruptive_ParellelConfiguredOnPhaseTestAnnotation_Shuffled() {

        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestShuffled_eventDefinedOnPhasedTestAnnotation> l_testClass = TestShuffled_eventDefinedOnPhasedTestAnnotation.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();

        myTestNG.run();

        assertThat("We should be in non-interruptive mode shuffled", PhasedTestManager.isPhasedTestShuffledMode(l_testClass));

        assertThat("We should have 9 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(9)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have the correct number of events in the logs (1 x phase groups)", PhasedEventManager.getEventLogs().size(),
                Matchers.equalTo(6));
    }
}
