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

import com.adobe.campaign.tests.integro.phased.events.PhasedParent;
import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MutationListener
        implements IAnnotationTransformer, IMethodInterceptor {
    private static final Logger log = LogManager.getLogger();

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
        PhasedTestManager.setStepDependencie(l_phasedClasses.stream()
                    .map(ScenarioStepDependencyFactory::listMethodCalls).collect(Collectors.toMap(ScenarioStepDependencies::getScenarioName, Function.identity())));


            //if (Phases.getCurrentPhase().hasSplittingEvent()) {
            log.info("{} Generating Phased Providers", PhasedTestManager.PHASED_TEST_LOG_PREFIX);
            //NIA
            PhasedTestManager.generatePhasedProviders(l_classMethodMap, PhasedTestManager.getStepDependencie(),
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

            for (ScenarioStepDependencies lt_sd : PhasedTestManager.getStepDependencie().values()) {
                for (StepDependencies lt_methodName : lt_sd.fetchExecutionOrderList()) {
                    l_phasedDependencyMethods.stream()
                            .filter(m -> m.getMethod().getConstructorOrMethod().getName()
                                    .equals(lt_methodName.getStepName())).findFirst().ifPresent(c -> lr_nonPhasedMethods.add(c));
                }
            }

            log.info("Lists before have {} methods. After it is {}", list.size(), lr_nonPhasedMethods.size());

            //return lr_nonPhasedMethods;
        return list.stream().filter(l -> l.getMethod().getRealClass().equals(PhasedParent.class)).collect(
                Collectors.toList());

    }
}

