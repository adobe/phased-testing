/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
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
