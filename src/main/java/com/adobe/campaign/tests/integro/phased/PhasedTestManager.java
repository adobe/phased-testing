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
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.StackTraceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class PhasedTestManager {

    protected static final String STD_GROUP_SELECT_TESTS_BY_PRODUCER = "PHASED_PRODUCED_TESTS";
    protected static final String STD_KEY_CLASS_SEPARATOR = "->";

    public static final String PHASED_TEST_LOG_PREFIX = "[Phased Testing] ";
    public static final String STD_SCENARIO_CONTEXT_SEPARATOR = ";";

    protected static Logger log = LogManager.getLogger();

    public static final String PROP_PHASED_DATA_PATH = "PHASED.TESTS.STORAGE.PATH";
    public static final String PROP_OUTPUT_DIR = "PHASED.TESTS.OUTPUT.DIR";
    public static final String PROP_SELECTED_PHASE = "PHASED.TESTS.PHASE";
    public static final String PROP_PHASED_TEST_DATABROKER = "PHASED.TESTS.DATABROKER";
    public static final String PROP_DISABLE_RETRY = "PHASED.TESTS.RETRY.DISABLED";
    public static final String PROP_MERGE_STEP_RESULTS = "PHASED.TESTS.REPORT.BY.PHASE_GROUP";
    public static final String PROP_TEST_SELECTION_BY_PROPERTIES = "PROP.TEST.SELECTION.BY.PROPERTIES";
    private static final String PROP_SCENARIO_EXPORTED_PREFIX = "PHASED.TESTS.STORAGE.SCENARIO.PREFIX";

    public static final String DEFAULT_CACHE_DIR = "phased_output";
    public static final String STD_CACHE_DIR = System.getProperty(PROP_OUTPUT_DIR, DEFAULT_CACHE_DIR);
    public static final String STD_STORE_DIR = "phased_tests";
    public static final String STD_STORE_FILE = "phaseData.properties";

    //Values for the DataProvider used in both Shuffled and Single run phases
    protected static final String STD_PHASED_GROUP_PREFIX = "phased-shuffledGroup_";
    protected static final String STD_PHASED_GROUP_SINGLE = "phased-singleRun";

    public static final String  STD_MERGE_STEP_ERROR_PREFIX = "Phased Error: Failure in step ";

    /**
     * The different states a step can assume in a scenario
     * <p>
     * <p>
     * Author : gandomi
     */
    public enum ScenarioState {
        CONTINUE, SKIP_NORESULT, SKIP_PREVIOUS_FAILURE,CONFIG_FAILURE;
    }

    protected static Properties phasedCache = new Properties();
    private static  Map<String, ScenarioContextData> scenarioContext = new HashMap<>();

    protected static Map<String, MethodMapping> methodMap = new HashMap<>();

    protected static Properties phaseContext = new Properties();

    private static PhasedDataBroker dataBroker = null;

    protected static Boolean mergedReportsActivated = Boolean.FALSE;

    protected static Boolean selectTestsByProducerMode = Boolean.FALSE;

    protected static final String SCENARIO_CONTEXT_PREFIX = System.getProperty(PROP_SCENARIO_EXPORTED_PREFIX, "[TC]");

    public static class MergedReportData {

        protected static LinkedHashSet<PhasedReportElements> prefix = new LinkedHashSet<>();
        protected static LinkedHashSet<PhasedReportElements> suffix = new LinkedHashSet<>();

        /**
         * Allows you to defined the generated name when phased steps are merged
         * for a scenario. If nothing is set we use the phase group.
         * <p>
         * Author : gandomi
         *
         * @param in_prefix A sorted set of report elements to be added as prefix to the
         *                  scenario name
         * @param in_suffix A sorted set of report elements to be added as suffix to the
         *                  scenario name
         */
        protected static void configureMergedReportName(LinkedHashSet<PhasedReportElements> in_prefix,
                LinkedHashSet<PhasedReportElements> in_suffix) {
            MergedReportData.prefix = in_prefix;
            MergedReportData.suffix = in_suffix;

        }

        /**
         * Resets the report configuration
         * <p>
         * Author : gandomi
         */
        public static void resetReport() {
            prefix.clear();
            suffix.clear();
        }
    }

    /**
     * @return the phasedCache
     */
    public static Properties getPhasedCache() {
        return phasedCache;
    }

    /**
     * @return the scenarioContext
     */
    protected static Map<String, ScenarioContextData> getScenarioContext() {
        return scenarioContext;
    }

    /**
     * @return the dataBroker
     */
    protected static PhasedDataBroker getDataBroker() {
        return dataBroker;
    }

    /**
     * @param dataBroker the dataBroker to set
     */
    public static void setDataBroker(Object dataBroker) {
        PhasedTestManager.dataBroker = (PhasedDataBroker) dataBroker;
    }

    /**
     * Initiaizes the databroker given the full class path of the implementation
     * of the interface {@code PhasedDataBroker}
     * <p>
     * Author : gandomi
     *
     * @param in_classPath The classpath for the implementation of the data broker
     * @throws PhasedTestConfigurationException Whenever there is a problem instantiating the Phased DataBroker
     *                                          class
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
                    "Error while fetching / instantiating the given PhasedDataBroker class " + in_classPath + ".", e);
        }

        if (!(l_dataBroker instanceof PhasedDataBroker)) {
            throw new PhasedTestConfigurationException("The given class was not an instance of PhasedDataBroker");
        }

        setDataBroker(l_dataBroker);

    }

    /**
     * This method clears the data broker
     * <p>
     * Author : gandomi
     */
    public static void clearDataBroker() {
        dataBroker = null;

    }

    /**
     * This method stores a phased test data in the cache. It will be stored
     * with the keys: "class, method, instance" and a value
     * <p>
     * Author : gandomi
     *
     * @param in_storeValue The value you want stored
     * @return The key that was used in storing the value
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
     * <p>
     * Author : gandomi
     *
     * @param in_storageKey A string that is added to the generated key for identification of
     *                      the stored data
     * @param in_storeValue The value we want to store
     * @return The key that was used in storing the value
     * @deprecated This method has been renamed. Please use
     * {@link #produce(String, String)} instead.
     */
    @Deprecated public static String produceWithKey(String in_storageKey, String in_storeValue) {
        final String l_className = StackTraceManager.fetchCalledBy().getClassName();
        final String l_fullId = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(), l_className,
                in_storageKey);
        return storePhasedCache(l_fullId, in_storeValue);
    }

    /**
     * Stores a value with the given key. We include the class as prefix.
     * <p>
     * Author : gandomi
     *
     * @param in_storageKey A string that is added to the generated key for identification of
     *                      the stored data
     * @param in_storeValue The value we want to store
     * @return The key that was used in storing the value
     */
    public static String produce(String in_storageKey, String in_storeValue) {
        final String l_className = StackTraceManager.fetchCalledBy().getClassName();
        final String l_fullId = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(), l_className,
                in_storageKey);
        return storePhasedCache(l_fullId, in_storeValue);
    }

    /**
     * This method generates the identifier for a producer/consumer used for
     * storing in the cache
     * <p>
     * Author : gandomi
     *
     * @param in_idInPhaseContext The id of the step in the context
     * @param in_storageKey       An additional identifier for storing the data
     * @return The identity of the storage key as stored in the cache
     */
    protected static String generateStepKeyIdentity(final String in_idInPhaseContext, String in_storageKey) {

        return generateStepKeyIdentity(in_idInPhaseContext, in_idInPhaseContext, in_storageKey);
    }

    /**
     * This method generates the identifier for a producer/consumer
     * <p>
     * Author : gandomi
     *
     * @param in_idInPhaseContext The id of the step in the context
     * @param in_idPrefixToStore  The prefix of the full name for storing values. Usually the class
     *                            full name
     * @param in_storageKey       An additional identifier for storing the data
     * @return The identity of the storage key as stored in the cache
     */
    protected static String generateStepKeyIdentity(final String in_idInPhaseContext, final String in_idPrefixToStore,
            String in_storageKey) {
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
     * <p>
     * Author : gandomi
     *
     * @param in_idInPhaseContext The id of the step in the context
     * @return The identity of the storage key as stored in the cache
     */
    protected static String generateStepKeyIdentity(String in_idInPhaseContext) {
        return generateStepKeyIdentity(in_idInPhaseContext, null);
    }

    /**
     * Stores a value in the cache
     * <p>
     * Author : gandomi
     *
     * @param in_storeKey   The key to be used for storing the value
     * @param in_storeValue The value to be stored
     * @return The key used for storing the value
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
     * <p>
     * Author : gandomi
     *
     * @param in_stepName The step name aka method name (not class name nor arguments) that
     *                    stored a value in the current scenario
     * @return The value store by the method
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

        return fetchStoredConsumable(l_storageKey, l_calledElement.toString());
    }

    /**
     * Returns the value stored in the context, and requested by a test.
     * <p>
     * Author : gandomi
     *
     * @param in_consumableKey The key identifier for the consumable
     * @param in_calledByTest  The string representation of the test accessing the consumable
     * @return The value for the given consumable. If not found a
     * PhasedTestException is thrown
     */
    public static String fetchStoredConsumable(final String in_consumableKey, String in_calledByTest) {
        if (!phasedCache.containsKey(in_consumableKey)) {
            throw new PhasedTestException(
                    "The given consumable " + in_consumableKey + " requested by " + in_calledByTest
                            + " was not available.");
        }

        return phasedCache.getProperty(in_consumableKey);
    }

    /**
     * Given a step in the Phased Test it fetches the value committed for that
     * test.
     * <p>
     * Author : gandomi
     *
     * @param in_storageKey A key that was used to store the value in this scenario
     * @return The value that was stored
     * @deprecated This method has been renamed. Please use
     * {@link #consume(String)} instead
     */
    @Deprecated public static String consumeWithKey(String in_storageKey) {
        final StackTraceElement l_fetchCalledBy = StackTraceManager.fetchCalledBy();

        String l_realKey = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(),
                l_fetchCalledBy.getClassName(), in_storageKey);

        return fetchStoredConsumable(l_realKey, l_fetchCalledBy.toString());
    }

    /**
     * Given a step in the Phased Test it fetches the value committed for that
     * test.
     * <p>
     * Author : gandomi
     *
     * @param in_storageKey A key that was used to store the value in this scenario
     * @return The value that was stored
     */
    public static String consume(String in_storageKey) {
        final StackTraceElement l_fetchCalledBy = StackTraceManager.fetchCalledBy();

        String l_realKey = generateStepKeyIdentity(StackTraceManager.fetchCalledByFullName(),
                l_fetchCalledBy.getClassName(), in_storageKey);

        return fetchStoredConsumable(l_realKey, l_fetchCalledBy.toString());
    }

    /**
     * cleans the cache of the PhasedManager
     * <p>
     * Author : gandomi
     */
    protected static void clearCache() {
        phasedCache.clear();

        methodMap = new HashMap<>();

        phaseContext.clear();
        scenarioContext.clear();
    }

    /**
     * Exports the cache into a standard PhasedTest property file.
     * <p>
     * Author : gandomi
     *
     * @return The file that was used for storing the phase cache
     */
    public static File exportPhaseData() {

        File l_exportCacheFile = fetchExportFile();

        return exportContext(l_exportCacheFile);
    }

    /**
     * Returns the export file that will be used for exporting the PhaseCache
     * <p>
     * Author : gandomi
     *
     * @return A file that matches the location of the export file
     */
    public static File fetchExportFile() {
        File l_exportCacheFile;
        if (System.getProperties().containsKey(PROP_PHASED_DATA_PATH)) {
            l_exportCacheFile = new File(System.getProperty(PROP_PHASED_DATA_PATH));

        } else {
            l_exportCacheFile = new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR), STD_STORE_FILE);
        }
        return l_exportCacheFile;
    }

    /**
     * Exports the Phase cache and the scenario context into the given file
     * <p>
     * Author : gandomi
     *
     * @param in_file that will contain the phase cache and scenario contexts
     * @return The file used for storing the Phase Context.
     */
    protected static File exportContext(File in_file) {

        log.info(PHASED_TEST_LOG_PREFIX + " Exporting Phased Testing data to " + in_file.getPath());

        Properties lt_transformedScenarios = new Properties();
        scenarioContext.forEach((key, value) -> lt_transformedScenarios.put(attachContextFlag(key.toString()), value.exportToString()));

        try (FileWriter fw = new FileWriter(in_file)) {

            getPhasedCache().store(fw, null);
            lt_transformedScenarios.store(fw, null);

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
     * Imports a file and stored the properties in the phased cache and in the
     * scenario context.
     * <p>
     * Author : gandomi
     *
     * @param in_phasedTestFile A file that contains the phase cache data from a previous phase
     * @return A Properties object with the phase cache data from the previous
     * phase
     */
    protected static Properties importContext(File in_phasedTestFile) {
        log.info(PHASED_TEST_LOG_PREFIX + "Importing phase cache.");
        Properties lr_importedProperties = new Properties();
        try (InputStream input = new FileInputStream(in_phasedTestFile)) {

            // load a properties file
            lr_importedProperties.load(input);
        } catch (IOException e) {
            log.error("Error when loading file " + in_phasedTestFile);
            throw new PhasedTestException("Error when loading file " + in_phasedTestFile.getPath() + ".", e);
        }

        //Import produced data into phase cache
        lr_importedProperties.stringPropertyNames().stream().filter(k -> !k.startsWith(SCENARIO_CONTEXT_PREFIX))
                .forEach(fk -> phasedCache.put(fk, lr_importedProperties.get(fk)));

        //Import scenario contexts into scenario context
        lr_importedProperties.stringPropertyNames().stream().filter(k -> k.startsWith(SCENARIO_CONTEXT_PREFIX)).forEach(
                fk -> scenarioContext
                        .put(fk.substring(SCENARIO_CONTEXT_PREFIX.length()), new ScenarioContextData(
                                (String) lr_importedProperties.get(fk))));

        return lr_importedProperties;
    }

    /**
     * Loads the Phased Test data from the standard location which is by default
     * {@value #DEFAULT_CACHE_DIR}/{@value #STD_STORE_DIR}/{@value #STD_STORE_FILE}
     * <p>
     * Author : gandomi
     *
     * @return A Properties object with the phase cache data from the previous
     * phase
     */
    protected static Properties importPhaseData() {
        File l_importCacheFile = null;

        if (dataBroker == null) {

            if (System.getProperties().containsKey(PROP_PHASED_DATA_PATH)) {
                l_importCacheFile = new File(System.getProperty(PROP_PHASED_DATA_PATH));

            } else {
                l_importCacheFile = new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR), STD_STORE_FILE);
                log.warn(PHASED_TEST_LOG_PREFIX + " The system property " + PROP_PHASED_DATA_PATH
                        + " not set. Fetching Phased Test data from " + l_importCacheFile.getPath());
            }
        } else {
            log.info(PHASED_TEST_LOG_PREFIX + "Fetching cache through DataBroker");
            l_importCacheFile = dataBroker.fetch(STD_STORE_FILE);
        }
        return importContext(l_importCacheFile);

    }

    /**
     * Calculates the data providers for the current method and test context
     * <p>
     * Author : gandomi
     *
     * @param in_method The step/method for which we want to fond out the data provider
     * @return A two-dimensional array of all the data providers attached to the
     * current step/method
     */
    public static Object[][] fetchProvidersShuffled(Method in_method) {
        String l_methodFullName = ClassPathParser.fetchFullName(in_method);
        return fetchProvidersShuffled(l_methodFullName);
    }

    /**
     * Returns the provider for shuffling tests. In general the values are
     * Shuffle group prefix + Nr of steps before the Phase Event and the number
     * of steps after the event.
     * <p>
     * Author : gandomi
     *
     * @param in_methodFullName The full name of the method used for identifying it in the phase
     *                          context
     * @return A two-dimensional array of all the data providers attached to the
     * current step/method
     */
    public static Object[][] fetchProvidersShuffled(String in_methodFullName) {

        return fetchProvidersShuffled(in_methodFullName, Phases.getCurrentPhase());
    }

    /**
     * Returns the provider for shuffling tests. In general the values are
     * Shuffle group prefix + Nr of steps before the Phase Event and the number
     * of steps after the event.
     * <p>
     * Author : gandomi
     *
     * @param in_methodFullName The full name of the method used for identifying it in the phase
     *                          context
     * @param in_phasedState    The phase state for which we should retrieve the parameters. The
     *                          parameters will be different based on the phase.
     * @return A two-dimensional array of all the data providers attached to the
     * current step/method
     */
    public static Object[][] fetchProvidersShuffled(String in_methodFullName, Phases in_phasedState) {

        final MethodMapping l_methodMapping = methodMap.get(in_methodFullName);
        Object[][] l_objectArrayPhased = new Object[l_methodMapping.nrOfProviders][1];

        for (int rows = 0; rows < l_methodMapping.nrOfProviders; rows++) {

            int lt_nrBeforePhase = in_phasedState.equals(Phases.PRODUCER) ? (l_methodMapping.totalClassMethods
                    - rows) : rows;

            int lt_nrAfterPhase = l_methodMapping.totalClassMethods - lt_nrBeforePhase;

            StringBuilder lt_sb = new StringBuilder(STD_PHASED_GROUP_PREFIX);

            lt_sb.append(lt_nrBeforePhase);
            lt_sb.append("_");
            lt_sb.append(lt_nrAfterPhase);

            l_objectArrayPhased[rows][0] = lt_sb.toString();
        }

        //Fetch class level data providers
        Object[][] l_userDefinedDataProviders = fetchDataProviderValues(l_methodMapping.declaredClass);

        //Merge
        Object[][] lr_dataProviders = dataProvidersCrossJoin(l_objectArrayPhased, l_userDefinedDataProviders);

        log.debug(PhasedTestManager.PHASED_TEST_LOG_PREFIX + "returning provider for method " + in_methodFullName);
        return lr_dataProviders;
    }

    /**
     * Returns the data provider for a single phase
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step for which we want to get the data providers for
     * @return An array containing the data providers for the method. Otherwise
     * an empty array
     */
    public static Object[] fetchProvidersSingle(Method in_method) {
        log.debug("Returning provider for method " + ClassPathParser.fetchFullName(in_method));

        if (Phases.PRODUCER.isSelected() && isExecutedInProducerMode(in_method)) {

            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        if (Phases.CONSUMER.isSelected() && !isExecutedInProducerMode(in_method)) {
            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        if (Phases.NON_PHASED.isSelected() && in_method.getDeclaringClass().getAnnotation(PhasedTest.class)
                .executeInactive()) {
            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        return new Object[] {};
    }

    /**
     * This method calculates how often a class should be run.
     * <p>
     * Author : gandomi
     *
     * @param in_classMethodMap A map of a class and it is methods (A scenario and its steps)
     * @return A map letting us know that for a the given method how often it
     * will be executed in the current phase
     */
    public static Map<String, MethodMapping> generatePhasedProviders(Map<Class, List<String>> in_classMethodMap) {

        return generatePhasedProviders(in_classMethodMap, Phases.getCurrentPhase());

    }

    /**
     * This method calculates how often a scenario should be run, given the
     * steps/methods it has.
     * <p>
     * Author : gandomi
     *
     * @param in_classMethodMap A map of a class and it is methods (A scenario and its steps)
     * @param in_phaseState     The phase in which we are
     * @return A map letting us know that for a the given method how often it
     * will be executed in the current phase
     */
    public static Map<String, MethodMapping> generatePhasedProviders(Map<Class, List<String>> in_classMethodMap,
            Phases in_phaseState) {
        methodMap = new HashMap<>();

        for (Class lt_class : in_classMethodMap.keySet()) {

            List<String> lt_methodList = in_classMethodMap.get(lt_class);

            if (in_phaseState.equals(Phases.CONSUMER)) {
                Collections.reverse(lt_methodList);
            }

            for (int i = 0; i < in_classMethodMap.get(lt_class).size(); i++) {
                methodMap.put(lt_methodList.get(i),
                        new MethodMapping(lt_class, in_classMethodMap.get(lt_class).size() - i,
                                in_classMethodMap.get(lt_class).size()));

            }
        }
        return methodMap;

    }

    /**
     * Updates the context with the method and its current Phase Group ID
     * <p>
     * Author : gandomi
     *
     * @param in_methodFullName The full name of the method
     * @param in_phasedGroupId  The Id of the phase group
     */
    public static void storePhasedContext(String in_methodFullName, String in_phasedGroupId) {
        phaseContext.put(in_methodFullName, in_phasedGroupId);

    }

    /**
     * For testing purposes only. Used when we want to test the consumer
     * <p>
     * Author : gandomi
     *
     * @param in_testMethod A test method
     * @param in_phaseGroup A phase group Id
     * @param in_storedData The data to be stored for the scenario step
     * @return The key used to store the value in the cache
     */
    protected static String storeTestData(Method in_testMethod, String in_phaseGroup, String in_storedData) {
        phaseContext.put(ClassPathParser.fetchFullName(in_testMethod), in_phaseGroup);
        return storePhasedCache(generateStepKeyIdentity(ClassPathParser.fetchFullName(in_testMethod)), in_storedData);

    }

    /**
     * For testing purposes only. Used when we want to test the consumer
     * <p>
     * Author : gandomi
     *
     * @param in_class      A test method
     * @param in_phaseGroup A phase group Id
     * @param in_storedData The data to be stored for the scenario step
     * @return The key used to store the value in the cache
     */
    protected static String storeTestData(Class in_class, String in_phaseGroup, boolean in_storedData) {
        ScenarioContextData l_scenarioContext = new ScenarioContextData();
        l_scenarioContext.passed =  in_storedData;

        return storeTestData(in_class, in_phaseGroup, l_scenarioContext);

    }

    /**
     * For testing purposes only. Used when we want to test the consumer
     * <p>
     * Author : gandomi
     *
     * @param in_class      A test method
     * @param in_phaseGroup A phase group Id
     * @param in_storedData The data to be stored for the scenario step
     * @return The key used to store the value in the cache
     */
    protected static String storeTestData(Class in_class, String in_phaseGroup, ScenarioContextData in_storedData) {
        phaseContext.put(in_class.getTypeName(), in_phaseGroup);

        final String lr_storedKey = generateStepKeyIdentity(in_class.getTypeName());
        ScenarioContextData l_scenarioContext = new ScenarioContextData();

        scenarioContext.put(lr_storedKey, in_storedData);
        return lr_storedKey;
    }

    /**
     * Basically lets us know if we execute the given method in producer mode.
     * We look at the attribute value phaseEnd. This method is specifically for
     * the Single Mode.
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step we want to know its phase location
     * @return true if the step is anywhere before the phase limit
     */
    public static boolean isExecutedInProducerMode(Method in_method) {

        if (PhasedTestManager.isPhasedTestSingleMode(in_method)) {
            final Class<?> l_declaringClass = in_method.getDeclaringClass();
            List<Method> l_myMethods = Arrays.stream(l_declaringClass.getMethods())
                    .filter(PhasedTestManager::isPhasedTest).collect(Collectors.toList());

            Comparator<Method> compareMethodByName = (Method m1, Method m2) -> m1.getName().compareTo(m2.getName());

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
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step which we want to know if it is the last step in
     *                  the current phase
     * @return True if the test is before or in the phase End.
     */
    protected static boolean isPhaseLimit(Method in_method) {

        return in_method.isAnnotationPresent(PhaseEvent.class);
    }

    /**
     * This method tells us if the method is a valid phased test. This is done
     * by seeing if the annotation PhasedStep is on the method, and if the
     * annotation PhasedTest is on the class
     * <p>
     * Author : gandomi
     *
     * @param in_method Any test method that is being executed
     * @return true if The annotations PhasedTest and PhasedStep are present
     */
    public static boolean isPhasedTest(Method in_method) {
        return isPhasedTest(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the class is a PhasedTestClass
     * <p>
     * Author : gandomi
     *
     * @param in_class Any class that contains tests
     * @return True if the class is a phased test scenario
     */
    @SuppressWarnings("unchecked") public static boolean isPhasedTest(Class in_class) {
        return in_class.isAnnotationPresent(PhasedTest.class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * consequently in two phases
     *
     * @param in_method Any test method
     * @return True if the test step/method is part of a SingleRun Phase Test
     * scenario
     */
    protected static boolean isPhasedTestSingleMode(Method in_method) {
        return isPhasedTest(in_method) && !isPhasedTestShuffledMode(in_method);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * consequently in two phases
     *
     * @param in_class Any class that contains tests
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
     * @param in_method A test method
     * @return True if the given test method/step is part of a Shuffled Phased
     * Test scenario
     */
    protected static boolean isPhasedTestShuffledMode(Method in_method) {
        return isPhasedTest(in_method) && isPhasedTestShuffledMode(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed
     * in a Shuffled manner. For a test with 3 steps the test will be executed 6
     * times in total
     *
     * @param in_class A test class/scenario
     * @return True if the given test scenario is a Shuffled Phased Test
     * scenario
     */
    protected static boolean isPhasedTestShuffledMode(Class in_class) {
        return isPhasedTest(in_class) && ((PhasedTest) in_class.getAnnotation(PhasedTest.class)).canShuffle() && Phases
                .getCurrentPhase().hasSplittingEvent();
    }

    /**
     * This method provides an ID for the scenario given the ITestNGResult. This
     * is assembled using the Classname + the PhaseGroup
     * <p>
     * Author : gandomi
     *
     * @param in_testNGResult A TestNG Test Result object
     * @return The identity of the scenario
     */
    public static String fetchScenarioName(ITestResult in_testNGResult) {
        StringBuilder sb = new StringBuilder(
                in_testNGResult.getMethod().getConstructorOrMethod().getMethod().getDeclaringClass().getTypeName());
        return sb.append(ClassPathParser.fetchParameterValues(in_testNGResult)).toString();
    }

    /**
     * This method logs the stage result of the Phased Test Group. The key will
     * be the class including the phase test group. It allows us to know if the
     * test is allowed to continue.
     * <p>
     * Once the context is logged as false for a test it remains false
     * <p>
     * Author : gandomi
     *
     * @param in_testResult The test result
     */
    public static void scenarioStateStore(ITestResult in_testResult) {

        final String l_scenarioName = fetchScenarioName(in_testResult);
        final String l_stepName = ClassPathParser.fetchFullName(in_testResult);

        if (scenarioContext.containsKey(l_scenarioName)) {

            scenarioContext.get(l_scenarioName).synchronizeState(in_testResult);

        } else {
            ScenarioContextData l_scenarioContextData = new ScenarioContextData();
            l_scenarioContextData.synchronizeState(in_testResult);
            scenarioContext.put(l_scenarioName,l_scenarioContextData);
        }

    }

    private static String attachContextFlag(String in_scenarioName) {
        StringBuilder sb = new StringBuilder(SCENARIO_CONTEXT_PREFIX);
        return sb.append(in_scenarioName).toString();
    }

    /**
     * This method lets us know if we should continue with the scenario. If the context is not yet stored for the
     * scenario, we should continue. In the case that the value for the scenario is equal to the current step name, we
     * will continue.
     * <p>
     * There is one Exception. If the cause of the failure is the current test.
     *
     * <table>
     * <caption>Use Cases for Scenario States</caption>
     * <tr>
     * <th>CASE</th>
     * <th>Phase</th>
     * <th>Current step Nr</th>
     * <th>Previous Step Result</th>
     * <th>Expected result</th>
     * <th>MERGED RESULT</th></tr>
     * <tr>
     * <td>1</td>
     * <td>Producer/NonPhased</td>
     * <td>1</td>
     * <td>N/A</td>
     * <td>Continue</td>
     * <td>PASSED</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Producer/NonPhased</td>
     * <td>&gt; 1</td>
     * <td>FAILED</td>
     * <td>SKIP</td>
     * <td>FAILED</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Producer/NonPhased</td>
     * <td>&gt; 1</td>
     * <td>PASSED</td>
     * <td>Continue</td>
     * <td>PASSED</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>Consumer</td>
     * <td>1</td>
     * <td>N/A</td>
     * <td>Continue</td>
     * <td>PASSED</td>
     * </tr>
     * <tr>
     * <td>5</td>
     * <td>Consumer</td>
     * <td>&gt; 1</td>
     * <td>PASSED</td>
     * <td>Continue</td>
     * <td>PASSED</td>
     * </tr>
     * <tr>
     * <td>6</td>
     * <td>Consumer</td>
     * <td>&gt; 1</td>
     * <td>FAILED/SKIPPED</td>
     * <td>SKIP</td>
     * <td>FAILED</td>
     * </tr>
     * <tr>
     * <td>7</td>
     * <td>Consumer</td>
     * <td>&gt; 1</td>
     * <td>N/A</td>
     * <td>SKIP</td>
     * <td>SKIP</td>
     * </tr>
     * <tr>
     * <td>8</td>
     * <td>Any</td>
     * <td>Any</td>
     * <td>N/A</td>
     * <td>SKIP - not forced</td>
     * <td>SKIP</td>
     * </tr>
     * </table>
     * <p>
     * Author : gandomi
     *
     * @param in_testResult The test result
     * @return A decision regarding the continuation of the scenario. We also provide the reasons as to why the skipping
     * happens. ScenarioState.SKIP_NORESULT returns when we should skip due to non-execution of a previous step.
     * SKIP_PREVIOUS_FAILURE is returned when we are supposed o skip because of a failure in a previous step
     */
    public static ScenarioState scenarioStateDecision(ITestResult in_testResult) {
        final String l_scenarioName = fetchScenarioName(in_testResult);
        //Case      PHASE               STEP    Previous_Step       Expected_Result
        //Case 1    Producer/NonPhased  1       N/A                 Continue    true    testStateIstKeptBetweenPhases_Continue
        //Case 2    Producer/NonPhased  > 1     FAILED/Skipped      SKIP                false
        //Case 3    Producer/NonPhased  > 1     Passed              Continue            true    testStateIstKeptBetweenPhases_Continue
        //Case 4    Consumer            1       N/A                 Continue            true    testStateIstKeptBetweenPhases_Continue
        //Case 5    Cosnumer            > 1     Passed              Continue            true
        //Case 6    Cosnumer            > 1     Failed/Skipped      SKIP                false
        //Case 7    Cosnumer            > 1     N/A                 SKIP                false
        //Case 8    ANY                 ANY     N/A                 SKIP (not forced)

        //#43 to change this to false
        //If scenario has not yet been executed in the current phase
        if (!getScenarioContext().containsKey(l_scenarioName)) {
            //True only if we are executing end to end 0_X
            return hasStepsExecutedInProducer(in_testResult) ? ScenarioState.SKIP_NORESULT : ScenarioState.CONTINUE;
        }

        if (in_testResult.getThrowable() instanceof Throwable) {
            return ScenarioState.CONFIG_FAILURE;
        }
        return getScenarioContext().get(l_scenarioName).passed
                ? ScenarioState.CONTINUE : ScenarioState.SKIP_PREVIOUS_FAILURE;
    }

    /**
     * Creates a standard report name. I.e. it provides a name as how the test
     * scenario and phased group should be represented.
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestRessult object
     * @return A string prefixed with the scenario name and suffixed with the
     * phaseGroup
     */
    public static String fetchTestNameForReport(ITestResult in_testResult) {

        final String l_stdItemeparator = "__";

        //Adding the prefixes
        StringBuilder sb = new StringBuilder();
        for (PhasedReportElements lt_pre : MergedReportData.prefix) {
            sb.append(lt_pre.fetchElement(in_testResult));
            sb.append(l_stdItemeparator);
        }

        //Adding the required values : the phaseGroup
        sb.append(PhasedReportElements.PHASE_GROUP.fetchElement(in_testResult));

        //Adding the suffixes
        for (PhasedReportElements lt_pre : MergedReportData.suffix) {

            final String lt_elmentValue = lt_pre.fetchElement(in_testResult);
            if (!lt_elmentValue.isEmpty()) {
                sb.append(l_stdItemeparator);
                sb.append(lt_elmentValue);
            }

        }

        return sb.toString();
    }

    /**
     * This method calculates the duration in milliseconds for a phased test
     * scenario. Given a List of ITestNGResult related to that scenario, it
     * makes a sum of the start and end milliseconds of its steps.
     * <p>
     * Throws an {@link IllegalArgumentException} when the given List is null or
     * empty.
     * <p>
     * Throws an {@link IllegalArgumentException} when the given List is not
     * from the same Phase group or scenario
     * <p>
     * Author : gandomi
     *
     * @param in_resultList A List of ITestNGResults related to a given scenario
     * @return A long value representing the duration in milliseconds
     */
    protected static long fetchDurationMillis(List<ITestResult> in_resultList) {
        if (in_resultList == null || in_resultList.isEmpty()) {
            throw new IllegalArgumentException("The given result list of TestNGResults is either null or empty");
        }

        if (!in_resultList.stream()
                .allMatch(t -> t.getMethod().getRealClass().equals(in_resultList.get(0).getMethod().getRealClass()))) {
            throw new IllegalArgumentException("The given tests are not of the same Class");
        }

        if (!in_resultList.stream()
                .allMatch(t -> Arrays.equals(t.getParameters(), in_resultList.get(0).getParameters()))) {
            throw new IllegalArgumentException("The given tests are not of the same Phase Group");
        }

        return in_resultList.stream().mapToLong(t -> (t.getEndMillis() - t.getStartMillis())).sum();
    }

    /**
     * This method creates a step name by prefixing the step name with the phase
     * group
     * <p>
     * Author : gandomi
     *
     * @param result The TestNGResult object
     * @return A string representation of the test name
     */
    protected static String fetchPhasedStepName(ITestResult result) {
        if (result.getParameters().length == 0) {
            throw new IllegalArgumentException(
                    "No parameters found. The given test result for test " + ClassPathParser.fetchFullName(result)
                            + " does not seem to have been part of a Phased Test");
        }

        StringBuilder sb = new StringBuilder(concatenateParameterArray(result.getParameters()));

        sb.append('_');
        sb.append(result.getName());
        return sb.toString();
    }

    /**
     * This method is used in the context of merged reports. We use this to
     * enrich the exception message when merging reports.
     * <p>
     * An {@link IllegalArgumentException} is thrown if the given
     * in_failedTestResult is not failed.
     * <p>
     * Author : gandomi
     *
     * @param in_failedTestResult The exception we want to wrap.
     */
    public static void generateStepFailure(ITestResult in_failedTestResult) {
        if (in_failedTestResult.getStatus() != ITestResult.FAILURE) {
            throw new IllegalArgumentException("The given Test Result for "
                    + in_failedTestResult.getMethod().getMethodName() + " is not a failed test.");
        }
        Throwable l_thrownException = in_failedTestResult.getThrowable();
        StringBuilder sb = new StringBuilder();
        if (l_thrownException.getMessage() != null) {
            sb.append(l_thrownException.getMessage());
            sb.append(" ");
        }
        sb.append("[Failed at step : ");
        sb.append(in_failedTestResult.getMethod().getMethodName());
        sb.append(" - ");
        sb.append(Phases.getCurrentPhase().toString());
        sb.append("]");

        PhasedTestManager.changeExceptionMessage(l_thrownException, sb.toString());

    }

    /**
     * Given two data provider arrays, this method performs a scalar join of the
     * data providers. For two objects :
     * <p>
     * Dataprovider1:
     * <table>
     * <caption>DataProvider1</caption>
     * <tr>
     * <td>A</td>
     * </tr>
     * </table>
     * <p>
     * Dataprovider2:
     * <table>
     * <caption>DataProvider2</caption>
     * <tr>
     * <td>X</td>
     * </tr>
     * <tr>
     * <td>Y</td>
     * </tr>
     * </table>
     * <p>
     * We will get:
     * <table>
     * <caption>CrossJoined DataProviders</caption>
     * <tr>
     * <td>A</td>
     * <td>X</td>
     * </tr>
     * <tr>
     * <td>A</td>
     * <td>Y</td>
     * </tr>
     * </table>
     * <p>
     * Author : gandomi
     *
     * @param in_providerSeriesLeft  The data provider data which is used on the left side of the join
     * @param in_providerSeriesRight The data provider data which is used on the left right of the join
     * @return A merged array that contains elements of both arrays
     */
    public static Object[][] dataProvidersCrossJoin(Object[][] in_providerSeriesLeft,
            Object[][] in_providerSeriesRight) {

        if ((in_providerSeriesRight == null) || (in_providerSeriesRight.length == 0)) {
            return in_providerSeriesLeft;
        }

        //Calculate dimensions
        int l_totalNrOfLines = in_providerSeriesLeft.length * in_providerSeriesRight.length;
        int l_totalNrOfColumns = in_providerSeriesLeft[0].length + in_providerSeriesRight[0].length;

        //initialize return object
        Object[][] lr_dataprovider = new Object[l_totalNrOfLines][l_totalNrOfColumns];

        //fill return object
        int line = 0;
        for (Object[] lineLeft : in_providerSeriesLeft) {
            for (Object[] lineRight : in_providerSeriesRight) {
                System.arraycopy(lineLeft, 0, lr_dataprovider[line], 0, lineLeft.length);
                System.arraycopy(lineRight, 0, lr_dataprovider[line], lineLeft.length, lineRight.length);
                line++;
            }
        }
        return lr_dataprovider;
    }

    /**
     * With this method we activate the merged reports
     * <p>
     * Author : gandomi
     */
    public static void activateMergedReports() {
        mergedReportsActivated = true;
    }

    /**
     * With this method we activate the merged reports
     * <p>
     * Author : gandomi
     */
    public static void deactivateMergedReports() {
        mergedReportsActivated = false;
    }

    /**
     * With this method we activate the selection of tests by the stored phase context
     * <p>
     * Author : gandomi
     */
    public static void activateTestSelectionByProducerMode() {
        selectTestsByProducerMode = true;
    }

    /**
     * With this method we activate the selection of tests by the stored phase context
     * <p>
     * Author : gandomi
     */
    public static void deactivateTestSelectionByProducerMode() {
        selectTestsByProducerMode = false;
    }

    public static Boolean isTestsSelectedByProducerMode() {
        return selectTestsByProducerMode;
    }

    /**
     * This method fetches the declared DataProvider values related to a class
     * <p>
     * Author : gandomi
     *
     * @param in_phasedTestClass A Phased Test class
     * @return The data providers attached to the class. An empty array is
     * returned if there is no data provider defined at a class level
     */
    protected static Object[][] fetchDataProviderValues(Class<?> in_phasedTestClass) {

        final Object[][] lr_defaultReturnValue = new Object[0][0];
        if (!in_phasedTestClass.isAnnotationPresent(Test.class)) {
            log.warn(PhasedTestManager.PHASED_TEST_LOG_PREFIX
                    + "The given phased test class does not have the Test annotation on it. Data Providers for Phased Tests can only be considered at that level.");

            return lr_defaultReturnValue;
        }

        //Fetch the data provider class and name
        Class<?> l_dataProviderClass = in_phasedTestClass.getAnnotation(Test.class).dataProviderClass();
        String l_dataProviderName = in_phasedTestClass.getAnnotation(Test.class).dataProvider();

        //No data provider set returning empty array
        if (l_dataProviderName.isEmpty()) {
            return lr_defaultReturnValue;
        }

        if (l_dataProviderName.equals(PhasedTestManager.STD_PHASED_GROUP_SINGLE)
                || l_dataProviderName.startsWith(PhasedTestManager.STD_PHASED_GROUP_PREFIX)) {
            return lr_defaultReturnValue;
        }

        if (l_dataProviderClass.equals(PhasedDataProvider.class)) {
            return lr_defaultReturnValue;
        }

        //If the data provider class is equal to Object then i is declared in he currenttt class
        if (l_dataProviderClass.getTypeName().equals(Object.class.getTypeName())) {
            l_dataProviderClass = in_phasedTestClass;
        }

        //Fetch the dataprovider method
        Method m = Arrays.stream(l_dataProviderClass.getDeclaredMethods())
                .filter(a -> a.isAnnotationPresent(DataProvider.class))
                .filter(f -> f.getDeclaredAnnotation(DataProvider.class).name().equals(l_dataProviderName))
                .findFirst().orElse(null);

        if (m != null) {
            //In case of private data providers
            m.setAccessible(true);
        } else {
            throw new PhasedTestConfigurationException(
                    "No method found which matched the data provider class " + l_dataProviderClass.getTypeName()
                            + " or data provider name " + l_dataProviderName);
        }

        try {
            return (Object[][]) m.invoke(l_dataProviderClass.newInstance(), new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException e) {
            log.error(PhasedTestManager.PHASED_TEST_LOG_PREFIX
                    + "Problem when fetching the user defined data providers.");
            throw new PhasedTestConfigurationException("Unable to call thee data provider method", e);
        }
    }

    /**
     * Given an array of Objects, we concatenate them into a simple String
     * <p>
     * Author : gandomi
     *
     * @param in_values an array of objects that can be transformed to a string
     * @return a concatenation of the values. Otherwise empty String
     */
    protected static String concatenateParameterArray(Object[] in_values) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < in_values.length; i++) {

            sb.append(i == 0 ? "" : "__");

            sb.append(in_values[i]);

        }

        return sb.toString();
    }

    /**
     * Checks if the merged report is activated
     * <p>
     * Author : gandomi
     *
     * @return TRUE Iff the value is true otherwise it returns false
     */
    public static boolean isMergedReportsActivated() {
        return mergedReportsActivated == Boolean.TRUE;
    }

    /**
     * This method changes the message within the given Exception
     * <p>
     * Author : gandomi
     *
     * @param in_exception  An exception that has been thrown.
     * @param in_newMessage The new message that should replace the old one
     */
    public static void changeExceptionMessage(Throwable in_exception, String in_newMessage) {

        try {
            Class<?> l_changeClass = in_exception.getClass();

            //parse tree to reach the thowable class
            while (l_changeClass.getSuperclass() != Object.class) {
                l_changeClass = l_changeClass.getSuperclass();
            }

            //Manipulate field detail message
            Field exceptionMessage = l_changeClass.getDeclaredField("detailMessage");
            exceptionMessage.setAccessible(true);
            exceptionMessage.set(in_exception, in_newMessage);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                | SecurityException e) {

            throw new PhasedTestConfigurationException(
                    "We were unable to change the message in the thrown exception " + in_exception.getClass().getName(),
                    e);
        }

    }

    /**
     * Given a phased test method and and its phase group, lets you know if it
     * has ssteps executed in the producer phase
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestNG Test result
     * @return true if we are in consumer, and we are not a 0_X phase group that
     * is executed end to end in the consumer phase
     */
    public static boolean hasStepsExecutedInProducer(ITestResult in_testResult) {
        return hasStepsExecutedInProducer(in_testResult, Phases.getCurrentPhase());

    }

    /**
     * Given a phased test method and and its phase group, lets you know if it
     * has ssteps executed in the producer phase
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestNG Test result
     * @param in_phase      The phase in which we are currently.
     * @return true if we are in consumer, and we are not a 0_X phase group that
     * is executed end to end in the consumer phase
     */
    public static boolean hasStepsExecutedInProducer(ITestResult in_testResult, Phases in_phase) {
        return (in_phase.equals(Phases.CONSUMER) && (fetchNrOfStepsBeforePhaseChange(in_testResult) > 0));
    }

    /**
     * Given a string representing the phase group, returns the number of steps
     * planned before a phase change
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A test result object containing the necessary analysis data
     * @return The number of steps planned before a phase change. If we are
     * non-phased we return 0
     */
    public static Integer fetchNrOfStepsBeforePhaseChange(ITestResult in_testResult) {

        if (isPhasedTestShuffledMode(in_testResult.getMethod().getConstructorOrMethod().getMethod())) {

            final String l_phaseGroup = in_testResult.getParameters()[0].toString();

            if (!l_phaseGroup.startsWith(STD_PHASED_GROUP_PREFIX)) {
                throw new PhasedTestException("The phase group of this test does not seem correct: " + l_phaseGroup);
            }

            String l_numberString = l_phaseGroup.substring(STD_PHASED_GROUP_PREFIX.length(),
                    l_phaseGroup.indexOf("_", STD_PHASED_GROUP_PREFIX.length()));
            return Integer.valueOf(l_numberString);
        } else {

            return 1;
        }
    }

    /**
     * Extracts all the tests that have been executed before the current phase.
     * This will later be used to create the test list to be executed
     * <p>
     * Author : gandomi
     *
     * @return A set of class paths
     */
    public static Set<String> fetchExecutedPhasedClasses() {

        return getScenarioContext().keySet().stream().map(PhasedTestManager::fetchClassFromScenarioContext)
                .collect(Collectors.toSet());
    }

    /**
     * Given a scenario we extract the class covering it. If the given class is
     * not a phased execution, we do not transform the scring.
     * <p>
     * Author : gandomi
     *
     * @param in_scenario The execued scenario. This will include the phase group
     * @return The class path of the phased scenario
     */
    public static String fetchClassFromScenarioContext(String in_scenario) {
        if (in_scenario == null) {
            throw new IllegalArgumentException("The given scenario name may not be null");
        }

        if (in_scenario.contains("(")) {
            return in_scenario.substring(0, in_scenario.indexOf('('));
        }

        return in_scenario;
    }

    protected static class ScenarioContextData {
        public static final String FAILED_STEP_WHEN_PASSED = "NA";
        protected boolean passed;
        protected long duration;
        protected String failedStep;
        protected Phases failedInPhase;

        ScenarioContextData() {
            passed=true;
            duration=0;
            failedStep = FAILED_STEP_WHEN_PASSED;
            failedInPhase = Phases.NON_PHASED;
        }

        /**
         * Used in the case of importing of contexts
         * @param in_importString
         */
        protected ScenarioContextData(String in_importString) {
            this.importFromString(in_importString);
        }

        /**
         * Detailed constructor
         * @param  in_passed
         * @param in_duration
         * @param in_failedStep
         * @param in_phase
         */
        protected ScenarioContextData(boolean in_passed, long in_duration, String in_failedStep, Phases in_phase) {
            this.passed = in_passed;
            this.duration = in_duration;
            this.failedStep = in_failedStep;
            this.failedInPhase = in_phase;
        }

        /**
         * Constructor, where the phase is fetched from the Phase state is taken from the context
         * @param  in_passed
         * @param in_duration
         * @param in_failedStep
         */
        public ScenarioContextData(boolean in_passed, long in_duration, String in_failedStep) {
            this.passed = in_passed;
            this.duration = in_duration;
            this.failedStep = in_failedStep;
            this.failedInPhase = Phases.getCurrentPhase();
        }

        /**
         * Given a TestResult object we will update the given scenarioContext
         *
         * Author : gandomi
         *
         * @param in_testResult
         */
        public void synchronizeState(ITestResult in_testResult) {
            switch (in_testResult.getStatus()) {
            case ITestResult.FAILURE:
                failedStep = ClassPathParser.fetchFullName(in_testResult);
                failedInPhase = Phases.getCurrentPhase();
            case ITestResult.SKIP:
                passed = false;
            }
            duration += (in_testResult.getEndMillis() - in_testResult.getStartMillis());
        }

        /**
         * Exports the content of this class to a CSV (";" separated) string
         *
         * Author : gandomi
         *
         * @return A string representation of this class
         */
        public String exportToString() {
            StringBuilder sb = new StringBuilder(Boolean.toString(this.passed));
            sb.append(";").append(this.duration).append(";").append(this.failedStep).append(";")
                    .append(this.failedInPhase.name());
            return sb.toString();
        }

        /**
         * Imports the values of a string.
         *
         * Author : gandomi
         *
         * @param in_importString A string that is used to populate the fields of this class.
         */
        public void importFromString(String in_importString) {
            String[] l_valueArray = in_importString.split(STD_SCENARIO_CONTEXT_SEPARATOR);

            if (l_valueArray.length < 2) {
                throw new IllegalArgumentException(
                        "The imported string cannot be parsed as it does not contain the minimum 2 entries.");
            }

            this.passed = Boolean.valueOf(l_valueArray[0]);
            this.duration = Long.valueOf(l_valueArray[1]);

            if (!this.passed) {
                if (l_valueArray.length < 4) {
                    throw new IllegalArgumentException(
                            "The imported string cannot be parsed as it does not contain the minimum 4 of entries.");
                }

                this.failedStep = !l_valueArray[2].isBlank() ? l_valueArray[2] : FAILED_STEP_WHEN_PASSED;

                try {
                    this.failedInPhase = !l_valueArray[3].isBlank() ? Phases.valueOf(
                            l_valueArray[3]) : Phases.NON_PHASED;
                } catch (IllegalArgumentException exc) {
                    throw new IllegalArgumentException(
                            "The given import string " + in_importString + " does not allow us to deduce the Phase.");
                }
            }

        }
    }
}
