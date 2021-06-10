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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestNGException;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;

public class PhasedTestListener implements ITestListener, IAnnotationTransformer {
    protected static Logger log = LogManager.getLogger();

    // @Override
    public void onTestStart(ITestResult result) {

        final Method l_method = result.getMethod().getConstructorOrMethod().getMethod();

        //reset context
        if (PhasedTestManager.isPhasedTest(l_method)) {
            
            if (System.getProperty(PhasedTestManager.PROP_DISABLE_RETRY, "true").equalsIgnoreCase("true")) {
                log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"Disabling Retry for phased Tests.");
                result.getMethod().setRetryAnalyzerClass(DisabledRetryAnalyzer.class);
            }

            final String l_dataProvider = result.getParameters()[0].toString();
            PhasedTestManager.storePhasedContext(ClassPathParser.fetchFullName(l_method), l_dataProvider);
            
            if (!PhasedTestManager.scenarioStateContinue(result)) {
                final String skipMessage = PhasedTestManager.PHASED_TEST_LOG_PREFIX+"Skipping scenario step "+ClassPathParser.fetchFullName(result)+ " due to failure in a previous steps.";
                log.info(skipMessage);
                throw new SkipException(skipMessage);
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        standardPostTestActions(result);

    }

    /**
     * This method appends the shuffle group name to the method name
     *
     * Author : gandomi
     *
     * @param result The TestNG result context
     *
     */
    protected void appendShuffleGroupToName(ITestResult result) {
        StringBuilder sb = new StringBuilder(result.getParameters()[0].toString());
        sb.append('_');
        sb.append(result.getName());
        try {
            Field method = TestResult.class.getDeclaredField("m_method");
            method.setAccessible(true);
            method.set(result, result.getMethod().clone());
            Field methodName = BaseTestMethod.class.getDeclaredField("m_methodName");
            methodName.setAccessible(true);
            methodName.set(result.getMethod(), sb.toString());
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
     * @param result The TestNG result context
     *
     */
    protected void standardPostTestActions(ITestResult result) {
        if (PhasedTestManager.isPhasedTest(result.getMethod().getConstructorOrMethod().getMethod())) {
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
        log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"onStart - current Execution State is : " + Phases.getCurrentPhase());

        /*** Import DataBroker ***/
        String l_phasedDataBrokerClass = null;
        if (System.getProperties().containsKey(PhasedTestManager.PROP_PHASED_TEST_DATABROKER)) {
            l_phasedDataBrokerClass = System.getProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        } else if (context.getSuite().getXmlSuite().getAllParameters()
                .containsKey(PhasedTestManager.PROP_PHASED_TEST_DATABROKER)) {
            l_phasedDataBrokerClass = context.getSuite().getXmlSuite()
                    .getParameter(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        } else if (!Phases.NON_PHASED.isSelected()) {
            log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"No PhasedDataBroker set. Using the file system path " + PhasedTestManager.STD_STORE_DIR
                    + "/" + PhasedTestManager.STD_STORE_FILE + " instead ");
        }

        if (l_phasedDataBrokerClass != null) {
            try {
                PhasedTestManager.setDataBroker(l_phasedDataBrokerClass);
            } catch (PhasedTestConfigurationException e) {
                log.error(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"Errors while setting the PhasedDataBroker", e);
                throw new TestNGException(e);
            }
        }

        /*** import context for consumer ***/
        //The second condition is there for testing purposes. You can bypass the file by filling the Test
        if (Phases.CONSUMER.isSelected() && PhasedTestManager.getPhasedCache().isEmpty()) {
            PhasedTestManager.importPhaseData();
        }

        //Creating a method map
        Map<Class, List<String>> l_classMethodMap = new HashMap<>();
        for (ITestNGMethod lt_testNGMethod : context.getSuite().getAllMethods()) {
            Method lt_method = lt_testNGMethod.getConstructorOrMethod().getMethod();

            if (PhasedTestManager.isPhasedTestShuffledMode(lt_method)) {
                log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"In Shuffled mode : current test " + ClassPathParser.fetchFullName(lt_method));
                if (!l_classMethodMap.containsKey(lt_method.getDeclaringClass())) {
                    l_classMethodMap.put(lt_method.getDeclaringClass(), new ArrayList<>());
                }

                l_classMethodMap.get(lt_method.getDeclaringClass())
                        .add(ClassPathParser.fetchFullName(lt_method));
            }
        }

        if (Phases.getCurrentPhase().hasSplittingEvent()) {
            log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"Generating Phased Provders");
            PhasedTestManager.generatePhasedProviders(l_classMethodMap, Phases.getCurrentPhase());
        }

    }

    @Override
    public void onFinish(ITestContext context) {
        
        //Once the tests have finished in producer mode we, need to export the data
        if (Phases.PRODUCER.isSelected()) {
            log.info(PhasedTestManager.PHASED_TEST_LOG_PREFIX+"At the end. Exporting data");
            PhasedTestManager.exportPhaseData();
        }
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
            Method testMethod) {

        if (testClass != null) {

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
