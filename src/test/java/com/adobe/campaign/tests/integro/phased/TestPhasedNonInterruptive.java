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

import com.adobe.campaign.tests.integro.phased.data.*;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDP;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledDPSimple;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledNoArgs;
import com.adobe.campaign.tests.integro.phased.data.dp.PhasedSeries_L_ShuffledWrongArgs;
import com.adobe.campaign.tests.integro.phased.data.nested.PhasedSeries_J_RecipientClass.PhasedSeries_J_ShuffledClassInAClass;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.utils.TestTools;
import org.hamcrest.Matchers;
import org.mockito.Mockito;
import org.testng.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertThrows;

public class TestPhasedNonInterruptive {
    @BeforeMethod
    public void resetVariables() {

        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);
        System.clearProperty(PhasedTestManager.PROP_DISABLE_RETRY);
        System.clearProperty(PhasedTestManager.PROP_MERGE_STEP_RESULTS);

        PhasedTestManager.deactivateMergedReports();
        PhasedTestManager.deactivateTestSelectionByProducerMode();

        PhasedTestManager.MergedReportData.resetReport();

        //Delete temporary cache
        File l_newFile = GeneralTestUtils
                .createEmptyCacheFile(GeneralTestUtils.createCacheDirectory("phased2"), "newFile.properties");

        l_newFile.delete();

        //Delete standard cache file
        File l_importCacheFile = new File(
                GeneralTestUtils.fetchCacheDirectory(PhasedTestManager.STD_STORE_DIR),
                PhasedTestManager.STD_STORE_FILE);

        if (l_importCacheFile.exists()) {
            l_importCacheFile.delete();
        }

        PhasedTestManager.MergedReportData.configureMergedReportName(new LinkedHashSet<>(),
                new LinkedHashSet<>(
                        Arrays.asList(PhasedReportElements.DATA_PROVIDERS, PhasedReportElements.PHASE)));
    }


    @Test
    public void testNonInterruptive_Parellel_SHUFFLED() throws NoSuchMethodException, SecurityException {
        // Rampup
        TestNG myTestNG = TestTools.createTestNG();
        TestListenerAdapter tla = TestTools.fetchTestResultsHandler(myTestNG);

        // Define suites
        XmlSuite mySuite = TestTools.addSuitToTestNGTest(myTestNG, "Automated Suite Phased Testing");

        // Add listeners
        mySuite.addListener("com.adobe.campaign.tests.integro.phased.PhasedTestListener");

        // Create an instance of XmlTest and assign a name for it.
        XmlTest myTest = TestTools.attachTestToSuite(mySuite, "Test Shuffled Phased Tests");

        final Class<PhasedSeries_F_Shuffle> l_testClass = PhasedSeries_F_Shuffle.class;
        myTest.setXmlClasses(Collections.singletonList(new XmlClass(l_testClass)));

        myTestNG.run();

        assertThat("We should have 6 successful methods of phased Tests",
                (int) tla.getPassedTests().stream().filter(m -> m.getInstance().getClass().equals(l_testClass)).count(),
                is(equalTo(3)));

        //Global
        assertThat("We should have no failed tests", tla.getFailedTests().size(), equalTo(0));
        assertThat("We should have no skipped tests", tla.getSkippedTests().size(), equalTo(0));

        ITestContext context = tla.getTestContexts().get(0);

    }
}
