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

import org.hamcrest.Matchers;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

public class PhasesTests {
    @BeforeClass
    public void cleanCache() {
        PhasedTestManager.clearCache();

        System.clearProperty(PhasedTestManager.PROP_PHASED_DATA_PATH);
        System.clearProperty(PhasedTestManager.PROP_SELECTED_PHASE);
        System.clearProperty(PhasedTestManager.PROP_PHASED_TEST_DATABROKER);

    }

    @AfterMethod
    public void clearAllData() {
        cleanCache();
    }

    
    @Test
    public void testSetPhase() {
        assertThat("We should have no phase value", System.getProperty(PhasedTestManager.PROP_SELECTED_PHASE),Matchers.nullValue());
        Phases.PRODUCER.activate();
        
        assertThat("We should have the correct phase", Phases.PRODUCER.isSelected());
    }
    
    @Test
    public void testSetPhasedNonPhased() {
        Phases.NON_PHASED.activate();
        
        assertThat("We should have the correct phase", Phases.NON_PHASED.isSelected());
    }
    
    @Test
    public void testSetPhaseToNonPhased() {
        Phases.PRODUCER.activate();
        
        assertThat("We should have the correct phase", Phases.PRODUCER.isSelected());
   
        Phases.NON_PHASED.activate();
        
        assertThat("We should have the correct phase", Phases.NON_PHASED.isSelected());
    }
    
    
    @Test
    public void testPhaseStates() {
        assertThat("We should have the state INACTIVE by default for the PhasedTestStates",
                Phases.getCurrentPhase(), is(equalTo(Phases.NON_PHASED)));

        assertThat("We should find the correct state PRODUCER",
                Phases.fetchCorrespondingPhase("producer"), is(equalTo(Phases.PRODUCER)));

        assertThat("We should find the correct state CONSUMER",
                Phases.fetchCorrespondingPhase("conSumer"), is(equalTo(Phases.CONSUMER)));

        System.setProperty(PhasedTestManager.PROP_SELECTED_PHASE, "PRODuCER");
        assertThat("We should now have the state Producer for the PhasedTestStates",
                Phases.getCurrentPhase(), is(equalTo(Phases.PRODUCER)));

        assertThat("We should have a check me ethod that works", Phases.PRODUCER.isSelected());

    }

    
    @Test
    public void testPhasesFetchWithEvents() {
        Phases[] l_phasesWithEvents = Phases.fetchPhasesWithEvents();
        
        assertThat("All phases should have a splitting event", Arrays.asList(l_phasesWithEvents).stream().allMatch(p -> p.hasSplittingEvent));

    }

}
