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

import com.adobe.campaign.tests.integro.phased.permutational.ScenarioStepDependencies;
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
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class PhasedTestManager {

    private PhasedTestManager() {
        //Utility class. Defeat instantiation.
    }

    static final String STD_GROUP_SELECT_TESTS_BY_PRODUCER = "PHASED_PRODUCED_TESTS";
    static final String STD_KEY_CLASS_SEPARATOR = "->";

    public static final String PHASED_TEST_LOG_PREFIX = "[Phased Testing] ";
    public static final String STD_SCENARIO_CONTEXT_SEPARATOR = ";";

    private static final Logger log = LogManager.getLogger();

    public static final String DEFAULT_CACHE_DIR = "phased_output";
    public static final String STD_CACHE_DIR = PhasedTestConfigValueHandler.PROP_OUTPUT_DIR.fetchValue();
    public static final String STD_STORE_DIR = "phased_tests";
    public static final String STD_STORE_FILE = "phaseData.properties";

    //Values for the DataProvider used in both Shuffled and Single run phases
    static final String STD_PHASED_GROUP_PREFIX = "phased-shuffledGroup_";
    static final String STD_PHASED_GROUP_SINGLE = "phased-singleRun";

    static final String STD_PHASED_GROUP_NIE_PREFIX = "phased-shuffledGroupNIE_";

    public static final String STD_MERGE_STEP_ERROR_PREFIX = "Phased Error: Failure in step ";


    /**
     * The different states a step can assume in a scenario
     * <p>
     * <p>
     * Author : gandomi
     */
    public enum ScenarioState {
        CONTINUE, SKIP_NORESULT, SKIP_PREVIOUS_FAILURE, CONFIG_FAILURE
    }

    static Properties phasedCache = new Properties();
    private static final Map<String, ScenarioContextData> scenarioContext = new HashMap<>();

    static Map<String, MethodMapping> methodMap = new HashMap<>();

    static Properties phaseContext = new Properties();

    private static PhasedDataBroker dataBroker = null;

    static Boolean mergedReportsActivated = Boolean.TRUE;

    static Boolean selectTestsByProducerMode = Boolean.FALSE;

    static final String SCENARIO_CONTEXT_PREFIX = PhasedTestConfigValueHandler.PROP_SCENARIO_EXPORTED_PREFIX.fetchValue();

    public static class MergedReportData {

        protected static LinkedHashSet<PhasedReportElements> prefix = new LinkedHashSet<>();
        protected static LinkedHashSet<PhasedReportElements> suffix = new LinkedHashSet<>();

        MergedReportData() {
        }

        /**
         * Allows you to defined the generated name when phased steps are merged for a scenario. If nothing is set we
         * use the phase group.
         * <p>
         * Author : gandomi
         *
         * @param in_prefix A sorted set of report elements to be added as prefix to the scenario name
         * @param in_suffix A sorted set of report elements to be added as suffix to the scenario name
         */
        public static void configureMergedReportName(LinkedHashSet<PhasedReportElements> in_prefix,
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
    static Map<String, ScenarioContextData> getScenarioContext() {
        return scenarioContext;
    }

    /**
     * @return the dataBroker
     */
    static PhasedDataBroker getDataBroker() {
        return dataBroker;
    }

    /**
     * @param dataBroker the dataBroker to set
     */
    public static void setDataBroker(Object dataBroker) {
        PhasedTestManager.dataBroker = (PhasedDataBroker) dataBroker;
    }

    /**
     * Initiaizes the databroker given the full class path of the implementation of the interface {@code
     * PhasedDataBroker}
     * <p>
     * Author : gandomi
     *
     * @param in_classPath The classpath for the implementation of the data broker
     * @throws PhasedTestConfigurationException Whenever there is a problem instantiating the Phased DataBroker class
     */
    public static void setDataBroker(String in_classPath) throws PhasedTestConfigurationException {
        log.info("{} Setting Data broker with classpath {}", PHASED_TEST_LOG_PREFIX, in_classPath);
        Class<?> l_dataBrokerImplementation;
        Object l_dataBroker;
        try {
            l_dataBrokerImplementation = Class.forName(in_classPath);
            if (!PhasedDataBroker.class.isAssignableFrom(l_dataBrokerImplementation)) {
                throw new PhasedTestConfigurationException("The given class was not an instance of PhasedDataBroker");
            }

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

    public static Map<String, MethodMapping> getMethodMap() {
        return methodMap;
    }

    /**
     * This method stores a phased test data in the cache. It will be stored with the keys: "class, method, instance"
     * and a value
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
            sb.append("(").append(phaseContext.get(l_methodFullName)).append(")");
        }

        final String lr_storeKey = sb.toString();
        return storePhasedCache(lr_storeKey, in_storeValue);
    }

    /**
     * Stores a value with the given key. We include the class as prefix.
     * <p>
     * Author : gandomi
     *
     * @param in_storageKey A string that is added to the generated key for identification of the stored data
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
     * This method generates the identifier for a producer/consumer used for storing in the cache
     * <p>
     * Author : gandomi
     *
     * @param in_idInPhaseContext The id of the step in the context
     * @param in_storageKey       An additional identifier for storing the data
     * @return The identity of the storage key as stored in the cache
     */
    static String generateStepKeyIdentity(final String in_idInPhaseContext, String in_storageKey) {

        return generateStepKeyIdentity(in_idInPhaseContext, in_idInPhaseContext, in_storageKey);
    }

    /**
     * This method generates the identifier for a producer/consumer
     * <p>
     * Author : gandomi
     *
     * @param in_idInPhaseContext The id of the step in the context
     * @param in_idPrefixToStore  The prefix of the full name for storing values. Usually the class full name
     * @param in_storageKey       An additional identifier for storing the data
     * @return The identity of the storage key as stored in the cache
     */
    static String generateStepKeyIdentity(final String in_idInPhaseContext, final String in_idPrefixToStore,
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
    static String generateStepKeyIdentity(String in_idInPhaseContext) {
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
     * Given a step in the Phased Test it fetches the value committed for that test. It will fetch a Phased Test data
     * with the method/test that called this method. This method is to be used if you have produced your Phased Data
     * using {@link #produceInStep(String)}
     * <p>
     * Author : gandomi
     *
     * @param in_stepName The step name aka method name (not class name nor arguments) that stored a value in the
     *                    current scenario
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
     * @return The value for the given consumable. If not found a PhasedTestException is thrown
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
     * Given a step in the Phased Test it fetches the value committed for that test.
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
    static synchronized void clearCache() {
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

        if (PhasedTestConfigValueHandler.PROP_PHASED_DATA_PATH.isSet() ) {
            return new File(PhasedTestConfigValueHandler.PROP_PHASED_DATA_PATH.fetchValue());
        } else {
            return new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR), STD_STORE_FILE);
        }
    }

    /**
     * Exports the Phase cache and the scenario context into the given file
     * <p>
     * Author : gandomi
     *
     * @param in_file that will contain the phase cache and scenario contexts
     * @return The file used for storing the Phase Context.
     */
    static File exportContext(File in_file) {

        log.info("{} Exporting Phased Testing data to {}", PHASED_TEST_LOG_PREFIX, in_file.getPath());

        Properties lt_transformedScenarios = new Properties();
        scenarioContext.forEach(
                (key, value) -> lt_transformedScenarios.put(attachContextFlag(key), value.exportToString()));

        try (FileWriter fw = new FileWriter(in_file)) {

            getPhasedCache().store(fw, null);
            lt_transformedScenarios.store(fw, null);

        } catch (IOException e) {
            log.error("Error when creating file {}", in_file.getPath(), e);
            throw new PhasedTestException("Error when creating file " + in_file.getPath() + ".", e);
        }
        //Store in DataBroker
        if (dataBroker != null) {
            log.info("{} Exporting Phased Testing to location specified by provided PhasedDataBroker.",
                    PHASED_TEST_LOG_PREFIX);
            dataBroker.store(in_file);
        }

        return in_file;
    }

    /**
     * Imports a file and stored the properties in the phased cache and in the scenario context.
     * <p>
     * Author : gandomi
     *
     * @param in_phasedTestFile A file that contains the phase cache data from a previous phase
     * @return A Properties object with the phase cache data from the previous phase
     */
    static Properties importContext(File in_phasedTestFile) {
        log.info("{} Importing phase cache.", PHASED_TEST_LOG_PREFIX);
        Properties lr_importedProperties = new Properties();
        try (InputStream input = new FileInputStream(in_phasedTestFile)) {

            // load a properties file
            lr_importedProperties.load(input);
        } catch (IOException e) {
            log.error("Error when loading file {}", in_phasedTestFile.getPath());
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
     * Loads the Phased Test data from the standard location which is by default {@value #DEFAULT_CACHE_DIR}/{@value
     * #STD_STORE_DIR}/{@value #STD_STORE_FILE}
     * <p>
     * Author : gandomi
     *
     * @return A Properties object with the phase cache data from the previous phase
     */
    static Properties importPhaseData() {
        File l_importCacheFile;

        if (dataBroker == null) {

            if (PhasedTestConfigValueHandler.PROP_PHASED_DATA_PATH.isSet()) {
                l_importCacheFile = new File(PhasedTestConfigValueHandler.PROP_PHASED_DATA_PATH.fetchValue());

            } else {
                l_importCacheFile = new File(GeneralTestUtils.fetchCacheDirectory(STD_STORE_DIR), STD_STORE_FILE);
                log.warn("{} The system property {} not set. Fetching Phased Test data from {}.",
                        PHASED_TEST_LOG_PREFIX, PhasedTestConfigValueHandler.PROP_PHASED_DATA_PATH.fetchValue(), l_importCacheFile.getPath());
            }
        } else {
            log.info("{} Fetching cache through DataBroker.", PHASED_TEST_LOG_PREFIX);
            l_importCacheFile = dataBroker.fetch(STD_STORE_FILE);
        }
        return importContext(l_importCacheFile);

    }

    /**
     * Returns the provider for shuffling tests. In general the values are Shuffle group prefix + Nr of steps before the
     * Phase Event and the number of steps after the event.
     * <p>
     * Author : gandomi
     *
     * @param in_method The step/method for which we want to fond out the data provider
     * @return A two-dimensional array of all the data providers attached to the current step/method
     */
    public static Object[][] fetchProvidersShuffled(Method in_method) {
        return fetchProvidersShuffled(in_method, Phases.getCurrentPhase());
    }

    /**
     * Returns the provider for shuffling tests. In general the values are Shuffle group prefix + Nr of steps before the
     * Phase Event and the number of steps after the event.
     * <p>
     * Author : gandomi
     *
     * @param in_method The full name of the method used for identifying it in the phase context
     * @param in_phasedState    The phase state for which we should retrieve the parameters. The parameters will be
     *                          different based on the phase.
     * @return A two-dimensional array of all the data providers attached to the current step/method
     */
    public static Object[][] fetchProvidersShuffled(Method in_method, Phases in_phasedState) {

        final MethodMapping l_methodMapping = methodMap.get(ClassPathParser.fetchFullName(in_method));
        Object[][] l_objectArrayPhased = new Object[l_methodMapping.nrOfProviders][1];


        for (int rows = 0; rows < l_methodMapping.nrOfProviders; rows++) {

            int lt_nrBeforePhase = in_phasedState.equals(Phases.PRODUCER) ? (l_methodMapping.totalClassMethods
                    - rows) : rows;

            int lt_nrAfterPhase = l_methodMapping.totalClassMethods - lt_nrBeforePhase;

            if (in_phasedState.hasSplittingEvent()) {
                l_objectArrayPhased[rows][0] = STD_PHASED_GROUP_PREFIX + lt_nrBeforePhase
                        + "_"
                        + lt_nrAfterPhase;
            } else {
                l_objectArrayPhased[rows][0] = STD_PHASED_GROUP_NIE_PREFIX + (rows+1);
            }
        }

        //Fetch class level data providers
        Object[][] l_userDefinedDataProviders = fetchDataProviderValues(l_methodMapping.declaredClass);

        //Merge
        Object[][] lr_dataProviders = dataProvidersCrossJoin(l_objectArrayPhased, l_userDefinedDataProviders);

        log.debug("{} Returning provider for method {}", PhasedTestManager.PHASED_TEST_LOG_PREFIX, ClassPathParser.fetchFullName(in_method));
        return lr_dataProviders;
    }

    /**
     * Returns the data provider for a single phase
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step for which we want to get the data providers for
     * @return An array containing the data providers for the method. Otherwise an empty array
     */
    public static Object[] fetchProvidersSingle(Method in_method) {
        log.debug("Returning provider for method {}", ClassPathParser.fetchFullName(in_method));

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

        if (Phases.ASYNCHRONOUS.isSelected()) {
            return new Object[] { STD_PHASED_GROUP_SINGLE };
        }

        return new Object[] {};
    }

    /**
     * Returns the data provider for a standard Non-Phased test. If the test is single and not execute inactive, we do
     * not execute it
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step for which we want to get the data providers for
     * @return An array containing the data providers for the method. Otherwise an empty array
     */
    public static  Object[]  fetchProvidersStandard(Method in_method) {
        log.debug("Returning provider for method {}", ClassPathParser.fetchFullName(in_method));

        if (Phases.NON_PHASED.isSelected() && !in_method.getDeclaringClass().getAnnotation(PhasedTest.class)
                .executeInactive()) {
            return new Object[] { };
        }

        return new Object[] {PhasedDataProvider.DEFAULT};
    }

    /**
     * This method calculates how often a class should be run.
     * <p>
     * Author : gandomi
     *
     * @param in_classMethodMap      A map of a class and it is methods (A scenario and its steps)
     * @return A map letting us know that for a the given method how often it will be executed in the current phase
     */
    public static Map<String, MethodMapping> generatePhasedProviders(Map<Class<?>, List<String>> in_classMethodMap) {

        return generatePhasedProviders(in_classMethodMap, Phases.getCurrentPhase());

    }

    /**
     * This method calculates how often a scenario should be run, given the steps/methods it has.
     * <p>
     * Author : gandomi
     *
     * @param in_classMethodMap A map of a class and it is methods (A scenario and its steps)
     * @param in_phaseState     The phase in which we are
     * @return A map letting us know that for a given method how often it will be executed in the current phase
     */
    public static Map<String, MethodMapping> generatePhasedProviders(Map<Class<?>, List<String>> in_classMethodMap,
            Phases in_phaseState) {

        return generatePhasedProviders(in_classMethodMap, null, in_phaseState);

    }

    /**
     * This method calculates how often a scenario should be run, given the steps/methods it has. This overriding allows
     * us to take into account the ordering
     * <p>
     * Author : gandomi
     *
     * @param in_classMethodMap       A map of a class and it is methods (A scenario and its steps)
     * @param in_scenarioDependencies A map allowing us to detect the test execution order
     * @param in_phaseState           The phase in which we are
     * @return A map letting us know that for a given method how often it will be executed in the current phase
     */
    public static Map<String, MethodMapping> generatePhasedProviders(Map<Class<?>, List<String>> in_classMethodMap,
            Map<String, ScenarioStepDependencies> in_scenarioDependencies, Phases in_phaseState) {
        methodMap = new HashMap<>();

        for (Entry<Class<?>, List<String>> entry : in_classMethodMap.entrySet()) {

            List<String> lt_methodList =
                    in_scenarioDependencies == null ? entry.getValue() : in_scenarioDependencies.get(
                                    entry.getKey().getTypeName()).fetchExecutionOrderList().stream()
                            .map(ol -> entry.getKey().getTypeName() + "." + ol.getStepName()).collect(
                                    Collectors.toList());

            if (in_phaseState.hasSplittingEvent) {

                if (in_phaseState.equals(Phases.CONSUMER)) {
                    Collections.reverse(lt_methodList);
                }

                for (int i = 0; i < entry.getValue().size(); i++) {
                    methodMap.put(lt_methodList.get(i),
                            new MethodMapping(entry.getKey(), entry.getValue().size() - i,
                                    entry.getValue().size(), i + 1));

                }
            } else {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    methodMap.put(lt_methodList.get(i),
                            new MethodMapping(entry.getKey(), entry.getValue().size(),
                                    entry.getValue().size(), i + 1));

                }
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
    static String storeTestData(Method in_testMethod, String in_phaseGroup, String in_storedData) {
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
    static String storeTestData(Class<?> in_class, String in_phaseGroup, boolean in_storedData) {
        ScenarioContextData l_scenarioContext = new ScenarioContextData();
        l_scenarioContext.passed = in_storedData;

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
    static String storeTestData(Class<?> in_class, String in_phaseGroup, ScenarioContextData in_storedData) {
        phaseContext.put(in_class.getTypeName(), in_phaseGroup);

        final String lr_storedKey = generateStepKeyIdentity(in_class.getTypeName());

        scenarioContext.put(lr_storedKey, in_storedData);
        return lr_storedKey;
    }

    /**
     * Basically lets us know if we execute the given method in producer mode. We look at the attribute value phaseEnd.
     * This method is specifically for the Single Mode.
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step we want to know its phase location
     * @return true if the step is anywhere before the phase limit
     */
    public static boolean isExecutedInProducerMode(Method in_method) {

        boolean proceed = PhasedTestManager.isPhasedTestSingleMode(in_method);
        if (!proceed) {
            return false;
        }

        final Class<?> l_declaringClass = in_method.getDeclaringClass();
        List<Method> l_myMethods = Arrays.stream(l_declaringClass.getMethods())
            .filter(PhasedTestManager::isPhasedTest)
            .sorted(Comparator.comparing(Method::getName))
            .collect(Collectors.toList());

        for (Method lt_declaredMethod : l_myMethods) {
            if (PhasedTestManager.isPhaseLimit(lt_declaredMethod)) {
                return false;
            }

            if (lt_declaredMethod.equals(in_method)) {
                return true;
            }

        }
        return false;
    }

    /**
     * Lets us know if the scenario contains a declared event
     * @param in_testScenario A Phased Test
     * @return true if the scenario has a step that contains the annotation {{@link PhaseEvent}}
     */
    public static boolean isPhasedTestWithEvent(Class in_testScenario) {

        return Arrays.stream(in_testScenario.getDeclaredMethods()).anyMatch(t -> t.isAnnotationPresent(PhaseEvent.class));
    }


    /**
     * Checks if the phase step is the last item in the current phase
     * <p>
     * Author : gandomi
     *
     * @param in_method The method/step which we want to know if it is the last step in the current phase
     * @return True if the test is before or in the phase End.
     */
    static boolean isPhaseLimit(Method in_method) {

        return in_method.isAnnotationPresent(PhaseEvent.class);
    }

    /**
     * This method tells us if the method is a valid phased test. This is done by seeing if the annotation PhasedStep is
     * on the method, and if the annotation PhasedTest is on the class
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
    public static boolean isPhasedTest(Class<?> in_class) {
        return in_class.isAnnotationPresent(PhasedTest.class);
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed consequently in two phases
     *
     * @param in_method Any test method
     * @return True if the test step/method is part of a SingleRun Phase Test scenario
     */
    static boolean isPhasedTestSingleMode(Method in_method) {
        return isPhasedTestSingleMode(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed consequently in two phases
     *
     * @param in_class Any class that contains tests
     * @return True if the test class is a SingleRun Phase Test scenario
     */
    static boolean isPhasedTestSingleMode(Class<?> in_class) {
        //TODO in 8.0.2 to be removed
        //return isPhasedTest(in_class) && (isPhasedTestWithEvent(in_class)
        return isPhasedTest(in_class) && (isPhasedTestWithEvent(in_class) || !in_class.getAnnotation(PhasedTest.class).canShuffle());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed in a shuffled manner. For a test with 3
     * steps the test will be executed 6 times in total
     *
     * @param in_method A test method
     * @return True if the given test method/step is part of a Shuffled Phased Test scenario
     */
    static boolean isPhasedTestShuffledMode(Method in_method) {
        return isPhasedTestShuffledMode(in_method.getDeclaringClass());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed in a shuffled manner. For a test with 3
     * steps the test will be executed 6 times in total
     *
     * @param in_testResult A test result
     * @return True if the given test method/step is part of a Shuffled Phased Test scenario
     */
     static boolean isPhasedTestShuffledMode(ITestResult in_testResult) {
        return isPhasedTestShuffledMode(in_testResult.getMethod().getConstructorOrMethod().getMethod());
    }

    /**
     * This method lets us know if the steps in a PhasedTest are to be executed in a Shuffled manner. For a test with 3
     * steps the test will be executed 6 times in total
     *
     * @param in_class A test class/scenario
     * @return True if the given test scenario is a Shuffled Phased Test scenario
     */
    static boolean isPhasedTestShuffledMode(Class<?> in_class) {
        //return isPhasedTest(in_class) && in_class.getAnnotation(PhasedTest.class).canShuffle();
        return isPhasedTest(in_class) && !isPhasedTestWithEvent(in_class) && in_class.getAnnotation(PhasedTest.class).canShuffle();
    }

    /**
     * This method provides an ID for the scenario given the ITestNGResult. This is assembled using the Classname + the
     * PhaseGroup
     * <p>
     * Author : gandomi
     *
     * @param in_testNGResult A TestNG Test Result object
     * @return The identity of the scenario
     */
    public static String fetchScenarioName(ITestResult in_testNGResult) {
        return in_testNGResult.getMethod().getConstructorOrMethod().getMethod().getDeclaringClass()
            .getTypeName() + ClassPathParser.fetchParameterValues(in_testNGResult);
    }

    /**
     * This method logs the stage result of the Phased Test Group. The key will be the class including the phase test
     * group. It allows us to know if the test is allowed to continue.
     * <p>
     * Once the context is logged as false for a test it remains false
     * <p>
     * Author : gandomi
     *
     * @param in_testResult The test result
     */
    public static void scenarioStateStore(ITestResult in_testResult) {

        final String l_scenarioName = fetchScenarioName(in_testResult);

        //TODO move to synchronize state
        if (scenarioContext.containsKey(l_scenarioName)) {
            scenarioContext.get(l_scenarioName).synchronizeState(in_testResult);
        } else {
            ScenarioContextData l_scenarioContextData = new ScenarioContextData();
            l_scenarioContextData.synchronizeState(in_testResult);
            scenarioContext.put(l_scenarioName, l_scenarioContextData);
        }
    }

    private static String attachContextFlag(String in_scenarioName) {
        return SCENARIO_CONTEXT_PREFIX + in_scenarioName;
    }

    /**
     * <table>
     * <caption>Use Cases for Scenario States</caption>
     * <tr><th>CASE</th><th>Phase</th><th>Current step</th><th>Previous Step Result</th><th>Expected result</th><th>MERGED RESULT</th><th>Comment</th></tr>
     * <tr><td>1</td><td>Producer/NonPhased</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td><td></td></tr>
     * <tr><td>2</td><td>Producer/NonPhased</td><td>&gt; 1</td><td>FAILED</td><td>SKIP</td><td>FAILED</td><td></td></tr>
     * <tr><td>3</td><td>Producer/NonPhased</td><td>&gt; 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td><td></td></tr>
     * <tr><td>4</td><td>Consumer</td><td>1</td><td>N/A</td><td>Continue</td><td>PASSED</td><td></td></tr>
     * <tr><td>5</td><td>Consumer</td><td>&gt; 1</td><td>PASSED</td><td>Continue</td><td>PASSED</td><td></td></tr>
     * <tr><td>6</td><td>Consumer</td><td>&gt; 1</td><td>FAILED/SKIPPED</td><td>SKIP</td><td>FAILED</td><td></td></tr>
     * <tr><td>7</td><td>Consumer</td><td>&gt; 1</td><td>N/A</td><td>SKIP</td><td>SKIP</td><td></td></tr>
     * <tr><td>8</td><td>ANY</td><td>ANY</td><td>N/A</td><td>SKIP but not forced</td><td>SKIP</td><td>In this case the tests skip due to a config error</td></tr>
     * <tr><td>9</td><td>ANY</td><td>ANY</td><td>N/A</td><td>Continue</td><td>Failure</td><td>his is the case of a retry</td></tr>
     * </table>
     *
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
        //Case      PHASE               STEP    Previous_Step       Expected_Result     Merged Result   Comment
        //Case 1    Producer/NonPhased  1       N/A                 Continue            true            testStateIstKeptBetweenPhases_Continue
        //Case 2    Producer/NonPhased  > 1     FAILED/Skipped      SKIP                false
        //Case 3    Producer/NonPhased  > 1     Passed              Continue            true            testStateIstKeptBetweenPhases_Continue
        //Case 4    Consumer            1       N/A                 Continue            true            testStateIstKeptBetweenPhases_Continue
        //Case 5    Cosnumer            > 1     Passed              Continue            true
        //Case 6    Cosnumer            > 1     Failed/Skipped      SKIP                false
        //Case 7    Cosnumer            > 1     N/A                 SKIP                false
        //Case 8    ANY                 ANY     N/A                 SKIP (not forced)
        //Case 9    ANY                 ANY     N/A                 Continue            false           In retry we do not change the state of the retry

        //#43 to change this to false
        //If scenario has not yet been executed in the current phase
        if (!getScenarioContext().containsKey(l_scenarioName)) {
            //True only if we are executing end to end 0_X
            return hasStepsExecutedInProducer(in_testResult) ? ScenarioState.SKIP_NORESULT : ScenarioState.CONTINUE;
        }

        //In the case of retry when activated for the phased tests, we let testng manage it.
        if (getScenarioContext().get(l_scenarioName).getCurrentStep()
                .equals(ClassPathParser.fetchFullName(in_testResult))) {
            return ScenarioState.CONTINUE;
        }

        if (in_testResult.getThrowable() != null) {
            return ScenarioState.CONFIG_FAILURE;
        }
        return getScenarioContext().get(l_scenarioName).passed
                ? ScenarioState.CONTINUE : ScenarioState.SKIP_PREVIOUS_FAILURE;
    }

    /**
     * Creates a standard report name. I.e. it provides a name as how the test scenario and phased group should be
     * represented.
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestRessult object
     * @return A string prefixed with the scenario name and suffixed with the phaseGroup
     */
    public static String fetchTestNameForReport(ITestResult in_testResult) {

        final String l_stdItemSeparator = "__";

        //Adding the prefixes
        StringBuilder sb = new StringBuilder();
        for (PhasedReportElements lt_pre : MergedReportData.prefix) {
            sb.append(lt_pre.fetchElement(in_testResult));
            sb.append(l_stdItemSeparator);
        }

        //Adding the required values : the phaseGroup
        sb.append(PhasedReportElements.PHASE_GROUP.fetchElement(in_testResult));

        //Adding the suffixes
        for (PhasedReportElements lt_pre : MergedReportData.suffix) {

            final String lt_elementValue = lt_pre.fetchElement(in_testResult);
            if (!lt_elementValue.isEmpty()) {
                sb.append(l_stdItemSeparator);
                sb.append(lt_elementValue);
            }

        }

        return sb.toString();
    }

    /**
     * This method calculates the duration in milliseconds for a phased test scenario. Given a List of ITestNGResult
     * related to that scenario, it makes a sum of the start and end milliseconds of its steps.
     * <p>
     * Throws an {@link IllegalArgumentException} when the given List is null or empty.
     * <p>
     * Throws an {@link IllegalArgumentException} when the given List is not from the same Phase group or scenario
     * <p>
     * Author : gandomi
     *
     * @param in_resultList A List of ITestNGResults related to a given scenario
     * @return A long value representing the duration in milliseconds
     */
    static long fetchDurationMillis(List<ITestResult> in_resultList) {
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
     * This method creates a step name by prefixing the step name with the phase group
     * <p>
     * Author : gandomi
     *
     * @param result The TestNGResult object
     * @return A string representation of the test name
     */
    static String fetchPhasedStepName(ITestResult result) {
        if (result.getParameters().length == 0) {
            throw new IllegalArgumentException(
                    "No parameters found. The given test result for test " + ClassPathParser.fetchFullName(result)
                            + " does not seem to have been part of a Phased Test");
        }

        return concatenateParameterArray(result.getParameters()) + '_' + result.getName();
    }

    /**
     * This method is used in the context of merged reports. We use this to enrich the exception message when merging
     * reports.
     * <p>
     * An {@link IllegalArgumentException} is thrown if the given in_failedTestResult is not failed.
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
     * Given two data provider arrays, this method performs a scalar join of the data providers. For two objects :
     * <p>
     * Dataprovider1:
     * <table>
     * <caption>DataProvider1</caption>
     * <tr><td>A</td></tr>
     * </table>
     * <p>
     * Dataprovider2:
     * <table>
     * <caption>DataProvider2</caption>
     * <tr><td>X</td></tr>
     * <tr><td>Y</td></tr>
     * </table>
     * <p>
     * We will get:
     * <table>
     * <caption>CrossJoined DataProviders</caption>
     * <tr><td>A</td><td>X</td></tr>
     * <tr><td>A</td><td>Y</td></tr>
     * </table>
     * <p>
     * Author : gandomi
     *
     * @param in_providerSeriesLeft  The data provider data which is used on the left side of the join
     * @param in_providerSeriesRight The data provider data which is used on the right side of the join
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
        Object[][] lr_dataProvider = new Object[l_totalNrOfLines][l_totalNrOfColumns];

        //fill return object
        int line = 0;
        for (Object[] lineLeft : in_providerSeriesLeft) {
            for (Object[] lineRight : in_providerSeriesRight) {
                System.arraycopy(lineLeft, 0, lr_dataProvider[line], 0, lineLeft.length);
                System.arraycopy(lineRight, 0, lr_dataProvider[line], lineLeft.length, lineRight.length);
                line++;
            }
        }
        return lr_dataProvider;
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
     * @return The data providers attached to the class. An empty array is returned if there is no data provider defined
     * at a class level
     */
    static Object[][] fetchDataProviderValues(Class<?> in_phasedTestClass) {

        final Object[][] lr_defaultReturnValue = new Object[0][0];

        if (!in_phasedTestClass.isAnnotationPresent(Test.class)) {
            log.warn(
                    "{} The given phased test class {} does not have the Test annotation on it. Data Providers for Phased Tests can only be considered at that level.",
                    PhasedTestManager.PHASED_TEST_LOG_PREFIX, in_phasedTestClass.getTypeName());

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

        //Fetch the data provider method
        final Class<?> clazzName = l_dataProviderClass;
        Method l_dataProviderMethod = Arrays.stream(l_dataProviderClass.getDeclaredMethods())
                .filter(a -> a.isAnnotationPresent(DataProvider.class))
                .filter(f -> f.getDeclaredAnnotation(DataProvider.class).name().equals(l_dataProviderName))
                .findFirst()
            .<PhasedTestConfigurationException>orElseThrow(() -> {
            throw new PhasedTestConfigurationException(
                    "No method found which matched the data provider class " + clazzName.getTypeName()
                            + " or data provider name " + l_dataProviderName);
            });

        l_dataProviderMethod.setAccessible(true);

        try {
            return (Object[][]) l_dataProviderMethod.invoke(l_dataProviderClass.newInstance(), new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException e) {
            log.error("{} Problem when fetching the user defined data providers.",
                    PhasedTestManager.PHASED_TEST_LOG_PREFIX);
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
    static String concatenateParameterArray(Object[] in_values) {
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
     * Given a phased test method and and its phase group, lets you know if it has ssteps executed in the producer
     * phase
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestNG Test result
     * @return true if we are in consumer, and we are not a 0_X phase group that is executed end to end in the consumer
     * phase
     */
    public static boolean hasStepsExecutedInProducer(ITestResult in_testResult) {
        return hasStepsExecutedInProducer(in_testResult, Phases.getCurrentPhase());

    }

    /**
     * Given a phased test method and and its phase group, lets you know if it has ssteps executed in the producer
     * phase
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A TestNG Test result
     * @param in_phase      The phase in which we are currently.
     * @return true if we are in consumer, and we are not a 0_X phase group that is executed end to end in the consumer
     * phase
     */
    public static boolean hasStepsExecutedInProducer(ITestResult in_testResult, Phases in_phase) {
        return (in_phase.equals(Phases.CONSUMER) && (fetchNrOfStepsBeforePhaseChange(in_testResult) > 0));
    }

    /**
     * Given a string representing the phase group, returns the number of steps planned before a phase change
     * <p>
     * Author : gandomi
     *
     * @param in_testResult A test result object containing the necessary analysis data
     * @return The number of steps planned before a phase change. If we are non-phased we return 0
     */
    public static Integer fetchNrOfStepsBeforePhaseChange(ITestResult in_testResult) {

        if (isPhasedTestShuffledMode(in_testResult) && Phases.getCurrentPhase()
                .hasSplittingEvent()) {

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
     * Extracts all the tests that have been executed before the current phase. This will later be used to create the
     * test list to be executed
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
     * Given a scenario we extract the class covering it. If the given class is not a phased execution, we do not
     * transform the string.
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

    /**
     * Applies the choice the user made regarding merge reports
     */
    static void applyMergeReportChoice() {
        //Activating merge results if the value is set in the system properties
        if (PhasedTestConfigValueHandler.PROP_MERGE_STEP_RESULTS.is("true")) {
            activateMergedReports();
        }

        if (PhasedTestConfigValueHandler.PROP_MERGE_STEP_RESULTS.is("false")) {
            deactivateMergedReports();
        }
    }

    protected static class ScenarioContextData {
        public static final String NOT_APPLICABLE_STEP_NAME = "NA";
        private boolean passed;
        private long duration;
        private String failedStep;
        private Phases failedInPhase;
        private String currentStep;
        private int stepNr;


        ScenarioContextData() {
            passed = true;
            duration = 0;
            failedStep = NOT_APPLICABLE_STEP_NAME;
            setFailedInPhase(Phases.NON_PHASED);
            setCurrentStep(NOT_APPLICABLE_STEP_NAME);
        }

        /**
         * Used in the case of importing of contexts
         *
         * @param in_importString A csv sring separated by ";"
         */
        protected ScenarioContextData(String in_importString) {
            this();
            this.importFromString(in_importString);
        }

        /**
         * Detailed constructor
         *
         * @param in_passed      If the scenario is passed
         * @param in_duration    Duration of the scenario
         * @param in_failedStep  The step which caused the failure.
         * @param in_phase       The Phase in which the error happened
         * @param in_currentStep The current step in which we are
         */
        protected ScenarioContextData(boolean in_passed, long in_duration, String in_failedStep, Phases in_phase,
                String in_currentStep) {
            this.passed = in_passed;
            this.duration = in_duration;
            this.failedStep = in_failedStep;
            this.setFailedInPhase(in_phase);
            this.setCurrentStep(in_currentStep);
        }

        /**
         * Constructor, where we pass the failed step, and the phase is deduced.
         *
         * @param in_passed     If the scenario is passed
         * @param in_duration   Duration of the scenario
         * @param in_failedStep The step which caused the failure.
         */
        public ScenarioContextData(boolean in_passed, long in_duration, String in_failedStep) {
            this();
            this.passed = in_passed;
            this.duration = in_duration;
            this.failedStep = in_failedStep;
            this.setFailedInPhase(Phases.getCurrentPhase());
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getFailedStep() {
            return failedStep;
        }

        public void setFailedStep(String failedStep) {
            this.failedStep = failedStep;
        }

        public Phases getFailedInPhase() {
            return failedInPhase;
        }

        public void setFailedInPhase(Phases failedInPhase) {
            this.failedInPhase = failedInPhase;
        }

        public int getStepNr() {
            return stepNr;
        }

        public void setStepNr(int stepNr) {
            this.stepNr = stepNr;
        }

        /**
         * Given a TestResult object we will update the given scenarioContext
         * <p>
         * Author : gandomi
         *
         * @param in_testResult A test result object
         */
        public void synchronizeState(ITestResult in_testResult) {
            switch (in_testResult.getStatus()) {
            case ITestResult.FAILURE:
                failedStep = ClassPathParser.fetchFullName(in_testResult);
                setFailedInPhase(Phases.getCurrentPhase());
            case ITestResult.SKIP:
                passed = false;
            default:
                break;
            }
            duration += (in_testResult.getEndMillis() - in_testResult.getStartMillis());
            setCurrentStep(ClassPathParser.fetchFullName(in_testResult));
            stepNr++;
        }

        /**
         * Exports the content of this class to a CSV (";" separated) string
         * <p>
         * Author : gandomi
         *
         * @return A string representation of this class
         */
        public String exportToString() {
            return this.passed + ";" + this.duration + ";" + this.failedStep + ";"
                    + this.getFailedInPhase().name();
        }

        /**
         * Imports the values of a string.
         * <p>
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

            this.passed = Boolean.parseBoolean(l_valueArray[0]);
            this.duration = Long.parseLong(l_valueArray[1]);

            if (this.passed) {
                return;
            }

            if (l_valueArray.length < 4) {
                throw new IllegalArgumentException(
                    "The imported string cannot be parsed as it does not contain the minimum 4 of entries.");
            }

            this.failedStep =
                !l_valueArray[2].trim().isEmpty() ? l_valueArray[2] : NOT_APPLICABLE_STEP_NAME;

            try {
                this.setFailedInPhase(!l_valueArray[3].trim().isEmpty() ? Phases.valueOf(
                    l_valueArray[3]) : Phases.NON_PHASED);
            } catch (IllegalArgumentException exc) {
                throw new IllegalArgumentException(
                    "The given import string " + in_importString
                        + " does not allow us to deduce the Phase.");
            }

            //TODO include the StepNr ?

        }

        public String getCurrentStep() {
            return currentStep;
        }

        public void setCurrentStep(String currentStep) {
            this.currentStep = currentStep;
        }
    }

    /**
     * Extracts the index of the current shuffle group
     * @param in_testResult A test result object
     * @return The index of the shuffle group
     */
    public static int asynchronousExtractIndex(ITestResult in_testResult) {
        String l_currentShuffleGroup = in_testResult.getParameters()[0].toString();

        int lr_index = -1;

        if (PhasedTestManager.isPhasedTestShuffledMode(in_testResult)) {

            try {

                String l_indexString = l_currentShuffleGroup.substring(l_currentShuffleGroup.length() - 1,
                        l_currentShuffleGroup.length());
                log.debug("Parsing {} begin : {}, end {}, gives : {}", l_currentShuffleGroup,
                        l_currentShuffleGroup.length() - 2, l_currentShuffleGroup.length() - 1, l_indexString);
                lr_index = Integer.parseInt(
                        l_indexString);

            } catch (NumberFormatException nfe) {
                throw new PhasedTestException("Problem extracting shuffle group number " + l_currentShuffleGroup, nfe);
            }
        }
        return lr_index;
    }

}
