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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;
import org.testng.annotations.*;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;
import org.testng.internal.annotations.DisabledRetryAnalyzer;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class PhasedTestListener implements ITestListener, IAnnotationTransformer, IAlterSuiteListener {

    protected static Logger log = LogManager.getLogger();

    @Override
    public void alter(List<XmlSuite> suites) {
        // *** Import DataBroker ***
        String l_phasedDataBrokerClass = null;
        if (System.getProperties().containsKey(PhasedTestManager.PROP_PHASED_TEST_DATABROKER)) {
            l_phasedDataBrokerClass = System.getProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        } else if (suites.get(0).getAllParameters()
                .containsKey(PhasedTestManager.PROP_PHASED_TEST_DATABROKER)) {
            l_phasedDataBrokerClass = suites.get(0)
                    .getParameter(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        } else if (!Phases.NON_PHASED.isSelected()) {
            log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX
                    + "No PhasedDataBroker set. Using the file system path " + PhasedTestManager.STD_STORE_DIR
                    + "/" + PhasedTestManager.STD_STORE_FILE + " instead ");
        }

        if (l_phasedDataBrokerClass != null) {
            try {
                PhasedTestManager.setDataBroker(l_phasedDataBrokerClass);
            } catch (PhasedTestConfigurationException e) {
                log.error(PhasedTestManager.PHASED_TEST_LOG_PREFIX
                        + "Errors while setting the PhasedDataBroker", e);
                throw new TestNGException(e);
            }
        }

        // *** import context for consumer ***
        //The second condition is there for testing purposes. You can bypass the file by filling the Test
        if (Phases.CONSUMER.isSelected() && PhasedTestManager.getPhasedCache().isEmpty()) {
            PhasedTestManager.importPhaseData();
        }

        //Inject the phased tests executed in the previous phase
        // This is activated when the test group "PHASED_PRODUCED_TESTS" group
        for (XmlTest lt_xmlTest : suites.get(0).getTests().stream()
                .filter(t -> t.getIncludedGroups().contains(PhasedTestManager.STD_GROUP_SELECT_TESTS_BY_PRODUCER))
                .collect(Collectors.toList())) {

            PhasedTestManager.activateTestSelectionByProducerMode();

            //Attach new classes to suite
            final Set<XmlClass> l_newXMLTests = PhasedTestManager.fetchExecutedPhasedClasses().stream()
                    .map(XmlClass::new).collect(Collectors.toSet());

            //add the original test classes
            l_newXMLTests.addAll(lt_xmlTest.getXmlClasses());
            lt_xmlTest.setXmlClasses(new ArrayList<>(l_newXMLTests));
        }

        //Do we keep this?
        IAlterSuiteListener.super.alter(suites);

    }

    @Override
    public void transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor,
            Method testMethod) {

        if (testMethod != null) {

            //Checking if the Before- and AfterPhase method should be executed.
            if (testMethod.isAnnotationPresent(BeforePhase.class)) {

                log.info("PhasedTestListener : in Before Phase transform");

                //If annotation is not set on the correct TestNG Configuration annotation then throw and excception
                List<Class<?>> l_beforeConfigs = Arrays.asList(BeforeSuite.class, BeforeTest.class,
                        BeforeGroups.class, BeforeClass.class);

                checkAnnotationCompatibility(testMethod, l_beforeConfigs);

                if (Arrays.stream(testMethod.getAnnotation(BeforePhase.class).appliesToPhases())
                        .noneMatch(t -> t.equals(Phases.getCurrentPhase()))) {
                    log.info("Omitting BeforePhase method {}", ClassPathParser.fetchFullName(testMethod));
                    annotation.setEnabled(false);
                }
            }

            if (testMethod.isAnnotationPresent(AfterPhase.class)) {

                log.info("PhasedTestListener : in After Phase transform");

                //If annotation is not set on the correct TestNG Configuration annotation then throw and excception
                List<Class<?>> l_afterConfigs = Arrays.asList(AfterSuite.class, AfterTest.class,
                        AfterGroups.class, AfterClass.class);

                checkAnnotationCompatibility(testMethod, l_afterConfigs);

                if (Arrays.stream(testMethod.getAnnotation(AfterPhase.class).appliesToPhases())
                        .noneMatch(t -> t.equals(Phases.getCurrentPhase()))) {
                    log.info("Omitting AfterPhase method {}", ClassPathParser.fetchFullName(testMethod));

                    annotation.setEnabled(false);
                }
            }
        }
    }

    /**
     * Given a list of expected annotations, this method sees if the given
     * method contains any of these them
     * 
     * Author : gandomi
     *
     * @param in_testMethod
     *        a Test Method
     * @param in_expctedAnnotations
     *        A list of classes, defining which annotations should be attached
     *        to the given method
     *
     */
    protected void checkAnnotationCompatibility(Method in_testMethod, List<Class<?>> in_expctedAnnotations) {
        if (Arrays.stream(in_testMethod.getDeclaredAnnotations())
                .noneMatch(t -> in_expctedAnnotations.contains(t.annotationType()))) {

            Iterator<Annotation> l_declaredAnnotationIterator = Arrays
                    .asList(in_testMethod.getDeclaredAnnotations()).iterator();
            StringBuilder lt_listOfAnnotations = new StringBuilder();

            //Prepare message
            while (l_declaredAnnotationIterator.hasNext()) {
                lt_listOfAnnotations.append(l_declaredAnnotationIterator.next().annotationType().getName());
                lt_listOfAnnotations.append(l_declaredAnnotationIterator.hasNext() ? ", " : "");
            }

            throw new PhasedTestConfigurationException(
                    "You have declared a @BeforePhase or @AfterPhase annotation with an incompatible TestNG Configuration Anntoation. The method "
                            + ClassPathParser.fetchFullName(in_testMethod)
                            + " has the following annotations: " + lt_listOfAnnotations.toString());
        }
    }

    // @Override
    public void onTestStart(ITestResult result) {

        final Method l_method = result.getMethod().getConstructorOrMethod().getMethod();

        //reset context
        if (PhasedTestManager.isPhasedTest(l_method)) {

            //Disable retrying of phased tests
            if (System.getProperty(PhasedTestManager.PROP_DISABLE_RETRY, "true").equalsIgnoreCase("true")) {
                log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "Disabling Retry for phased Tests.");
                result.getMethod().setRetryAnalyzerClass(DisabledRetryAnalyzer.class);
            }

            final String l_dataProvider = PhasedTestManager.concatenateParameterArray(result.getParameters());

            PhasedTestManager.storePhasedContext(ClassPathParser.fetchFullName(l_method), l_dataProvider);

            switch (PhasedTestManager.scenarioStateDecision(result)) {
                case SKIP_PREVIOUS_FAILURE : 
                        final String skipMessageSKIPFAILURE = PhasedTestManager.PHASED_TEST_LOG_PREFIX
                        + "Skipping scenario step " + ClassPathParser.fetchFullName(result)
                        + " due to failure in a previous steps.";
                        log.info(skipMessageSKIPFAILURE);
                        throw new SkipException(skipMessageSKIPFAILURE);
                case SKIP_NORESULT : 
                            final String skipMessageNoResult = PhasedTestManager.PHASED_TEST_LOG_PREFIX
                            + "Skipping scenario step " + ClassPathParser.fetchFullName(result)
                            + " because the previous steps have no been execued.";
                        log.error(skipMessageNoResult);
                        throw new SkipException(skipMessageNoResult);
                default : 
                    //Continue
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        standardPostTestActions(result);

    }

    /**
     * Renames the test result with a new name
     * 
     * Author : gandomi
     *
     * @param in_testResult
     *        A TestNG Result Object
     */
    protected void renameMethodReport(ITestResult in_testResult) {
        String l_newName = PhasedTestManager.fetchTestNameForReport(in_testResult);
        try {
            Field method = TestResult.class.getDeclaredField("m_method");
            method.setAccessible(true);
            method.set(in_testResult, in_testResult.getMethod().clone());
            Field methodName = BaseTestMethod.class.getDeclaredField("m_methodName");
            methodName.setAccessible(true);
            methodName.set(in_testResult.getMethod(), l_newName);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new PhasedTestException(
                    "Error while changing the phased step name " + in_testResult.getName() + ".", e);
        }
    }

    /**
     * This method appends the shuffle group name to the method name
     *
     * Author : gandomi
     *
     * @param result
     *        The TestNG result context
     *
     */
    protected void appendShuffleGroupToName(ITestResult result) {
        String l_enrichedStepName = PhasedTestManager.fetchPhasedStepName(result);
        try {
            Field method = TestResult.class.getDeclaredField("m_method");
            method.setAccessible(true);
            method.set(result, result.getMethod().clone());
            Field methodName = BaseTestMethod.class.getDeclaredField("m_methodName");
            methodName.setAccessible(true);
            methodName.set(result.getMethod(), l_enrichedStepName);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new PhasedTestException(
                    "Error while changing the phased step name " + result.getName() + ".", e);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        standardPostTestActions(result);

    }

    /**
     * This method groups all the post test actions, that are common in all
     * cases
     *
     * Author : gandomi
     *
     * @param result
     *        The TestNG result context
     *
     */
    protected void standardPostTestActions(ITestResult result) {
        if (PhasedTestManager.isPhasedTest(result.getMethod().getConstructorOrMethod().getMethod())) {
            //TRIM add property check
            appendShuffleGroupToName(result);
            PhasedTestManager.scenarioStateStore(result);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        standardPostTestActions(result);

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        standardPostTestActions(result);

    }

    @Override
    public void onStart(ITestContext context) {
        log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "onStart - current Execution State is : "
                + Phases.getCurrentPhase());

        //Creating a method map
        Map<Class, List<String>> l_classMethodMap = new HashMap<>();
        for (ITestNGMethod lt_testNGMethod : context.getSuite().getAllMethods()) {
            Method lt_method = lt_testNGMethod.getConstructorOrMethod().getMethod();

            //Check if the number of method arguments are correct
            final Object[][] lt_currentDataProviders = PhasedTestManager
                    .fetchDataProviderValues(lt_method.getDeclaringClass());

            //The +1 is because of the minimum number of arguments
            final int lt_nrOfExpectedArgments = lt_currentDataProviders.length == 0 ? 1
                    : lt_currentDataProviders[0].length + 1;

            if (PhasedTestManager.isPhasedTest(lt_method)
                    && (lt_nrOfExpectedArgments > lt_method.getParameterCount())) {
                throw new PhasedTestConfigurationException(
                        "The method " + ClassPathParser.fetchFullName(lt_method) + " needs to declare "
                                + lt_nrOfExpectedArgments + " arguments. Instead it has only declared "
                                + lt_method.getParameterCount() + "!");
            }

            if (PhasedTestManager.isPhasedTestShuffledMode(lt_method)) {
                log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "In Shuffled mode : current test "
                        + ClassPathParser.fetchFullName(lt_method));
                if (!l_classMethodMap.containsKey(lt_method.getDeclaringClass())) {
                    l_classMethodMap.put(lt_method.getDeclaringClass(), new ArrayList<>());
                }

                l_classMethodMap.get(lt_method.getDeclaringClass())
                        .add(ClassPathParser.fetchFullName(lt_method));
            }
        }

        if (Phases.getCurrentPhase().hasSplittingEvent()) {
            log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "Generating Phased Providers");
            PhasedTestManager.generatePhasedProviders(l_classMethodMap, Phases.getCurrentPhase());
        }

    }

    @Override
    public void onFinish(ITestContext context) {

        //Once the tests have finished in producer mode we, need to export the data
        if (Phases.PRODUCER.isSelected()) {
            log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "At the end. Exporting data");
            PhasedTestManager.exportPhaseData();
        }

        //Activating merge results if the value is set in the system properties
        if (System.getProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "NOTSET")
                .equalsIgnoreCase("true")) {
            PhasedTestManager.activateMergedReports();
        }

        if (System.getProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS, "NOTSET")
                .equalsIgnoreCase("false")) {
            PhasedTestManager.deactivateMergedReports();
        }

        if (PhasedTestManager.isMergedReportsActivated()) {

            log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX
                    + "Purging results - Keeping one method per test class");

            //Fetch classes That are phased test classes
            Map<String, List<ITestResult>> l_phasedScenarios = new HashMap<String, List<ITestResult>>();

            //Fetch results for Failed tests
            context.getFailedTests().getAllResults().stream().filter(
                    t -> PhasedTestManager.isPhasedTest(t.getMethod().getConstructorOrMethod().getMethod()))
                    .forEach(tr -> updatePhasedScenarios(l_phasedScenarios, tr));

            //Fetch results for Skipped tests
            context.getSkippedTests().getAllResults().stream().filter(
                    t -> PhasedTestManager.isPhasedTest(t.getMethod().getConstructorOrMethod().getMethod()))
                    .forEach(tr -> updatePhasedScenarios(l_phasedScenarios, tr));

            //Fetch results for Passed tests
            context.getPassedTests().getAllResults().stream().filter(
                    t -> PhasedTestManager.isPhasedTest(t.getMethod().getConstructorOrMethod().getMethod()))
                    .forEach(tr -> updatePhasedScenarios(l_phasedScenarios, tr));

            for (String lt_phasedClass : l_phasedScenarios.keySet()) {
                log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "Reducing Report for " + lt_phasedClass);

                long lt_durationMillis = PhasedTestManager
                        .fetchDurationMillis(l_phasedScenarios.get(lt_phasedClass));

                //When the phase test scenario was not a success
                if (!PhasedTestManager.getScenarioContext().get(lt_phasedClass)
                        .equals(Boolean.TRUE.toString())) {

                    //Delete all the passed steps : These steps are not remevant if we are merging the step results
                    Iterator<ITestResult> lt_passedTestIterator = context.getPassedTests().getAllResults()
                            .iterator();

                    while (lt_passedTestIterator.hasNext()) {
                        ITestResult lt_currentSuccess = lt_passedTestIterator.next();

                        if (PhasedTestManager.fetchScenarioName(lt_currentSuccess).equals(lt_phasedClass)) {
                            lt_passedTestIterator.remove();
                        }
                    }

                    //Removing Skipped Tests
                    //Keep 1 IFF all tests were skipped
                    Iterator<ITestResult> lt_skippedTestIterator = context.getSkippedTests().getAllResults()
                            .iterator();

                    boolean l_allSkipped = l_phasedScenarios.get(lt_phasedClass).stream()
                            .allMatch(p -> (p.getStatus() == ITestResult.SKIP));

                    log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "SKIP Check : " + lt_phasedClass
                            + " has all its results as skipped : " + l_allSkipped);

                    boolean l_foundSkipped = false;

                    while (lt_skippedTestIterator.hasNext()) {
                        ITestResult lt_currentSkip = lt_skippedTestIterator.next();

                        if (PhasedTestManager.fetchScenarioName(lt_currentSkip).equals(lt_phasedClass)) {

                            if (!l_allSkipped) {
                                log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "Removing "
                                        + ClassPathParser.fetchFullName(lt_currentSkip)
                                        + " because there are unskipped values.");
                                lt_skippedTestIterator.remove();
                            } else {
                                if (l_foundSkipped) {
                                    lt_skippedTestIterator.remove();
                                } else {
                                    log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "Keeping "
                                            + ClassPathParser.fetchFullName(lt_currentSkip)
                                            + " because when all results are skipped we keep only the first one..");

                                    l_foundSkipped = true;

                                    lt_currentSkip.setEndMillis(
                                            lt_currentSkip.getStartMillis() + lt_durationMillis);

                                    renameMethodReport(lt_currentSkip);
                                }
                            }
                        }
                    }

                    //Renaming Failed Tests
                    Iterator<ITestResult> lt_failedTestIterator = context.getFailedTests().getAllResults()
                            .iterator();
                    while (lt_failedTestIterator.hasNext()) {
                        ITestResult lt_currentFail = lt_failedTestIterator.next();

                        if (PhasedTestManager.fetchScenarioName(lt_currentFail).equals(lt_phasedClass)) {
                            //Update duration
                            lt_currentFail.setEndMillis(lt_currentFail.getStartMillis() + lt_durationMillis);

                            //Wrap the Exception
                            PhasedTestManager.generateStepFailure(lt_currentFail);

                            //Rename test
                            renameMethodReport(lt_currentFail);
                        }
                    }

                } else {
                    //Removing Passed Tests
                    Iterator<ITestResult> lt_passedTestIterator = context.getPassedTests().getAllResults()
                            .iterator();
                    boolean l_foundPasssed = false;
                    while (lt_passedTestIterator.hasNext()) {
                        ITestResult lt_currentSuccess = lt_passedTestIterator.next();

                        if (PhasedTestManager.fetchScenarioName(lt_currentSuccess).equals(lt_phasedClass)) {
                            if (l_foundPasssed) {
                                lt_passedTestIterator.remove();
                            } else {
                                l_foundPasssed = true;
                                renameMethodReport(lt_currentSuccess);

                                lt_currentSuccess
                                        .setEndMillis(lt_currentSuccess.getStartMillis() + lt_durationMillis);
                            }
                        }
                    }

                }
            }

        }

    }

    /**
     * Updates the map of scenarios. The key is the class name + the phase group
     *
     * Author : gandomi
     *
     * @param in_phasedScenarios
     *        A map of sccenarios that should be updated
     * @param in_testResult
     *        The candidae test result that should be used for updating the map
     *
     */
    protected void updatePhasedScenarios(Map<String, List<ITestResult>> in_phasedScenarios,
            ITestResult in_testResult) {
        String lt_scName = PhasedTestManager.fetchScenarioName(in_testResult);

        //Inilialize id map doe not have the given entry
        if (!in_phasedScenarios.containsKey(lt_scName)) {
            in_phasedScenarios.put(lt_scName, new ArrayList<ITestResult>());
        }

        in_phasedScenarios.get(lt_scName).add(in_testResult);
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
            Method testMethod) {

        if (testClass != null) {

            //inject the
            if (PhasedTestManager.isTestsSelectedByProducerMode() && PhasedTestManager.fetchExecutedPhasedClasses().contains(testClass.getTypeName())) {

                //Create new group array
                Set<String> l_newArrayString = new HashSet<String>();
                Arrays.stream(annotation.getGroups()).forEach(i -> l_newArrayString.add(i));
                l_newArrayString.add(PhasedTestManager.STD_GROUP_SELECT_TESTS_BY_PRODUCER);
                String[] l_newGroupArray = new String[l_newArrayString.size()];
                annotation.setGroups(l_newArrayString.toArray(l_newGroupArray));
            }

            if (PhasedTestManager.isPhasedTestShuffledMode(testClass)) {
                annotation.setDataProvider(PhasedDataProvider.SHUFFLED);
                annotation.setDataProviderClass(PhasedDataProvider.class);

            }

            if (PhasedTestManager.isPhasedTestSingleMode(testClass)) {
                annotation.setDataProvider(PhasedDataProvider.SINGLE);
                annotation.setDataProviderClass(PhasedDataProvider.class);
            }
        }

        //Managing Phased tests on method level
        if (testMethod != null) {

            if (PhasedTestManager.isPhasedTestShuffledMode(testMethod)) {
                annotation.setDataProvider(PhasedDataProvider.SHUFFLED);
                annotation.setDataProviderClass(PhasedDataProvider.class);

            }

            if (PhasedTestManager.isPhasedTestSingleMode(testMethod)) {
                annotation.setDataProvider(PhasedDataProvider.SINGLE);
                annotation.setDataProviderClass(PhasedDataProvider.class);
            }
        }

    }

}
