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

import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.StackTraceManager;

public class PhasedTestManager {

    protected static final String STD_KEY_CLASS_SEPARATOR = "->";

    public static final String PHASED_TEST_LOG_PREFIX = "[Phased Testing] ";

    protected static Logger log = LogManager.getLogger();

    public static final String PROP_PHASED_DATA_PATH = "PHASED.TESTS.STORAGE.PATH";
    public static final String PROP_OUTPUT_DIR = "PHASED.TESTS.OUTPUT.DIR";
    public static final String PROP_SELECTED_PHASE = "PHASED.TESTS.PHASE";
    public static final String PROP_PHASED_TEST_DATABROKER = "PHASED.TESTS.DATABROKER";
    public static final String PROP_DISABLE_RETRY = "PHASED.TESTS.RETRY.DISABLED";

    public static final String DEFAULT_CACHE_DIR = "phased_output";
    public static final String STD_CACHE_DIR = System.getProperty(PROP_OUTPUT_DIR, DEFAULT_CACHE_DIR);
    public static final String STD_STORE_DIR = "phased_tests";
    public static final String STD_STORE_FILE = "phaseData.properties";

    //Values for the DataProvider used in both Shuffled and Single run phases
    protected static final String STD_PHASED_GROUP_PREFIX = "phased-shuffledGroup_";
    protected static final String STD_PHASED_GROUP_SINGLE = "phased-singleRun";

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
     *        The classpath for the implementation of the data broker
     * @throws PhasedTestConfigurationException
     *         Whenever there is a problem instantiating the Phased DataBroker
     *         class
     *
     */
    public static void setDataBroker(String in_classPath) throws PhasedTestConfigurationException {
        log.info(PHASED_TEST_LOG_PREFIX + "Setting Data broker with classpath " + in_classPath);
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
     * @param in_storeValue
     *        The value you want stored
     * @return The key that was used in storing the value
     *
     */
    public static String produceInStep(String in_storeValue) {
        final String l_methodFullName = StackTraceManager.fetchCalledByFullName();
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
     * default {@link #produceInStep(String)} should be preferred
     *
     * Author : gandomi
     *
     * @param in_storageKey
     *        A string that is added to the generated key for identification of
     *        the stored data
     * @param in_storeValue
     *        The value we want to store
     * @return The key that was used in storing the value
     * 
     * @deprecated This method has been renamed. Please use {@link #produce(String,String)} instead.
     *
     */
    @Deprecated
    public static String produceWithKey(String in_storageKey, String in_storeValue) {
        final String l_className = StackTraceManager.fetchCalledBy().getClassName();
        final String l_fullId = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(),
                l_className, in_storageKey);
        return storePhasedCache(l_fullId, in_storeValue);
    }
    
    
    /**
     * Stores a value with the given key. We include the class as prefix.
     *
     * Author : gandomi
     *
     * @param in_storageKey
     *        A string that is added to the generated key for identification of
     *        the stored data
     * @param in_storeValue
     *        The value we want to store
     * @return The key that was used in storing the value
     * 
     */
    public static String produce(String in_storageKey, String in_storeValue) {
        final String l_className = StackTraceManager.fetchCalledBy().getClassName();
        final String l_fullId = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(),
                l_className, in_storageKey);
        return storePhasedCache(l_fullId, in_storeValue);
    }

    /**
     * This method generates the identifier for a producer/consumer used for
     * storingg in the cache
     *
     * Author : gandomi
     *
     * @param in_idInPhaseContext
     *        The id of the step in the context
     * @param in_storageKey
     *        An additional identifier for storing the data
     * @return The identity of the storage key as stored in the cache
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
     *        The id of the step in the context
     * @param in_idPrefixToStore
     *        The prefix of the full name for storing values. Usually the class
     *        full name
     * @param in_storageKey
     *        An additional identifier for storing the data
     * @return The identity of the storage key as stored in the cache
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

    /**
     * This method generates the identifier for a producer/consumer
     *
     * Author : gandomi
     *
     * @param in_idInPhaseContext
     *        The id of the step in the context
     * @return The identity of the storage key as stored in the cache
     */
    protected static String generateStepKeyIdentity(String in_idInPhaseContext) {
        return generateStepKeyIdentity(in_idInPhaseContext, null);
    }

    /**
     * Stores a value in the cache
     *
     * Author : gandomi
     *
     * @param in_storeKey
     *        The key to be used for storing the value
     * @param in_storeValue
     *        The value to be stored
     * @return The key used for storing the value
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
     * Data using {@link #produceInStep(String)}
     *
     * Author : gandomi
     *
     * @param in_stepName
     *        The step name aka method name (not class name nor arguments) that
     *        stored a value in the current scenario
     * @return The value store by the method
     *
     */
    public static String consumeFromStep(String in_stepName) {
        StackTraceElement l_calledElement = StackTraceManager.fetchCalledBy();
        StringBuilder sb = new StringBuilder(l_calledElement.getClassName());

        sb.append('.');
        sb.append(in_stepName);

        String l_methodFullNameOfProducer = StackTraceManager.fetchCalledByFullName();
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
     *        A key that was used to store the value in this scenario
     * @return The value that was stored
     * 
     * @deprecated This method has been renamed. Please use {@link #consume(String)} instead 
     *
     */
    @Deprecated
    public static String consumeWithKey(String in_storageKey) {
        final StackTraceElement l_fetchCalledBy = StackTraceManager.fetchCalledBy();

        String l_realKey = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(),
                l_fetchCalledBy.getClassName(), in_storageKey);

        if (!phasedCache.containsKey(l_realKey)) {
            throw new PhasedTestException("The given consumable " + l_realKey + ", requested by "
                    + l_fetchCalledBy.toString() + " was not available.");
        }

        return phasedCache.getProperty(l_realKey);
    }

    /**
     * Given a step in the Phased Test it fetches the value committed for that
     * test.
     *
     * Author : gandomi
     *
     * @param in_storageKey
     *        A key that was used to store the value in this scenario
     * @return The value that was stored
     * 
     */
    public static String consume(String in_storageKey) {
        final StackTraceElement l_fetchCalledBy = StackTraceManager.fetchCalledBy();

        String l_realKey = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(),
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
     * @return The file that was used for storing the phase cache
     *
     */
    protected static File exportPhaseData() {

        File l_exportCacheFile = null;
        if (System.getProperties().containsKey(PROP_PHASED_DATA_PATH)) {
            l_exportCacheFile = new File(System.getProperty(PROP_PHASED_DATA_PATH));

        } else {
            l_exportCacheFile = new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR), STD_STORE_FILE);
        }

        return exportCache(l_exportCacheFile);
    }

    /**
     * Exports the Phase cache into the given file
     *
     * Author : gandomi
     *
     * @param in_file
     *        that will contain the phase cache/data
     * @return The file used for storing the cache.
     *
     */
    protected static File exportCache(File in_file) {
        
        log.info(PHASED_TEST_LOG_PREFIX + " Exporting Phased Testing data to " + in_file.getPath());
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
     *        A file that contains the phase cache data from a previous phase
     * @return A Properties object with the phase cache data from the previous
     *         phase
     *
     */
    protected static Properties importCache(File in_phasedTestFile) {
        log.info(PHASED_TEST_LOG_PREFIX+"Importing phase cache.");
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
     * Loads the Phased Test data from the standard location which is by default
     * {@value #DEFAULT_CACHE_DIR}/{@value #STD_STORE_DIR}/{@value #STD_STORE_FILE}
     *
     * Author : gandomi
     *
     * @return A Properties object with the phase cache data from the previous
     *         phase
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
            log.info(PHASED_TEST_LOG_PREFIX+"Fetching cache through DataBroker");
            l_importCacheFile = dataBroker.fetch(STD_STORE_FILE);
        }
        return importCache(l_importCacheFile);

    }

    /**
     * Calculates the data providers for the current method and test context
     *
     * Author : gandomi
     *
     * @param in_method
     *        The step/method for which we want to fond out the data provider
     * @return A two-dimensional array of all the data providers attached to the
     *         current step/method
     *
     */
    public static Object[][] fetchProvidersShuffled(Method in_method) {
        String l_methodFullName = ClassPathParser.fetchFullName(in_method);
        return fetchProvidersShuffled(l_methodFullName);
    }

    /**
     * Returns the provider for shuffling tests. In general the values are
     * Shuffle group prefix + Nr of steps before the Phase Event and the number
     * of steps after the event.
     * 
     * Author : gandomi
     *
     * @param in_methodFullName
     *        The full name of the method used for identifying it in the phase
     *        context
     * @return A two-dimensional array of all the data providers attached to the
     *         current step/method
     *
     */
    public static Object[][] fetchProvidersShuffled(String in_methodFullName) {

        return fetchProvidersShuffled(in_methodFullName, Phases.getCurrentPhase());
    }

    /**
     * Returns the provider for shuffling tests. In general the values are
     * Shuffle group prefix + Nr of steps before the Phase Event and the number
     * of steps after the event.
     *
     * Author : gandomi
     *
     * @param in_methodFullName
     *        The full name of the method used for identifying it in the phase
     *        context
     * @param in_phasedState
     *        The phase state for which we should retrieve the parameters. The
     *        parameters will be different based on the phase.
     * @return A two-dimensional array of all the data providers attached to the
     *         current step/method
     *
     */
    public static Object[][] fetchProvidersShuffled(String in_methodFullName, Phases in_phasedState) {

        final MethodMapping l_methodMapping = methodMap.get(in_methodFullName);
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
        log.debug("returning provider for method " + in_methodFullName);
        return lr_objectArray;
    }

    /**
     * Returns the data provider for a single phase
     *
     * Author : gandomi
     *
     * @param in_method
     *        The method/step for which we want to get the data providers for
     * @return An array containing the data providers for the method. Otherwise
     *         an empty array
     *
     */
    public static Object[] fetchProvidersSingle(Method in_method) {
        log.debug("Returning provider for method " + ClassPathParser.fetchFullName(in_method));

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
     *        A map of a class and it is methods (A scenario and its steps)
     * @return A map letting us know that for a the given method how often it
     *         will be executed in the current phase
     *
     */
    public static Map<String, MethodMapping> generatePhasedProviders(
            Map<Class, List<String>> in_classMethodMap) {

        return generatePhasedProviders(in_classMethodMap, Phases.getCurrentPhase());

    }

    /**
     * This method calculates how often a scenario should be run, given the
     * steps/methods it has.
     *
     * Author : gandomi
     *
     * @param in_classMethodMap
     *        A map of a class and it is methods (A scenario and its steps)
     * @param in_phaseState
     *        The phase in which we are
     * @return A map letting us know that for a the given method how often it
     *         will be executed in the current phase
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

    /**
     * Updates the context with the method and its current Phase Group ID
     *
     * Author : gandomi
     *
     * @param in_methodFullName
     *        The full name of the method
     * @param in_phasedGroupId
     *        The Id of the phase group
     *
     */
    public static void storePhasedContext(String in_methodFullName, String in_phasedGroupId) {
        phaseContext.put(in_methodFullName, in_phasedGroupId);

    }

    /**
     * For testing purposes only. Used when we want to test the consumer
     *
     * Author : gandomi
     *
     * @param in_testMethod
     *        A test method
     * @param in_phaseGroup
     *        A phase group Id
     * @param in_storedData
     *        The data to be stored for the scenario step
     * @return The key used to store the value in the cache
     */
    protected static String storeTestData(Method in_testMethod, String in_phaseGroup, String in_storedData) {
        phaseContext.put(ClassPathParser.fetchFullName(in_testMethod), in_phaseGroup);
        return storePhasedCache(generateStepKeyIdentity(ClassPathParser.fetchFullName(in_testMethod)),
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
     *        The method/step we want to know its phase location
     * @return true if the step is anywhere before the phase limit
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
     * @param in_method
     *        The method/step which we want to know if it is the last step in
     *        the current phase
     * @return True if the test is before or in the phase End.
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
     *        Any test method that is being executed
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
     *        Any class that contains tests
     * @return True if the class is a phased test scenario
     *
     */
    @SuppressWarnings("unchecked")
    public static boolean isPhasedTest(Class in_class) {
        return in_class.isAnnotationPresent(PhasedTest.class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * consequently in two phases
     * 
     * @param in_method
     *        Any test method
     * @return True if the test step/method is part of a SingleRun Phase Test
     *         scenario
     */
    protected static boolean isPhasedTestSingleMode(Method in_method) {
        return isPhasedTest(in_method) && !isPhasedTestShuffledMode(in_method);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * consequently in two phases
     * 
     * @param in_class
     *        Any class that contains tests
     * @return True if the test class is a SingleRun Phase Test scenario
     */
    protected static boolean isPhasedTestSingleMode(Class in_class) {
        return isPhasedTest(in_class) && !isPhasedTestShuffledMode(in_class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * in a shuffled manner. For a test with 3 steps the test will be executed 6
     * times in total
     * 
     * @param in_method
     *        A test method
     * @return True if the given test method/step is part of a Shuffled Phased
     *         Test scenario
     */
    protected static boolean isPhasedTestShuffledMode(Method in_method) {
        return isPhasedTest(in_method) && isPhasedTestShuffledMode(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * in a Shuffled manner. For a test with 3 steps the test will be executed 6
     * times in total
     * 
     * @param in_class
     *        A test class/scenario
     * @return True if the given test scenario is a Shuffled Phased Test
     *         scenario
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
     *        A TestNG Test Result object
     * @return The identity of the scenario
     *
     */
    public static String fetchScenarioName(ITestResult in_testNGResult) {
        StringBuilder sb = new StringBuilder(in_testNGResult.getMethod().getConstructorOrMethod().getMethod()
                .getDeclaringClass().getTypeName());
        return sb.append(ClassPathParser.fetchParameterValues(in_testNGResult)).toString();
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
        final String l_stepName = ClassPathParser.fetchFullName(in_testResult);

        if (phasedCache.containsKey(l_scenarioName)) {

            //If the phase context is true we check if the state for the scenario should change. Otherwise we interrupt the tests
            if (phasedCache.get(l_scenarioName).equals(Boolean.TRUE.toString())
                    || phasedCache.get(l_scenarioName).equals(l_stepName)) {
                phasedCache.put(l_scenarioName,
                        in_testResult.getStatus() == ITestResult.SUCCESS ? Boolean.TRUE.toString()
                                : l_stepName);
            }

        } else {
            phasedCache.put(l_scenarioName,
                    (in_testResult.getStatus() == ITestResult.SUCCESS) ? Boolean.TRUE.toString()
                            : l_stepName);
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

        if (phasedCache2.get(l_scenarioName).equals(ClassPathParser.fetchFullName(in_testResult))) {
            return true;
        }

        return phasedCache2.get(l_scenarioName).equals(Boolean.TRUE.toString());
    }
}
