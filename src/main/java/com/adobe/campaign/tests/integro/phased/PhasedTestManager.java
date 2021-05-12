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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import com.adobe.campaign.tests.integro.core.utils.ClassPathParser;
import com.adobe.campaign.tests.integro.core.utils.GeneralTestUtils;

public class PhasedTestManager {

    protected static final String STD_KEY_CLASS_SEPARATOR = "->";

    protected static final String PHASED_TEST_LOG_PREFIX = "[Phased Testing] ";

    protected static Logger log = LogManager.getLogger();

    public static final String PROP_PHASED_DATA_PATH = "INTEGRO.TEST.PHASED.STORAGE.PATH";
    public static final String PROP_SELECTED_PHASE = "INTEGRO.TEST.PHASED.PHASE";
    public static final String PROP_PHASED_TEST_DATABROKER = "INTEGRO.TEST.PHASED.DATABROKER";
    public static final String PROP_DISABLE_RETRY = "INTEGRO.TEST.PHASED.RETRY.DISABLED";


    public static final String STD_STORE_DIR = "phased_tests";
    public static final String STD_STORE_FILE = "phaseData.properties";

    //Values for the DataProvider used in both Shuffled and Single run phases
    protected static final String STD_PHASED_GROUP_PREFIX = "shuffledGroup_";
    protected static final String STD_PHASED_GROUP_SINGLE = "singleRun";

    
    protected static Properties phasedCache = new Properties();

    protected static Map<String, MethodMapping> methodMap = new HashMap<>();

    protected static Properties phaseContext = new Properties();

    private static PhasedDataBroker dataBroker = null;

    /**
     * @return the phasedCache
     */
    public static Properties getPhasedCache() {
        return phasedCache;
    }

    /**
     * @return the dataBroker
     */
    protected static PhasedDataBroker getDataBroker() {
        return dataBroker;
    }

    /**
     * @param dataBroker
     *        the dataBroker to set
     */
    public static void setDataBroker(Object dataBroker) {
        PhasedTestManager.dataBroker = (PhasedDataBroker) dataBroker;
    }

    /**
     * Initiaizes the databroker given the full class path of the implementation
     * of the interface {@code PhasedDataBroker}
     *
     * Author : gandomi
     *
     * @param in_classPath
     * @throws ClassNotFoundException
     * @throws PhasedTestConfigurationException
     * @throws IllegalAccessException
     * @throws InstantiationException
     *
     */
    public static void setDataBroker(String in_classPath) throws PhasedTestConfigurationException {
        log.info("Phased Testing : Setting Data broker with classpath " + in_classPath);
        Class<?> l_dataBrokerImplementation;
        Object l_dataBroker;
        try {
            l_dataBrokerImplementation = Class.forName(in_classPath);

            l_dataBroker = l_dataBrokerImplementation.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new PhasedTestConfigurationException(
                    "Error while fetching / instantiating the given PhasedDataBroker class " + in_classPath
                            + ".",
                    e);
        }

        if (!(l_dataBroker instanceof PhasedDataBroker)) {
            throw new PhasedTestConfigurationException(
                    "The given class was not an instance of PhasedDataBroker");
        }

        setDataBroker(l_dataBroker);

    }

    /**
     * This method clears the data broker
     *
     * Author : gandomi
     *
     *
     */
    public static void clearDataBroker() {
        dataBroker = null;

    }

    /**
     * This method stores a phased test data in the cache. It will be stored
     * with the keys: "class, method, instance" and a value
     *
     * Author : gandomi
     *
     * @param in_storeValue The value you want stored
     * @return The key that was used in storing the value
     *
     */
    public static String produce(String in_storeValue) {
        final String l_methodFullName = ClassPathParser.fetchCalledByFullName();
        StringBuilder sb = new StringBuilder(l_methodFullName);

        if (phaseContext.containsKey(l_methodFullName)) {
            sb.append("(");
            sb.append(phaseContext.get(l_methodFullName));
            sb.append(")");
        }

        final String lr_storeKey = sb.toString();
        return storePhasedCache(lr_storeKey, in_storeValue);
    }

    /**
     * Stores a value with the given key. We include the class as prefix. By
     * default {@link #produce(String)} should be preferred
     *
     * Author : gandomi
     *
     * @param in_storageKey A string that is added to the generated key for identification of the stored data
     * @param in_storeValue The value we want to store
     * @return The key that was used in storing the value
     *
     */
    public static String produceWithKey(String in_storageKey, String in_storeValue) {
        final String l_className = ClassPathParser.fetchCalledBy().getClassName();
        final String l_fullId = generateStepKeyIdentity(ClassPathParser.fetchCalledByFullName(), l_className,
                in_storageKey);
        return storePhasedCache(l_fullId, in_storeValue);
    }

    /**
     * This method generates the identifier for a producer/consumer
     *
     * Author : gandomi
     *
     * @param in_idInPhaseContext
     * @param in_storageKey
     * @return
     *
     */
    protected static String generateStepKeyIdentity(final String in_idInPhaseContext, String in_storageKey) {

        return generateStepKeyIdentity(in_idInPhaseContext, in_idInPhaseContext, in_storageKey);
    }

    /**
     * This method generates the identifier for a producer/consumer
     *
     * Author : gandomi
     *
     * @param in_idInPhaseContext
     * @param in_idPrefixToStore
     * @param in_storageKey
     * @return
     *
     */
    protected static String generateStepKeyIdentity(final String in_idInPhaseContext,
            final String in_idPrefixToStore, String in_storageKey) {
        StringBuilder sb = new StringBuilder(in_idPrefixToStore);

        if (phaseContext.containsKey(in_idInPhaseContext)) {
            sb.append("(");
            sb.append(phaseContext.get(in_idInPhaseContext));
            sb.append(")");
        }

        if (in_storageKey != null && !in_storageKey.trim().isEmpty()) {
            sb.append(STD_KEY_CLASS_SEPARATOR);
            sb.append(in_storageKey);
        }

        return sb.toString();
    }

    protected static String generateStepKeyIdentity(String in_idInPhaseContext) {
        return generateStepKeyIdentity(in_idInPhaseContext, null);
    }

    /**
     *
     * Author : gandomi
     *
     * @param in_storeValue
     * @param in_storeKey
     * @return
     *
     */
    private static String storePhasedCache(final String in_storeKey, String in_storeValue) {
        if (phasedCache.containsKey(in_storeKey)) {
            throw new PhasedTestException("Phased Test data " + in_storeKey + " already stored.");
        }

        phasedCache.put(in_storeKey, in_storeValue);
        return in_storeKey;
    }

    /**
     * Given a step in the Phased Test it fetches the value committed for that
     * test. It will fetch a Phased Test data with the method/test that called
     * this method. This method is to be used if you have produced your Phased
     * Data using {@link #produce(String))}
     *
     * Author : gandomi
     *
     * @param in_storedStep
     * @return
     *
     */
    public static String consume(String in_storedStep) {
        StackTraceElement l_calledElement = ClassPathParser.fetchCalledBy();
        StringBuilder sb = new StringBuilder(l_calledElement.getClassName());

        sb.append('.');
        sb.append(in_storedStep);

        String l_methodFullNameOfProducer = ClassPathParser.fetchCalledByFullName();
        //Fetch current data  provider
        if (phaseContext.containsKey(l_methodFullNameOfProducer)) {
            sb.append("(");
            sb.append(phaseContext.get(l_methodFullNameOfProducer));
            sb.append(")");
        }

        final String l_storageKey = sb.toString();

        if (!phasedCache.containsKey(l_storageKey)) {
            throw new PhasedTestException("The given consumable " + l_storageKey + " requested by "
                    + l_calledElement.toString() + " was not available.");
        }

        return phasedCache.getProperty(l_storageKey);
    }

    /**
     * Given a step in the Phased Test it fetches the value committed for that
     * test.
     *
     * Author : gandomi
     *
     * @param in_storageKey
     * @return
     *
     */
    public static String consumeWithKey(String in_storageKey) {
        final StackTraceElement l_fetchCalledBy = ClassPathParser.fetchCalledBy();

        String l_realKey = generateStepKeyIdentity(ClassPathParser.fetchCalledByFullName(),
                l_fetchCalledBy.getClassName(), in_storageKey);

        if (!phasedCache.containsKey(l_realKey)) {
            throw new PhasedTestException("The given consumable " + l_realKey + ", requested by "
                    + l_fetchCalledBy.toString() + " was not available.");
        }

        return phasedCache.getProperty(l_realKey);
    }

    /**
     * cleans the cache of the PhasedManager
     *
     * Author : gandomi
     *
     *
     */
    protected static void clearCache() {
        phasedCache.clear();

        methodMap = new HashMap<>();

        phaseContext.clear();
    }

    /**
     * exports the cache into a standard PhasedTest property file
     *
     * Author : gandomi
     *
     * @return
     *
     */
    protected static File exportPhaseData() {

        File l_exportCacheFile = null;
        if (System.getProperties().containsKey(PROP_PHASED_DATA_PATH)) {
            l_exportCacheFile = new File(System.getProperty(PROP_PHASED_DATA_PATH));

        } else {
            l_exportCacheFile = new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR), STD_STORE_FILE);
        }

        log.info(PHASED_TEST_LOG_PREFIX + " Exporting Phased Testing data to " + l_exportCacheFile.getPath());

        return exportCache(l_exportCacheFile);
    }

    /**
     *
     * Author : gandomi
     *
     * @param in_file
     * @return
     *
     */
    protected static File exportCache(File in_file) {
        try (FileWriter fw = new FileWriter(in_file)) {

            getPhasedCache().store(fw, null);

        } catch (IOException e) {
            log.error("Error when creating file " + in_file);
            throw new PhasedTestException("Error when creating file " + in_file + ".", e);
        }
        //Store in DataBroker
        if (dataBroker != null) {
            log.info(PHASED_TEST_LOG_PREFIX
                    + " Exporting Phased Testing to location specified by provided PhasedDataBroker.");
            dataBroker.store(in_file);
        }

        return in_file;
    }

    /**
     * Imports a file and stored the properties in the phased cache.
     *
     * Author : gandomi
     *
     * @param in_phasedTestFile
     * @return
     * @throws FileNotFoundException
     *
     */
    protected static Properties importCache(File in_phasedTestFile) {
        try (InputStream input = new FileInputStream(in_phasedTestFile)) {

            // load a properties file
            phasedCache.load(input);
        } catch (IOException e) {
            log.error("Error when loading file " + in_phasedTestFile);
            throw new PhasedTestException("Error when loading file " + in_phasedTestFile.getPath() + ".", e);

        }
        return getPhasedCache();
    }

    /**
     * Loads the Phased Test data from the standard location which is
     * {@value GeneralTestUtils#STD_CACHE_DIR}/{@value #STD_STORE_DIR}/{@value #STD_STORE_FILE}
     *
     * Author : gandomi
     *
     * @return
     * @throws FileNotFoundException
     *
     */
    protected static Properties importPhaseData() {
        File l_importCacheFile = null;

        if (dataBroker == null) {

            if (System.getProperties().containsKey(PROP_PHASED_DATA_PATH)) {
                l_importCacheFile = new File(System.getProperty(PROP_PHASED_DATA_PATH));

            } else {
                l_importCacheFile = new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR),
                        STD_STORE_FILE);
                log.warn(PHASED_TEST_LOG_PREFIX + " The system property " + PROP_PHASED_DATA_PATH
                        + " not set. Fetching Phased Test data from " + l_importCacheFile.getPath());
            }
        } else {
            l_importCacheFile = dataBroker.fetch(STD_STORE_FILE);
        }
        return importCache(l_importCacheFile);

    }

    /**
     * Returns the data provder given a method
     *
     * Author : gandomi
     *
     * @param m
     * @return
     *
     */
    public static Object[][] fetchProvidersShuffled(Method m) {
        String l_methodFullName = GeneralTestUtils.fetchFullName(m);
        return fetchProvidersShuffled(l_methodFullName);
    }

    /**
     *
     * Author : gandomi
     *
     * @param l_methodFullName
     * @return
     *
     */
    public static Object[][] fetchProvidersShuffled(String l_methodFullName) {

        return fetchProvidersShuffled(l_methodFullName, Phases.getCurrentPhase());
    }

    /**
     * Retruns the provider for shuffling tests. In general the values are
     * Shuffle group prefix + Nr of steps before the Phase Event and the number
     * of steps after the event.
     *
     * Author : gandomi
     *
     * @param l_methodFullName
     * @return
     *
     */
    public static Object[][] fetchProvidersShuffled(String l_methodFullName, Phases in_phasedState) {

        final MethodMapping l_methodMapping = methodMap.get(l_methodFullName);
        Object[][] lr_objectArray = new Object[l_methodMapping.nrOfProviders][1];

        for (int rows = 0; rows < l_methodMapping.nrOfProviders; rows++) {

            int lt_nrBeforePhase = in_phasedState.equals(Phases.PRODUCER)
                    ? (l_methodMapping.totalClassMethods - rows)
                    : rows;

            int lt_nrAfterPhase = l_methodMapping.totalClassMethods - lt_nrBeforePhase;

            StringBuilder lt_sb = new StringBuilder(STD_PHASED_GROUP_PREFIX);

            lt_sb.append(lt_nrBeforePhase);
            lt_sb.append("_");
            lt_sb.append(lt_nrAfterPhase);

            lr_objectArray[rows][0] = lt_sb.toString();
        }
        log.debug("returning provider for method " + l_methodFullName);
        return lr_objectArray;
    }

    /**
     * Returns the data provider for a single phase
     *
     * Author : gandomi
     *
     * @param in_method
     * @return An array containing the data providers for the method. Otherwise
     *         an empty array
     *
     */
    public static Object[] fetchProvidersSingle(Method in_method) {
        log.debug("Returning provider for method " + GeneralTestUtils.fetchFullName(in_method));

        if (Phases.PRODUCER.isSelected() && isExecutedInProducerMode(in_method)) {

            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        if (Phases.CONSUMER.isSelected() && !isExecutedInProducerMode(in_method)) {
            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        if (Phases.NON_PHASED.isSelected()
                && in_method.getDeclaringClass().getAnnotation(PhasedTest.class).executeInactive()) {
            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        return new Object[] {};
    }

    /**
     * This method calculates how often a class should be run.
     *
     * Author : gandomi
     *
     * @param in_classMethodMap
     * @return
     *
     */
    public static Map<String, MethodMapping> generatePhasedProviders(
            Map<Class, List<String>> in_classMethodMap) {

        return generatePhasedProviders(in_classMethodMap, Phases.PRODUCER);

    }

    /**
     * This method calculates how often a class should be run.
     *
     * Author : gandomi
     *
     * @param in_classMethodMap
     * @param in_phaseState
     * @return
     *
     */
    public static Map<String, MethodMapping> generatePhasedProviders(
            Map<Class, List<String>> in_classMethodMap, Phases in_phaseState) {
        methodMap = new HashMap<>();

        for (Class lt_class : in_classMethodMap.keySet()) {

            List<String> lt_methodList = in_classMethodMap.get(lt_class);

            if (in_phaseState.equals(Phases.CONSUMER)) {
                Collections.reverse(lt_methodList);
            }

            for (int i = 0; i < in_classMethodMap.get(lt_class).size(); i++) {
                methodMap.put(lt_methodList.get(i), new MethodMapping(
                        in_classMethodMap.get(lt_class).size() - i, in_classMethodMap.get(lt_class).size()));

            }
        }
        return methodMap;

    }

    public static void storePhasedContext(String in_methodFullName, String in_phasedGroupId) {
        phaseContext.put(in_methodFullName, in_phasedGroupId);

    }

    /**
     * For testing purposes only. Used when we want to test the consumer
     *
     * Author : gandomi
     *
     * @param in_testMethod
     * @param in_phaseGroup
     * @param in_storedData
     *
     */
    protected static String storeTestData(Method in_testMethod, String in_phaseGroup, String in_storedData) {
        phaseContext.put(GeneralTestUtils.fetchFullName(in_testMethod), in_phaseGroup);
        return storePhasedCache(generateStepKeyIdentity(GeneralTestUtils.fetchFullName(in_testMethod)),
                in_storedData);

    }

    /**
     * Basically lets us know if we execute the given method in producer mode.
     * We look at the attribute value phaseEnd. This method is specifically for
     * the Single Mode.
     *
     * Author : gandomi
     *
     * @param in_method
     * @return true if the test is before or in the phase End.
     *
     */
    public static boolean isExecutedInProducerMode(Method in_method) {

        if (PhasedTestManager.isPhasedTestSingleMode(in_method)) {
            final Class<?> l_declaringClass = in_method.getDeclaringClass();
            List<Method> l_myMethods = Arrays.asList(l_declaringClass.getMethods()).stream()
                    .filter(f -> PhasedTestManager.isPhasedTest(f)).collect(Collectors.toList());

            Comparator<Method> compareMethodByName = (Method m1, Method m2) -> m1.getName()
                    .compareTo(m2.getName());

            Collections.sort(l_myMethods, compareMethodByName);

            for (Method lt_declaredMethod : l_myMethods) {
                if (PhasedTestManager.isPhaseLimit(lt_declaredMethod)) {
                    return false;
                }

                if (lt_declaredMethod.equals(in_method)) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Checks if the phase step is the last item in the current phase
     *
     * Author : gandomi
     *
     * @param in_methodInstance
     * @return
     *
     */
    protected static boolean isPhaseLimit(Method in_method) {

        return in_method.isAnnotationPresent(PhaseEvent.class);
    }

    /**
     * This method tells us if the method is a valid phased test. This is done
     * by seeing if the annotation PhasedStep is on the method, and if the
     * annotation PhasedTest is on the class
     *
     * Author : gandomi
     *
     * @param in_method
     * @return true if The annotations PhasedTest and PhasedStep are present
     *
     */
    public static boolean isPhasedTest(Method in_method) {
        return isPhasedTest(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the class is a PhasedTestClass
     *
     * Author : gandomi
     *
     * @param in_class
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    public static boolean isPhasedTest(Class in_class) {
        return in_class.isAnnotationPresent(PhasedTest.class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * consequently in two phases
     */
    protected static boolean isPhasedTestSingleMode(Method in_method) {
        return isPhasedTest(in_method) && !isPhasedTestShuffledMode(in_method);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * consequently in two phases
     */
    protected static boolean isPhasedTestSingleMode(Class in_class) {
        return isPhasedTest(in_class) && !isPhasedTestShuffledMode(in_class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * in a shuffled manner. For a test with 3 steps the test will be executed 6
     * times in total
     */
    protected static boolean isPhasedTestShuffledMode(Method in_method) {
        return isPhasedTest(in_method) && isPhasedTestShuffledMode(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * in a Shuffled manner. For a test with 3 steps the test will be executed 6
     * times in total
     */
    protected static boolean isPhasedTestShuffledMode(Class in_class) {
        return isPhasedTest(in_class) && ((PhasedTest) in_class.getAnnotation(PhasedTest.class)).canShuffle()
                && Phases.getCurrentPhase().hasSplittingEvent();
    }

    /**
     * This method provides an ID for the scenario given the ITestNGResult. This
     * is assembled using the Classname + the PhaseGroup
     *
     * Author : gandomi
     *
     * @param in_testNGResult
     * @return
     *
     */
    public static String fetchScenarioName(ITestResult in_testNGResult) {
        StringBuilder sb = new StringBuilder(in_testNGResult.getMethod().getConstructorOrMethod().getMethod()
                .getDeclaringClass().getTypeName());
        return sb.append(GeneralTestUtils.fetchParameterValues(in_testNGResult)).toString();
    }

    /**
     * This method logs the stage result of the Phased Test Group. The key will
     * be the class including the phase test group. It allows us to know if the
     * test is allowed to continue.
     * 
     * Once the context is logged as false for a test it remains false
     *
     * Author : gandomi
     *
     * @param in_testResult
     *        The test result
     *
     */
    public static void scenarioStateStore(ITestResult in_testResult) {

        final String l_scenarioName = fetchScenarioName(in_testResult);
        final String l_stepName = GeneralTestUtils.fetchFullName(in_testResult);

        if (phasedCache.containsKey(l_scenarioName)) {

            //If the phase context is true we check if the state for the scenario should change. Otherwise we interrupt the tests
            if (phasedCache.get(l_scenarioName).equals(Boolean.TRUE.toString())
                    || phasedCache.get(l_scenarioName).equals(l_stepName)) {
                phasedCache.put(l_scenarioName,
                        in_testResult.getStatus() == ITestResult.SUCCESS ? Boolean.TRUE.toString() : l_stepName);
            }

        } else {
            phasedCache.put(l_scenarioName,
                    (in_testResult.getStatus() == ITestResult.SUCCESS) ? Boolean.TRUE.toString() : l_stepName);
        }

    }

    /**
     * This method lets us know if we should continue with the scenario. If the
     * context is not yet stored for the scenario, we should continue. In the
     * case that the value for the scenario is equal to the current step name,
     * we will continue.
     * <p>
     * There is one Exception. If the cause of the failure is the current test.
     *
     * Author : gandomi
     *
     * @param in_testResult
     *        The test result
     * @return true if the log state is equal to true (1) or if we have not yet
     *         stored a context for the scenario.
     *
     */
    public static boolean scenarioStateContinue(ITestResult in_testResult) {
        final String l_scenarioName = fetchScenarioName(in_testResult);
        final Properties phasedCache2 = phasedCache;
        if (!phasedCache2.containsKey(l_scenarioName)) {
            return true;
        }

        if (phasedCache2.get(l_scenarioName).equals(GeneralTestUtils.fetchFullName(in_testResult))) {
            return true;
        }
        
        return phasedCache2.get(l_scenarioName).equals(Boolean.TRUE.toString());
    }
}
