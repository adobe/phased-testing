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

import com.adobe.campaign.tests.integro.phased.data.events.MyNonInterruptiveEvent;
import com.adobe.campaign.tests.integro.phased.data.events.TestWithEvent_eventAsAnnotation;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.hamcrest.Matchers;
import org.testng.*;
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
    public void resetVariables() {

        PhasedEventManager.resetEvents();

        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_DISABLE_RETRY);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.deactivateTestSelectionByProducerMode();

        PhasedTestManager.MergedReportData.resetReport();

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

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
    }


    @Test(description = "A test to check that our non-interruptive event works as expected")
    public void testNonInterruptiveEventHelloWorld() {

        //Start event.
        NonInterruptiveEvent nie = new MyNonInterruptiveEvent();
        Date start = new Date();
        assertThat("We should successfully start the event", nie.startEvent());


        //Check that it is still running
        assertThat("The event should be currently on-going", !nie.isFinished());
       // assertThat("Make sure that this call is logged", nie.callList, Matchers.contains("testNonInterruptiveEventHelloWorld"));

        //Stop event
        assertThat("The event should no longer be on-going", nie.waitTillFinished());
        Date finish = new Date();

        //Make sure event is stopped
        assertThat("The event should still be stopped now", nie.isFinished());

        assertThat("The duration should be more than a second", (finish.getTime() - start.getTime()), greaterThan(500l) );
        assertThat("The duration should be less than 2 seconds", (finish.getTime() - start.getTime()), lessThan(600l) );
    }

    @Test(description = "A test to check that our non-interruptive event works as expected when instantiated as a string")
    public void testNonInterruptiveEventHelloWorld_asString()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        //Start event.
        NonInterruptiveEvent nie = (NonInterruptiveEvent) Class.forName(MyNonInterruptiveEvent.class.getTypeName()).newInstance();
        Date start = new Date();
        assertThat("We should successfully start the event", nie.startEvent());


        //Check that it is still running
        assertThat("The event should be currently on-going", !nie.isFinished());
        // assertThat("Make sure that this call is logged", nie.callList, Matchers.contains("testNonInterruptiveEventHelloWorld"));

        //Stop event
        assertThat("The event should no longer be on-going", nie.waitTillFinished());
        Date finish = new Date();

        //Make sure event is stopped
        assertThat("The event should still be stopped now", nie.isFinished());

        assertThat("The duration should be more than a second", (finish.getTime() - start.getTime()), greaterThan(500l) );
        assertThat("The duration should be less than 2 seconds", (finish.getTime() - start.getTime()), lessThan(600l) );
    }

    @Test
    public void eventManagerTests() {
        Date l_currentDate = new Date();
        String myEvent = MyNonInterruptiveEvent.class.getTypeName();
        NonInterruptiveEvent nie = PhasedEventManager.startEvent(myEvent, "B");

        assertThat("We should have stored an event object",PhasedEventManager.getEvents().size(), equalTo(1));
        assertThat("We should have stored an event object",PhasedEventManager.getEvents().get("B"), notNullValue());
        assertThat("We should have stored our event object",PhasedEventManager.getEvents().get("B"), equalTo(nie));

        Date start = new Date();
        assertThat("There should be an event logged which is between the current and after dates", PhasedEventManager.getEventLogs().size(), equalTo(1));
        assertThat("The event should be currently on-going", !nie.isFinished());

        //Stop event
        NonInterruptiveEvent nieEND = PhasedEventManager.finishEvent(myEvent, "B");
        assertThat("The event should no longer be on-going", nie, Matchers.equalTo(nieEND));
        Date finish = new Date();

        assertThat("The event should still be stopped now", nieEND.isFinished());

        assertThat("The duration should be more than a second", (finish.getTime() - start.getTime()), greaterThan(500l) );
        assertThat("The duration should be less than 2 seconds", (finish.getTime() - start.getTime()), lessThan(600l) );
    }

    @Test
    public void testFetchEventInformation() throws NoSuchMethodException {
        final Class<TestWithEvent_eventAsAnnotation> l_testClass = TestWithEvent_eventAsAnnotation.class;
        Method l_myEventMethod = l_testClass.getMethod("step2", String.class);

        assertThat("Step2 should have the Event annotation", l_myEventMethod.isAnnotationPresent(PhaseEvent.class));
        assertThat("Step2 should have the Event annotation event with an empty class list", l_myEventMethod.getDeclaredAnnotation(PhaseEvent.class).eventClasses().length, Matchers.equalTo(1));
        assertThat("Step2 should have the Event annotation event with an empty class list", l_myEventMethod.getDeclaredAnnotation(PhaseEvent.class).eventClasses()[0], Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
    }

    /**
     * In this example we pass an event as a system property. The class has an event
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    @Test
    public void testNonInterruptive_Parellel_SHUFFLED() throws NoSuchMethodException, SecurityException {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<TestWithEvent_eventAsAnnotation> l_testClass = TestWithEvent_eventAsAnnotation.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        Phases.ASYNCHRONOUS.activate();
        myTestNG.run();

        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(), equalTo(Phases.ASYNCHRONOUS));
        assertThat("The correct phase must have been selected", Phases.getCurrentPhase(), not(equalTo(Phases.NON_PHASED)));

        assertThat("We should have 3 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        assertThat("We should have 2 events in the logs", PhasedEventManager.getEventLogs().size(), Matchers.equalTo(2));
        PhaseEventLogEntry l_eventStartLog = PhasedEventManager.getEventLogs().get(0);
        assertThat("The first element should be an event defined in step2",l_eventStartLog.getEventMode(), Matchers.equalTo(
                PhasedEventManager.EventMode.START));

        assertThat("The first element should be an event defined in step2",l_eventStartLog.getEventName(), Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The first element should be an event started by step2",l_eventStartLog.getPhasedStepName(), Matchers.equalTo("com.adobe.campaign.tests.integro.phased.data.events.TestWithEvent_eventAsAnnotation.step2(phased-singleRun)"));


        PhaseEventLogEntry l_eventEndLog = PhasedEventManager.getEventLogs().get(1);
        assertThat("The end of our event should be after its start", l_eventStartLog.getEventDate().getTime(), Matchers.greaterThanOrEqualTo(l_eventEndLog.getEventDate().getTime()));
        assertThat("The second element should be an event defined in step2",l_eventEndLog.getEventMode(), Matchers.equalTo(
                PhasedEventManager.EventMode.END));
        assertThat("The second element should be an event defined in step2",l_eventEndLog.getEventName(), Matchers.equalTo(MyNonInterruptiveEvent.class.getTypeName()));
        assertThat("The second element should be an event started by step2",l_eventEndLog.getPhasedStepName(), Matchers.equalTo("com.adobe.campaign.tests.integro.phased.data.events.TestWithEvent_eventAsAnnotation.step2(phased-singleRun)"));

        ITestResult x = tla.getPassedTests().stream().filter(t -> t.getName().equals("step2")).collect(Collectors.toList()).get(0);
//        assertThat("Our event should have started before the test", l_eventStartLog.getEventDate().getTime(), lessThan(x.getStartMillis()));

        assertThat("Our test should have started before the end of the event", x.getStartMillis(), lessThan(l_eventEndLog.getEventDate().getTime()));

    }
}
