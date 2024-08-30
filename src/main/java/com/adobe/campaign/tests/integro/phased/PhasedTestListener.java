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

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestException;
import com.adobe.campaign.tests.integro.phased.internal.PhaseProcessorFactory;
import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencies;
import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencyFactory;
import com.adobe.campaign.tests.integro.phased.permutational.StepDependencies;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;
import org.testng.internal.annotations.DisabledRetryAnalyzer;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhasedTestListener
        implements ITestListener, IAnnotationTransformer, IAlterSuiteListener, IMethodInterceptor {

    protected static Logger log = LogManager.getLogger();
    private static final BiPredicate<ITestResult, String> SCENARIO_NAME_MATCHER = (itr, clazzName) ->
            PhasedTestManager.fetchScenarioName(itr).equals(clazzName);
    private static final Function<ITestResult, Method> METHOD_EXTRACTOR = itr ->
            itr.getMethod().getConstructorOrMethod().getMethod();

    @Override
    public void alter(List<XmlSuite> suites) {
        log.debug("{} in alter - current Execution State is : {}", PhasedTestManager.PHASED_TEST_LOG_PREFIX
                , Phases.getCurrentPhase());

        // *** Import DataBroker ***
        String l_phasedDataBrokerClass = null;
        if (ConfigValueHandlerPhased.PROP_PHASED_TEST_DATABROKER.isSet()) {
            l_phasedDataBrokerClass = ConfigValueHandlerPhased.PROP_PHASED_TEST_DATABROKER.fetchValue();
        } else if (suites.get(0).getAllParameters()
                .containsKey(ConfigValueHandlerPhased.PROP_PHASED_TEST_DATABROKER.systemName)) {
            l_phasedDataBrokerClass = suites.get(0)
                    .getParameter(ConfigValueHandlerPhased.PROP_PHASED_TEST_DATABROKER.systemName);
        } else if (!Phases.NON_PHASED.isSelected()) {
            log.info("{} No PhasedDataBroker set. Using the file system path {}/{} instead ",
                    PhasedTestManager.PHASED_TEST_LOG_PREFIX, PhasedTestManager.STD_STORE_DIR,
                    PhasedTestManager.STD_STORE_FILE
            );
        }

        if (l_phasedDataBrokerClass != null) {
            try {
                PhasedTestManager.setDataBroker(l_phasedDataBrokerClass);
            } catch (PhasedTestConfigurationException e) {
                log.error("{} Errors while setting the PhasedDataBroker", PhasedTestManager.PHASED_TEST_LOG_PREFIX, e);
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
    }

    @Override
    public void transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor,
            Method testMethod) {
        Optional.ofNullable(testMethod)
                .ifPresent(tm -> {
                    boolean result = PhaseProcessorFactory.getProcessor(tm).canProcessPhase();
                    annotation.setEnabled(result);
                });
    }

    // @Override
    public void onTestStart(ITestResult result) {

        final Method l_method = result.getMethod().getConstructorOrMethod().getMethod();

        //reset context

        if (PhasedTestManager.isPhasedTest(l_method)) {

            //Cases 1,2,4,5
            final String l_dataProvider = PhasedTestManager.concatenateParameterArray(result.getParameters());

            PhasedTestManager.storePhasedContext(ClassPathParser.fetchFullName(l_method), l_dataProvider);

            switch (PhasedTestManager.scenarioStateDecision(result)) {
            case SKIP_PREVIOUS_FAILURE:
                final String skipMessageSKIPFAILURE = PhasedTestManager.PHASED_TEST_LOG_PREFIX
                        + "Skipping scenario step " + ClassPathParser.fetchFullName(result)
                        + " due to failure in step " + PhasedTestManager.getScenarioContext()
                        .get(PhasedTestManager.fetchScenarioName(result)).getFailedStep() + " in Phase "
                        + PhasedTestManager.getScenarioContext()
                        .get(PhasedTestManager.fetchScenarioName(result)).getFailedInPhase().name();

                log.info(skipMessageSKIPFAILURE);
                result.setStatus(ITestResult.SKIP);
                result.setThrowable(new PhasedStepFailure(skipMessageSKIPFAILURE));
                break;
            case SKIP_NORESULT:
                final String skipMessageNoResult = PhasedTestManager.PHASED_TEST_LOG_PREFIX
                        + "Skipping scenario step " + ClassPathParser.fetchFullName(result)
                        + " because the previous steps have not been executed in the previous phase.";
                log.error(skipMessageNoResult);
                result.setStatus(ITestResult.SKIP);
                result.setThrowable(new PhasedStepFailure(skipMessageNoResult));
                break;
            case CONFIG_FAILURE:
            default:
                //Continue
            }

            //Managing events
            //Cases 4 & 5
            if (Phases.ASYNCHRONOUS.isSelected()) {

                //Check if there is an event declared
                String lt_event = PhasedEventManager.fetchEvent(result);
                if (lt_event != null) {
                    //TODO use PhasedTestManager for fetching full name instead
                    PhasedEventManager.startEvent(lt_event, ClassPathParser.fetchFullName(result));
                }
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        standardPostTestActions(result);

    }

    /**
     * Renames the test result with a new name
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestNG Result Object
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
            log.error("Error while changing the phased step name {}.", in_testResult.getName(), e);
            throw new PhasedTestException(
                    "Error while changing the phased step name " + in_testResult.getName() + ".", e);
        }
    }

    /**
     * This method appends the shuffle group name to the method name
     * <p>
     * Author : gandomi
     *
     * @param result The TestNG result context
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
            log.error("Error while changing the phased step name {}.", result.getName(), e);
            throw new PhasedTestException(
                    "Error while changing the phased step name " + result.getName() + ".", e);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        standardPostTestActions(result);

    }

    /**
     * This method groups all the post test actions, that are common in all cases
     * <p>
     * Author : gandomi
     *
     * @param result The TestNG result context
     */
    protected void standardPostTestActions(ITestResult result) {
        final Method l_method = result.getMethod().getConstructorOrMethod().getMethod();

        if (PhasedTestManager.isPhasedTest(l_method)) {
            //TRIM add property check
            appendShuffleGroupToName(result);
            PhasedTestManager.scenarioStateStore(result);

            //Cases 4 & 5
            if (Phases.ASYNCHRONOUS.isSelected()) {
                PhasedTestManager.getPhasedCache();
                //Check if there is an event declared
                String lt_event = PhasedEventManager.fetchEvent(result);
                if (lt_event != null) {
                    //TODO use PhasedTestManager for fetching full name instead
                    PhasedEventManager.finishEvent(lt_event, ClassPathParser.fetchFullName(result));
                }
            }
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
        log.debug("{} onStart - current Execution State is : {}.",
                PhasedTestManager.PHASED_TEST_LOG_PREFIX, Phases.getCurrentPhase());
    }

    @Override
    public void onFinish(ITestContext context) {

        //Once the tests have finished in producer mode we, need to export the data
        if (Phases.PRODUCER.isSelected()) {
            log.info("{} At the end. Exporting data", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            PhasedTestManager.exportPhaseData();
        }

        PhasedTestManager.applyMergeReportChoice();

        boolean isInactive = !PhasedTestManager.isMergedReportsActivated();
        if (isInactive) {
            return;
        }

        log.debug("{} Purging results - Keeping one method per test class",
                PhasedTestManager.PHASED_TEST_LOG_PREFIX);

        //Fetch classes That are phased test classes
        Map<String, List<ITestResult>> l_phasedScenarios =
                mergedStreamOfAllResults(context)
                        .filter(t -> PhasedTestManager.isPhasedTest(METHOD_EXTRACTOR.apply(t)))
                        .collect(Collectors.groupingBy(PhasedTestManager::fetchScenarioName,
                                Collectors.toList()));

        for (Entry<String, List<ITestResult>> each : l_phasedScenarios.entrySet()) {
            log.info("{} Reducing Report for {}",PhasedTestManager.PHASED_TEST_LOG_PREFIX, each);

            //When the phase test scenario was not a success
            if (PhasedTestManager.getScenarioContext().get(each.getKey()).isPassed()) {
                handlePassedPhases(context, each.getKey());
            } else {
                handleFailedPhases(context, each);
            }
        }

        PhasedEventManager.stopEventExecutor();
    }

    private void handlePassedPhases(ITestContext context, String lt_phasedClass) {
        //Removing Passed Tests
        Iterator<ITestResult> lt_passedTestIterator = context.getPassedTests().getAllResults()
                .iterator();
        boolean l_foundPassed = false;
        while (lt_passedTestIterator.hasNext()) {
            ITestResult lt_currentSuccess = lt_passedTestIterator.next();
            boolean proceed = SCENARIO_NAME_MATCHER.test(lt_currentSuccess, lt_phasedClass);
            if (!proceed) {
                continue;
            }

            if (l_foundPassed) {
                lt_passedTestIterator.remove();
                continue;
            }
            l_foundPassed = true;
            renameMethodReport(lt_currentSuccess);

            lt_currentSuccess.setEndMillis(
                    lt_currentSuccess.getStartMillis()
                            + PhasedTestManager.getScenarioContext()
                            .get(PhasedTestManager.fetchScenarioName(lt_currentSuccess))
                            .getDuration());
        }
    }

    private void handleFailedPhases(ITestContext context, Entry<String, List<ITestResult>> entry) {
        //Delete all the passed steps : These steps are not relevant if we are merging the step results

        context.getPassedTests().getAllResults().removeIf(
                lt_currentSuccess -> SCENARIO_NAME_MATCHER.test(lt_currentSuccess, entry.getKey()));

        //Removing Skipped Tests
        //Keep 1 IFF all tests were skipped
        Iterator<ITestResult> lt_skippedTestIterator = context.getSkippedTests().getAllResults()
                .iterator();

        boolean l_allSkipped = entry.getValue().stream()
                .allMatch(p -> (p.getStatus() == ITestResult.SKIP));

        log.debug("{} SKIP Check : {} has all its results as skipped : {}",
                PhasedTestManager.PHASED_TEST_LOG_PREFIX, entry.getKey(), l_allSkipped);

        boolean l_foundSkipped = false;

        while (lt_skippedTestIterator.hasNext()) {
            ITestResult lt_currentSkip = lt_skippedTestIterator.next();

            boolean proceed = SCENARIO_NAME_MATCHER.test(lt_currentSkip, entry.getKey());
            if (!proceed) {
                continue;
            }

            if (!l_allSkipped) {
                log.debug("{} Removing {} because there are un-skipped values.",
                        PhasedTestManager.PHASED_TEST_LOG_PREFIX,
                        ClassPathParser.fetchFullName(lt_currentSkip));
                lt_skippedTestIterator.remove();
                continue;
            }
            if (l_foundSkipped) {
                lt_skippedTestIterator.remove();
                continue;
            }
            log.debug(
                    "{} Keeping {} because when all results are skipped we keep only the first one..",
                    PhasedTestManager.PHASED_TEST_LOG_PREFIX,
                    ClassPathParser.fetchFullName(lt_currentSkip));

            l_foundSkipped = true;

            lt_currentSkip.setEndMillis(
                    lt_currentSkip.getStartMillis()
                            + PhasedTestManager.getScenarioContext()
                            .get(PhasedTestManager.fetchScenarioName(lt_currentSkip))
                            .getDuration());

            renameMethodReport(lt_currentSkip);
        }

        //Renaming Failed Tests
        for (ITestResult lt_currentFail : context.getFailedTests().getAllResults()) {
            boolean proceed = SCENARIO_NAME_MATCHER.test(lt_currentFail, entry.getKey());
            if (!proceed) {
                continue;
            }
            //Update duration
            lt_currentFail.setEndMillis(
                    lt_currentFail.getStartMillis()
                            + PhasedTestManager.getScenarioContext()
                            .get(PhasedTestManager.fetchScenarioName(lt_currentFail))
                            .getDuration());

            //Wrap the Exception
            PhasedTestManager.generateStepFailure(lt_currentFail);

            //Rename test
            renameMethodReport(lt_currentFail);
        }
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
            Method testMethod) {
        if (testClass == null && testMethod==null) {
            return;
        }

        Class l_currentClass = testClass != null ? testClass : testMethod.getDeclaringClass();


        //inject the selector by PRODUCER
        if (PhasedTestManager.isTestsSelectedByProducerMode() && PhasedTestManager.fetchExecutedPhasedClasses()
                .contains(l_currentClass.getTypeName())) {
            //Create new group array
            Set<String> l_newArrayString = new HashSet<>(Arrays.asList(annotation.getGroups()));
            l_newArrayString.add(PhasedTestManager.STD_GROUP_SELECT_TESTS_BY_PRODUCER);
            String[] l_newGroupArray = new String[l_newArrayString.size()];
            annotation.setGroups(l_newArrayString.toArray(l_newGroupArray));
        }

        if (PhasedTestManager.isPhasedTest(l_currentClass)) {
            if (Phases.NON_PHASED.isSelected()) {
                annotation.setDataProvider(
                        ConfigValueHandlerPhased.PHASED_TEST_NONPHASED_LEGACY.is("true") ? PhasedDataProvider.SINGLE : PhasedDataProvider.DEFAULT);

            } else {
                annotation.setDataProvider(PhasedTestManager.isPhasedTestShuffledMode(
                        l_currentClass) ? PhasedDataProvider.SHUFFLED : PhasedDataProvider.SINGLE);
            }

            annotation.setDataProviderClass(PhasedDataProvider.class);
        }
    }


    private static Stream<ITestResult> mergedStreamOfAllResults(ITestContext context) {
        return Stream.concat(context.getFailedTests().getAllResults().stream(),
                Stream.concat(
                        context.getSkippedTests().getAllResults().stream(),
                        context.getPassedTests().getAllResults().stream()
                )
        );
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> list, ITestContext iTestContext) {
        log.debug("In IMethodInterceptor : intercept");

        Map<Class<?>, List<String>> l_classMethodMap = new HashMap<>();
        Set<Class> l_phasedClasses = new HashSet<>();

        if (ConfigValueHandlerPhased.PROP_DISABLE_RETRY.is("true")) {
            log.debug("{} Disabling Retry for phased Tests.", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            list.stream().filter(l -> PhasedTestManager.isPhasedTest(l.getMethod().getRealClass())).forEach(i -> i.getMethod().setRetryAnalyzerClass(DisabledRetryAnalyzer.class));
        }

        for (Method lt_method : list.stream()
                .map(mi -> mi.getMethod().getConstructorOrMethod().getMethod()).filter(PhasedTestManager::isPhasedTest)
                .collect(Collectors.toList())) {
            l_phasedClasses.add(lt_method.getDeclaringClass());
            //Method lt_method = lt_testNGMethod.getConstructorOrMethod().getMethod();

            //Check if the number of method arguments are correct
            final Object[][] lt_currentDataProviders = PhasedTestManager
                    .fetchDataProviderValues(lt_method.getDeclaringClass());

            //The +1 is because of the minimum number of arguments
            final int lt_nrOfExpectedArguments = lt_currentDataProviders.length == 0 ? 1
                    : lt_currentDataProviders[0].length + 1;

            if (lt_nrOfExpectedArguments > lt_method.getParameterCount()) {
                StringBuilder l_errorMsg = new StringBuilder("The method ");
                l_errorMsg.append(ClassPathParser.fetchFullName(lt_method)).append(" needs to declare ")
                        .append(lt_nrOfExpectedArguments).append(" arguments. Instead it has only declared ")
                        .append(lt_method.getParameterCount()).append("!");
                log.error(l_errorMsg.toString());
                throw new PhasedTestConfigurationException(
                        l_errorMsg.toString());
            }

            //Prepare data for shuffle calculation
            //NIA Cases 1 & 5
            if (PhasedTestManager.isPhasedTestShuffledMode(lt_method)) {
                log.debug("{} In Shuffled mode : current test {}", PhasedTestManager.PHASED_TEST_LOG_PREFIX
                        , ClassPathParser.fetchFullName(lt_method));
                if (!l_classMethodMap.containsKey(lt_method.getDeclaringClass())) {
                    l_classMethodMap.put(lt_method.getDeclaringClass(), new ArrayList<>());
                }

                l_classMethodMap.get(lt_method.getDeclaringClass())
                        .add(ClassPathParser.fetchFullName(lt_method));
            }
        }

        //If the property PHASED.TESTS.DETECT.ORDER not set, we follow the standard TestNG order
        if (ConfigValueHandlerPhased.PHASED_TEST_DETECT_ORDER.is("false")) {
            log.info("{} Generating Phased Providers", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            //NIA
            PhasedTestManager.generatePhasedProviders(l_classMethodMap, Phases.getCurrentPhase());
            return list;
        } else {

            //Generate scenario step dependencies
            Map<String, ScenarioStepDependencies> l_scenarioDependencies = l_phasedClasses.stream()
                    .map(ScenarioStepDependencyFactory::listMethodCalls).collect(Collectors.toMap(ScenarioStepDependencies::getScenarioName, Function.identity()));

            //if (Phases.getCurrentPhase().hasSplittingEvent()) {
                log.info("{} Generating Phased Providers", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
                //NIA
                PhasedTestManager.generatePhasedProviders(l_classMethodMap, l_scenarioDependencies,
                        Phases.getCurrentPhase());
            //}

            //Start by adding the non-phased tests
            List<IMethodInstance> lr_nonPhasedMethods = list.stream()
                    .filter(m -> !PhasedTestManager.isPhasedTest(m.getMethod().getConstructorOrMethod().getMethod()))
                    .collect(
                            Collectors.toList());

            //Create list of methods that are phased
            List<IMethodInstance> l_phasedDependencyMethods = list.stream().filter(m -> l_phasedClasses.contains(
                    m.getMethod().getConstructorOrMethod().getDeclaringClass())).collect(
                    Collectors.toList());

            for (ScenarioStepDependencies lt_sd : l_scenarioDependencies.values()) {
                for (StepDependencies lt_methodName : lt_sd.fetchExecutionOrderList()) {
                    l_phasedDependencyMethods.stream()
                            .filter(m -> m.getMethod().getConstructorOrMethod().getName()
                                    .equals(lt_methodName.getStepName())).findFirst().ifPresent(c -> lr_nonPhasedMethods.add(c));
                }
            }

            log.info("Lists before have {} methods. After it is {}", list.size(), lr_nonPhasedMethods.size());

            return lr_nonPhasedMethods;
        }
    }
}
