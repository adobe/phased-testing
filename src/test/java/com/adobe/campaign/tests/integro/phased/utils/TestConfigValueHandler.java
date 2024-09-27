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
package com.adobe.campaign.tests.integro.phased.utils;

import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class TestConfigValueHandler {

    @BeforeMethod
    @AfterMethod
    public void resetValues() {
        ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.reset();
    }

    @Test
    public void testValuesForNonInterruptiveEvent() {
        ConfigValueHandlerPhased eventItem = ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE;

        assertThat("By default the list of NIE should be empty",eventItem.systemName, equalTo("PHASED.EVENTS.NONINTERRUPTIVE"));
        assertThat("By default the list of NIE should be empty",eventItem.defaultValue, isEmptyOrNullString());
        assertThat("The value is not set by default", !eventItem.requiredValue);
    }

    @Test
    public void testSettingValues() {

        ConfigValueHandlerPhased eventItem = ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE;

        assertThat("The value should not be set yet", !eventItem.isSet());
        assertThat("By default the list of NIE should be empty",eventItem.fetchValue(), equalTo(eventItem.defaultValue));


        String l_parameterValue = "MyValue";
        eventItem.activate(l_parameterValue);

        assertThat("The value should now be set", eventItem.isSet());
        assertThat("By default the list of NIE should be empty",eventItem.fetchValue(), equalTo(l_parameterValue));

    }

    @Test
    public void testReSettingValues() {

        ConfigValueHandlerPhased eventItem = ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE;

        String l_parameterValue = "MyValue";
        eventItem.activate(l_parameterValue);

        assertThat("By default the list of NIE should be empty",eventItem.fetchValue(), equalTo(l_parameterValue));

        eventItem.reset();

        assertThat("By default the list of NIE should be empty",eventItem.fetchValue(), equalTo(eventItem.defaultValue));
    }

    @Test
    public void testReSettingAllValues() {

        ConfigValueHandlerPhased eventItem = ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE;

        String l_parameterValue = "MyValue";
        eventItem.activate(l_parameterValue);

        assertThat("By default the list of NIE should be empty",eventItem.fetchValue(), equalTo(l_parameterValue));

        eventItem.resetAllValues();

        assertThat("By default the list of NIE should be empty",eventItem.fetchValue(), equalTo(eventItem.defaultValue));
    }

    @Test
    public void testEqualsIgnoreCase() {
        ConfigValueHandlerPhased eventItem = ConfigValueHandlerPhased.PHASED_TEST_NONPHASED_LEGACY;

        assertThat("We should correctly have the value 'false'", eventItem.is("false"));

        eventItem.activate("FALse");

        assertThat("We should correctly have the value 'false'", eventItem.is("false"));

        eventItem.activate("true");

        assertThat("We should correctly have the value 'true'", !eventItem.is("false"));

        assertThat("We should correctly have the value 'true'", eventItem.is("True"));

    }



}
