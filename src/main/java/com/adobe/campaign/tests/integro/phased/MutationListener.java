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
import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestDefinitionException;
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
import org.testng.internal.annotations.DisabledRetryAnalyzer;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MutationListener
        implements IAnnotationTransformer, IMethodInterceptor, ITestListener, IAlterSuiteListener {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void alter(List<XmlSuite> suites) {
        IAlterSuiteListener.super.alter(suites);

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
                        l_currentClass) ? PhasedDataProvider.MUTATIONAL : PhasedDataProvider.SINGLE);
            }

            annotation.setDataProviderClass(PhasedDataProvider.class);
        }
    }


/*
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> list, ITestContext iTestContext) {
        list.stream().forEach(l -> log.info("Method {}, Class {}, Parent {}", l.getMethod().getMethodName(),
                l.getMethod().getTestClass().getClass().getTypeName(), l.getMethod().getTestClass().getClass().getSuperclass().getTypeName()));

        //list.removeAll();
        return list.stream().filter(l -> l.getMethod().getRealClass().equals(PhasedParent.class)).collect(
                Collectors.toList());
    }

 */


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

            if (!Modifier.isAbstract(lt_method.getDeclaringClass().getModifiers())) {
                l_phasedClasses.add(lt_method.getDeclaringClass());
            }
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
        /*
        if (ConfigValueHandlerPhased.PHASED_TEST_DETECT_ORDER.is("false")) {
            log.info("{} Generating Phased Providers", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            //NIA
            PhasedTestManager.generatePhasedProviders(l_classMethodMap, Phases.getCurrentPhase());
            //return list;
            return list.stream().filter(l -> l.getMethod().getRealClass().equals(PhasedParent.class)).collect(
                    Collectors.toList());
        } else {

         */

            //Generate scenario step dependencies
        PhasedTestManager.setStepDependencies(l_phasedClasses.stream()
                    .map(ScenarioStepDependencyFactory::listMethodCalls).collect(Collectors.toMap(ScenarioStepDependencies::getScenarioName, Function.identity())));


            //if (Phases.getCurrentPhase().hasSplittingEvent()) {
            log.info("{} Generating Phased Providers", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            //NIA
            PhasedTestManager.generatePhasedProviders(l_classMethodMap, PhasedTestManager.getStepDependencies(),
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

            for (ScenarioStepDependencies lt_sd : PhasedTestManager.getStepDependencies().values()) {
                if (!lt_sd.isExecutable()) {
                    throw new PhasedTestDefinitionException("The scenario " + lt_sd.getScenarioName()
                            + " is not executable. This is probably due to steps that consume a  resource that is not produced.");
                }
                for (StepDependencies lt_methodName : lt_sd.fetchExecutionOrderList()) {
                    l_phasedDependencyMethods.stream()
                            .filter(m -> m.getMethod().getConstructorOrMethod().getName()
                                    .equals(lt_methodName.getStepName())).findFirst().ifPresent(c -> lr_nonPhasedMethods.add(c));
                }
            }

            log.info("Lists before have {} methods. After it is {}", list.size(), lr_nonPhasedMethods.size());

            //return lr_nonPhasedMethods;
        return list.stream().filter(l -> l.getMethod().getRealClass().equals(Mutational.class)).collect(
                Collectors.toList());

    }

    @Override
    public void onTestStart(ITestResult result) {
        ITestListener.super.onTestStart(result);

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

            /*
            //Managing events
            //Cases 4 & 5
            //TODO Move to PhasedParent
            if (Phases.ASYNCHRONOUS.isSelected()) {

                //Check if there is an event declared
                String lt_event = PhasedEventManager.fetchEvent(result);
                if (PhasedEventManager.fetchEvent(result) != null) {
                    //TODO use PhasedTestManager for fetching full name instead
                    PhasedEventManager.startEvent(lt_event, ClassPathParser.fetchFullName(result));
                }
            }

             */
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        standardPostTestActions(result);

    }

    @Override
    public void onTestSuccess(ITestResult result) {
        standardPostTestActions(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
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

        /*
        if (PhasedTestManager.isPhasedTest(l_method)) {
            //TRIM add property check
            //appendShuffleGroupToName(result);
          //  PhasedTestManager.scenarioStateStore(result);

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

         */
    }


    @Override
    public void onFinish(ITestContext context) {
        ITestListener.super.onFinish(context);

        //Once the tests have finished in producer mode we, need to export the data
        if (Phases.PRODUCER.isSelected()) {
            log.info("{} At the end. Exporting data", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            PhasedTestManager.exportPhaseData();
        }
        log.debug("{} Purging results - Keeping one method per test class",
                PhasedTestManager.PHASED_TEST_LOG_PREFIX);

        PhasedEventManager.stopEventExecutor();
    }
}

