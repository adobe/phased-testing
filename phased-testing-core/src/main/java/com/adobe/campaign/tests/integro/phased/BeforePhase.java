/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
/**
 * 
 */
package com.adobe.campaign.tests.integro.phased;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
/**
 * When set, this allows for the method to be executed before or after a phase.
 * It should be accompanied with the TestNG Configuration Annotations, such
 * as @BeforeSuite, @AferSuite, etc... In that case the
 * 
 * By default it applies to the splitting event phases:CONSUMER and PRODUCER
 * Phase
 * 
 * Author : gandomi
 *
 */
public @interface BeforePhase {

    /**
     * You can specify the phases to which the BeforPhase method should be
     * executed. By default it is activated for the active phases, Producer and
     * Consumer.
     *
     * Author : gandomi
     *
     * @return the value
     *
     */
    Phases[] appliesToPhases() default { Phases.CONSUMER, Phases.PRODUCER };
}
