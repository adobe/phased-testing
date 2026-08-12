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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RUNTIME)
@Target(ElementType.METHOD)
/**
 * A PhasedTest Step in a class means that the class itself is a PhasedTest
 *
 * Author : gandomi
 *
 */
public @interface PhaseEvent {

    boolean phaseEnd() default false;

    String[] consumes() default {};

    String[] eventClasses() default {};
}
