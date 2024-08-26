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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.TestNGException;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class contains all TedstNG framework related methods
 */
public class TestNGFrameworkInterface {
    private static final Logger log = LogManager.getLogger();

    /**
     * This method imports the PhasedTestManager and sets the PhasedDataBroker
     * @param suites The list of XmlSuites coming from the testNG framework
     */
    static void importDataBroker(List<XmlSuite> suites) {
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
    }

    /**
     * This method applies the selection of test by producer data if the test group has been selected
     * @param suites the suites of the testNG framework
     */
    static void applySelectionByProducer(List<XmlSuite> suites) {
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
}
