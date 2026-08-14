/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class TestMutationListenerLegacyWarning {

    /**
     * A minimal in-memory appender used to capture log events emitted during a test, so we can assert
     * that a warning was actually logged.
     */
    private static class ListAppender extends AbstractAppender {
        private final List<LogEvent> events = new ArrayList<>();

        ListAppender() {
            super("ListAppender", null, null, false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }

    private ListAppender listAppender;
    private org.apache.logging.log4j.core.Logger coreLogger;

    @BeforeMethod
    public void attachAppender() {
        ConfigValueHandlerPhased.resetAllValues();

        listAppender = new ListAppender();
        listAppender.start();

        coreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(ConfigValueHandlerPhased.class);
        coreLogger.addAppender(listAppender);
    }

    @AfterMethod
    public void detachAppender() {
        coreLogger.removeAppender(listAppender);
        listAppender.stop();
        ConfigValueHandlerPhased.resetAllValues();
    }

    @Test
    public void testListenerWarnsWhenLegacyPropertyIsSet() {
        System.setProperty(ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.legacySystemName, "com.acme.MyEvent");

        new MutationListener().alter(Collections.singletonList(new XmlSuite()));

        List<LogEvent> l_warnings = getWarnEvents();
        assertThat("A warning should be logged for the legacy property in use", l_warnings, hasSize(1));
        assertThat(l_warnings.get(0).getMessage().getFormattedMessage(),
                equalTo("IMPORTANT: The property PHASED.EVENTS.NONINTERRUPTIVE is DEPRECATED. Please use "
                        + "MUTATIONAL.EVENTS.NONINTERRUPTIVE henceforth."));
    }

    @Test
    public void testListenerDoesNotWarnWhenOnlyCurrentPropertyIsSet() {
        System.setProperty(ConfigValueHandlerPhased.EVENTS_NONINTERRUPTIVE.systemName, "com.acme.MyEvent");

        new MutationListener().alter(Collections.singletonList(new XmlSuite()));

        assertThat("No legacy-name warning should be logged", getWarnEvents(), hasSize(0));
    }

    private List<LogEvent> getWarnEvents() {
        List<LogEvent> l_warnings = new ArrayList<>();
        for (LogEvent lt_event : listAppender.events) {
            if (lt_event.getLevel().equals(Level.WARN)) {
                l_warnings.add(lt_event);
            }
        }
        return l_warnings;
    }
}
